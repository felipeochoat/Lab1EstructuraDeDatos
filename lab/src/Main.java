/**
 *
 * @author Alejandro Amaya, Eduardo Ibarra y Felipe Ochoa
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;


public class Main {


    // Método para añadir registro a archivo multa - Punto 2
    public static void LlenarMultasRegistradas(Scanner sc, String file_name){
        String Placa, NombreProp, TipoInfraccion, Estado;
        int CodigoMulta, CedulaProp, FechaMulta;
        double MontoPendiente;
       
        try {
            FileWriter outFile = new FileWriter("data/"+file_name + ".txt", false);  //Archivo.txt

            // if false the file will be deleted and created everytime
            // if true the registers will be appended to the end of the file

            PrintWriter registro_multasr = new PrintWriter(outFile);
            
            // LOGICA
            String hay_Mas;
            System.out.println("¿Quiere registrar una multa nueva? si - no");
            hay_Mas = sc.nextLine();
            while(hay_Mas.equalsIgnoreCase("si")){
                System.out.println("Código de la multa:");
                CodigoMulta = sc.nextInt();

                System.out.println("Placa vehículo");
                Placa = sc.nextLine();

                System.out.println("Cédula propietario");
                CedulaProp = sc.nextInt();

                System.out.println("Nombre del propietario");
                NombreProp = sc.nextLine();

                System.out.println("Tipo infracción");
                TipoInfraccion = sc.nextLine();

                System.out.println("Fecha multa");
                FechaMulta = sc.nextInt();
                
                System.out.println("Monto pendiente");
                MontoPendiente = sc.nextDouble();

                System.out.println("Estado");
                Estado = sc.nextLine();

                //Validaciones de los campos
                while(MontoPendiente < 0){
                    System.out.println("El monto pendiente debe ser positivo");
                    MontoPendiente = sc.nextDouble();
                }

                if (!Placa.isEmpty() && !NombreProp.isEmpty() && !TipoInfraccion.isEmpty() && !Estado.isEmpty()){
                    registro_multas.println(CodigoMulta +","+ Placa +","+ CedulaProp +","+ NombreProp +","+ TipoInfraccion +","+ FechaMulta +","+ MontoPendiente +","+ Estado);
                }

                System.out.println("¿Quiere registrar otra multa nueva? si - no");
                hay_Mas = sc.nextLine();  
            }
           registro_multas.close();

        } catch (IOException ex) {
            System.out.println("Error creando el archivo");
            ex.printStackTrace();
        }
    }

    // Método para añadir un nuevo pago a una multa - Punto 2
    public static void LlenarPagosMultas(Scanner sc, String file_name){
        String Placa;
        int FechaPago;
        double MontoPagado;
       
        try {
            FileWriter outFile = new FileWriter(file_name + ".txt", false);  //Archivo.txt

            // if false the file will be deleted and created everytime
            // if true the registers will be appended to the end of the file

            PrintWriter registro_pago = new PrintWriter(outFile);
            
            // LOGICA
            String hay_Mas;
            System.out.println("¿Quiere registrar un pago nuevo? si - no");
            hay_Mas = sc.nextLine();
            while(hay_Mas.equalsIgnoreCase("si")){

                System.out.println("Placa vehículo");
                Placa = sc.nextLine();

                System.out.println("Fecha pago");
                FechaPago = sc.nextInt();
                
                System.out.println("Monto pagado");
                MontoPagado = sc.nextDouble();

                //Validaciones de los campos
                //while(Double.parseDouble(MontoPendiente) < 0){
                    //System.out.println("El monto pendiente debe ser positivo");
                    //MontoPendiente = sc.nextDouble();
                //}

                //if (!Placa.isEmpty() && !NombreProp.isEmpty() && !TipoInfraccion.isEmpty() && !Estado.isEmpty() && !CodigoMulta.isEmpty() && !CedulaProp.isEmpty() && !FechaMulta.isEmpty() && !MontoPendiente.isEmpty()){
                    //registro_multas.println(campo1 +"\t"+ campo2 +"\t"+ campo3);
                //}

                System.out.println("¿Quiere registrar otra nuevo pago? si - no");
                hay_Mas = sc.nextLine();  
            }
           registro_pago.close();

        } catch (IOException ex) {
            System.out.println("Error creando el archivo");
            ex.printStackTrace();
        }
    }


    // Clase main
    public static void main(String[] args) throws Exception {

        Scanner sc = new Scanner (System.in);

        System.out.println("Digite el nombre del archivo al que quiere añadir un registro: ");
        String file_name = sc.nextLine(); // Archivo1 - MultasRegistradas

        if (file_name == "Multas_Registradas"){
            LlenarMultasRegistradas(sc, file_name);
        } else if (file_name == "Pagos_Multas"){
            LlenarPagosMultas(sc, file_name);
        }
        
        //System.out.println("Llenar informacion de transacciones");
        //Llenar(sc, "Transacciones");

        //System.out.println("¿Qué archivo desea abrir?");
        //String file_name = sc.nextLine();
        //Leer(sc, file_name);
        //Update_stuff("Clientes",  "Transacciones");
        //sc.close();
    }
}
