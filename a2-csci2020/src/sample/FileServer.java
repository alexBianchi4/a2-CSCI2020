package sample;

import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileServer {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private ArrayList<ClientHandler> clients = new ArrayList<>();
    private ExecutorService pool = Executors.newFixedThreadPool(10);
    public static File serverDir;

    public FileServer() throws Exception{
        serverSocket = new ServerSocket(8080);
        serverDir = new File(System.getProperty("user.dir")+"\\sharedFolder");
        if(!serverDir.exists()){ // creates the shared folder if it wasn't there already
            serverDir.mkdir();
        }
        while(true){
            System.out.println("[SERVER] waiting for client connection...");
            clientSocket = serverSocket.accept();
            System.out.println("[SERVER] connected to client!");
            ClientHandler thread = new ClientHandler(clientSocket);
            clients.add(thread);
            pool.execute(thread);
        }
    }

    public static File getServerDir(){
        return serverDir;
    }

    public static void main(String[] args) throws Exception{
        FileServer server = new FileServer();
    }
}
