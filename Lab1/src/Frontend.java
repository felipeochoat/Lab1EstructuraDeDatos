import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class Frontend extends JFrame {

    private Backend backend;

    public Frontend(Backend backend) {
        super("Gestión de Multas de Tránsito");
        this.backend = backend;

        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // === Crear Tabs ===
        JTabbedPane tabs = new JTabbedPane();

        // Paneles
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

        panel.add(new JLabel("Código:")); panel.add(txtCodigo);
        panel.add(new JLabel("Placa:")); panel.add(txtPlaca);
        panel.add(new JLabel("Cédula:")); panel.add(txtCedula);
        panel.add(new JLabel("Nombre:")); panel.add(txtNombre);
        panel.add(new JLabel("Tipo:")); panel.add(cbTipo);
        panel.add(new JLabel("Fecha:")); panel.add(txtFecha);
        panel.add(new JLabel("Monto:")); panel.add(txtMonto);
        panel.add(new JLabel("")); panel.add(btnAgregar);

        btnAgregar.addActionListener(e -> {
            try {
                String codigo = txtCodigo.getText().trim();
                String placa = txtPlaca.getText().trim();
                String cedula = txtCedula.getText().trim();
                String nombre = txtNombre.getText().trim();
                String tipo = cbTipo.getSelectedItem().toString();
                String fecha = txtFecha.getText().trim();
                double monto = Double.parseDouble(txtMonto.getText().trim());

                String resultado = backend.agregarMulta(codigo, placa, cedula, nombre, tipo, fecha, monto);
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

        JTextField txtCodigo = new JTextField();
        JTextField txtFecha = new JTextField("YYYY-MM-DD");
        JTextField txtMonto = new JTextField();

        JButton btnPago = new JButton("Registrar Pago");

        panel.add(new JLabel("Código Multa:")); panel.add(txtCodigo);
        panel.add(new JLabel("Fecha de pago:")); panel.add(txtFecha);
        panel.add(new JLabel("Monto:")); panel.add(txtMonto);
        panel.add(new JLabel("")); panel.add(btnPago);

        btnPago.addActionListener(e -> {
            try {
                String codigo = txtCodigo.getText().trim();
                String fecha = txtFecha.getText().trim();
                double monto = Double.parseDouble(txtMonto.getText().trim());

                Backend.PagoResultado resultado = backend.registrarPago(codigo, fecha, monto);
                JOptionPane.showMessageDialog(this, resultado.mensaje +
                        " | Cuotas restantes: " + resultado.cuotasRestantes);

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "❌ Monto inválido.");
            }
        });

        return panel;
    }

    // === Panel: Consultar Multas ===
    private JPanel crearPanelConsultar() {
        JPanel panel = new JPanel(new BorderLayout());

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

        JPanel arriba = new JPanel();
        JComboBox<String> cbFiltro = new JComboBox<>(new String[]{"Placa", "Cédula", "Código"});
        JTextField txtValor = new JTextField(10);
        JButton btnBuscar = new JButton("Buscar Vencidas");

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