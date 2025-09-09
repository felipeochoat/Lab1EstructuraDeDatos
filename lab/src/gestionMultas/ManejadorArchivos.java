package gestionMultas;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class ManejadorArchivos {
    private static final String MULTAS_ARCHIVO = "data/Multas_Registradas.txt";
    private static final String PAGOS_ARCHIVO = "data/Pagos_Multas.txt";

    private List<Multa> multas = new ArrayList<>();
    private List<Pago> pagos = new ArrayList<>();

    public void loadAll() {
        loadMultas();
        loadPagos();
    }

    public void loadMultas() {
        multas.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(MULTAS_ARCHIVO))) {
            String line;
            while ((line = br.readLine()) != null) {
                try {
                    multas.add(new Multa(line));
                } catch (Exception ex) {
                    System.out.println("Multa mal formada: " + line + " -> " + ex.getMessage());
                }
            }
        } catch (FileNotFoundException e) {
            // ok, archivo no existe todavía
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadPagos() {
        pagos.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(PAGOS_ARCHIVO))) {
            String line;
            while ((line = br.readLine()) != null) {
                try {
                    pagos.add(new Pago(line));
                } catch (Exception ex) {
                    System.out.println("Pago mal formado: " + line + " -> " + ex.getMessage());
                }
            }
        } catch (FileNotFoundException e) {
            // ok
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveMultas() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(MULTAS_ARCHIVO, false))) {
            for (Multa m : multas) pw.println(m.pasarACSV());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void savePagos() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(PAGOS_ARCHIVO, false))) {
            for (Pago p : pagos) pw.println(p.pasarACSV());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Multa> getMultas() { return multas; }
    public List<Pago> getPagos() { return pagos; }

    public Optional<Multa> findMultaByCodigo(String codigo) {
        return multas.stream().filter(m -> m.getCodigoMulta().equals(codigo)).findFirst();
    }

    public List<Multa> buscarPorPlaca(String placa) {
        return multas.stream().filter(m -> m.getPlaca().equalsIgnoreCase(placa)).collect(Collectors.toList());
    }

    public List<Multa> buscarPorCedula(String cedula) {
        return multas.stream().filter(m -> m.getCedulaProp().equals(cedula)).collect(Collectors.toList());
    }

    // Registrar pago (máximo 3 pagos por multa y validación de monto)
    public String registrarPago(Pago pago) {
        Optional<Multa> opt = findMultaByCodigo(pago.getCodigoMulta());
        if (!opt.isPresent()) return "Código de multa no encontrado";

        Multa m = opt.get();
        long pagosHechos = pagos.stream().filter(p -> p.getCodigoMulta().equals(pago.getCodigoMulta())).count();
        if (pagosHechos >= 3) return "Ya se realizaron 3 pagos para esta multa";

        if (pago.getMontoPagado() <= 0) return "Monto inválido";
        if (pago.getMontoPagado() > m.getMontoPendiente())
            return "El pago no puede exceder el monto pendiente (" + m.getMontoPendiente() + ")";

        pagos.add(pago);

        double nuevaRestante = m.getMontoPendiente() - pago.getMontoPagado();
        if (nuevaRestante <= 0.0001) {
            m.setMontoPendiente(0.0);
            m.setEstado("Pagada");
        } else {
            m.setMontoPendiente(nuevaRestante);
            m.setEstado("Pendiente");
        }

        savePagos();
        saveMultas();
        return "Pago registrado correctamente. Estado multa: " + m.getEstado();
    }

    public int eliminarMultasPagadas() {
        int antes = multas.size();
        multas.removeIf(m -> "Pagada".equalsIgnoreCase(m.getEstado()));
        int eliminadas = antes - multas.size();
        saveMultas();
        return eliminadas;
    }
}
