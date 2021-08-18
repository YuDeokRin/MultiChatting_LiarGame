package application;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class Main extends Application {

	Socket LoginSocket;
	Socket ChatSocket;
	TextArea textArea;
	// client는 여러개의 thread 필요 x
	// 클라이언트 프로그램 동작 메소드입니다.

	public void startLiarGame() {
		// 여러개의 thread 필요 없기에 runnable 대신 thread 객채 사용
		Thread thread = new Thread() {
			public void run() {
				try {
					sendChat("게임시작");
				} catch (Exception e) {
					if (!ChatSocket.isClosed()) {
						stopClient();// 오류 시
						System.out.println("[서버 접속 실패]1");
						Platform.exit();// 프로그램 종료
					}
				}
			}
		};
		thread.start();
	}

	public void endLiarGame() {
		// 여러개의 thread 필요 없기에 runnable 대신 thread 객채 사용
		Thread thread = new Thread() {
			public void run() {
				try {
					sendChat("게임종료");
				} catch (Exception e) {
					if (!ChatSocket.isClosed()) {
						stopClient();// 오류 시
						System.out.println("[서버 접속 실패]2");
						Platform.exit();// 프로그램 종료
					}
				}
			}
		};
		thread.start();
	}

	// 클라이언트 프로그램 동작 메소드입니다.
	public void startClient(String userName, String IP, int port) {
		// 여러개의 thread 필요 없기에 runnable 대신 thread 객채 사용
		Thread thread = new Thread() {
			public void run() {
				try {
					LoginSocket = new Socket(IP, port);// 소켓 새로 생성 꼭 체크
					ChatSocket = new Socket(IP, port + 1);
					sendLogin(userName);
					boolean chk = receiveLogin();
					if (chk == true) {
						sendChat(userName + "님이 채팅에 참가하셨습니다.\n");
						receiveChat();
					}
				} catch (Exception e) {
					if (!LoginSocket.isClosed()) {
						stopClient();// 오류 시
						System.out.println("[서버 접속 실패]3");
						Platform.exit();// 프로그램 종료
					}
				}
			}
		};
		thread.start();
	}

	// 클라이언트 프로그램 종료 메소드
	public void stopClient() {
		try {
			if (LoginSocket != null && !LoginSocket.isClosed()) {
				LoginSocket.close();
			}
			if (ChatSocket != null && !ChatSocket.isClosed()) {
				ChatSocket.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 서버로부터 메세지를 전달 받는 메소드
	public boolean receiveLogin() {
		while (true) {
			// 계속 전달 받음
			try {
				InputStream in = LoginSocket.getInputStream();// 서버로부터 전달 받음
				byte[] buffer = new byte[512];
				int length = in.read(buffer);// read 함수로 실제 입력 받는다.
				if (length == -1)
					throw new IOException();
				String message = new String(buffer, 0, length, "UTF-8");
				if (message.equals("생성가능")) {
					Platform.runLater(() -> {
						textArea.appendText("접속하신 ID는 사용 가능합니다.\n");
					});
					return true;
				} else {
					Platform.runLater(() -> {
						textArea.appendText("입력 하신 ID는 사용 불가능 합니다. 다른 ID를 사용하세요\n");
					});
					return false;
				}
			} catch (Exception e) {
				stopClient();
				return false;
			}
		}
	}

	// 서버로부터 메세지를 전달 받는 메소드
	public void receiveChat() {
		while (true) {
			// 계속 전달 받음
			try {
				InputStream in = ChatSocket.getInputStream();// 서버로부터 전달 받음
				byte[] buffer = new byte[512];
				int length = in.read(buffer);// read 함수로 실제 입력 받는다.
				if (length == -1)
					throw new IOException();
				String message = new String(buffer, 0, length, "UTF-8");

				Platform.runLater(() -> {
					textArea.appendText(message);
				});
			} catch (Exception e) {
				stopClient();
				break;
			}
		}
	}

	// 서버로 메세지를 전송하는 메소드
	public void sendLogin(String message) {
		// 서버로 전달하기 위해서도 thread 필요, receive thread와 다름
		Thread thread = new Thread() {
			public void run() {
				try {
					OutputStream out = LoginSocket.getOutputStream();
					byte[] buffer = message.getBytes("UTF-8");
					out.write(buffer);
					out.flush();
				} catch (Exception e) {
					stopClient();
				}
			}
		};
		thread.start();
	}

	// 서버로 메세지를 전송하는 메소드
	public void sendChat(String message) {
		// 서버로 전달하기 위해서도 thread 필요, receive thread와 다름
		Thread thread = new Thread() {
			public void run() {
				try {
					OutputStream out = ChatSocket.getOutputStream();
					byte[] buffer = message.getBytes("UTF-8");
					out.write(buffer);
					out.flush();
				} catch (Exception e) {
					stopClient();
				}
			}
		};
		thread.start();
	}

	// 실제로 프로그램을 동작시키는 메소드입니다.
	@Override
	public void start(Stage primaryStage) {
		BorderPane root = new BorderPane();
		root.setPadding(new Insets(5));

		HBox hbox = new HBox(); // BorderPane 위에 하나 더 layout을 만든다.
		hbox.setSpacing(5);

		TextField userName = new TextField();
		userName.setPrefWidth(150);
		userName.setPromptText("닉네임을 입력하세요.");
		HBox.setHgrow(userName, Priority.ALWAYS);

		TextField IPText = new TextField("127.0.0.1");
		TextField portText = new TextField("9876");
		portText.setPrefWidth(80);

		// hbox 내에 실제로 들어갈 수 있도록
		hbox.getChildren().addAll(userName, IPText, portText);
		root.setTop(hbox);// border padding 위에 넣어줌

		textArea = new TextArea();
		textArea.setEditable(false);// 수정할 수 없도록
		root.setCenter(textArea);

		TextField input = new TextField();
		input.setPrefWidth(Double.MAX_VALUE);
		input.setDisable(true);// 접속 전에는 안되도록

		input.setOnAction(event -> {
			sendChat(userName.getText());
			input.setText("");// 전송 했으니 메세지 전송 칸 비운다.
			input.requestFocus();// 다시 보낼 수 있도록 Focus 설정해준다.
		});

		Button sendButton = new Button("보내기");
		sendButton.setDisable(true);

		sendButton.setOnAction(event -> {
			sendChat(userName.getText() + ": " + input.getText() + "\n");
			input.setText("");
			input.requestFocus();
		});
		Button connectionButton = new Button("접속하기");
		connectionButton.setOnAction(event -> {
			if (connectionButton.getText().equals("접속하기")) {
				int port = 9876;

				// send("/w "+userName.getText());//단축키 마냥
				try {
					port = Integer.parseInt(portText.getText());
				} catch (Exception e) {
					e.printStackTrace();
				}
				startClient(userName.getText(), IPText.getText(), port);
				Platform.runLater(() -> {
					textArea.appendText("[ 채팅방 접속]\n");
				});
				connectionButton.setText("종료하기");// 접속이 이루어 졌으니
				input.setDisable(false);
				sendButton.setDisable(false);
				input.requestFocus();
			} else {
				// 종료하기였다면
				stopClient();
				Platform.runLater(() -> {
					textArea.appendText("[ 채팅방 퇴장]\n");
				});
				connectionButton.setText("접속하기");
				input.setDisable(true);
				sendButton.setDisable(true);
			}
		});
		////////// 임시///////////
		Button startButton = new Button("게임시작\n");
		startButton.setOnAction(event -> {
			if (startButton.getText().equals("게임시작\n")) {
				int port = 9877;
				try {
					port = Integer.parseInt(portText.getText());
				} catch (Exception e) {
					e.printStackTrace();
				}
				startLiarGame();
				Platform.runLater(() -> {
					textArea.appendText("[게임시작]\n");
				});
				input.setDisable(false);
				sendButton.setDisable(false);
				input.requestFocus();
			} else {
				// 종료하기였다면
				endLiarGame();
				Platform.runLater(() -> {
					textArea.appendText("[게임종료]\n");
				});
				input.setDisable(true);
				sendButton.setDisable(true);
			}
		});
//////////임시///////////
		Button voteButton = new Button("투표하기");
		voteButton.setOnAction(event -> {
			if (voteButton.getText().equals("투표하기")) {
				int port = 9876;
				try {
					port = Integer.parseInt(portText.getText());
				} catch (Exception e) {
					e.printStackTrace();
				}
				// 투표하는 함수 실행
				Platform.runLater(() -> {
					textArea.appendText("[투표 시작]\n");
				});
				input.setDisable(false);
				sendButton.setDisable(false);
				input.requestFocus();
			}
		});

		BorderPane pane = new BorderPane();
		pane.setLeft(connectionButton);
		pane.setTop(startButton);
		pane.setBottom(voteButton);
		pane.setCenter(input);
		pane.setRight(sendButton);
		root.setBottom(pane);
		Scene scene = new Scene(root, 400, 400);
		primaryStage.setTitle("[ 채팅 클라이언트]");
		primaryStage.setScene(scene);
		primaryStage.setOnCloseRequest(event -> stopClient());
		primaryStage.show();

		connectionButton.requestFocus();
	}

	// 프로그램의 진입점이다.
	public static void main(String[] args) {
		launch(args);
	}
}
