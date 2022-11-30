import java.io.*;
import java.lang.reflect.Array;
import java.net.*;
import java.util.*;
public class threadServer extends Thread {
    Socket socket = null;

    public threadServer(Socket socket) {
        this.socket = socket;
    }

    public static int blackScore = 0;
    public static int whiteScore = 0;
    public static int blackID = 12345;
    public static int whiteID = 12346;
    public static int nextID = 12346;
    public static List<ConnectionHandler> clients = new ArrayList<>();
    public static Object lock = new Object();
    public static List<Integer> point = new ArrayList<Integer>();
    public static String turn = "BLACK";
    public static int map[][] =
                    {{1,1,1,1,1,1,1,1},
                    {1,1,1,1,1,1,1,1},
                    {1,1,1,1,1,1,1,1},
                    {2,2,2,1,2,2,2,0},
                    {2,2,2,2,1,2,2,2},
                    {1,1,1,1,1,1,1,1},
                    {1,1,1,1,1,1,1,1},
                    {1,1,1,1,1,1,1,1}};

    // Mảng chứa vị trí các quân cờ đen
    private static List<Integer> blackPos=new ArrayList<Integer>();

    // Mảng chứa vị trí các quân cờ trắng
    private static List<Integer> whitePos=new ArrayList<Integer>();

    private static boolean canMove(int row, int col, int rowDir, int colDir, int opponent) {
        int currentRow = row + rowDir;
        int currentCol = col + colDir;

        // Nếu vượt ra phạm vi bàn cờ thì trả về
        if (currentRow==8 || currentRow<0 || currentCol==8 || currentCol<0)
        {
            return false;
        }

        // Nếu là quân cờ cùng màu hoặc không có quân cờ nào thì trả về
        if ((map[currentRow][currentCol] != opponent && map[currentRow][currentCol] != 0) || map[currentRow][currentCol] == 0) {
            return false;
        }

        while (map[currentRow][currentCol] == opponent) {
            currentRow = currentRow + rowDir;
            currentCol = currentCol + colDir;

            if (map[currentRow][currentCol] != opponent && map[currentRow][currentCol] != 0) {
                return true;
            }

            if (currentRow==8 || currentRow<0 || currentCol==8 || currentCol<0)
            {
                return false;
            }
        }

        return false;
    }

    // Kiểm tra nước đi hợp lệ
    private static boolean validMove(int x, int y) {
        int opponent = 2; // Đối thủ là trắng
        if (turn == "WHITE") {
            opponent = 1; // Đối thủ là đen
        }

        if (map[x][y] == 0) {
            // Kiểm tra bên phải
            if (canMove(x, y, 0, 1, opponent)) {
                return true;
            }
            // Kiểm tra bên trái
            else if (canMove(x, y, 0, -1, opponent)) {
                return true;
            }
            // Kiểm tra bên dưới
            else if (canMove(x, y, 1, 0, opponent)) {
                return true;
            }
            // Kiểm tra bên trên
            else if (canMove(x, y, -1, 0, opponent)) {
                return true;
            }
            // Kiểm tra góc phải dưới
            else if (canMove(x, y, 1, 1, opponent)) {
                return true;
            }
            // Kiểm tra góc phải trên
            else if (canMove(x, y, -1, 1, opponent)) {
                return true;
            }
            // Kiểm tra góc trái trên
            else if (canMove(x, y, -1, -1, opponent)) {
                return true;
            }
            // Kiểm tra góc trái dưới
            else if (canMove(x, y, 1, -1, opponent)) {
                return true;
            }
        }
        return false;
    }

    // Logic của 1 lượt đi
    private static void getTurn(int turn, int row, int col) {
        // Đặt nước đi là quân cờ
        map[row][col] = turn;

        // Kiểm tra và lật cờ
        // Kiểm tra trên và dưới
        direction(row, col, turn, 0, -1);
        direction(row, col, turn, 0, 1);

        // Kiểm tra phải và trái
        direction(row, col, turn, 1,0);
        direction(row, col, turn, -1, 0);

        // Kiểm tra các góc
        direction(row, col, turn, 1,1);
        direction(row, col, turn, 1,-1);
        direction(row, col, turn, -1,1);
        direction(row, col, turn, -1,-1);
    }

