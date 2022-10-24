import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server2 {
    public static void main(String[] args) {
        int serverIndex = 0;
        try {
            ServerSocket sk=new ServerSocket(27001);

            System.out.println("Server is connecting....");
            boolean listening=true;
            while(listening){
                serverIndex++;
                new threadServer(sk.accept()).start();
                System.out.println("Server " + serverIndex + " is connect");

            }
        } catch (IOException e) {
            System.out.print("Ket noi hong");
        }
    }
}
