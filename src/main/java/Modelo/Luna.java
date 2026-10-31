package modelo;

import java.math.BigDecimal;

/**
 *
 * @author Mairon
 */
public class Luna extends Producto {

    private String tipoVision;
    private String material;
    private BigDecimal indiceRefraccion;
    private String tratamiento;

    //Constructor vacio
    public Luna() {
        super();
    }

    public Luna(int idProducto, String codigo, String nombre, String marca, BigDecimal precio,
                int stock, int stockMinimo, boolean activo,
                String tipoVision, String material, BigDecimal indiceRefraccion, String tratamiento) {
        super(idProducto, codigo, nombre, marca, precio, stock, stockMinimo, activo);
        this.tipoVision = tipoVision;
        this.material = material;
        this.indiceRefraccion = indiceRefraccion;
        this.tratamiento = tratamiento;
    }

    @Override
    public String getTipo() {
        return TIPO_LUNA;
    }

    public String getTipoVision() {
        return tipoVision;
    }

    public void setTipoVision(String tipoVision) {
        this.tipoVision = tipoVision;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public BigDecimal getIndiceRefraccion() {
        return indiceRefraccion;
    }

    public void setIndiceRefraccion(BigDecimal indiceRefraccion) {
        this.indiceRefraccion = indiceRefraccion;
    }

    public String getTratamiento() {
        return tratamiento;
    }

    public void setTratamiento(String tratamiento) {
        this.tratamiento = tratamiento;
    }
}
