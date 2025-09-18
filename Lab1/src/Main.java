public class Main {
    public static void main(String[] args) throws Exception {
        Backend backend = new Backend();
        new Frontend(backend);
    }
}