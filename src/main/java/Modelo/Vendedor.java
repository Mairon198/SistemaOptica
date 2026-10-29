package modelo;

public class Vendedor {

    private int idVendedor;
    private String nombres;
    private String apellidos;
    private String usuario;
    private String telefono;
    private boolean activo = true;

    public Vendedor() {
    }

    public Vendedor(int idVendedor, String nombres, String apellidos, String usuario, String telefono) {
        this.idVendedor = idVendedor;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.usuario = usuario;
        this.telefono = telefono;
    }

    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }

    public int getIdVendedor() {
        return idVendedor;
    }

    public void setIdVendedor(int idVendedor) {
        this.idVendedor = idVendedor;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return getNombreCompleto();
    }
}