    // Lật quân cờ theo hướng nhất định
    private static void direction (int row, int col, int turn, int colDir, int rowDir) {
        int currentRow= row + rowDir;
        int currentCol = col + colDir;

        // Nếu vượt ra phạm vi bàn cờ thì trả về
        if (currentRow==8 || currentRow<0 || currentCol==8 || currentCol<0)
        {
            return;
        }

        // Nếu tại vị trí đang xét là 1 quân cờ
        while (map[currentRow][currentCol]==1 || map[currentRow][currentCol]==2)
        {
            // Nếu vị trí cuối cùng là quân cờ cùng màu với lượt
            // thì thực hiện đổi màu tất cả các quân cờ nằm ở hướng ngược lại hướng đã chọn
            if (map[currentRow][currentCol]==turn)
            {
                while(!(row==currentRow && col==currentCol))
                {
                    map[currentRow][currentCol]=turn;
                    currentRow=currentRow-rowDir;
                    currentCol=currentCol-colDir;
                }
                break;
            }
            // Tìm vị trí cuối cùng theo hướng đã chọn
            else
            {
                currentRow=currentRow + rowDir;
                currentCol=currentCol + colDir;
            }

            // Nếu vượt ra phạm vi bàn cờ thì trả về
            if (currentRow<0 || currentCol<0 || currentRow==8 || currentCol==8)
            {
                break;
            }
        }
    }

    // Xét trường hợp kết thúc game đấu
    private static boolean gameOver() {
        boolean over = false;   // Trạng thái game đấu: false = chưa kết thúc, true = đã kết thúc
        int countPiece = 0; // Đếm số quân cờ trên bàn cờ

        for (int row=0; row<8; row++) {
            for (int col=0; col<8; col++) {
                if (map[row][col] != 0) {
                    countPiece++;
                }
                if (countPiece == 64) {
                    over = true;
                }
            }
        }
        return over;
    }

    private static void gameResult() {
        int countBlack = 0; // Đếm số quân đen
        int countWhite = 0; // Đếm số quân trắng

        for (int row=0; row<8; row++) {
            for (int col=0; col<8; col++) {
                if (map[row][col] == 1) {
                    countBlack++;
                }
                else {
                    countWhite++;
                }
            }
        }
        if (countBlack > countWhite) {
            System.out.println("Ván đấu kết thúc. Đen thắng");
        }
        else if (countBlack < countWhite) {
            System.out.println("Ván đấu kết thúc. Trắng thắng");
        }
        else {
            System.out.println("Ván đấu kết thúc. Hai bên hòa");
        }
    }

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
    public static int[] next(int move) {
        System.out.print(move);
        if (move < 0) {
            move = move*-1;
        }
        int b[] = {0, 0};
        b[0] = move/10;
        b[1] = move%10;
        return b;
    }

