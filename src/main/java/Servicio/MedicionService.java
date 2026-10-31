package servicio;

import dao.ClienteDAO;
import dao.MedicionDAO;
import dao.impl.ClienteDAOImpl;
import dao.impl.MedicionDAOImpl;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import modelo.Cliente;
import modelo.Medicion;

public class MedicionService {

    private final MedicionDAO medicionDAO;
    private final ClienteDAO clienteDAO;

    public MedicionService() {
        this(new MedicionDAOImpl(), new ClienteDAOImpl());
    }

    public MedicionService(MedicionDAO medicionDAO, ClienteDAO clienteDAO) {
        this.medicionDAO = medicionDAO;
        this.clienteDAO = clienteDAO;
    }

    public List<Cliente> listarClientes() {
        return clienteDAO.listar();
    }

    public List<Medicion> listarPorCliente(int idCliente) {
        return medicionDAO.listarPorCliente(idCliente);
    }

    public void guardar(Medicion m) throws ValidacionException {
        if (m.getCliente() == null || m.getCliente().getIdCliente() == 0) {
            throw new ValidacionException("Seleccione el cliente.");
        }
        if (m.getOdEsfera() == null || m.getOiEsfera() == null) {
            throw new ValidacionException("La esfera de ambos ojos es obligatoria.");
        }
        validarRango(m.getOdEsfera(), "-25", "25", "OD Esfera");
        validarRango(m.getOiEsfera(), "-25", "25", "OI Esfera");
        validarRango(m.getOdCilindro(), "-10", "10", "OD Cilindro");
        validarRango(m.getOiCilindro(), "-10", "10", "OI Cilindro");
        validarEje(m.getOdEje(), "OD Eje");
        validarEje(m.getOiEje(), "OI Eje");
        validarRango(m.getAdicion(), "0", "4", "Adición");
        validarRango(m.getDip(), "40", "80", "DIP");
        if (m.getObservaciones() != null && m.getObservaciones().length() > 500) {
            throw new ValidacionException("Las observaciones no pueden pasar de 500 caracteres.");
        }

        boolean ok;
        if (m.getIdMedicion() == 0) {
            m.setFecha(LocalDateTime.now());
            ok = medicionDAO.insertar(m);
        } else {
            ok = medicionDAO.actualizar(m);
        }
        if (!ok) {
            throw new ValidacionException("No se pudo guardar la medición. Verifique la conexión.");
        }
    }

    private void validarRango(BigDecimal valor, String min, String max, String campo) throws ValidacionException {
        if (valor != null && (valor.compareTo(new BigDecimal(min)) < 0 || valor.compareTo(new BigDecimal(max)) > 0)) {
            throw new ValidacionException(campo + " debe estar entre " + min + " y " + max + ".");
        }
    }

    private void validarEje(Integer eje, String campo) throws ValidacionException {
        if (eje != null && (eje < 0 || eje > 180)) {
            throw new ValidacionException(campo + " debe estar entre 0 y 180.");
        }
    }
}
