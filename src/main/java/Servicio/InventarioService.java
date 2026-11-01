package servicio;

import dao.AccesorioDAO;
import dao.AroDAO;
import dao.LunaDAO;
import dao.ProductoDAO;
import dao.impl.AccesorioDAOImpl;
import dao.impl.AroDAOImpl;
import dao.impl.LunaDAOImpl;
import dao.impl.ProductoDAOImpl;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import modelo.Accesorio;
import modelo.Aro;
import modelo.Luna;
import modelo.Producto;

public class InventarioService {

    //Limite para la alerta de stock bajo
    public static final int LIMITE_STOCK_BAJO = 5;

    private final AroDAO aroDAO;
    private final LunaDAO lunaDAO;
    private final AccesorioDAO accesorioDAO;
    private final ProductoDAO productoDAO;

    public InventarioService() {
        this(new AroDAOImpl(), new LunaDAOImpl(), new AccesorioDAOImpl());
    }

    public InventarioService(AroDAO aroDAO, LunaDAO lunaDAO, AccesorioDAO accesorioDAO) {
        this(aroDAO, lunaDAO, accesorioDAO, new ProductoDAOImpl());
    }

    public InventarioService(AroDAO aroDAO, LunaDAO lunaDAO, AccesorioDAO accesorioDAO, ProductoDAO productoDAO) {
        this.aroDAO = aroDAO;
        this.lunaDAO = lunaDAO;
        this.accesorioDAO = accesorioDAO;
        this.productoDAO = productoDAO;
    }

    //Si el codigo de un producto nuevo pertenece a uno eliminado del mismo tipo, devuelve su id (si no, 0)
    public int idEliminadoConCodigo(Producto p) {
        if (p.getIdProducto() != 0 || vacio(p.getCodigo())) {
            return 0;
        }
        return productoDAO.buscarIdInactivoPorCodigo(p.getCodigo().trim().toUpperCase(), p.getTipo());
    }

    public void reactivarAro(Aro aro, int idProducto) throws ValidacionException {
        validarProducto(aro);
        aro.setIdProducto(idProducto);
        reactivar(aroDAO.actualizar(aro), idProducto);
    }

    public void reactivarLuna(Luna luna, int idProducto) throws ValidacionException {
        validarProducto(luna);
        luna.setIdProducto(idProducto);
        reactivar(lunaDAO.actualizar(luna), idProducto);
    }

    public void reactivarAccesorio(Accesorio accesorio, int idProducto) throws ValidacionException {
        validarProducto(accesorio);
        accesorio.setIdProducto(idProducto);
        reactivar(accesorioDAO.actualizar(accesorio), idProducto);
    }

    private void reactivar(boolean actualizado, int idProducto) throws ValidacionException {
        if (!actualizado || !productoDAO.activar(idProducto)) {
            throw new ValidacionException("No se pudo reactivar el producto. Verifique la conexión.");
        }
    }


    public List<Aro> listarAros() {
        return aroDAO.listar();
    }

    public List<Aro> buscarAros(String texto) {
        return vacio(texto) ? aroDAO.listar() : aroDAO.buscar(texto);
    }

    public void guardarAro(Aro aro) throws ValidacionException {
        validarProducto(aro);
        boolean ok = aro.getIdProducto() == 0 ? aroDAO.insertar(aro) : aroDAO.actualizar(aro);
        if (!ok) {
            throw new ValidacionException(errorAlGuardar());
        }
    }

    public void eliminarAro(int idProducto) throws ValidacionException {
        if (!aroDAO.eliminar(idProducto)) {
            throw new ValidacionException("No se pudo eliminar el aro.");
        }
    }

    //Aros: menos de 5 unidades. Lunas y accesorios: su stock minimo o menos
    public static boolean esStockBajo(Producto p) {
        if (Producto.TIPO_ARO.equals(p.getTipo())) {
            return p.getStock() < LIMITE_STOCK_BAJO;
        }
        return p.getStock() <= p.getStockMinimo();
    }

    public List<Aro> arosConStockBajo() {
        return filtrarStockBajo(aroDAO.listar());
    }

    public List<Luna> lunasConStockBajo() {
        return filtrarStockBajo(lunaDAO.listar());
    }

    public List<Accesorio> accesoriosConStockBajo() {
        return filtrarStockBajo(accesorioDAO.listar());
    }

