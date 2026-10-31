package modelo;

import java.math.BigDecimal;


public class Aro extends Producto {

    private String material;
    private String forma;
    private String color;
    private String genero;
    private String medidas;
    private String imagenUrl;

    //Constructor vacio
    public Aro() {
        super();
    }

    public Aro(int idProducto, String codigo, String nombre, String marca, BigDecimal precio,
               int stock, int stockMinimo, boolean activo,
               String material, String forma, String color, String genero,
               String medidas, String imagenUrl) {
        super(idProducto, codigo, nombre, marca, precio, stock, stockMinimo, activo);
        this.material = material;
        this.forma = forma;
        this.color = color;
        this.genero = genero;
        this.medidas = medidas;
        this.imagenUrl = imagenUrl;
    }

    @Override
    public String getTipo() {
        return TIPO_ARO;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public String getForma() {
        return forma;
    }

    public void setForma(String forma) {
        this.forma = forma;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public String getMedidas() {
        return medidas;
    }

    public void setMedidas(String medidas) {
        this.medidas = medidas;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }
}
