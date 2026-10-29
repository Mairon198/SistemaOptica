package modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Medicion {

    private int idMedicion;
    private Cliente cliente;
    private LocalDateTime fecha;
    private BigDecimal odEsfera;
    private BigDecimal odCilindro;
    private Integer odEje;
    private BigDecimal oiEsfera;
    private BigDecimal oiCilindro;
    private Integer oiEje;
    private BigDecimal adicion;
    private BigDecimal dip;
    private String observaciones;

    public Medicion() {
    }

    public int getIdMedicion() {
        return idMedicion;
    }

    public void setIdMedicion(int idMedicion) {
        this.idMedicion = idMedicion;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public BigDecimal getOdEsfera() {
        return odEsfera;
    }

    public void setOdEsfera(BigDecimal odEsfera) {
        this.odEsfera = odEsfera;
    }

    public BigDecimal getOdCilindro() {
        return odCilindro;
    }

    public void setOdCilindro(BigDecimal odCilindro) {
        this.odCilindro = odCilindro;
    }

    public Integer getOdEje() {
        return odEje;
    }

    public void setOdEje(Integer odEje) {
        this.odEje = odEje;
    }

    public BigDecimal getOiEsfera() {
        return oiEsfera;
    }

    public void setOiEsfera(BigDecimal oiEsfera) {
        this.oiEsfera = oiEsfera;
    }

    public BigDecimal getOiCilindro() {
        return oiCilindro;
    }

    public void setOiCilindro(BigDecimal oiCilindro) {
        this.oiCilindro = oiCilindro;
    }

    public Integer getOiEje() {
        return oiEje;
    }

    public void setOiEje(Integer oiEje) {
        this.oiEje = oiEje;
    }

    public BigDecimal getAdicion() {
        return adicion;
    }

    public void setAdicion(BigDecimal adicion) {
        this.adicion = adicion;
    }

    public BigDecimal getDip() {
        return dip;
    }

    public void setDip(BigDecimal dip) {
        this.dip = dip;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
