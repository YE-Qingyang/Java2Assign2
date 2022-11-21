package application.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;


public class Controller implements Initializable {
  private static final int PLAY_1 = 1;
  private static final int PLAY_2 = 2;
  private static final int EMPTY = 0;
  private static final int BOUND = 90;
  private static final int OFFSET = 15;

  @FXML
  private Label ChessType;

  @FXML
  private Label linkStatus;

  @FXML
  private Pane base_square;

  @FXML
  private Label gameResult;

  public Rectangle getGame_panel() {
    return game_panel;
  }

  @FXML
  private Rectangle game_panel;

  @FXML
  private Label roomNumber;

  @FXML
  private Label turn;
  private String position;

  @FXML
  private Label waitConnect;

  private boolean TURN;

  private static final int[][] chessBoard = new int[3][3];
  private static final boolean[][] flag = new boolean[3][3];

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    game_panel.setOnMousePressed(event -> {
      if (isYourTurn() && (!isFinish()) && isConnect()) {
        int x = (int) (event.getX() / BOUND);
        int y = (int) (event.getY() / BOUND);
        position = x + "," + y;
        refreshBoard(x, y);
      }
    });
  }

  public boolean isConnect() {
    return waitConnect.getText().equals("Connected!");
  }

  public String getPosition() {
    return position;
  }

  public void setResult(int player) {
    if (isWin(player)) {
      gameResult.setText("You win! Game over.");
      clearTurnField();
    }
    if (isWin(3 - player)) {
      gameResult.setText("You lose... Game over.");
      clearTurnField();
    }
    if (isDraw()) {
      gameResult.setText("The game is tied. Game over.");
      clearTurnField();
    }
  }


  public void setTurnField(boolean isYourTurn) {
    if (!isFinish()) {
      if (isYourTurn) {
        turn.setText("It's your turn!");
      } else {
        turn.setText("Not your turn...");
      }
    }
  }

  public void clearTurnField() {
    turn.setText("");
  }

  public boolean isYourTurn() {
    return turn.getText().equals("It's your turn!");
  }

  public void setTURN(boolean isCircle) {
    TURN = isCircle;
  }

  public void setWaitConnect(String text) {
    waitConnect.setText(text);
  }

  public Label getRoomNumber() {
    return roomNumber;
  }

  public void setLinkStatus(String text) {
    linkStatus.setText(text);
  }

  private boolean refreshBoard(int x, int y) {
    if (chessBoard[x][y] == EMPTY) {
      chessBoard[x][y] = TURN ? PLAY_1 : PLAY_2;
      drawChess();
      return true;
    }
    return false;
  }

  public String getChessBoard() {
    StringBuilder chess = new StringBuilder();
    for (int i = 0; i < chessBoard.length; i++) {
      for (int j = 0; j < chessBoard[0].length; j++) {
        switch (chessBoard[i][j]) {
          case EMPTY:
            chess.append("0");
            break;
          case PLAY_1:
            chess.append("1");
            break;
          case PLAY_2:
            chess.append("2");
            break;
          default:
            break;
        }
      }
    }
    return chess.toString();
  }

  public void restoreChessBoard(String chessboard) {
    int cnt = 0;
    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 3; j++) {
        chessBoard[i][j] = Integer.parseInt(String.valueOf(chessboard.charAt(cnt)));
        cnt++;
      }
    }
    drawChess();
  }

  public void refresh(int x, int y) {
    if (chessBoard[x][y] == EMPTY) {
      chessBoard[x][y] = !TURN ? PLAY_1 : PLAY_2;
      drawChess();
    }
  }

  private void drawChess() {
    for (int i = 0; i < chessBoard.length; i++) {
      for (int j = 0; j < chessBoard[0].length; j++) {
        if (flag[i][j]) {
          // This square has been drawing, ignore.
          continue;
        }
        switch (chessBoard[i][j]) {
          case PLAY_1:
            drawCircle(i, j);
            break;
          case PLAY_2:
            drawLine(i, j);
            break;
          case EMPTY:
            // do nothing
            break;
          default:
            System.err.println("Invalid value!");
        }
      }
    }
  }

  private void drawCircle(int i, int j) {
    Circle circle = new Circle();
    base_square.getChildren().add(circle);
    circle.setCenterX(i * BOUND + BOUND / 2.0 + OFFSET);
    circle.setCenterY(j * BOUND + BOUND / 2.0 + OFFSET);
    circle.setRadius(BOUND / 2.0 - OFFSET / 2.0);
    circle.setStroke(Color.RED);
    circle.setFill(Color.TRANSPARENT);
    flag[i][j] = true;
  }

  private void drawLine(int i, int j) {
    Line line_a = new Line();
    Line line_b = new Line();
    base_square.getChildren().add(line_a);
    base_square.getChildren().add(line_b);
    line_a.setStartX(i * BOUND + OFFSET * 1.5);
    line_a.setStartY(j * BOUND + OFFSET * 1.5);
    line_a.setEndX((i + 1) * BOUND + OFFSET * 0.5);
    line_a.setEndY((j + 1) * BOUND + OFFSET * 0.5);
    line_a.setStroke(Color.BLUE);

    line_b.setStartX((i + 1) * BOUND + OFFSET * 0.5);
    line_b.setStartY(j * BOUND + OFFSET * 1.5);
    line_b.setEndX(i * BOUND + OFFSET * 1.5);
    line_b.setEndY((j + 1) * BOUND + OFFSET * 0.5);
    line_b.setStroke(Color.BLUE);
    flag[i][j] = true;
  }

  public Label getChessType() {
    return ChessType;
  }

  public boolean isWin(int player) {
    for (int i = 0; i < chessBoard.length; i++) {
      int num = 0;
      for (int j = 0; j < chessBoard[0].length; j++) {
        if (chessBoard[i][j] == player) {
          num++;
        }
      }
      if (num == 3) {
        return true;
      }
      num = 0;
    }

    for (int j = 0; j < chessBoard.length; j++) {
      int num = 0;
      for (int i = 0; i < chessBoard[0].length; i++) {
        if (chessBoard[i][j] == player) {
          num++;
        }
      }
      if (num == 3) {
        return true;
      }
      num = 0;
    }

    if (chessBoard[1][1] == player) {
      if (chessBoard[0][0] == player && chessBoard[2][2] == player) {
        return true;
      }
      if (chessBoard[0][2] == player && chessBoard[2][0] == player) {
        return true;
      }
    }

    return false;
  }

  public boolean isDraw() {
    for (int i = 0; i < chessBoard.length; i++) {
      for (int j = 0; j < chessBoard[0].length; j++) {
        if (chessBoard[i][j] == EMPTY) {
          return false;
        }
      }
    }
    return !isWin(PLAY_1) && (!isWin(PLAY_2));
  }

  public boolean isFinish() {
    return isDraw() || isWin(PLAY_2) || isWin(PLAY_1);
  }
}
