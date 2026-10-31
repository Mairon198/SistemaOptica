package servicio;

import dao.AccesorioDAO;
import dao.AroDAO;
import dao.LunaDAO;
import dao.impl.AccesorioDAOImpl;
import dao.impl.AroDAOImpl;
import dao.impl.LunaDAOImpl;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import modelo.Accesorio;
import modelo.Aro;
import modelo.Luna;
import modelo.Producto;

public class InventarioService {

    //Limite para la alerta de stock bajo
    public static final int LIMITE_STOCK_BAJO = 5;

    private final AroDAO aroDAO;
    private final LunaDAO lunaDAO;
    private final AccesorioDAO accesorioDAO;

    public InventarioService() {
        this(new AroDAOImpl(), new LunaDAOImpl(), new AccesorioDAOImpl());
    }

    public InventarioService(AroDAO aroDAO, LunaDAO lunaDAO, AccesorioDAO accesorioDAO) {
        this.aroDAO = aroDAO;
        this.lunaDAO = lunaDAO;
        this.accesorioDAO = accesorioDAO;
    }


    public List<Aro> listarAros() {
        return aroDAO.listar();
    }

    public List<Aro> buscarAros(String texto) {
        return vacio(texto) ? aroDAO.listar() : aroDAO.buscar(texto);
    }

    public void guardarAro(Aro aro) throws ValidacionException {
        validarProducto(aro);
        boolean ok = aro.getIdProducto() == 0 ? aroDAO.insertar(aro) : aroDAO.actualizar(aro);
        if (!ok) {
            throw new ValidacionException(errorAlGuardar());
        }
    }

    public void eliminarAro(int idProducto) throws ValidacionException {
        if (!aroDAO.eliminar(idProducto)) {
            throw new ValidacionException("No se pudo eliminar el aro.");
        }
    }

    public List<Aro> arosConStockBajo() {
        List<Aro> bajos = new ArrayList<>();
        for (Aro a : aroDAO.listar()) {
            if (esStockBajo(a)) {
                bajos.add(a);
            }
        }
        return bajos;
    }

    public static boolean esStockBajo(Producto p) {
        return p.getStock() < LIMITE_STOCK_BAJO;
    }


    public List<Luna> listarLunas() {
        return lunaDAO.listar();
    }

    public List<Luna> buscarLunas(String texto) {
        return vacio(texto) ? lunaDAO.listar() : lunaDAO.buscar(texto);
    }

    public void guardarLuna(Luna luna) throws ValidacionException {
        validarProducto(luna);
        if (luna.getIndiceRefraccion() != null
                && (luna.getIndiceRefraccion().compareTo(new BigDecimal("1.40")) < 0
                || luna.getIndiceRefraccion().compareTo(new BigDecimal("2.00")) > 0)) {
            throw new ValidacionException("El índice de refracción debe estar entre 1.40 y 2.00.");
        }
        boolean ok = luna.getIdProducto() == 0 ? lunaDAO.insertar(luna) : lunaDAO.actualizar(luna);
        if (!ok) {
            throw new ValidacionException(errorAlGuardar());
        }
    }

    public void eliminarLuna(int idProducto) throws ValidacionException {
        if (!lunaDAO.eliminar(idProducto)) {
            throw new ValidacionException("No se pudo eliminar la luna.");
        }
    }


    public List<Accesorio> listarAccesorios() {
        return accesorioDAO.listar();
    }

    public List<Accesorio> buscarAccesorios(String texto) {
        return vacio(texto) ? accesorioDAO.listar() : accesorioDAO.buscar(texto);
    }

    public void guardarAccesorio(Accesorio accesorio) throws ValidacionException {
        validarProducto(accesorio);
        boolean ok = accesorio.getIdProducto() == 0
                ? accesorioDAO.insertar(accesorio) : accesorioDAO.actualizar(accesorio);
        if (!ok) {
            throw new ValidacionException(errorAlGuardar());
        }
    }

    public void eliminarAccesorio(int idProducto) throws ValidacionException {
        if (!accesorioDAO.eliminar(idProducto)) {
            throw new ValidacionException("No se pudo eliminar el accesorio.");
        }
    }


    //Productos con stock para las ventas
    public List<Producto> listarDisponibles(String tipo) {
        List<? extends Producto> todos;
        switch (tipo) {
            case Producto.TIPO_LUNA:
                todos = lunaDAO.listar();
                break;
            case Producto.TIPO_ACCESORIO:
                todos = accesorioDAO.listar();
                break;
            default:
                todos = aroDAO.listar();
        }
        List<Producto> disponibles = new ArrayList<>();
        for (Producto p : todos) {
            if (p.getStock() > 0) {
                disponibles.add(p);
            }
        }
        return disponibles;
    }

    public Aro buscarAroPorId(int idProducto) {
        return aroDAO.buscarPorId(idProducto);
    }


    private void validarProducto(Producto p) throws ValidacionException {
        if (vacio(p.getCodigo())) {
            throw new ValidacionException("El código es obligatorio.");
        }
        if (vacio(p.getNombre())) {
            throw new ValidacionException("El nombre es obligatorio.");
        }
        if (p.getPrecio() == null || p.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidacionException("El precio no puede ser negativo.");
        }
        if (p.getStock() < 0) {
            throw new ValidacionException("El stock no puede ser negativo.");
        }
        if (p.getStockMinimo() < 0) {
            throw new ValidacionException("El stock mínimo no puede ser negativo.");
        }
        p.setCodigo(p.getCodigo().trim().toUpperCase());
        p.setNombre(p.getNombre().trim());
    }

    private String errorAlGuardar() {
        return "No se pudo guardar. Verifique que el código no esté repetido y que haya conexión.";
    }

    private static boolean vacio(String s) {
        return s == null || s.trim().isEmpty();
    }
}
