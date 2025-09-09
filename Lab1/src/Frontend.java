import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class Frontend extends JFrame {

    private Backend backend;

    public Frontend(Backend backend) {
        super("Gestión de Multas");
        this.backend = backend;

        setSize(500, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(0, 1, 5, 5));

        JButton btnAgregar = new JButton("Agregar Multa");
        JButton btnPago = new JButton("Registrar Pago");
        JButton btnConsultar = new JButton("Consultar Multa por código");

        add(btnAgregar);
        add(btnPago);
        add(btnConsultar);

        // ==================
        // 1. AGREGAR MULTA 
        // ==================
        btnAgregar.addActionListener(e -> {
            // --- Código ---
            String codigo = null;
            while (codigo == null || codigo.length() != 5) {
                codigo = JOptionPane.showInputDialog(this, "Código (5 caracteres):");
                if (codigo == null) return;
                if (codigo.length() != 5) JOptionPane.showMessageDialog(this, "❌ Código inválido: deben ser 5 caracteres.");
            }

            // --- Placa ---
            String placa = null;
            while (placa == null || !placa.matches("^[A-Z]{3}[0-9]{3}$")) {
                placa = JOptionPane.showInputDialog(this, "Placa (formato ABC123):");
                if (placa == null) return;
                if (!placa.matches("^[A-Z]{3}[0-9]{3}$")) JOptionPane.showMessageDialog(this, "❌ Placa inválida: formato correcto es ABC123.");
            }

            // --- Cédula ---
            String cedula = null;
            while (cedula == null || !cedula.matches("^[0-9]{10}$")) {
                cedula = JOptionPane.showInputDialog(this, "Cédula (10 dígitos):");
                if (cedula == null) return;
                if (!cedula.matches("^[0-9]{10}$")) JOptionPane.showMessageDialog(this, "❌ Cédula inválida: debe tener 10 dígitos.");
            }

            // --- Nombre ---
            String nombre = null;
            while (nombre == null || !nombre.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$")) {
                nombre = JOptionPane.showInputDialog(this, "Nombre propietario:");
                if (nombre == null) return;
                if (!nombre.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$")) JOptionPane.showMessageDialog(this, "❌ Nombre inválido: solo letras y espacios.");
            }

            // --- Tipo de infracción ---
            String[] tipos = {"Exceso de velocidad", "Mal parqueo", "Semáforo en rojo", "Documentos vencidos"};
            String tipo = (String) JOptionPane.showInputDialog(this, "Seleccione el tipo de infracción:", "Tipo de infracción",
                    JOptionPane.QUESTION_MESSAGE, null, tipos, tipos[0]);
            if (tipo == null) return;

            // --- Fecha ---
            String fechaStr = null;
            LocalDate fecha = null;
            while (true) {
                fechaStr = JOptionPane.showInputDialog(this, "Fecha (YYYY-MM-DD):");
                if (fechaStr == null) return;
                fechaStr = fechaStr.trim();
                if (!fechaStr.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
                    JOptionPane.showMessageDialog(this, "❌ Formato inválido. Use YYYY-MM-DD.");
                    continue;
                }
                try {
                    fecha = LocalDate.parse(fechaStr);
                } catch (DateTimeParseException ex) {
                    JOptionPane.showMessageDialog(this, "❌ Fecha inválida.");
                    continue;
                }
                if (fecha.isAfter(LocalDate.now())) {
                    JOptionPane.showMessageDialog(this, "❌ La fecha no puede ser futura.");
                    continue;
                }
                break;
            }

            // --- Monto ---
            double monto = 0;
            boolean montoValido = false;
            while (!montoValido) {
                String montoStr = JOptionPane.showInputDialog(this, "Monto:");
                if (montoStr == null) return;
                try {
                    monto = Double.parseDouble(montoStr);
                    if (monto > 0) montoValido = true;
                    else JOptionPane.showMessageDialog(this, "❌ El monto debe ser mayor que 0.");
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "❌ Monto inválido: debe ser un número.");
                }
            }

            // Registrar en backend
            String resultado = backend.agregarMulta(codigo, placa, cedula, nombre, tipo, fecha.toString(), monto);
            JOptionPane.showMessageDialog(this, resultado);
        });

        // ==================
        // 2. REGISTRAR PAGO
        // ==================
        btnPago.addActionListener(e -> {
            // --- Código multa ---
            String codigo = null;
            while (codigo == null || codigo.length() != 5) {
                codigo = JOptionPane.showInputDialog(this, "Código multa (5 caracteres):");
                if (codigo == null) return;
                if (codigo.length() != 5) JOptionPane.showMessageDialog(this, "❌ Código inválido: deben ser 5 caracteres.");
            }

            // --- Fecha pago ---
            String fechaStr = null;
            while (true) {
                fechaStr = JOptionPane.showInputDialog(this, "Fecha de pago (YYYY-MM-DD):");
                if (fechaStr == null) return;
                fechaStr = fechaStr.trim();
                if (!fechaStr.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
                    JOptionPane.showMessageDialog(this, "❌ Formato inválido. Use YYYY-MM-DD.");
                    continue;
                }
                try {
                    LocalDate fecha = LocalDate.parse(fechaStr);
                    if (fecha.isAfter(LocalDate.now())) {
                        JOptionPane.showMessageDialog(this, "❌ La fecha no puede ser futura.");
                        continue;
                    }
                } catch (DateTimeParseException ex) {
                    JOptionPane.showMessageDialog(this, "❌ Fecha inválida.");
                    continue;
                }
                break;
            }

            // --- Monto pago ---
            double monto = 0;
            boolean montoValido = false;
            while (!montoValido) {
                String montoPagoStr = JOptionPane.showInputDialog(this, "Monto pagado:");
                if (montoPagoStr == null) return;
                try {
                    monto = Double.parseDouble(montoPagoStr);
                    if (monto > 0) montoValido = true;
                    else JOptionPane.showMessageDialog(this, "❌ El monto debe ser mayor que 0.");
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "❌ Monto inválido: debe ser un número.");
                }
            }

            // Llamar al backend y mostrar resultado
            Backend.PagoResultado resultado = backend.registrarPago(codigo, fechaStr, monto);
            JOptionPane.showMessageDialog(this, resultado.mensaje + " | Cuotas restantes: " + resultado.cuotasRestantes + "/3");
        });

        // ===============================
        // 3. CONSULTAR MULTA POR CÓDIGO
        // ===============================
        btnConsultar.addActionListener(e -> {
            String codigo = JOptionPane.showInputDialog(this, "Ingrese el código de la multa (5 caracteres):");
            if (codigo == null || codigo.length() != 5) {
                JOptionPane.showMessageDialog(this, "❌ Código inválido.");
                return;
            }

            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new FileReader("Lab1/archivos/Pagos_Multas.txt"))) {
                String linea;
                while ((linea = br.readLine()) != null) {
                    String[] partes = linea.split(",");
                    if (partes[0].equals(codigo)) {
                        sb.append("Código: ").append(partes[0])
                                .append(" | Fecha: ").append(partes[1])
                                .append(" | Monto: ").append(partes[2]).append("\n");
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            if (sb.length() == 0) {
                JOptionPane.showMessageDialog(this, "No hay pagos registrados para el código " + codigo);
            } else {
                JOptionPane.showMessageDialog(this, sb.toString());
            }
        });

        setLocationRelativeTo(null);
        setVisible(true);
    }
}
