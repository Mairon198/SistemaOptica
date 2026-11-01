package dao;

public interface ProductoDAO {

    int buscarIdInactivoPorCodigo(String codigo, String tipo);

    boolean activar(int idProducto);
}
