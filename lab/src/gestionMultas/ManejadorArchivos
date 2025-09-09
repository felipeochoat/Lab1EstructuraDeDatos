package gestionMultas;

import gestionMultas.Multa;
import gestionMultas.Pago;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ManejadorArchivos {
    private static final String MULTAS_ARCHIVO = "data/Multas_Registradas.txt";
    private static final String PAGOS_ARCHIVO = "data/Pagos_Multas.txt";

    private List<Multa> multas = new ArrayList<>();
    private List<Pago> pagos = new ArrayList<>();

    public void loadAll() {
        loadMultas();
        loadPagos();
        actualizarEstadosSegunPagos();
    }

    public void loadMultas() {
        multas.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(MULTAS_ARCHIVO))) {
            String line;
            while ((line = br.readLine()) != null) {
                try {
                    // usas el constructor que recibe línea CSV
                    Multa m = new Multa(line);
                    multas.add(m);
                } catch (Exception ex) {
                    System.out.println("Multa mal formada: " + line + " -> " + ex.getMessage());
                }
            }
        } catch (FileNotFoundException e) {
            // si no existe el archivo, no pasa nada
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
                    Pago p = new Pago(line); // haz tu clase Pago igual que Multa, con constructor CSV
                    pagos.add(p);
                } catch (Exception ex) {
                    System.out.println("Pago mal formado: " + line + " -> " + ex.getMessage());
                }
            }
        } catch (FileNotFoundException e) {
            // si no existe, se ignora
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveMultas() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(MULTAS_ARCHIVO, false))) {
            for (Multa m : multas) pw.println(m.pasarACSV()); // usas tu método
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

    public List<Multa> getMultas() {
        return multas;
    }

    public List<Pago> getPagos() {
        return pagos;
    }

    public Optional<Multa> findMultaByCodigo(String codigo) {
        return multas.stream().filter(m -> m.getCodigoMulta().equals(codigo)).findFirst();
    }

    public List<Multa> buscarPorPlaca(String placa) {
        List<Multa> res = new ArrayList<>();
        for (Multa m : multas) if (m.getPlaca().equalsIgnoreCase(placa)) res.add(m);
        return res;
    }

    public List<Multa> buscarPorCedula(String cedula) {
        List<Multa> res = new ArrayList<>();
        for (Multa m : multas) if (m.getCedulaProp().equals(cedula)) res.add(m);
        return res;
    }

    // Registrar pago (valida max 3 pagos y monto)
    public String registrarPago(Pago pago) {
        Optional<Multa> opt = findMultaByCodigo(pago.getCodigoMulta());
        if (!opt.isPresent()) return "Código de multa no encontrado";

        Multa m = opt.get();
        long pagosHechos = pagos.stream()
                .filter(p -> p.getCodigoMulta().equals(pago.getCodigoMulta()))
                .count();
        if (pagosHechos >= 3) return "Ya se realizaron 3 pagos para esta multa";

        if (pago.getMontoPagado() <= 0) return "Monto inválido";

        if (pago.getMontoPagado() > m.getMontoPendiente()) {
            return "El pago no puede exceder el monto pendiente (" + m.getMontoPendiente() + ")";
        }

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

    public void actualizarEstadosSegunPagos() {
        for (Multa m : multas) {
            double sumPagos = pagos.stream()
                    .filter(p -> p.getCodigoMulta().equals(m.getCodigoMulta()))
                    .mapToDouble(Pago::getMontoPagado)
                    .sum();

            double restante = m.getMontoPendiente() - sumPagos;
            if (restante <= 0.0001) {
                m.setMontoPendiente(0.0);
                m.setEstado("Pagada");
            } else {
                m.setMontoPendiente(restante);
                m.setEstado("Pendiente");
            }
        }
        saveMultas();
    }
}