package servicio;

import dao.ApartadoDAO;
import dao.AroDAO;
import dao.impl.ApartadoDAOImpl;
import dao.impl.AroDAOImpl;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import modelo.Apartado;
import modelo.Aro;
import modelo.Cliente;
import modelo.Usuario;

public class CatalogoService {

    private final AroDAO aroDAO;
    private final ApartadoDAO apartadoDAO;

    public CatalogoService() {
        this(new AroDAOImpl(), new ApartadoDAOImpl());
    }

    public CatalogoService(AroDAO aroDAO, ApartadoDAO apartadoDAO) {
        this.aroDAO = aroDAO;
        this.apartadoDAO = apartadoDAO;
    }

    public static class Opciones {
        public final List<String> marcas = new ArrayList<>();
        public final List<String> materiales = new ArrayList<>();
        public final List<String> formas = new ArrayList<>();
    }

    //Valores distintos para llenar los filtros
    public Opciones opcionesFiltro() {
        TreeSet<String> marcas = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        TreeSet<String> materiales = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        TreeSet<String> formas = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (Aro a : aroDAO.listar()) {
            agregar(marcas, a.getMarca());
            agregar(materiales, a.getMaterial());
            agregar(formas, a.getForma());
        }
        Opciones o = new Opciones();
        o.marcas.addAll(marcas);
        o.materiales.addAll(materiales);
        o.formas.addAll(formas);
        return o;
    }

    public List<Aro> buscarAros(String marca, String material, String forma,
                                BigDecimal precioMin, BigDecimal precioMax) throws ValidacionException {
        if ((precioMin != null && precioMin.signum() < 0) || (precioMax != null && precioMax.signum() < 0)) {
            throw new ValidacionException("El precio no puede ser negativo.");
        }
        if (precioMin != null && precioMax != null && precioMin.compareTo(precioMax) > 0) {
            throw new ValidacionException("El precio mínimo no puede ser mayor que el máximo.");
        }
        return aroDAO.listarFiltrado(marca, material, forma, precioMin, precioMax);
    }

    public void apartar(Usuario usuario, Aro aro, String observaciones) throws ValidacionException {
        int idCliente = idCliente(usuario);
        if (aro == null) {
            throw new ValidacionException("Seleccione en la tabla el aro que desea apartar.");
        }
        Aro actual = aroDAO.buscarPorId(aro.getIdProducto());
        if (actual == null || !actual.isActivo() || actual.getStock() <= 0) {
            throw new ValidacionException("Ese aro ya no está disponible.");
        }
        if (apartadoDAO.existeActivo(idCliente, aro.getIdProducto())) {
            throw new ValidacionException("Ya tiene este aro apartado.");
        }
        if (observaciones != null && observaciones.length() > 255) {
            throw new ValidacionException("La observación no puede pasar de 255 caracteres.");
        }
        Cliente c = new Cliente();
        c.setIdCliente(idCliente);
        if (!apartadoDAO.insertar(new Apartado(c, actual, observaciones))) {
            throw new ValidacionException("No se pudo apartar el aro. Verifique la conexión.");
        }
    }

    public List<Apartado> listarApartados(Usuario usuario) throws ValidacionException {
        return apartadoDAO.listarPorCliente(idCliente(usuario));
    }

    public void cancelar(Usuario usuario, Apartado apartado) throws ValidacionException {
        if (apartado == null) {
            throw new ValidacionException("Seleccione el apartado que desea cancelar.");
        }
        if (!apartado.isActivo()) {
            throw new ValidacionException("Solo se pueden cancelar los apartados en estado APARTADO.");
        }
        if (!apartadoDAO.cancelar(apartado.getIdApartado(), idCliente(usuario))) {
            throw new ValidacionException("No se pudo cancelar el apartado.");
        }
    }

    private int idCliente(Usuario usuario) throws ValidacionException {
        if (usuario == null || usuario.getIdCliente() == null) {
            throw new ValidacionException("Su cuenta no está asociada a un cliente.");
        }
        return usuario.getIdCliente();
    }

    private void agregar(TreeSet<String> conjunto, String valor) {
        if (valor != null && !valor.trim().isEmpty()) {
            conjunto.add(valor.trim());
        }
    }
}
