import java.io.*;
import java.net.*;
import java.util.*;
public class threadServer extends Thread {
    Socket socket = null;

    public threadServer(Socket socket) {
        this.socket = socket;
    }

    public static String turn = "BLACK";
    public static int map[][] =
            {{0,0,0,0,0,0,0,0},
                    {0,0,0,0,0,0,0,0},
                    {0,0,0,0,0,0,0,0},
                    {0,0,0,1,2,0,0,0},
                    {0,0,0,2,1,0,0,0},
                    {0,0,0,0,0,0,0,0},
                    {0,0,0,0,0,0,0,0},
                    {0,0,0,0,0,0,0,0}};

    // Lấy tọa độ các ô
    public static List<Integer> coordinates(int map[][]) {
        List<Integer> a = new ArrayList<Integer>();
        for (int i = 1; i <= map.length; i++) {
            for (int j = 1; j <= map[i-1].length; j++) {
                if (map[i-1][j-1] != 0)
                {
                    a.add(i*10+j);
                }
            }
        }
        return a;
    }

    // Lấy tọa dộ nước đi
    public static int[] next(byte[] bytes) {
        int a = restoreInt(bytes);
        if (a < 0) {
            a = a*-1;
            turn = "WHITE";
        } else {
            turn = "BLACK";
        }
        int b[] = {0, 0};
        b[0] = a/10;
        b[1] = a%10;
        return b;
    }

    // Thực hiện bước đi
    public static void getMap(int[] a) {
        int x = a[0] - 1;
        int y = a[1] - 1;
        if (map[x][y] == 0 && turn == "BLACK") {
            map[x][y] = 1;
        } else if (map[x][y] == 0 && turn == "WHITE") {
            map[x][y] = 2;
        }
        else {
            System.out.println("Nước đi thất bại");
        }
    }

//    public static byte[] convert_map(int[][] map) {
//        byte[] bytes = {};
//        for (int i = 0; i < map.length; i++) {
//            for (int j = 0; j < map[i].length; j++) {
//                if (map[i][j] != 0) {
//                    bytes += convert_data((i+1)*10+j+1);
//                }
//            }
//        }
//        return bytes;
//    }

    // Print Map
    public static void printMap(int[][] map){
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                System.out.print(map[i][j] + " ");
            }
            System.out.println("");
        }
    }

    // Chuyển int thành byte
    public static byte[] convert_data(int data)
    {
        byte[] b = new byte[4];
        b[0] = (byte)data;
        b[1] = (byte)((data >> 8) & 0xFF);
        b[2] = (byte)((data >> 16) & 0xFF);
        b[3] = (byte)((data >> 24) & 0xFF);
        return b;
    }

    // Chuyển byte thành int
    public static int restoreInt(byte[] bytes) {
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
        for (int i = 0; i < 4; i++)         out[i] = type_[i];
        for (int i = 4; i < 8; i++)         out[i] = len_[i - 4];
        for (int i = 8; i < (8 + len); i++) out[i] = data[i - 8];

        return out;
    }

    public void run() {
        byte[] input = new byte[4];

        int type = 0;
        int len = 0;
        try {

            InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();

            String ok = "OK";
            os.write(ok.getBytes());

            while (true) {
                is.read(input);
                type = restoreInt(input);
                is.read(input);
                len = restoreInt(input);

                if (type == 0) {
                    int id = 12345;
                    os.write(set_pkt(1, 4, convert_data((int) (id))));
                }
                else if (type == 2) {
                    is.read(input);
                    //int next = restoreInt(input);
                    getMap(next(input));
                    //System.out.println(next);
                    printMap(map);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}