    public List<Producto> productosConStockBajo() {
        List<Producto> bajos = new ArrayList<>();
        bajos.addAll(arosConStockBajo());
        bajos.addAll(lunasConStockBajo());
        bajos.addAll(accesoriosConStockBajo());
        return bajos;
    }

    public Producto buscarProductoPorId(String tipo, int idProducto) {
        switch (tipo) {
            case Producto.TIPO_LUNA:
                return lunaDAO.buscarPorId(idProducto);
            case Producto.TIPO_ACCESORIO:
                return accesorioDAO.buscarPorId(idProducto);
            default:
                return aroDAO.buscarPorId(idProducto);
        }
    }

    private <T extends Producto> List<T> filtrarStockBajo(List<T> productos) {
        List<T> bajos = new ArrayList<>();
        for (T p : productos) {
            if (esStockBajo(p)) {
                bajos.add(p);
            }
        }
        return bajos;
    }


    public List<Luna> listarLunas() {
        return lunaDAO.listar();
    }

    public List<Luna> buscarLunas(String texto) {
        return vacio(texto) ? lunaDAO.listar() : lunaDAO.buscar(texto);
    }

    public void guardarLuna(Luna luna) throws ValidacionException {
        validarProducto(luna);
        if (luna.getIndiceRefraccion() != null
                && (luna.getIndiceRefraccion().compareTo(new BigDecimal("1.40")) < 0
                || luna.getIndiceRefraccion().compareTo(new BigDecimal("2.00")) > 0)) {
            throw new ValidacionException("El índice de refracción debe estar entre 1.40 y 2.00.");
        }
        boolean ok = luna.getIdProducto() == 0 ? lunaDAO.insertar(luna) : lunaDAO.actualizar(luna);
        if (!ok) {
            throw new ValidacionException(errorAlGuardar());
        }
    }

    public void eliminarLuna(int idProducto) throws ValidacionException {
        if (!lunaDAO.eliminar(idProducto)) {
            throw new ValidacionException("No se pudo eliminar la luna.");
        }
    }


    public List<Accesorio> listarAccesorios() {
        return accesorioDAO.listar();
    }

    public List<Accesorio> buscarAccesorios(String texto) {
        return vacio(texto) ? accesorioDAO.listar() : accesorioDAO.buscar(texto);
    }

    public void guardarAccesorio(Accesorio accesorio) throws ValidacionException {
        validarProducto(accesorio);
        boolean ok = accesorio.getIdProducto() == 0
                ? accesorioDAO.insertar(accesorio) : accesorioDAO.actualizar(accesorio);
        if (!ok) {
            throw new ValidacionException(errorAlGuardar());
        }
    }

    public void eliminarAccesorio(int idProducto) throws ValidacionException {
        if (!accesorioDAO.eliminar(idProducto)) {
            throw new ValidacionException("No se pudo eliminar el accesorio.");
        }
    }


    //Productos con stock para las ventas
    public List<Producto> listarDisponibles(String tipo) {
        List<? extends Producto> todos;
        switch (tipo) {
            case Producto.TIPO_LUNA:
                todos = lunaDAO.listar();
                break;
            case Producto.TIPO_ACCESORIO:
                todos = accesorioDAO.listar();
                break;
            default:
                todos = aroDAO.listar();
        }
        List<Producto> disponibles = new ArrayList<>();
        for (Producto p : todos) {
            if (p.getStock() > 0) {
                disponibles.add(p);
            }
        }
        return disponibles;
    }

    public Aro buscarAroPorId(int idProducto) {
        return aroDAO.buscarPorId(idProducto);
    }


    private void validarProducto(Producto p) throws ValidacionException {
        if (vacio(p.getCodigo())) {
            throw new ValidacionException("El código es obligatorio.");
        }
        if (vacio(p.getNombre())) {
            throw new ValidacionException("El nombre es obligatorio.");
        }
        if (p.getPrecio() == null || p.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidacionException("El precio no puede ser negativo.");
        }
        if (p.getStock() < 0) {
            throw new ValidacionException("El stock no puede ser negativo.");
        }
        if (p.getStockMinimo() < 0) {
            throw new ValidacionException("El stock mínimo no puede ser negativo.");
        }
        p.setCodigo(p.getCodigo().trim().toUpperCase());
        p.setNombre(p.getNombre().trim());
    }

    private String errorAlGuardar() {
        return "No se pudo guardar. Verifique que el código no esté repetido y que haya conexión.";
    }

    private static boolean vacio(String s) {
        return s == null || s.trim().isEmpty();
    }
}
