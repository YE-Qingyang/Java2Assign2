package application;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class GameServer {
    private int port = 8888;
    private ServerSocket serverSocket;
    private List<Player> clients;
    private List<Match> matches;
    private Lock lock;

    public GameServer() {
        try {
            serverSocket = new ServerSocket(this.port);
        } catch (IOException e) {
            // e.printStackTrace();
        }
        clients = new LinkedList<>();
        lock = new ReentrantLock();
        matches = new LinkedList<>();

        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println(clientSocket.getPort());
                Player player = new Player(clientSocket);
                clients.add(player);
                int flag = 0;
                Match match = null;
                for (Match m : matches) {
                    if (!m.isConnected()) {
                        match = m;
                        flag = 1;
                        break;
                    }
                }
                if (flag == 0) {
                    match = new Match();
                    matches.add(match);
                }
                match.addPlayer(player);
                player.send(player.getRoomNum(), "Link", "Success");
                player.start();
                if (flag == 0) {
                    match.start();
                }
            } catch (IOException e) {
//                 e.printStackTrace();
            }
        }
    }

    // store the data of each player and communicate message with clients
    class Player extends Thread {
        private Socket socket;
        private boolean isMatched;
        private ObjectOutputStream os;
        private ObjectInputStream is;
        private String roomNum;
        private int playerType;

        public Player(Socket socket) {
            this.socket = socket;
            isMatched = false;
            roomNum = null;
            try {
                os = new ObjectOutputStream(socket.getOutputStream());
                is = new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {
                // e.printStackTrace();
            }
        }

        public void run() {
            while (true) {
                String s = null;
                try {
                    s = (String) is.readObject();
                    System.out.println(s);
                } catch (IOException | ClassNotFoundException e) {
//                  e.printStackTrace();
                    clients.forEach(p -> p.send(roomNum, "Quit", Integer.toString(this.getPlayerType())));
                    matches.forEach(m -> {
                        if (Integer.toString(m.room).equals(roomNum)) {
                            m.quitMatch(this.getPlayerType());
                        }
                    });
                    break;
                }
                if (s != null) {
                    // handle the message from clients
                    String[] message = s.split(":");
                    String room = message[0];
                    String key = message[1];
                    String value = message[2];

                    clients.forEach(p -> p.send(room, key, value));
                }
            }
        }

        public void send(String room, String type, String message) {
            try {
                os.writeObject(room + ":" + type + ":" + message);
                os.flush();
            } catch (IOException e) {
                // e.printStackTrace();
            }
        }

        public boolean isMatched() {
            return isMatched;
        }

        public void setMatched(boolean matched) {
            isMatched = matched;
        }

        public String getRoomNum() {
            return roomNum;
        }

        public void setRoomNum(String roomNum) {
            this.roomNum = roomNum;
        }

        public int getPlayerType() {
            return playerType;
        }

        public void setPlayerType(int playerType) {
            this.playerType = playerType;
        }
    }


    // match two player in a match
    class Match extends Thread {
        private Player circle;
        private Player line;
        private int room;

        public Match() {
            this.circle = null;
            this.line = null;
            room = (int) (Math.random() * 100 + 1);
            for (Match m : matches) {
                while (m.room == room) {
                    room = (int) (Math.random() * 100 + 1);
                }
            }
        }

        public boolean isConnected() {
            return circle != null && line != null;
        }

        public void addPlayer(Player player) {
            if (circle == null) {
                circle = player;
                player.setRoomNum(Integer.toString(room));
                player.setPlayerType(1);
                player.send(player.getRoomNum(), "Chess", "Circle");
                player.send(player.getRoomNum(), "Connect", "Waiting");
            } else {
                line = player;
                player.setRoomNum(Integer.toString(room));
                player.setPlayerType(2);
                player.send(player.getRoomNum(), "Chess", "Line");
                player.send(player.getRoomNum(), "Connect", "Waiting");
            }
            if (isConnected()) {
                circle.send(player.getRoomNum(), "Connect", "Success");
                line.send(player.getRoomNum(), "Connect", "Success");
                circle.setMatched(true);
                line.setMatched(true);
            }
        }

        public void quitMatch(int type) {
            if (type == 1) {
                circle = null;
                // line.send(line.getRoomNum(), "Connect", "Waiting");
                if (line != null) {
                    line.setMatched(false);
                }
            }
            if (type == 2) {
                line = null;
                // circle.send(circle.getRoomNum(), "Connect", "Waiting");
                if (circle != null) {
                    circle.setMatched(false);
                }
            }

        }
    }


    public static void main(String[] args) {
        System.out.println("Server is waiting for accept...");
        GameServer gameServer = new GameServer();
    }
}
