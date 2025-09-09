package gestionMultas;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Pago {
    private String codigoMulta;
    private LocalDate fechaPago;
    private double montoPagado;

    private static final DateTimeFormatter DF = DateTimeFormatter.ISO_LOCAL_DATE;

    public Pago(String codigoMulta, LocalDate fechaPago, double montoPagado) {
        this.codigoMulta = codigoMulta;
        this.fechaPago = fechaPago;
        this.montoPagado = montoPagado;
    }

    // Constructor desde CSV
    public Pago(String lineaCSV) throws Exception {
        String[] p = lineaCSV.split(",");
        if (p.length < 3) throw new Exception("Registro de pago con campos insuficientes");
        this.codigoMulta = p[0].trim();
        this.fechaPago = LocalDate.parse(p[1].trim(), DF);
        this.montoPagado = Double.parseDouble(p[2].trim());
    }

    public String pasarACSV() {
        return String.format("%s,%s,%.2f", codigoMulta, fechaPago.format(DF), montoPagado);
    }

    public String getCodigoMulta() { return codigoMulta; }
    public LocalDate getFechaPago() { return fechaPago; }
    public double getMontoPagado() { return montoPagado; }

    @Override
    public String toString() {
        return String.format("Pago - Multa: %s, Fecha: %s, Monto: %.2f",
                codigoMulta, fechaPago.format(DF), montoPagado);
    }
}
