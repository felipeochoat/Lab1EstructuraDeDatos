package gestionMultas;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Multa {
    private String CodigoMulta;     // ej: M0001
    private String Placa;
    private String CedulaProp;
    private String NombreProp;
    private String TipoInfraccion;
    private LocalDate FechaMulta;
    private double MontoPendiente;
    private String Estado; // "Pendiente" o "Pagada"

    private static final DateTimeFormatter DF = DateTimeFormatter.ISO_LOCAL_DATE;

    // Constructor completo
    public Multa(String CodigoMulta, String Placa, String CedulaProp, String NombreProp,
                 String TipoInfraccion, LocalDate FechaMulta, double MontoPendiente, String Estado) {
        this.CodigoMulta = CodigoMulta;
        this.Placa = Placa;
        this.CedulaProp = CedulaProp;
        this.NombreProp = NombreProp;
        this.TipoInfraccion = TipoInfraccion;
        this.FechaMulta = FechaMulta;
        this.MontoPendiente = MontoPendiente;
        this.Estado = Estado;
    }

    // Constructor desde CSV
    public Multa(String lineaCSV) throws Exception {
        String[] parts = lineaCSV.split(",");
        if (parts.length < 8) throw new Exception("Campos insuficientes en Multa");
        this.CodigoMulta = parts[0].trim();
        this.Placa = parts[1].trim();
        this.CedulaProp = parts[2].trim();
        this.NombreProp = parts[3].trim();
        this.TipoInfraccion = parts[4].trim();
        this.FechaMulta = LocalDate.parse(parts[5].trim(), DF);
        this.MontoPendiente = Double.parseDouble(parts[6].trim());
        this.Estado = parts[7].trim();
    }

    public String pasarACSV() {
        return String.format("%s,%s,%s,%s,%s,%s,%.2f,%s",
                CodigoMulta, Placa, CedulaProp, NombreProp,
                TipoInfraccion, FechaMulta.format(DF), MontoPendiente, Estado);
    }

    public boolean estaVencida() {
        if ("Pagada".equalsIgnoreCase(Estado)) return false;
        return LocalDate.now().isAfter(FechaMulta.plusDays(90));
    }

    // Getters y Setters
    public String getCodigoMulta() { return CodigoMulta; }
    public String getPlaca() { return Placa; }
    public String getCedulaProp() { return CedulaProp; }
    public String getNombreProp() { return NombreProp; }
    public String getTipoInfraccion() { return TipoInfraccion; }
    public LocalDate getFechaMulta() { return FechaMulta; }
    public double getMontoPendiente() { return MontoPendiente; }
    public void setMontoPendiente(double monto) { this.MontoPendiente = monto; }
    public String getEstado() { return Estado; }
    public void setEstado(String estado) { this.Estado = estado; }

    @Override
    public String toString() {
        return String.format("Multa[%s] - Placa: %s, Cédula: %s, Prop: %s, Infracción: %s, Fecha: %s, Pendiente: $%.2f, Estado: %s",
                CodigoMulta, Placa, CedulaProp, NombreProp,
                TipoInfraccion, FechaMulta.format(DF), MontoPendiente, Estado);
    }
}