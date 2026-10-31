package modelo;

import java.time.LocalDateTime;

public class Apartado {

    public static final String APARTADO = "APARTADO";
    public static final String VENDIDO = "VENDIDO";
    public static final String CANCELADO = "CANCELADO";

    private int idApartado;
    private Cliente cliente;
    private Aro aro;
    private LocalDateTime fecha;
    private String estado = APARTADO;
    private String observaciones;

    public Apartado() {
    }

    public Apartado(Cliente cliente, Aro aro, String observaciones) {
        this.cliente = cliente;
        this.aro = aro;
        this.observaciones = observaciones;
    }

    public int getIdApartado() {
        return idApartado;
    }

    public void setIdApartado(int idApartado) {
        this.idApartado = idApartado;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Aro getAro() {
        return aro;
    }

    public void setAro(Aro aro) {
        this.aro = aro;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public boolean isActivo() {
        return APARTADO.equals(estado);
    }
}
