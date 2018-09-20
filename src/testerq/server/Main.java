package testerq.server;

public class Main {
    static boolean running = true;
    public static void main(String[] args) {
        int portNumber = Integer.parseInt(args[0]);
        
        NetworkServer.getInstance();
        NetworkServer.Listen(portNumber);
        
        while(running) {
            //update server state
        }
    }
    
    public static void setRunning(boolean running) {
        Main.running = running;
    }
    
}
