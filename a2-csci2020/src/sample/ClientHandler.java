package sample;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable{

    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private File serverDir;

    public ClientHandler(Socket clientSocket) throws IOException {
        serverDir  = FileServer.getServerDir();
        this.client = clientSocket;
        in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        out = new PrintWriter(client.getOutputStream(),true);
    }

    public void run(){
        while(true){
            try{
                String command = in.readLine();
                if(command.equals("UPLOAD")){
                    upload();
                }else if(command.equals("DOWNLOAD")){
                    download();
                }else if(command.equals("DIR")){
                    getServerFiles();
                }
            }catch(IOException e){}
        }
    }

    public void upload(){
        try{
            String name = in.readLine();
            byte[] bytes = new byte[20000];
            InputStream fIn = client.getInputStream();
            FileOutputStream fOut = new FileOutputStream(serverDir.getAbsolutePath()+"\\"+name);
            fIn.read(bytes,0,bytes.length);
            fOut.write(bytes,0,bytes.length);

        }catch (IOException e){}
    }

    public void download() {
        try {
            String name = in.readLine();
            FileInputStream fileReader = new FileInputStream(serverDir+"\\"+name);
            byte[] bytes = new byte[20000];
            // convert the file into an array of bytes
            fileReader.read(bytes, 0, bytes.length);
            // put the bytes into a stream format and sent to client
            OutputStream os = client.getOutputStream();
            os.write(bytes, 0, bytes.length);
        }catch(FileNotFoundException e) {
        }catch (IOException e){}
    }

    public void getServerFiles(){
        File[] files = serverDir.listFiles();
        for(File f : files){
            out.println(f.getName());
        }
        out.println("quit");
    }

}
