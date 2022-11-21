package application;

import application.controller.Controller;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;


public class Client extends Application {
    private Socket socket = null;
    private ObjectOutputStream os;
    private ObjectInputStream is;
    private Listener listener;
    private String roomNum;
    private int chessType;

    private Controller controller;
    private boolean isConnect;

    private String position;

    public Client() {
        try {
            this.socket = new Socket("127.0.0.1", 8888);
            os = new ObjectOutputStream(socket.getOutputStream());
            is = new ObjectInputStream(socket.getInputStream());
            this.isConnect = false;
            String s = (String) is.readObject();
            String[] info = s.split(":");
            this.roomNum = info[0];
            if (info[1].equals("Chess")) {
                if (info[2].equals("Circle")) {
                    this.chessType = 1;
                } else {
                    this.chessType = 2;
                }
            }
            System.out.println(s);
            position = "";

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }


    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getClassLoader().getResource("mainUI.fxml"));

            Pane root = fxmlLoader.load();

            controller = fxmlLoader.getController();
            controller.getRoomNumber().setText("Room Number: " + roomNum);
            if (chessType == 1) {
                controller.getChessType().setText("Your Chess: Circle");
                controller.setTURN(true);
                // controller.setTurnField(true);
            } else {
                controller.getChessType().setText("Your Chess: Line");
                controller.setTURN(false);
                // controller.setTurnField(false);
            }

            controller.getGame_panel().setOnMouseReleased(event -> {
                if (controller.isYourTurn() && isConnect()) {
                    position = controller.getPosition();
                    send(roomNum, "Play" + chessType, position);
                }
            });

            listener = new Listener(socket);
            listener.start();

            primaryStage.setTitle("Tic Tac Toe");
            primaryStage.setScene(new Scene(root));
            primaryStage.setResizable(false);
            primaryStage.show();

            primaryStage.setOnCloseRequest(event -> {
//                send(roomNum, "Quit", Integer.toString(chessType));
                primaryStage.close();
                System.exit(0);
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    public boolean isConnect() {
        return isConnect;
    }

    class Listener extends Thread {
        private Socket socket;

        public Listener(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            String s = null;
            while (true) {

                try {
                    s = (String) is.readObject();
                    System.out.println(s);
                } catch (IOException | ClassNotFoundException e) {
                    // e.printStackTrace();
                    Platform.runLater(() -> controller.setLinkStatus("Disconnect from the server..."));
                }
                if (s != null) {
                    String[] message = s.split(":");
                    String room = message[0];
                    String key = message[1];
                    String value = message[2];
                    if (room.equals(roomNum)) {
                        Platform.runLater(() -> {
                            switch (key) {
                                case "Connect": {
                                    if (value.equals("Waiting")) {
                                        controller.setWaitConnect("Waiting for connecting...");
                                        isConnect = false;
                                        controller.clearTurnField();
                                    } else if (value.equals("Success")) {
                                        controller.setWaitConnect("Connected!");
                                        isConnect = true;
                                        String chessboard = controller.getChessBoard();
                                        if (controller.getChessBoard().equals("000000000")) {
                                            if (chessType == 1) {
                                                controller.setTurnField(true);
                                            }
                                            if (chessType == 2) {
                                                controller.setTurnField(false);
                                            }
                                        } else {
                                            send(roomNum, "Board", chessboard);
                                        }
                                    }
                                    break;
                                }
                                case "Link": {
                                    if (value.equals("Success")) {
                                        controller.setLinkStatus("Connected to the server!");
                                    }
                                    break;
                                }
                                case "Play1": {
                                    if (value != null && chessType == 2) {
                                        String[] pos = value.split(",");
                                        controller.setTurnField(true);
                                        controller.refresh(Integer.parseInt(pos[0]), Integer.parseInt(pos[1]));
                                    }
                                    if (chessType == 1) {
                                        controller.setTurnField(false);
                                    }
                                    controller.setResult(chessType);
                                    break;
                                }
                                case "Play2": {
                                    if (value != null && chessType == 1) {
                                        String[] pos = value.split(",");
                                        controller.setTurnField(true);
                                        controller.refresh(Integer.parseInt(pos[0]), Integer.parseInt(pos[1]));
                                    }
                                    if (chessType == 2) {
                                        controller.setTurnField(false);
                                    }
                                    controller.setResult(chessType);
                                    break;
                                }
                                case "Quit": {
                                    if (value.equals(Integer.toString(3 - chessType))) {
                                        controller.setWaitConnect("Lose connect. Waiting for connecting...");
                                        isConnect = false;
                                        controller.clearTurnField();
                                    }
                                    break;
                                }
                                case "Board": {
                                    controller.restoreChessBoard(value);
                                    int player1 = 0;
                                    int player2 = 0;
                                    for (int i = 0; i < 9; i++) {
                                        if (String.valueOf(value.charAt(i)).equals("1")) {
                                            player1++;
                                        }
                                        if (String.valueOf(value.charAt(i)).equals("2")) {
                                            player2++;
                                        }
                                    }
                                    if (player1 > player2) {
                                        controller.setTurnField(chessType != 1);
                                    } else {
                                        controller.setTurnField(chessType == 1);
                                    }
                                    break;
                                }
                                default:
                                    break;
                            }
                        });

                    }
                }
            }
        }
    }

    private void send(String room, String type, String msg) {
        try {
            os.writeObject(room + ":" + type + ":" + msg);
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
