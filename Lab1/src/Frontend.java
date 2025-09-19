import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class Frontend extends JFrame {

    private Backend backend;

    public Frontend(Backend backend) {
        super("Gestión de Multas de Tránsito");
        this.backend = backend;

        setSize(750, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // === Crear Tabs ===
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(new Color(245, 245, 245));
        tabs.setForeground(new Color(40, 40, 40));

        tabs.add("Agregar Multa", crearPanelAgregarMulta());
        tabs.add("Registrar Pago", crearPanelRegistrarPago());
        tabs.add("Consultar Multas", crearPanelConsultar());
        tabs.add("Multas Vencidas", crearPanelVencidas());

        add(tabs);
        setVisible(true);
    }

    // === Panel: Agregar Multa ===
    private JPanel crearPanelAgregarMulta() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.setBackground(new Color(250, 250, 250));

        JTextField txtCodigo = new JTextField();
        JTextField txtPlaca = new JTextField();
        JTextField txtCedula = new JTextField();
        JTextField txtNombre = new JTextField();
        JComboBox<String> cbTipo = new JComboBox<>(new String[]{
                "Exceso de velocidad", "Mal parqueo", "Semáforo en rojo", "Documentos vencidos"
        });
        JTextField txtFecha = new JTextField("YYYY-MM-DD");
        JTextField txtMonto = new JTextField();

        JButton btnAgregar = new JButton("Registrar Multa");
        btnAgregar.setBackground(new Color(66, 135, 245));
        btnAgregar.setForeground(Color.WHITE);

        panel.add(new JLabel("Código (5 caracteres):")); panel.add(txtCodigo);
        panel.add(new JLabel("Placa (ABC123):")); panel.add(txtPlaca);
        panel.add(new JLabel("Cédula (10 dígitos):")); panel.add(txtCedula);
        panel.add(new JLabel("Nombre:")); panel.add(txtNombre);
        panel.add(new JLabel("Tipo:")); panel.add(cbTipo);
        panel.add(new JLabel("Fecha (YYYY-MM-DD):")); panel.add(txtFecha);
        panel.add(new JLabel("Monto:")); panel.add(txtMonto);
        panel.add(new JLabel("")); panel.add(btnAgregar);

        btnAgregar.addActionListener(e -> {
            try {
                String codigo = txtCodigo.getText().trim();
                if (codigo.length() != 5) { JOptionPane.showMessageDialog(this, "❌ Código inválido."); return; }

                String placa = txtPlaca.getText().trim().toUpperCase();
                if (!placa.matches("^[A-Z]{3}[0-9]{3}$")) { JOptionPane.showMessageDialog(this, "❌ Placa inválida."); return; }

                String cedula = txtCedula.getText().trim();
                if (!cedula.matches("^[0-9]{10}$")) { JOptionPane.showMessageDialog(this, "❌ Cédula inválida."); return; }

                String nombre = txtNombre.getText().trim();
                if (!nombre.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$")) { JOptionPane.showMessageDialog(this, "❌ Nombre inválido."); return; }

                String tipo = cbTipo.getSelectedItem().toString();

                LocalDate fecha;
                try {
                    fecha = LocalDate.parse(txtFecha.getText().trim());
                    if (fecha.isAfter(LocalDate.now())) {
                        JOptionPane.showMessageDialog(this, "❌ No se permiten fechas futuras.");
                        return;
                    }
                } catch (DateTimeParseException ex) {
                    JOptionPane.showMessageDialog(this, "❌ Fecha inválida.");
                    return;
                }

                double monto = Double.parseDouble(txtMonto.getText().trim());
                if (monto <= 0) { JOptionPane.showMessageDialog(this, "❌ Monto inválido."); return; }

                String resultado = backend.agregarMulta(codigo, placa, cedula, nombre, tipo, fecha.toString(), monto);
                JOptionPane.showMessageDialog(this, resultado);

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "❌ Monto inválido.");
            }
        });

        return panel;
    }

    // === Panel: Registrar Pago ===
    private JPanel crearPanelRegistrarPago() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.setBackground(new Color(250, 250, 250));

        JTextField txtCodigo = new JTextField();
        JTextField txtFecha = new JTextField("YYYY-MM-DD");
        JTextField txtMonto = new JTextField();

        JButton btnPago = new JButton("Registrar Pago");
        btnPago.setBackground(new Color(66, 135, 245));
        btnPago.setForeground(Color.WHITE);

        panel.add(new JLabel("Código Multa (5 caracteres):")); panel.add(txtCodigo);
        panel.add(new JLabel("Fecha de pago:")); panel.add(txtFecha);
        panel.add(new JLabel("Monto:")); panel.add(txtMonto);
        panel.add(new JLabel("")); panel.add(btnPago);

        btnPago.addActionListener(e -> {
            try {
                String codigo = txtCodigo.getText().trim();
                if (codigo.length() != 5) { JOptionPane.showMessageDialog(this, "❌ Código inválido."); return; }

                LocalDate fecha;
                try {
                    fecha = LocalDate.parse(txtFecha.getText().trim());
                    if (fecha.isAfter(LocalDate.now())) {
                        JOptionPane.showMessageDialog(this, "❌ No se permiten fechas futuras.");
                        return;
                    }
                    if (backend.fechaPasada(fecha, codigo)) {
                        JOptionPane.showMessageDialog(this, "❌ La fecha de pago no puede ser anterior a la fecha de la multa.");
                        return;
                    }
                } catch (DateTimeParseException ex) {
                    JOptionPane.showMessageDialog(this, "❌ Fecha inválida.");
                    return;
                }

                double monto = Double.parseDouble(txtMonto.getText().trim());
                if (monto <= 0) { JOptionPane.showMessageDialog(this, "❌ Monto inválido."); return; }

                Backend.PagoResultado resultado = backend.registrarPago(codigo, fecha.toString(), monto);
                JOptionPane.showMessageDialog(this, resultado.mensaje + " | Cuotas restantes: " + resultado.cuotasRestantes);

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "❌ Monto inválido.");
            }
        });

        return panel;
    }

    // === Panel: Consultar Multas ===
    private JPanel crearPanelConsultar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(250, 250, 250));

        JPanel arriba = new JPanel();
        JComboBox<String> cbFiltro = new JComboBox<>(new String[]{"Placa", "Cédula", "Código"});
        JTextField txtValor = new JTextField(10);
        JButton btnBuscar = new JButton("Consultar");

        arriba.add(new JLabel("Buscar por:"));
        arriba.add(cbFiltro);
        arriba.add(txtValor);
        arriba.add(btnBuscar);

        JTextArea areaResultados = new JTextArea();
        areaResultados.setEditable(false);
        JScrollPane scroll = new JScrollPane(areaResultados);

        btnBuscar.addActionListener(e -> {
            String opcion = cbFiltro.getSelectedItem().toString();
            String valor = txtValor.getText().trim();
            String resultado = "";

            switch (opcion) {
                case "Placa" -> resultado = backend.consultarMultaPorPlaca(valor);
                case "Cédula" -> resultado = backend.consultarMultaPorCedula(valor);
                case "Código" -> resultado = backend.consultarMultaPorCodigo(valor);
            }
            areaResultados.setText(resultado);
        });

        panel.add(arriba, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // === Panel: Consultar Multas Vencidas ===
    private JPanel crearPanelVencidas() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(250, 250, 250));

        JPanel arriba = new JPanel();
        JComboBox<String> cbFiltro = new JComboBox<>(new String[]{"Placa", "Cédula", "Código"});
        JTextField txtValor = new JTextField(10);
        JButton btnBuscar = new JButton("Buscar Vencidas");
        btnBuscar.setBackground(new Color(220, 53, 69));
        btnBuscar.setForeground(Color.WHITE);

        arriba.add(new JLabel("Buscar vencidas por:"));
        arriba.add(cbFiltro);
        arriba.add(txtValor);
        arriba.add(btnBuscar);

        JTextArea areaResultados = new JTextArea();
        areaResultados.setEditable(false);
        JScrollPane scroll = new JScrollPane(areaResultados);

        btnBuscar.addActionListener(e -> {
            String opcion = cbFiltro.getSelectedItem().toString();
            String valor = txtValor.getText().trim();
            String resultado = "";

            switch (opcion) {
                case "Placa" -> resultado = backend.consultarMultasVencidasPorPlaca(valor);
                case "Cédula" -> resultado = backend.consultarMultasVencidasPorCedula(valor);
                case "Código" -> resultado = backend.consultarMultasVencidasPorCodigo(valor);
            }
            areaResultados.setText(resultado);
        });

        panel.add(arriba, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }
}