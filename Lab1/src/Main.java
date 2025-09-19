public class Main {
    public static void main(String[] args) throws Exception {
        // Se crea el backend (manejo de archivos, lógica de negocio)
        Backend backend = new Backend();

        // Se lanza la interfaz gráfica (frontend) pasando el backend
        new Frontend(backend);
    }
}