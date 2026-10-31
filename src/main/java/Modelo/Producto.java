
package modelo;

import java.math.BigDecimal;

/**
 *
 * @author Mairon
 */
public abstract class Producto {

    public static final String TIPO_ARO = "ARO";
    public static final String TIPO_LUNA = "LUNA";
    public static final String TIPO_ACCESORIO = "ACCESORIO";

    private int idProducto;
    private String codigo;
    private String nombre;
    private String marca;
    private BigDecimal precio;
    private int stock;
    private int stockMinimo;
    private boolean activo;

    //Constructor vacio
    public Producto() {
        this.precio = BigDecimal.ZERO;
        this.stockMinimo = 5;
        this.activo = true;
    }

    public Producto(int idProducto, String codigo, String nombre, String marca,
                    BigDecimal precio, int stock, int stockMinimo, boolean activo) {
        this.idProducto = idProducto;
        this.codigo = codigo;
        this.nombre = nombre;
        this.marca = marca;
        this.precio = precio;
        this.stock = stock;
        this.stockMinimo = stockMinimo;
        this.activo = activo;
    }

    //Cada hijo devuelve su tipo: "ARO", "LUNA" o "ACCESORIO"
    public abstract String getTipo();

    public boolean isStockBajo() {
        return stock < stockMinimo;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(int stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return codigo + " - " + nombre + " - " + marca;
    }
}