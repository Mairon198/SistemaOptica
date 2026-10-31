package modelo;

import java.math.BigDecimal;

/**
 *
 * @author Mairon
 */
public class DetalleVenta {

    private int idDetalle;
    private int idVenta;
    private Producto producto;
    private int cantidad;
    private BigDecimal precioUnitario;

    //Constructor vacio
    public DetalleVenta() {}

    //Constructor rapido: toma el precio actual del producto (lo usa VentaService)
    public DetalleVenta(Producto producto, int cantidad) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = producto.getPrecio();
    }

    public DetalleVenta(int idDetalle, int idVenta, Producto producto, int cantidad, BigDecimal precioUnitario) {
        this.idDetalle = idDetalle;
        this.idVenta = idVenta;
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    //cantidad x precio unitario
    public BigDecimal getSubtotalLinea() {
        if (precioUnitario == null) {
            return BigDecimal.ZERO;
        }
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    public int getIdDetalle() {
        return idDetalle;
    }

    public void setIdDetalle(int idDetalle) {
        this.idDetalle = idDetalle;
    }

    public int getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(int idVenta) {
        this.idVenta = idVenta;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    @Override
    public String toString() {
        return producto.getNombre() + " - " + cantidad + " - " + getSubtotalLinea();
    }
}