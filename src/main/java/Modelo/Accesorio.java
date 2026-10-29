package modelo;

import java.math.BigDecimal;

/**
 *
 * @author Mairon
 */
public class Accesorio extends Producto {

    private String categoria;
    private String descripcion;

    //Constructor vacio
    public Accesorio() {
        super();
    }

    public Accesorio(int idProducto, String codigo, String nombre, String marca, BigDecimal precio,
                     int stock, int stockMinimo, boolean activo,
                     String categoria, String descripcion) {
        super(idProducto, codigo, nombre, marca, precio, stock, stockMinimo, activo);
        this.categoria = categoria;
        this.descripcion = descripcion;
    }

    @Override
    public String getTipo() {
        return TIPO_ACCESORIO;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
