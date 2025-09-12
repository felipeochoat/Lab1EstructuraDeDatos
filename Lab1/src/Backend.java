import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Backend {

    private File Multas_Registradas = new File("Lab1/archivos/Multas_Registradas.txt");
    private File Pagos_Multas = new File("Lab1/archivos/Pagos_Multas.txt");
    private File Multas_Eliminadas = new File("Lab1/archivos/Multas_Eliminadas.txt");
    private File Cuotas_Pagadas = new File("Lab1/archivos/Cuotas_Pagadas.txt");

    public Backend() throws IOException {
        File carpeta = new File("Lab1/archivos");
        if (!carpeta.exists()) carpeta.mkdirs();

        if (!Multas_Registradas.exists()) Multas_Registradas.createNewFile();
        if (!Pagos_Multas.exists()) Pagos_Multas.createNewFile();
        if (!Multas_Eliminadas.exists()) Multas_Eliminadas.createNewFile();
        if (!Cuotas_Pagadas.exists()) Cuotas_Pagadas.createNewFile();
    }

    // === 1. Agregar multa ===
    public String agregarMulta(String codigo, String placa, String cedula, String nombre,
                               String tipo, String fecha, double monto) {

       if (monto <= 0) return "❌ El monto debe ser mayor que 0.";
    try {
        if (existeCodigoMulta(codigo)==true) {
            return "❌ Ya existe una multa con el código: " + codigo;
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter(Multas_Registradas, true))) {
            pw.println(codigo + "," + placa + "," + cedula + "," + nombre + ","
                    + tipo + "," + fecha + "," + monto + ",Pendiente");
        }
        return "✅ Multa registrada correctamente.";
    } catch (IOException e) {
        e.printStackTrace();
        return "❌ Error al registrar la multa.";
    }
}

    public boolean existeCodigoMulta(String codigo) throws IOException{
        try (BufferedReader br = new BufferedReader(new FileReader(Multas_Registradas))) {
        String linea;
        while ((linea = br.readLine()) != null) {
            String[] partes = linea.split(",");
            if (partes[0].equals(codigo)) {
                return true; 
            }
        }
        return false; 
    }
}

    
    
    // === Clase interna para devolver resultado de pago ===
    public static class PagoResultado {
        public String mensaje;
        public int cuotasRestantes;

        public PagoResultado(String mensaje, int cuotasRestantes) {
            this.mensaje = mensaje;
            this.cuotasRestantes = cuotasRestantes;
        }
    }

    // === 2. Registrar pago ===
    public PagoResultado registrarPago(String codigo, String fecha, double monto) {
        if (monto <= 0) return new PagoResultado("❌ El monto del pago debe ser mayor que 0.", 0);

        // --- Actualizar cuotas ---
        int cuotas = 0;
        File tempCuotas = new File("Lab1/archivos/temp_cuotas.txt");

        try (BufferedReader br = new BufferedReader(new FileReader(Cuotas_Pagadas));
             PrintWriter pwTemp = new PrintWriter(new FileWriter(tempCuotas))) {

            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(",");
                if (!partes[0].equals(codigo)) pwTemp.println(linea);
                else cuotas = Integer.parseInt(partes[1]);
            }

        } catch (IOException e) {
            // archivo vacío
        }

        if (cuotas >= 3) return new PagoResultado("❌ Esta multa ya tiene el máximo de 3 pagos.", 0);

        cuotas++;

        try (PrintWriter pw = new PrintWriter(new FileWriter(tempCuotas, true))) {
            pw.println(codigo + "," + cuotas);
        } catch (IOException e) {
            e.printStackTrace();
            return new PagoResultado("❌ Error al actualizar las cuotas pagadas.", 0);
        }

        if (Cuotas_Pagadas.delete()) tempCuotas.renameTo(Cuotas_Pagadas);

        // --- Procesar pago en Multas_Registradas ---
        File tempFile = new File("Lab1/archivos/temp_multas.txt");
        boolean encontrado = false;
        boolean eliminada = false;
        String multaEliminada = "";

        try (BufferedReader br = new BufferedReader(new FileReader(Multas_Registradas));
             PrintWriter pwTemp = new PrintWriter(new FileWriter(tempFile))) {

            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(",");

                if (partes[0].equals(codigo)) {
                    encontrado = true;
                    double pendiente = Double.parseDouble(partes[6]);

                    if (monto > pendiente) {
                        tempFile.delete();
                        return new PagoResultado("❌ El pago no puede exceder el monto pendiente (" + pendiente + ")", 3 - (cuotas - 1));
                    }

                    double nuevoPendiente = pendiente - monto;

                    try (PrintWriter pwPago = new PrintWriter(new FileWriter(Pagos_Multas, true))) {
                        pwPago.println(codigo + "," + fecha + "," + monto);
                    }

                    if (nuevoPendiente <= 0.0001) {
                        eliminada = true;
                        multaEliminada = linea;
                    } else {
                        pwTemp.println(partes[0] + "," + partes[1] + "," + partes[2] + "," + partes[3] + ","
                                + partes[4] + "," + partes[5] + "," + nuevoPendiente + ",Pendiente");
                    }

                } else {
                    pwTemp.println(linea);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            return new PagoResultado("❌ Error al procesar el pago.", 3 - (cuotas - 1));
        }

        if (!encontrado) {
            tempFile.delete();
            return new PagoResultado("❌ No existe ninguna multa con el código " + codigo, 0);
        }

        if (Multas_Registradas.delete()) tempFile.renameTo(Multas_Registradas);

        int cuotasRestantes = 3 - cuotas;
        if (eliminada) {
            try (PrintWriter pwEliminadas = new PrintWriter(new FileWriter(Multas_Eliminadas, true))) {
                pwEliminadas.println(multaEliminada);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new PagoResultado("✅ Pago registrado. La multa " + codigo + " fue eliminada y archivada.", cuotasRestantes);
        } else {
            return new PagoResultado("✅ Pago registrado y monto pendiente actualizado.", cuotasRestantes);
        }
    }

    // === 3. Consultar todas las multas ===
    public String consultarMultas() {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(Multas_Registradas))) {
            String linea;
            while ((linea = br.readLine()) != null) sb.append(linea).append("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    // === 4. Consultar multas por placa ===
    public String consultarMultaPorPlaca(String placa) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(Multas_Registradas))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(",");
                if (partes.length >= 7 && partes[1].equalsIgnoreCase(placa)) {
                    sb.append(linea).append("\n");
                }
            }
        } catch (IOException e) {
            return "❌ Error al leer las multas.";
        }
        return sb.length() == 0 ? "No se encontraron multas para la placa " + placa : sb.toString();
    }

    // === 5. Consultar multas por cédula ===
    public String consultarMultaPorCedula(String cedula) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(Multas_Registradas))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(",");
                if (partes.length >= 7 && partes[2].equals(cedula)) {
                    sb.append(linea).append("\n");
                }
            }
        } catch (IOException e) {
            return "❌ Error al leer las multas.";
        }
        return sb.length() == 0 ? "No se encontraron multas para la cédula " + cedula : sb.toString();
    }

    // === 6. Consultar multas vencidas por placa ===
    public String consultarMultasVencidasPorPlaca(String placa) {
        return consultarMultasVencidas("placa", placa);
    }

    // === 7. Consultar multas vencidas por cédula ===
    public String consultarMultasVencidasPorCedula(String cedula) {
        return consultarMultasVencidas("cedula", cedula);
    }

    // === Lógica común para vencidas ===
    private String consultarMultasVencidas(String tipo, String valor) {
        StringBuilder sb = new StringBuilder();
        LocalDate hoy = LocalDate.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        try (BufferedReader br = new BufferedReader(new FileReader(Multas_Registradas))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(",");
                if (partes.length >= 7) {
                    String placa = partes[1];
                    String cedula = partes[2];
                    String fechaStr = partes[5];
                    double pendiente = Double.parseDouble(partes[6]);

                    try {
                        LocalDate fechaMulta = LocalDate.parse(fechaStr, fmt);
                        long dias = java.time.temporal.ChronoUnit.DAYS.between(fechaMulta, hoy);

                        boolean coincide = (tipo.equals("placa") && placa.equalsIgnoreCase(valor))
                                || (tipo.equals("cedula") && cedula.equals(valor));

                        if (coincide && dias > 90 && pendiente > 0.0001) {
                            sb.append(linea).append(" → VENCIDA (").append(dias).append(" días)\n");
                        }
                    } catch (DateTimeParseException e) {
                        // ignorar líneas mal formateadas
                    }
                }
            }
        } catch (IOException e) {
            return "❌ Error al leer las multas.";
        }

        return sb.length() == 0 ? "No se encontraron multas vencidas para " + valor : sb.toString();
    }
}