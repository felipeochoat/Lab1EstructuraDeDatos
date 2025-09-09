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

    //Constructor
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

    //Constructor a linea en CSV
    public Multa(String lineaCSV) {
        String[] parts = lineaCSV.split(",");
        if (parts.length >= 8) {
            this.CodigoMulta = parts[0].trim();
            this.Placa = parts[1].trim();
            this.CedulaProp = parts[2].trim();
            this.NombreProp = parts[3].trim();
            this.TipoInfraccion = parts[4].trim();
            this.FechaMulta = LocalDate.parse(parts[5].trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            this.MontoPendiente = Double.parseDouble(parts[6].trim());
            this.Estado = parts[7].trim();
        }
    }

    //Despues de validacion guardar 
    public String pasarACSV() {
        DateTimeFormatter formateador = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        return String.format("%s,%s,%s,%s,%s,%s,%.2f,%s",
                CodigoMulta, Placa, CedulaProp, NombreProp, 
                TipoInfraccion, FechaMulta.format(formateador), MontoPendiente, Estado);
    }

    //Chequeo si pasaron más de 90 días desde la fecha de emisión de la multa
    public boolean estaVencida() {
        if ("Pagada".equals(Estado)) {
            return false;
        }
        return LocalDate.now().isAfter(FechaMulta.plusDays(90));
    }


    // Getters and Setters
    public String getCodigoMulta() { return CodigoMulta; }
    public void setCodigoMulta(String CodigoMulta) { this.CodigoMulta = CodigoMulta; }
    
    public String getPlaca() { return Placa; }
    public void setPlaca(String Placa) { this.Placa = Placa; }
    
    public String getCedulaProp() { return CedulaProp; }
    public void setCedulaProp(String CedulaProp) { this.CedulaProp = CedulaProp; }
    
    public String getNombreProp() { return NombreProp; }
    public void setNombreProp(String NombreProp) { this.NombreProp = NombreProp; }
    
    public String getTipoInfraccion() { return TipoInfraccion; }
    public void setTipoInfraccion(String TipoInfraccion) { this.TipoInfraccion = TipoInfraccion; }
    
    public LocalDate getFechaMulta() { return FechaMulta; }
    public void setFechaMulta(LocalDate FechaMulta) { this.FechaMulta = FechaMulta; }
    
    public double getMontoPendiente() { return MontoPendiente; }
    public void setMontoPendiente(double MontoPendiente) { this.MontoPendiente = MontoPendiente; }
    
    public String getEstado() { return Estado; }
    public void setEstado(String Estado) { this.Estado = Estado; }
    
    @Override
    public String toString() {
        DateTimeFormatter formateador = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        return String.format("Multa[%s] - Placa: %s, Cédula: %s, Propietario: %s,  Infracción: %s, Fecha: %s, Monto: $%.2f, Estado: %s",
                CodigoMulta, Placa, CedulaProp, NombreProp, TipoInfraccion, FechaMulta.format(formateador), MontoPendiente, Estado);
    }

}
