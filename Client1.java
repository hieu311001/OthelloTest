import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;


public class Client1 {

    public static int ID;
    public static int myID;
    public static int lenMap;
    public static String IP = "localhost";
    public static final int PORT = 27001;
    public static final String MSV = "19020292";
    public static final String myPoint = "BLACK";
    public static int blackScore = 0;
    public static int whiteScore = 0;
    public static int[] point = new int[64];
    public static int map[][] =
            {{0,0,0,0,0,0,0,0},
             {0,0,0,0,0,0,0,0},
             {0,0,0,0,0,0,0,0},
             {0,0,0,1,2,0,0,0},
             {0,0,0,2,1,0,0,0},
             {0,0,0,0,0,0,0,0},
             {0,0,0,0,0,0,0,0},
             {0,0,0,0,0,0,0,0}};

    // Chuyển nước đi thành tọa độ
    // VD: 45 -> ô map[3][4]
    public static int[] getPoint(int point) {
        int[] arr = {0, 0};
        point = Math.abs(point);
        arr[0] = point / 10;
        arr[1] = point % 10;
        return arr;
    }

    // Lấy tọa độ các ô đã đánh khi nhận được thông tin tử server
    public static void printPoint(int[] point) {
        for (int i = 0; i < lenMap; i++) {
            int[] arr = getPoint(point[i]);

            if (point[i] > 0){
                map[arr[0]-1][arr[1]-1] = 1;
            } else {
                map[arr[0]-1][arr[1]-1] = 2;
            }
        }
    }

    // Print Map
    public static void printMap(int[][] map){
        printPoint(point);
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                System.out.print(map[i][j] + " ");
            }
            System.out.println("");
        }
        System.out.println("-------------------------------------");
    }

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

    // Mã hóa gói tin gửi nước đi (type = 4)
    public static byte[] pkt_turn(int id, int x, int y){
        byte[] out = new byte[8];

        for (int i = 0; i < 4; i++){
            out[i] = convert_data(id)[i];
        }
        for (int i = 4; i < 8; i++) {
            out[i] = convert_data(x*10+y)[i-4];
        }

        return out;
    }

    // Giải mã gói tin type = 3
    public static void restore_pkt(byte[] data) {
        lenMap = (data.length - 12)/4;
        byte[] black = Arrays.copyOfRange(data, 0, 4);  blackScore = restore(black);
        byte[] white = Arrays.copyOfRange(data, 4, 8);  whiteScore = restore(white);
        byte[] id = Arrays.copyOfRange(data, 8, 12);    ID = restore(id);
        for (int i = 0; i < (data.length - 12)/4; i++) {
            byte[] x = Arrays.copyOfRange(data, 12+i*4, 12+(i+1)*4);
            point[i] = restore(x);
        }
    }



    public static void main(String[] args) {
        // Khởi tạo bộ đọc đầu vào từ bàn phím
        Scanner myObj = new Scanner(System.in);

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
                byte[] input = new byte[4];

                is.read(input);
                type = restore(input);
                is.read(input);
                len = restore(input);

                if (type == 1) {
                    myID  = 12345;
                    is.read(input);
                    int req = restore(input);
                    if (req == 1) {
                        os.write(set_pkt(2, 4, convert_data(myID)));
                    }
                }
                else if (type == 3) {
                    // Lấy phần data còn lại sau khi lấy ra type và len
                    byte[] out = new byte[len];
                    is.read(out);
                    // Giải mã để lấy các thông tin cần thiết
                    restore_pkt(out);
                    // Vẽ map sau khi nhận được thông tin
                    printMap(map);

                    if(ID == myID) {
                        System.out.println("Nhập tọa độ x: ");
                        int x = myObj.nextInt();
                        System.out.println("Nhập tọa độ y: ");
                        int y = myObj.nextInt();
                        // Gửi gói tin chứa thông tin nước đi
                        os.write(set_pkt(4, 8, pkt_turn(myID, x, y)));
                    } else {
                        System.out.println("Lượt đối thủ");
                    }
                }
                else if (type == 5) {
                    System.out.println("Nước đi của bạn không họp lệ!");

                    // Lấy phần data còn lại sau khi lấy ra type và len
                    byte[] out = new byte[len];
                    is.read(out);
                    // Giải mã để lấy các thông tin cần thiết
                    restore_pkt(out);
                    // Vẽ map sau khi nhận được thông tin
                    printMap(map);

                    if(ID == myID) {
                        System.out.println("Nhập tọa độ x: ");
                        int x = myObj.nextInt();
                        System.out.println("Nhập tọa độ y: ");
                        int y = myObj.nextInt();
                        // Gửi gói tin chứa thông tin nước đi
                        os.write(set_pkt(4, 8, pkt_turn(myID, x, y)));
                    }
                }
                else if (type == 6) {
                    is.read(input);  len = restore(input);
                    is.read(input); int id = restore(input);

                    if(id == myID) {
                        System.out.println("Bạn đã giành chiến thắng!");
                    } else {
                        System.out.println("Bạn đã thua!");
                    }
                }
            }
        } catch (IOException e) {
            System.out.print("Kết nối hỏng");
        }
    }
}