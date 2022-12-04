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
    public static int nextID = 12345;
    public static int winID = 0;
    public static int numPlayer = 0;
    public static List<Integer> point = new ArrayList<Integer>();
    public static String turn = "BLACK";
    public static int[][] map =
            {{0,0,0,0,0,0,0,0},
                    {0,0,0,0,0,0,0,0},
                    {0,0,0,0,0,0,0,0},
                    {0,0,0,1,2,0,0,0},
                    {0,0,0,2,1,0,0,0},
                    {0,0,0,0,0,0,0,0},
                    {0,0,0,0,0,0,0,0},
                    {0,0,0,0,0,0,0,0}};

    // Mảng chứa vị trí các quân cờ đen
    private static final List<Integer> blackPos=new ArrayList<Integer>();

    // Mảng chứa vị trí các quân cờ trắng
    private static final List<Integer> whitePos=new ArrayList<Integer>();

    private static boolean canMove(int row, int col, int rowDir, int colDir, int opponent) {
        int currentRow = row + rowDir;
        int currentCol = col + colDir;

        // Nếu vượt ra phạm vi bàn cờ thì trả về
        if (currentRow==8 || currentRow<0 || currentCol==8 || currentCol<0)
        {
            return false;
        }

        // Nếu là quân cờ cùng màu thì trả về
        if (map[currentRow][currentCol] != opponent && map[currentRow][currentCol] != 0) {
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
            // Kiểm tra bên pahir
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
            else if (canMove(x, y, 1, -1, opponent)) {
                return true;
            }
            // Kiểm tra góc trái trên
            else if (canMove(x, y, -1, -1, opponent)) {
                return true;
            }
            // Kiểm tra góc trái dưới
            else return canMove(x, y, -1, 1, opponent);
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
            winID = blackID;
        }
        else if (countBlack < countWhite) {
            System.out.println("Ván đấu kết thúc. Trắng thắng");
            winID = whiteID;
        }
        else {
            System.out.println("Ván đấu kết thúc. Hai bên hòa");
            winID = 1;
        }
    }

    // Lấy tọa độ các ô
    public static List<Integer> coordinates(int[][] map) {
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
        if (move < 0) {
            move = move*-1;
            turn = "WHITE";
        } else {
            turn = "BLACK";
        }
        int[] b = {0, 0};
        b[0] = move/10;
        b[1] = move%10;
        return b;
    }

    // Thực hiện bước đi
    public static boolean getMap(int[] a, Board board) {
        int x = a[0] - 1;
        int y = a[1] - 1;
        if (validMove(x, y) && turn == "BLACK") {
            getTurn(1, x, y);
            board.paint(map);
        } else if (validMove(x, y) && turn == "WHITE") {
            getTurn(2, x, y);
            board.paint(map);
        }
        else {
            System.out.println("Nước đi thất bại");
            return false;
        }
        if (gameOver()) {
            gameResult();
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
            System.out.println();
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
        Board board = new Board();
        board.paint(map);
        board.view();
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
                    numPlayer++;
                    is.read(input); int id = restoreInt(input);
                    System.out.println("ID người chơi: " + id);


//                    System.out.println(out);
                    printMap(map);

                    int length = 12 + point.size()*4;
                    byte[] out = pkt_map(blackScore, whiteScore, blackID, point);

                    os.write(set_pkt(3, length, out));
                }
                else if (type == 4) {
                    is.read(input);  int id = restoreInt(input);
                    is.read(input);  int points = restoreInt(input);

                    // Dựa vào id để xác định turn này là quân nào đi
                    if (id == blackID) {
                        turn = "BLACK";
                    } else if (id == whiteID){
                        turn = "WHITE";
                    }

                    int[] move = next(points);
                    boolean checkPoint = getMap(move, board);
                    printMap(map);
                    if (!checkPoint) {
                        int length = 12 + point.size()*4;
                        byte[] out = pkt_map(blackScore, whiteScore, id, point);

                        os.write(set_pkt(5, length, out));
                    } else {
                        if (nextID == blackID) {
                            nextID = whiteID;
                        } else if(nextID == whiteID) {
                            nextID = blackID;
                        }
                        int length = 12 + point.size()*4;

                        byte[] out = pkt_map(blackScore, whiteScore, nextID, point);
                        os.write(set_pkt(3, length, out));
//                        if (id == blackID) {
//                            int length = 12 + point.size()*4;
//
//                            byte[] out = pkt_map(blackScore, whiteScore, whiteID, point);
//                            os.write(set_pkt(10, length, out));
//                            byte[] out2 = pkt_map(blackScore, whiteScore, whiteID, point);
//                            os.write(set_pkt(3, length, out2));
//                        } else if (id == whiteID) {
//                            int length = 12 + point.size()*4;
//
//                            byte[] out = pkt_map(blackScore, whiteScore, blackID, point);
//                            os.write(set_pkt(3, length, out));
//                            byte[] out2 = pkt_map(blackScore, whiteScore, whiteID, point);
//                            os.write(set_pkt(10, length, out2));
//                        }
                    }

                    if( winID != 0 && winID !=1) {
                        System.out.println("Người chiến thắng là: " + winID);
                        os.write(set_pkt(6, 4, convert_data(winID)));
                    } else if (winID == 1) {
                        System.out.println("Kết quả hòa");
                        os.write(set_pkt(6, 4, convert_data(1)));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Kết nối hỏng");
        }
    }
}