import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class Frontend extends JFrame {

    private Backend backend; //atributo para conectar

    public Frontend(Backend backend) { //constructor
        super("Gestión de Multas de Tránsito"); // título ventana
        this.backend = backend; //guarda el objeto backend que maneja los datos

        setSize(750, 550); // tamaño de la ventana
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //cerrar al salir
        setLocationRelativeTo(null); // centrar en pantalla

        // Pestañas principales de la interfaz
        JTabbedPane tabs = new JTabbedPane(); //crea pestañas para organizar diferentes funcionalidades
        tabs.setBackground(new Color(245, 245, 245));
        tabs.setForeground(new Color(40, 40, 40));

        // cada pestaña corresponde a una funcionalidad creada en JPanel
        tabs.add("Agregar Multa", crearPanelAgregarMulta());
        tabs.add("Registrar Pago", crearPanelRegistrarPago());
        tabs.add("Consultar Multas", crearPanelConsultar());
        tabs.add("Multas Vencidas", crearPanelVencidas());

        add(tabs);
        setVisible(true);
    }

    // === Panel para registrar una nueva multa ===
    private JPanel crearPanelAgregarMulta() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10)); //creacion con parametros
        panel.setBackground(new Color(250, 250, 250));

        // campos de entrada
        JTextField txtCodigo = new JTextField();
        JTextField txtPlaca = new JTextField();
        JTextField txtCedula = new JTextField();
        JTextField txtNombre = new JTextField();
        JComboBox<String> cbTipo = new JComboBox<>(new String[]{
                "Exceso de velocidad", "Mal parqueo", "Semáforo en rojo", "Documentos vencidos"
        });
        JTextField txtFecha = new JTextField();
        JTextField txtMonto = new JTextField();

        // botón de acción
        JButton btnAgregar = new JButton("Registrar Multa");
        btnAgregar.setBackground(new Color(66, 135, 245));
        btnAgregar.setForeground(Color.WHITE);

        // agregar etiquetas y campos
        panel.add(new JLabel("Código (5 caracteres):")); panel.add(txtCodigo);
        panel.add(new JLabel("Placa (ABC123):")); panel.add(txtPlaca);
        panel.add(new JLabel("Cédula (10 dígitos):")); panel.add(txtCedula);
        panel.add(new JLabel("Nombre:")); panel.add(txtNombre);
        panel.add(new JLabel("Tipo:")); panel.add(cbTipo);
        panel.add(new JLabel("Fecha (YYYY-MM-DD):")); panel.add(txtFecha);
        panel.add(new JLabel("Monto:")); panel.add(txtMonto);
        panel.add(new JLabel("")); panel.add(btnAgregar);

        // acción del botón
        btnAgregar.addActionListener(e -> {
            try {
                // validación código
                String codigo = txtCodigo.getText().trim();
                if (codigo.length() != 5) {
                    JOptionPane.showMessageDialog(this, "❌ Código inválido.");
                    return;
                }

                // validación placa
                String placa = txtPlaca.getText().trim().toUpperCase();
                if (!placa.matches("^[A-Z]{3}[0-9]{3}$")) {
                    JOptionPane.showMessageDialog(this, "❌ Placa inválida.");
                    return;
                }

                // validación cédula
                String cedula = txtCedula.getText().trim();
                if (!cedula.matches("^[0-9]{10}$")) {
                    JOptionPane.showMessageDialog(this, "❌ Cédula inválida.");
                    return;
                }

                // validación nombre
                String nombre = txtNombre.getText().trim();
                if (!nombre.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$")) {
                    JOptionPane.showMessageDialog(this, "❌ Nombre inválido.");
                    return;
                }

                // tipo infracción
                String tipo = cbTipo.getSelectedItem().toString();

                // validación fecha
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

                // validación monto
                double monto = Double.parseDouble(txtMonto.getText().trim());
                if (monto <= 0) {
                    JOptionPane.showMessageDialog(this, "❌ Monto inválido.");
                    return;
                }

                // mandar al backend
                String resultado = backend.agregarMulta(codigo, placa, cedula, nombre, tipo, fecha.toString(), monto);
                JOptionPane.showMessageDialog(this, resultado);

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "❌ Monto inválido.");
            }
        });

        return panel;
    }

    // === Panel para registrar un pago ===
    private JPanel crearPanelRegistrarPago() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.setBackground(new Color(250, 250, 250));

        JTextField txtCodigo = new JTextField();
        JTextField txtFecha = new JTextField();
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
                // validar código
                String codigo = txtCodigo.getText().trim();
                if (codigo.length() != 5) {
                    JOptionPane.showMessageDialog(this, "❌ Código inválido.");
                    return;
                }

                // validar fecha
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

                // validar monto
                double monto = Double.parseDouble(txtMonto.getText().trim());
                if (monto <= 0) {
                    JOptionPane.showMessageDialog(this, "❌ Monto inválido.");
                    return;
                }

                // llamar backend
                Backend.PagoResultado resultado = backend.registrarPago(codigo, fecha.toString(), monto);
                JOptionPane.showMessageDialog(this, resultado.mensaje + " | Cuotas restantes: " + resultado.cuotasRestantes);

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "❌ Monto inválido.");
            }
        });

        return panel;
    }

    // === Panel para consultar multas ===
    private JPanel crearPanelConsultar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(250, 250, 250));

        // barra de búsqueda
        JPanel arriba = new JPanel();
        JComboBox<String> cbFiltro = new JComboBox<>(new String[]{"Placa", "Cédula", "Código"});
        JTextField txtValor = new JTextField(10);
        JButton btnBuscar = new JButton("Consultar");

        arriba.add(new JLabel("Buscar por:"));
        arriba.add(cbFiltro);
        arriba.add(txtValor);
        arriba.add(btnBuscar);

        // resultados
        JTextArea areaResultados = new JTextArea();
        areaResultados.setEditable(false);
        JScrollPane scroll = new JScrollPane(areaResultados);

        // acción botón
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

    // === Panel para consultar multas vencidas ===
    private JPanel crearPanelVencidas() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(250, 250, 250));

        // barra de búsqueda
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

        // resultados
        JTextArea areaResultados = new JTextArea();
        areaResultados.setEditable(false);
        JScrollPane scroll = new JScrollPane(areaResultados);

        // acción botón
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