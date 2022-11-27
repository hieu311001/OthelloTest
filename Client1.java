import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.Scanner;


public class Client1 {

    public static String ID;
    public static String IP = "localhost";
    public static final int PORT = 27001;
    public static final String MSV = "19020292";
    public static final String myPoint = "BLACK";
    public static int map[][] =
            {{0,0,0,0,0,0,0,0},
             {0,0,0,0,0,0,0,0},
             {0,0,0,0,0,0,0,0},
             {0,0,0,1,2,0,0,0},
             {0,0,0,2,1,0,0,0},
             {0,0,0,0,0,0,0,0},
             {0,0,0,0,0,0,0,0},
             {0,0,0,0,0,0,0,0}};

    // Chuyển int thành byte
    public static byte[] convert_data(int data) {
        byte[] b = new byte[4];
        b[0] = (byte) data;
        b[1] = (byte) ((data >> 8) & 0xFF);
        b[2] = (byte) ((data >> 16) & 0xFF);
        b[3] = (byte) ((data >> 24) & 0xFF);
        return b;
    }

    // Chuyển byte thành int
    public static int restore(byte[] bytes) {
        return ((bytes[3] & 0xFF) << 24) |
                ((bytes[2] & 0xFF) << 16) |
                ((bytes[1] & 0xFF) << 8) |
                ((bytes[0] & 0xFF) << 0);
    }

    // Khởi tạo gói tin gửi đi
    public static byte[] set_pkt(int type, int len, byte[] data) {
        byte[] out = new byte[8 + len];
        byte[] type_ = convert_data(type);
        byte[] len_ = convert_data(len);
        for (int i = 0; i < 4; i++) out[i] = type_[i];
        for (int i = 4; i < 8; i++) out[i] = len_[i - 4];
        for (int i = 8; i < (8 + len); i++) out[i] = data[i - 8];

        return out;
    }

    public static void main(String[] args) {
        // Khởi tạo bộ đọc đầu vào từ bàn phím
        Scanner myObj = new Scanner(System.in);
        byte[] input = new byte[4];

        int type = 0;
        int len = 0;
        int result;
        Socket skt = null;
        try {
            System.out.println("Client is Connecting....");
            // Lấy ip của máy tĩnh
            IP = InetAddress.getLocalHost().getHostAddress();

            skt = new Socket(IP, PORT);
            System.out.println("Client is Connect");
            InputStream is = skt.getInputStream();
            OutputStream os = skt.getOutputStream();
            byte[] barr = MSV.getBytes();
            os.write(set_pkt(0, barr.length, barr));

            while (true) {

                is.read(input);
                type = restore(input);
                is.read(input);
                len = restore(input);

                if (type == 1) {
                    int x = myObj.nextInt();
                    int y = myObj.nextInt();

                    System.out.println(x + " và " + y);
                    if (myPoint == "BLACK") {
                        result = (x*10+y);
                    } else result = (x*10+y)*(-1);

                    os.write(set_pkt(2, 4,convert_data((int)(result)) ));
                } else if (type == 3) {
                    System.out.print("Kết quả gửi đi không đúng!");
                    break;
                } else if (type == 4) {
                    byte[] flag = new byte[len];
                    is.read(flag);
                    System.out.println("Cờ nhận được là: ");
                    for (byte i : flag) {
                        System.out.print((char) i);
                    }
                    break;
                }
            }
        } catch (IOException e) {
            System.out.print("Kết nối hỏng");
        }
    }
}