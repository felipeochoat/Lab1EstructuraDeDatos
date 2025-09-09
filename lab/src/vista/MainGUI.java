package vista;


import javax.swing.*;


import gestionMultas.Multa;
import gestionMultas.Pago;
import gestionMultas.ManejadorArchivos;


import java.awt.*;
import java.time.LocalDate;

public class MainGUI extends JFrame {
    private ManejadorArchivos fm = new ManejadorArchivos();

    public MainGUI() {
        super("Gestión de Multas - Laboratorio 1");
        fm.loadAll();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 300);
        setLayout(new GridLayout(0, 1, 5, 5));

        JButton btnConsultar = new JButton("Consultar por placa/cedula");
        JButton btnAgregar = new JButton("Agregar nueva multa");
        JButton btnPago = new JButton("Registrar pago");
        JButton btnActualizar = new JButton("Actualizar estados");
        JButton btnVencidas = new JButton("Ver multas vencidas");
        JButton btnEliminar = new JButton("Eliminar multas pagadas");

        add(btnConsultar);
        add(btnAgregar);
        add(btnPago);
        add(btnActualizar);
        add(btnVencidas);
        add(btnEliminar);

        // Consultar
        btnConsultar.addActionListener(e -> {
            String tipo = JOptionPane.showInputDialog(this, "Buscar por: 1) placa  2) cédula (ingrese 1 o 2)");
            if ("1".equals(tipo)) {
                String placa = JOptionPane.showInputDialog(this, "Placa:");
                var res = fm.buscarPorPlaca(placa);
                JOptionPane.showMessageDialog(this,
                        res.isEmpty() ? "No hay multas" :
                                res.stream().map(Multa::toString).reduce("", (a, b) -> a + "\n" + b));
            } else if ("2".equals(tipo)) {
                String ced = JOptionPane.showInputDialog(this, "Cédula:");
                var res = fm.buscarPorCedula(ced);
                JOptionPane.showMessageDialog(this,
                        res.isEmpty() ? "No hay multas" :
                                res.stream().map(Multa::toString).reduce("", (a, b) -> a + "\n" + b));
            }
        });

        // Agregar multa
        btnAgregar.addActionListener(e -> {
            try {
                String codigo = JOptionPane.showInputDialog(this, "Código multa (ej M0001):");
                String placa = JOptionPane.showInputDialog(this, "Placa:");
                String cedula = JOptionPane.showInputDialog(this, "Cédula:");
                String nombre = JOptionPane.showInputDialog(this, "Nombre propietario:");
                String tipoInfr = JOptionPane.showInputDialog(this, "Tipo infracción:");
                String fechaStr = JOptionPane.showInputDialog(this, "Fecha (YYYY-MM-DD):");
                String montoStr = JOptionPane.showInputDialog(this, "Monto total:");

                if (codigo == null || placa == null || cedula == null) {
                    JOptionPane.showMessageDialog(this, "Campos obligatorios");
                    return;
                }

                Multa m = new Multa(codigo, placa, cedula, nombre, tipoInfr,
                        LocalDate.parse(fechaStr),
                        Double.parseDouble(montoStr),
                        "Pendiente");

                if (fm.findMultaByCodigo(codigo).isPresent()) {
                    JOptionPane.showMessageDialog(this, "Código ya existe");
                    return;
                }

                fm.getMultas().add(m);
                fm.saveMultas();
                JOptionPane.showMessageDialog(this, "Multa agregada correctamente");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        // Registrar pago
        btnPago.addActionListener(e -> {
            try {
                String codigo = JOptionPane.showInputDialog(this, "Código multa a pagar:");
                String fecha = JOptionPane.showInputDialog(this, "Fecha pago (YYYY-MM-DD):");
                String monto = JOptionPane.showInputDialog(this, "Monto pagado:");

                Pago p = new Pago(codigo, LocalDate.parse(fecha), Double.parseDouble(monto));

                String res = fm.registrarPago(p);
                JOptionPane.showMessageDialog(this, res);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        // Actualizar estados
        btnActualizar.addActionListener(e -> {
            fm.actualizarEstadosSegunPagos();
            JOptionPane.showMessageDialog(this, "Estados actualizados");
        });

        // Ver vencidas
        btnVencidas.addActionListener(e -> {
            var lista = fm.getMultas().stream().filter(Multa::estaVencida).toList();
            JOptionPane.showMessageDialog(this,
                    lista.isEmpty() ? "No hay vencidas" :
                            lista.stream().map(Multa::toString).reduce("", (a, b) -> a + "\n" + b));
        });

        // Eliminar pagadas
        btnEliminar.addActionListener(e -> {
            long antes = fm.getMultas().size();
            fm.getMultas().removeIf(m -> "Pagada".equalsIgnoreCase(m.getEstado()));
            fm.saveMultas();
            long eliminadas = antes - fm.getMultas().size();
            JOptionPane.showMessageDialog(this, "Multas eliminadas: " + eliminadas);
        });

        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainGUI::new);
    }
}