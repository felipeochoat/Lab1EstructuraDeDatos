package gestionMultas;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Pago {

    private String Placa;
    private LocalDate FechaPago;
    private double MontoPagado;

    //Constructor
    public Pago(String Placa, LocalDate FechaPago, double MontoPagado) {
        this.Placa = Placa;
        this.FechaPago = FechaPago;
        this.MontoPagado = MontoPagado;
    }

    //Constructor a linea en CSV
    public Pago(String lineaCSV) {
        String[] parts = lineaCSV.split(",");
        if (parts.length >= 3) {
            this.Placa = parts[0].trim();
            this.FechaPago = LocalDate.parse(parts[1].trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            this.MontoPagado = Double.parseDouble(parts[2].trim());
        }
    }

    //Despues de validacion guardar 
    public String pasarACSV() {
        DateTimeFormatter formateador = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        return String.format("%s,%s,%.2f",
                Placa, FechaPago.format(formateador), MontoPagado);
    }

    // Getters and Setters
    public String getPlaca() { return Placa; }
    public void setPlaca(String Placa) { this.Placa = Placa; }
    
    public LocalDate getFechaPago() { return FechaPago; }
    public void setFechaPago(LocalDate FechaPago) { this.FechaPago = FechaPago; }
    
    public double getMontoPagado() { return MontoPagado; }
    public void setMontoPagado(double MontoPagado) { this.MontoPagado = MontoPagado; }
    
    @Override
    public String toString() {
        return String.format("Pago - Placa: %s, Fecha: %s, Monto: $%.2f", 
                Placa, FechaPago, MontoPagado);
    }
}
