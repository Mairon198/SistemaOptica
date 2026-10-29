package modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Mairon
 */
public class Venta {

    private int idVenta;
    private String numeroVenta;
    private Cliente cliente;
    private Vendedor vendedor;
    private Medicion medicion;
    private LocalDateTime fecha;
    private BigDecimal subtotal;
    private BigDecimal impuesto;
    private BigDecimal total;
    private String metodoPago;
    private String estado;
    private final List<DetalleVenta> detalles = new ArrayList<>();

    //Constructor vacio
    public Venta() {
        this.subtotal = BigDecimal.ZERO;
        this.impuesto = BigDecimal.ZERO;
        this.total = BigDecimal.ZERO;
        this.metodoPago = "EFECTIVO";
        this.estado = "COMPLETADA";
    }

    public Venta(int idVenta, String numeroVenta, Cliente cliente, Vendedor vendedor, Medicion medicion,
                 LocalDateTime fecha, BigDecimal subtotal, BigDecimal impuesto, BigDecimal total,
                 String metodoPago, String estado) {
        this.idVenta = idVenta;
        this.numeroVenta = numeroVenta;
        this.cliente = cliente;
        this.vendedor = vendedor;
        this.medicion = medicion;
        this.fecha = fecha;
        this.subtotal = subtotal;
        this.impuesto = impuesto;
        this.total = total;
        this.metodoPago = metodoPago;
        this.estado = estado;
    }

    public void agregarDetalle(DetalleVenta detalle) {
        detalles.add(detalle);
    }

    public void quitarDetalle(int indice) {
        detalles.remove(indice);
    }

    public int getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(int idVenta) {
        this.idVenta = idVenta;
    }

    public String getNumeroVenta() {
        return numeroVenta;
    }

    public void setNumeroVenta(String numeroVenta) {
        this.numeroVenta = numeroVenta;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Vendedor getVendedor() {
        return vendedor;
    }

    public void setVendedor(Vendedor vendedor) {
        this.vendedor = vendedor;
    }

    public Medicion getMedicion() {
        return medicion;
    }

    public void setMedicion(Medicion medicion) {
        this.medicion = medicion;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getImpuesto() {
        return impuesto;
    }

    public void setImpuesto(BigDecimal impuesto) {
        this.impuesto = impuesto;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public List<DetalleVenta> getDetalles() {
        return detalles;
    }

    @Override
    public String toString() {
        return numeroVenta + " - " + cliente.getNombreCompleto() + " - " + total;
    }
}