    // Thực hiện bước đi
    public static boolean getMap(int[] a) {
        System.out.println(turn);
        int x = a[0] - 1;
        int y = a[1] - 1;
        if (validMove(x, y) && turn == "BLACK") {
            getTurn(1, x, y);
        } else if (validMove(x, y) && turn == "WHITE") {
            getTurn(2, x, y);
        }
        else if (!validMove(x, y)) {
            return false;
        }
        return true;
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
        point.clear();
        int scoreBlack = 0;
        int scoreWhite = 0;
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                System.out.print(map[i][j] + " ");
                if (map[i][j] == 1) {
                    scoreBlack++;
                    point.add((i+1)*10+j+1);
                } else if (map[i][j] == 2) {
                    point.add(-((i+1)*10+j+1));
                    scoreWhite++;
                }
            }
            System.out.println("");
        }
        System.out.println("-------------------------------------");
        blackScore = scoreBlack;
        whiteScore = scoreWhite;
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

    // Tạo chuỗi byte gửi đi ở gói tin 3
    public static byte[] pkt_map(int blackScore, int whiteScore, int id, List<Integer> point){
        int len = 12 + point.size()*4;
        byte[] out = new byte[len];
        byte[] black = convert_data(blackScore);
        byte[] white = convert_data(whiteScore);
        // Mã hóa 2 điểm số
        for (int i = 0; i < 4 ; i++) {
            out[i] = black[i];
        }
        for (int i = 4; i < 8 ; i++) {
            out[i] = white[i-4];
        }
        // Mã hóa id
        for (int i = 8; i < 12; i++) {
            out[i] = convert_data(id)[i-8];
        }
        // Mã hóa map gửi đi
        for (int i = 0; i < point.size(); i++) {
            for (int j = 0; j < 4; j++) {
                byte[] bt = convert_data(point.get(i));
                out[12 +  j + i*4] = bt[j];
            }
        }
        return out;
    }

    // Khởi tạo gói tin gửi lên web server
    public static byte[] web_pkt(int action, int len_ip, byte[] ip, int port, int type_socket,
                                 int len_game, byte[] game, int len_info, byte[] info, int len_author, byte[] author) {
        byte[] ac = convert_data(action);
        byte[] lenIp = convert_data(len_ip);
        byte[] p = convert_data(port);
        byte[] skt = convert_data(type_socket);
        byte[] gm = convert_data(len_game);
        byte[] inf = convert_data(len_info);
        byte[] au = convert_data(len_author);

        int len_skt = 28 + len_ip + len_game + len_info + len_author;

        byte[] out = new byte[len_skt];

        for (int i = 0; i < 4; i++) {
            out[i] = ac[i];
            out[i+4] = lenIp[i];
        }

        for (int i = 8; i< 8 + ip.length; i++) {
            out[i] = ip[i-8];
        }

        for (int i = 8 + ip.length; i < 12 + ip.length; i++) {
            out[i] = p[i-8-ip.length];
        }

        for (int i = 12 + ip.length; i < 16 + ip.length; i++) {
            out[i] = skt[i-12-ip.length];
        }

        for (int i = 16 + ip.length; i < 20 + ip.length; i++) {
            out[i] = gm[i-16-ip.length];
        }

        for (int i = 20 + ip.length; i < 20 + ip.length + game.length; i++) {
            out[i] = game[i-20-ip.length];
        }

        for (int i = 20 + ip.length + game.length; i < 24 + ip.length + game.length; i++) {
            out[i] = inf[i-20-ip.length-game.length];
        }

        for (int i = 24 + ip.length + game.length; i < 24 + ip.length + game.length + info.length; i++) {
            out[i] = game[i-24-ip.length-game.length];
        }

        for (int i = 24 + ip.length + game.length + info.length; i < 28 + ip.length + game.length + info.length; i++) {
            out[i] = au[i-24-ip.length-game.length-info.length];
        }

        for (int i = 28 + ip.length + game.length + info.length; i < 28 + ip.length + game.length + info.length + author.length; i++) {
            out[i] = game[i-28-ip.length-game.length-info.length];
        }

        return out;
    }

    public void run() {
        byte[] input = new byte[4];

        int type = 0;
        int len = 0;
        try {

            InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();

            while (true) {
                is.read(input);
                type = restoreInt(input);
                is.read(input);
                len = restoreInt(input);

                if (type == 0) {
                    // Gửi gói tin xác nhân kết nối thành công
                    int accept = 1;
                    os.write(set_pkt(1, 4, convert_data(accept)));
                }
                else if (type == 2) {
                    is.read(input); int id = restoreInt(input);
                    System.out.println("ID người chơi: " + id);
                    printMap(map);

                    int nextID = 12345;
                    int length = 12 + point.size()*4;

                    byte[] out = pkt_map(blackScore, whiteScore, nextID, point);
//                    System.out.println(out);
                    os.write(set_pkt(3, length, out));

                }
                else if (type == 4) {
                    is.read(input);  int id = restoreInt(input);
                    is.read(input);  int points = restoreInt(input);

                    System.out.println(id);
                    // Dựa vào id để xác định turn này là quân nào đi
                    if (id == 12345) {
                        turn = "BLACK";
                    } else {
                        turn = "WHITE";
                    }

                    int[] move = next(points);
                    boolean checkPoint = getMap(move);
                    printMap(map);
                    if (!checkPoint) {
                        int length = 12 + point.size()*4;
                        byte[] out = pkt_map(blackScore, whiteScore, id, point);

                        os.write(set_pkt(5, length, out));
                    } else {
                        if (id == 12345) {
                            nextID = 12346;
                        }
                        else {
                            nextID = 12345;
                        }

                        if (gameOver()) {
                            gameResult();
                        }
//                        os.write(set_pkt(3, length, out));
                    }
                    if(gameOver()) {
                        os.write(set_pkt(6, 4, convert_data(id)));
                    }
                    else {
                        int length = 12 + point.size()*4;
                        byte[] out = pkt_map(blackScore, whiteScore, nextID, point);
                        for (ConnectionHandler client : clients) {
                            client.sendData(set_pkt(3, length, out));
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Kết nối hỏng");
        }
    }

    public static void main(String[] args) {
        int serverIndex = 0;
        try {
            // Kết nối đến web server
//            Socket skt = new Socket("104.194.240.16", 8081);
//            InputStream is = skt.getInputStream();
//            OutputStream os = skt.getOutputStream();
            // Tạo server socket cho 2 client
            ServerSocket sk=new ServerSocket(27001);

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
                Socket socket = sk.accept();
                ConnectionHandler client = new ConnectionHandler(sk, socket);
                synchronized (lock) {
                    clients.add(client);
                }
                new threadServer(socket).start();

                System.out.println("Server " + serverIndex + " is connect");
            }
        } catch (IOException e) {
            System.out.print(e);
        }
    }
}