import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server2 {
    public static void main(String[] args) {
        int serverIndex = 0;
        try {
            // Kết nối đến web server
//            Socket skt = new Socket("104.194.240.16", 8081);
//            InputStream is = skt.getInputStream();
//            OutputStream os = skt.getOutputStream();
            // Tạo server socket cho 2 client
            ServerSocket sk=new ServerSocket(27000);

            // Thông tin kết nối đến web server
//            String ip = "127.0.0.1";    byte[] ip_byte = ip.getBytes();
//            String name_game = "game";  byte[] game_byte = name_game.getBytes();
//            String info = "info";       byte[] info_byte = info.getBytes();
//            String author = "123";      byte[] auth = author.getBytes();

//            int length = 28 + ip_byte.length + game_byte.length + info_byte.length + auth.length;
//
//            byte[] a = threadServer.web_pkt(1,ip_byte.length, ip_byte, 4040, game_byte.length, game_byte, info_byte.length, info_byte, auth.length, auth);
//
//            os.write(a);

            System.out.println("Server is connecting....");
            boolean listening=true;
            while(listening){
                serverIndex++;
                new threadServer(sk.accept()).start();
                System.out.println("Server " + serverIndex + " is connect");
            }
        } catch (IOException e) {
            System.out.print(e);
        }
    }
}
