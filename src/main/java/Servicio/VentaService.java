package servicio;

import dao.ClienteDAO;
import dao.MedicionDAO;
import dao.VentaDAO;
import dao.impl.ClienteDAOImpl;
import dao.impl.MedicionDAOImpl;
import dao.impl.VentaDAOImpl;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import modelo.Aro;
import modelo.Cliente;
import modelo.DetalleVenta;
import modelo.Medicion;
import modelo.Producto;
import modelo.Usuario;
import modelo.Vendedor;
import modelo.Venta;

public class VentaService {

    //ISV 15%
    public static final BigDecimal TASA_IMPUESTO = new BigDecimal("0.15");

    private final VentaDAO ventaDAO;
    private final ClienteDAO clienteDAO;
    private final MedicionDAO medicionDAO;
    private final InventarioService inventarioService;

    public VentaService() {
        this(new VentaDAOImpl(), new ClienteDAOImpl(), new MedicionDAOImpl(), new InventarioService());
    }

    public VentaService(VentaDAO ventaDAO, ClienteDAO clienteDAO, MedicionDAO medicionDAO,
                        InventarioService inventarioService) {
        this.ventaDAO = ventaDAO;
        this.clienteDAO = clienteDAO;
        this.medicionDAO = medicionDAO;
        this.inventarioService = inventarioService;
    }


    public List<Cliente> listarClientes() {
        return clienteDAO.listar();
    }

    public List<Medicion> listarMediciones(int idCliente) {
        return medicionDAO.listarPorCliente(idCliente);
    }

    public List<Producto> listarProductosDisponibles(String tipo) {
        return inventarioService.listarDisponibles(tipo);
    }

    public List<Venta> listarVentas() {
        return ventaDAO.listar();
    }

    public Venta buscarVenta(int idVenta) {
        return ventaDAO.buscarPorId(idVenta);
    }


    public Venta nuevaVenta(Usuario vendedorEnSesion) {
        Venta venta = new Venta();
        if (vendedorEnSesion != null && vendedorEnSesion.getIdVendedor() != null) {
            Vendedor v = new Vendedor();
            v.setIdVendedor(vendedorEnSesion.getIdVendedor());
            v.setNombres(vendedorEnSesion.getNombreMostrar());
            v.setApellidos("");
            venta.setVendedor(v);
        }
        return venta;
    }

    //Agrega un producto a la venta, si ya estaba suma la cantidad
    public void agregarProducto(Venta venta, Producto producto, int cantidad) throws ValidacionException {
        if (producto == null) {
            throw new ValidacionException("Seleccione un producto.");
        }
        if (cantidad <= 0) {
            throw new ValidacionException("La cantidad debe ser mayor que cero.");
        }
        DetalleVenta existente = null;
        for (DetalleVenta d : venta.getDetalles()) {
            if (d.getProducto().getIdProducto() == producto.getIdProducto()) {
                existente = d;
                break;
            }
        }
        int cantidadTotal = cantidad + (existente == null ? 0 : existente.getCantidad());
        if (cantidadTotal > producto.getStock()) {
            throw new ValidacionException("Stock insuficiente para " + producto.getNombre()
                    + ". Disponible: " + producto.getStock() + " unidad(es).");
        }
        if (existente == null) {
            venta.agregarDetalle(new DetalleVenta(producto, cantidad));
        } else {
            existente.setCantidad(cantidadTotal);
        }
        calcularTotales(venta);
    }

    public void quitarProducto(Venta venta, int indice) {
        if (indice >= 0 && indice < venta.getDetalles().size()) {
            venta.quitarDetalle(indice);
            calcularTotales(venta);
        }
    }

    //Calcula subtotal, isv y total
    public void calcularTotales(Venta venta) {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (DetalleVenta d : venta.getDetalles()) {
            subtotal = subtotal.add(d.getSubtotalLinea());
        }
        subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);
        BigDecimal impuesto = subtotal.multiply(TASA_IMPUESTO).setScale(2, RoundingMode.HALF_UP);
        venta.setSubtotal(subtotal);
        venta.setImpuesto(impuesto);
        venta.setTotal(subtotal.add(impuesto));
    }


    //Valida y guarda la venta (descuenta el stock)
    public void registrar(Venta venta) throws ValidacionException {
        if (venta.getVendedor() == null || venta.getVendedor().getIdVendedor() == 0) {
            throw new ValidacionException("Su usuario no está asociado a un vendedor. No puede registrar ventas.");
        }
        if (venta.getCliente() == null) {
            throw new ValidacionException("Seleccione el cliente.");
        }
        if (venta.getMedicion() == null) {
            throw new ValidacionException("Seleccione la medición de lentes del cliente.");
        }
        if (venta.getMedicion().getCliente() != null
                && venta.getMedicion().getCliente().getIdCliente() != venta.getCliente().getIdCliente()) {
            throw new ValidacionException("La medición seleccionada no pertenece a este cliente.");
        }
        boolean tieneAro = false;
        for (DetalleVenta d : venta.getDetalles()) {
            if (Producto.TIPO_ARO.equals(d.getProducto().getTipo())) {
                tieneAro = true;
                break;
            }
        }
        if (!tieneAro) {
            throw new ValidacionException("La venta debe incluir al menos un aro.");
        }

        calcularTotales(venta);
        venta.setFecha(LocalDateTime.now());
        venta.setNumeroVenta(generarNumero(venta.getFecha()));
        if (venta.getEstado() == null) {
            venta.setEstado("COMPLETADA");
        }

        if (!ventaDAO.registrar(venta)) {
            String detalle = (ventaDAO instanceof VentaDAOImpl)
                    ? ((VentaDAOImpl) ventaDAO).getUltimoError() : null;
            throw new ValidacionException("No se pudo registrar la venta."
                    + (detalle == null ? "" : "\n" + detalle));
        }
    }

    //Aros que quedaron con stock bajo despues de la venta
    public List<String> avisosStockBajo(Venta venta) {
        List<String> avisos = new ArrayList<>();
        for (DetalleVenta d : venta.getDetalles()) {
            if (Producto.TIPO_ARO.equals(d.getProducto().getTipo())) {
                Aro actual = inventarioService.buscarAroPorId(d.getProducto().getIdProducto());
                if (actual != null && InventarioService.esStockBajo(actual)) {
                    avisos.add(actual.getCodigo() + " - " + actual.getNombre()
                            + ": quedan " + actual.getStock() + " unidad(es)");
                }
            }
        }
        return avisos;
    }

    private String generarNumero(LocalDateTime fecha) {
        return "V-" + fecha.format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))
                + "-" + (int) (Math.random() * 90 + 10);
    }
}
