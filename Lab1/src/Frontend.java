import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class Frontend extends JFrame {

    private Backend backend;

    public Frontend(Backend backend) {
        super("Gestión de Multas");
        this.backend = backend;

        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(0, 1, 5, 5));

        JButton btnAgregar = new JButton("Agregar Multa");
        JButton btnPago = new JButton("Registrar Pago");
        JButton btnConsultar = new JButton("Consultar Multa");
        JButton btnVencidas = new JButton("Consultar Multas Vencidas");

        add(btnAgregar);
        add(btnPago);
        add(btnConsultar);
        add(btnVencidas);

        // === 1. Agregar multa ===
        btnAgregar.addActionListener(e -> {
            // Código
            String codigo = null;
            while (codigo == null || codigo.length() != 5) {
                codigo = JOptionPane.showInputDialog(this, "Código (5 caracteres):");
                if (codigo == null) return;
                if (codigo.length() != 5) JOptionPane.showMessageDialog(this, "❌ Código inválido.");
               
            }

            // Placa
            String placa = null;
            while (placa == null || !placa.matches("^[A-Z]{3}[0-9]{3}$")) {
                placa = JOptionPane.showInputDialog(this, "Placa (formato ABC123):");
                if (placa == null) return;
                if (!placa.matches("^[A-Z]{3}[0-9]{3}$")) JOptionPane.showMessageDialog(this, "❌ Placa inválida.");
            }

            // Cédula
            String cedula = null;
            while (cedula == null || !cedula.matches("^[0-9]{10}$")) {
                cedula = JOptionPane.showInputDialog(this, "Cédula (10 dígitos):");
                if (cedula == null) return;
                if (!cedula.matches("^[0-9]{10}$")) JOptionPane.showMessageDialog(this, "❌ Cédula inválida.");
            }

            // Nombre
            String nombre = null;
            while (nombre == null || !nombre.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$")) {
                nombre = JOptionPane.showInputDialog(this, "Nombre propietario:");
                if (nombre == null) return;
                if (!nombre.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$")) JOptionPane.showMessageDialog(this, "❌ Nombre inválido.");
            }

            // Tipo
            String[] tipos = {"Exceso de velocidad", "Mal parqueo", "Semáforo en rojo", "Documentos vencidos"};
            String tipo = (String) JOptionPane.showInputDialog(this, "Tipo de infracción:", "Tipo",
                    JOptionPane.QUESTION_MESSAGE, null, tipos, tipos[0]);
            if (tipo == null) return;

            // Fecha
            LocalDate fecha = null;
            while (fecha == null) {
                String fechaStr = JOptionPane.showInputDialog(this, "Fecha (YYYY-MM-DD):");
                if (fechaStr == null) return;
                try {
                    fecha = LocalDate.parse(fechaStr.trim());
                    if (fecha.isAfter(LocalDate.now())) {
                        JOptionPane.showMessageDialog(this, "❌ No se permiten fechas futuras.");
                        fecha = null;
                    }
                } catch (DateTimeParseException ex) {
                    JOptionPane.showMessageDialog(this, "❌ Fecha inválida.");
                }
            }

            // Monto
            double monto = 0;
            while (monto <= 0) {
                String montoStr = JOptionPane.showInputDialog(this, "Monto:");
                if (montoStr == null) return;
                try {
                    monto = Double.parseDouble(montoStr);
                    if (monto <= 0) JOptionPane.showMessageDialog(this, "❌ Monto inválido.");
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "❌ Monto inválido.");
                }
            }

            String resultado = backend.agregarMulta(codigo, placa, cedula, nombre, tipo, fecha.toString(), monto);
            JOptionPane.showMessageDialog(this, resultado);
        });

        // === 2. Registrar pago ===
        btnPago.addActionListener(e -> {
            String codigo = JOptionPane.showInputDialog(this, "Código multa (5 caracteres):");
            if (codigo == null || codigo.length() != 5) {
                JOptionPane.showMessageDialog(this, "❌ Código inválido.");
                return;
            }

            LocalDate fecha = null;
            while (fecha == null) {
                String fechaStr = JOptionPane.showInputDialog(this, "Fecha de pago (YYYY-MM-DD):");
                if (fechaStr == null) return;
                try {
                    fecha = LocalDate.parse(fechaStr.trim());
                    if (fecha.isAfter(LocalDate.now())) {
                        JOptionPane.showMessageDialog(this, "❌ No se permiten fechas futuras.");
                        fecha = null;
                    }
                } catch (DateTimeParseException ex) {
                    JOptionPane.showMessageDialog(this, "❌ Fecha inválida.");
                }
            }

            double monto = 0;
            while (monto <= 0) {
                String montoStr = JOptionPane.showInputDialog(this, "Monto pagado:");
                if (montoStr == null) return;
                try {
                    monto = Double.parseDouble(montoStr);
                    if (monto <= 0) JOptionPane.showMessageDialog(this, "❌ Monto inválido.");
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "❌ Monto inválido.");
                }
            }

            Backend.PagoResultado resultado = backend.registrarPago(codigo, fecha.toString(), monto);
            JOptionPane.showMessageDialog(this, resultado.mensaje + " | Cuotas restantes: " + resultado.cuotasRestantes);
        });

        // === 3. Consultar multa (por placa o cédula) ===
        btnConsultar.addActionListener(e -> {
            String[] opciones = {"Placa", "Cédula"};
            String opcion = (String) JOptionPane.showInputDialog(this, "Buscar por:", "Consulta",
                    JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0]);
            if (opcion == null) return;

            String resultado = "";
            if (opcion.equals("Placa")) {
                String placa = JOptionPane.showInputDialog(this, "Placa (ABC123):");
                if (placa == null || !placa.matches("^[A-Z]{3}[0-9]{3}$")) {
                    JOptionPane.showMessageDialog(this, "❌ Placa inválida.");
                    return;
                }
                resultado = backend.consultarMultaPorPlaca(placa);
            } else {
                String cedula = JOptionPane.showInputDialog(this, "Cédula (10 dígitos):");
                if (cedula == null || !cedula.matches("^[0-9]{10}$")) {
                    JOptionPane.showMessageDialog(this, "❌ Cédula inválida.");
                    return;
                }
                resultado = backend.consultarMultaPorCedula(cedula);
            }

            JOptionPane.showMessageDialog(this, resultado);
        });

        // === 4. Consultar multas vencidas ===
        btnVencidas.addActionListener(e -> {
            String[] opciones = {"Placa", "Cédula"};
            String opcion = (String) JOptionPane.showInputDialog(this, "Buscar vencidas por:", "Consulta Vencidas",
                    JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0]);
            if (opcion == null) return;

            String resultado = "";
            if (opcion.equals("Placa")) {
                String placa = JOptionPane.showInputDialog(this, "Placa (ABC123):");
                if (placa == null || !placa.matches("^[A-Z]{3}[0-9]{3}$")) {
                    JOptionPane.showMessageDialog(this, "❌ Placa inválida.");
                    return;
                }
                resultado = backend.consultarMultasVencidasPorPlaca(placa);
            } else {
                String cedula = JOptionPane.showInputDialog(this, "Cédula (10 dígitos):");
                if (cedula == null || !cedula.matches("^[0-9]{10}$")) {
                    JOptionPane.showMessageDialog(this, "❌ Cédula inválida.");
                    return;
                }
                resultado = backend.consultarMultasVencidasPorCedula(cedula);
            }

            JOptionPane.showMessageDialog(this, resultado);
        });

        setLocationRelativeTo(null);
        setVisible(true);
    }
}