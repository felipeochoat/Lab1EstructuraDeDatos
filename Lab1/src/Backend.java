import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Backend {

    // Archivos que usa el sistema
    private File Multas_Registradas = new File("Lab1/archivos/Multas_Registradas.txt");
    private File Pagos_Multas = new File("Lab1/archivos/Pagos_Multas.txt");
    private File Multas_Eliminadas = new File("Lab1/archivos/Multas_Eliminadas.txt");
    private File Cuotas_Pagadas = new File("Lab1/archivos/Cuotas_Pagadas.txt");

    // Constructor: crea carpeta y archivos si no existen
    public Backend() throws IOException {
        File carpeta = new File("Lab1/archivos");
        if (!carpeta.exists()) carpeta.mkdirs();

        if (!Multas_Registradas.exists()) Multas_Registradas.createNewFile();
        if (!Pagos_Multas.exists()) Pagos_Multas.createNewFile();
        if (!Multas_Eliminadas.exists()) Multas_Eliminadas.createNewFile();
        if (!Cuotas_Pagadas.exists()) Cuotas_Pagadas.createNewFile();
    }

    // === Agregar una multa nueva ===
    public String agregarMulta(String codigo, String placa, String cedula, String nombre,
                               String tipo, String fecha, double monto) {

        if (monto <= 0) return "❌ El monto debe ser mayor que 0.";
        try {
            // revisar si ya existe una multa con ese código
            if (existeCodigoMulta(codigo)) {
                return "❌ Ya existe una multa con el código: " + codigo;
            }

            // escribir en el archivo de multas registradas
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

    // Verifica si ya existe el código en el archivo
    public boolean existeCodigoMulta(String codigo) throws IOException {
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

    // Clase auxiliar para devolver info de pagos
    public static class PagoResultado {
        public String mensaje;
        public int cuotasRestantes;

        public PagoResultado(String mensaje, int cuotasRestantes) {
            this.mensaje = mensaje;
            this.cuotasRestantes = cuotasRestantes;
        }
    }

    // === Registrar un pago ===
    public PagoResultado registrarPago(String codigo, String fecha, double monto) {
        if (monto <= 0) return new PagoResultado("❌ El monto del pago debe ser mayor que 0.", 0);

        File tempFile = new File("Lab1/archivos/temp_multas.txt");
        boolean encontrado = false;
        boolean eliminada = false;
        String multaEliminada = "";
        double pendiente = 0;

        // Buscar multa y validar monto
        try (BufferedReader br = new BufferedReader(new FileReader(Multas_Registradas))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(",");
                if (partes[0].equals(codigo)) {
                    encontrado = true;
                    pendiente = Double.parseDouble(partes[6]);
                    if (monto > pendiente) {
                        return new PagoResultado("❌ El pago no puede exceder el monto pendiente (" + pendiente + ")", 0);
                    }
                }
            }
        } catch (IOException e) {
            return new PagoResultado("❌ Error al procesar el pago.", 0);
        }

        if (!encontrado) return new PagoResultado("❌ No existe ninguna multa con el código " + codigo, 0);

        // Consultar las cuotas ya pagadas antes de crear archivo temporal
        int cuotas = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(Cuotas_Pagadas))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(",");
                if (partes[0].equals(codigo)) {
                    cuotas = Integer.parseInt(partes[1]);
                    break;
                }
            }
        } catch (IOException e) {
            // archivo vacío
        }

        // Si ya son 3 cuotas, se detiene aquí
        if (cuotas >= 3) {
            return new PagoResultado("❌ Esta multa ya tiene el máximo de 3 pagos.", 0);
        }

        // Actualizar cuotas en archivo temporal
        cuotas++;
        File tempCuotas = new File("Lab1/archivos/temp_cuotas.txt");

        try (BufferedReader br = new BufferedReader(new FileReader(Cuotas_Pagadas));
             PrintWriter pwTemp = new PrintWriter(new FileWriter(tempCuotas))) {

            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(",");
                if (!partes[0].equals(codigo)) pwTemp.println(linea);
            }
            // registrar el nuevo número de cuotas
            pwTemp.println(codigo + "," + cuotas);

        } catch (IOException e) {
            e.printStackTrace();
            return new PagoResultado("❌ Error al actualizar las cuotas pagadas.", 0);
        }

        if (Cuotas_Pagadas.delete()) tempCuotas.renameTo(Cuotas_Pagadas);

        // Procesar la actualización de Multas_Registradas
        try (BufferedReader br = new BufferedReader(new FileReader(Multas_Registradas));
             PrintWriter pwTemp = new PrintWriter(new FileWriter(tempFile))) {

            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(",");

                if (partes[0].equals(codigo)) {
                    double nuevoPendiente = pendiente - monto;

                    // Guardar el pago en archivo de pagos
                    try (PrintWriter pwPago = new PrintWriter(new FileWriter(Pagos_Multas, true))) {
                        pwPago.println(codigo + "," + fecha + "," + monto);
                    }

                    // Si ya está saldada → eliminarla
                    if (nuevoPendiente <= 0.0001) {
                        String pagada = partes[0] + "," + partes[1] + "," + partes[2] + "," + partes[3] + ","
                                + partes[4] + "," + partes[5] + ",0,Pagada";
                        eliminada = true;
                        multaEliminada = pagada;
                        limpiarPagosAsociados(codigo);
                    } else {
                        // Si no, actualizar pendiente
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

        if (Multas_Registradas.delete()) tempFile.renameTo(Multas_Registradas);

        int cuotasRestantes = 3 - cuotas;
        if (eliminada) {
            try (PrintWriter pwEliminadas = new PrintWriter(new FileWriter(Multas_Eliminadas, true))) {
                pwEliminadas.println(multaEliminada + " → Eliminada tras pago completo en " + LocalDate.now());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new PagoResultado("✅ Pago registrado. La multa " + codigo + " fue eliminada y archivada.", cuotasRestantes);
        } else {
            return new PagoResultado("✅ Pago registrado y monto pendiente actualizado.", cuotasRestantes);
        }
    }

    // Verifica que la fecha de pago no sea anterior a la fecha de la multa
    public boolean fechaPasada(LocalDate fecha, String codigo) {
        try (BufferedReader br = new BufferedReader(new FileReader(Multas_Registradas))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(",");
                if (partes[0].equals(codigo)) {
                    LocalDate fechaMulta = LocalDate.parse(partes[5], DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    if (fecha.isBefore(fechaMulta)) return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Borra todos los pagos de una multa ya eliminada
    private void limpiarPagosAsociados(String codigo) {
        File tempPagos = new File("Lab1/archivos/temp_pagos.txt");
        try (BufferedReader br = new BufferedReader(new FileReader(Pagos_Multas));
             PrintWriter pw = new PrintWriter(new FileWriter(tempPagos))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(",");
                if (!partes[0].equals(codigo)) pw.println(linea);
            }
        } catch (IOException e) { e.printStackTrace(); }
        if (Pagos_Multas.delete()) tempPagos.renameTo(Pagos_Multas);
    }

    // Consultar multas por placa
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

    // Consultar multas por cédula
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

    // Consultar multas por código
    public String consultarMultaPorCodigo(String codigo) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(Multas_Registradas))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(",");
                if (partes.length >= 7 && partes[0].equals(codigo)) {
                    sb.append(linea).append("\n");
                }
            }
        } catch (IOException e) {
            return "❌ Error al leer las multas.";
        }
        return sb.length() == 0 ? "No se encontraron multas para el código " + codigo : sb.toString();
    }

    // Consultar multas vencidas (más de 90 días sin pagar)
    public String consultarMultasVencidasPorPlaca(String placa) {
        return consultarMultasVencidas("placa", placa);
    }
    public String consultarMultasVencidasPorCedula(String cedula) {
        return consultarMultasVencidas("cedula", cedula);
    }
    public String consultarMultasVencidasPorCodigo(String codigo) {
        return consultarMultasVencidas("codigo", codigo);
    }

    private String consultarMultasVencidas(String tipo, String valor) {
        StringBuilder sb = new StringBuilder();
        LocalDate hoy = LocalDate.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        try (BufferedReader br = new BufferedReader(new FileReader(Multas_Registradas))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(",");
                if (partes.length >= 7) {
                    String codigo = partes[0];
                    String placa = partes[1];
                    String cedula = partes[2];
                    String fechaStr = partes[5];
                    double pendiente = Double.parseDouble(partes[6]);

                    try {
                        LocalDate fechaMulta = LocalDate.parse(fechaStr, fmt);
                        long dias = java.time.temporal.ChronoUnit.DAYS.between(fechaMulta, hoy);

                        boolean coincide = (tipo.equals("placa") && placa.equalsIgnoreCase(valor))
                                || (tipo.equals("cedula") && cedula.equals(valor))
                                || (tipo.equals("codigo") && codigo.equals(valor));

                        if (coincide && dias > 90 && pendiente > 0.0001) {
                            sb.append(linea).append(" → VENCIDA (").append(dias).append(" días)\n");
                        }
                    } catch (DateTimeParseException e) {
                        // fecha mal formateada → se ignora
                    }
                }
            }
        } catch (IOException e) {
            return "❌ Error al leer las multas.";
        }

        return sb.length() == 0 ? "No se encontraron multas vencidas para " + valor : sb.toString();
    }
}