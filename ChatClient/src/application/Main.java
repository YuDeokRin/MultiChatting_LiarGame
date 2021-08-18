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
	// client�� �������� thread �ʿ� x
	// Ŭ���̾�Ʈ ���α׷� ���� �޼ҵ��Դϴ�.

	public void startLiarGame() {
		// �������� thread �ʿ� ���⿡ runnable ��� thread ��ä ���
		Thread thread = new Thread() {
			public void run() {
				try {
					sendChat("���ӽ���");
				} catch (Exception e) {
					if (!ChatSocket.isClosed()) {
						stopClient();// ���� ��
						System.out.println("[���� ���� ����]1");
						Platform.exit();// ���α׷� ����
					}
				}
			}
		};
		thread.start();
	}

	public void endLiarGame() {
		// �������� thread �ʿ� ���⿡ runnable ��� thread ��ä ���
		Thread thread = new Thread() {
			public void run() {
				try {
					sendChat("��������");
				} catch (Exception e) {
					if (!ChatSocket.isClosed()) {
						stopClient();// ���� ��
						System.out.println("[���� ���� ����]2");
						Platform.exit();// ���α׷� ����
					}
				}
			}
		};
		thread.start();
	}

	// Ŭ���̾�Ʈ ���α׷� ���� �޼ҵ��Դϴ�.
	public void startClient(String userName, String IP, int port) {
		// �������� thread �ʿ� ���⿡ runnable ��� thread ��ä ���
		Thread thread = new Thread() {
			public void run() {
				try {
					LoginSocket = new Socket(IP, port);// ���� ���� ���� �� üũ
					ChatSocket = new Socket(IP, port + 1);
					sendLogin(userName);
					boolean chk = receiveLogin();
					if (chk == true) {
						sendChat(userName + "���� ä�ÿ� �����ϼ̽��ϴ�.\n");
						receiveChat();
					}
				} catch (Exception e) {
					if (!LoginSocket.isClosed()) {
						stopClient();// ���� ��
						System.out.println("[���� ���� ����]3");
						Platform.exit();// ���α׷� ����
					}
				}
			}
		};
		thread.start();
	}

	// Ŭ���̾�Ʈ ���α׷� ���� �޼ҵ�
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

	// �����κ��� �޼����� ���� �޴� �޼ҵ�
	public boolean receiveLogin() {
		while (true) {
			// ��� ���� ����
			try {
				InputStream in = LoginSocket.getInputStream();// �����κ��� ���� ����
				byte[] buffer = new byte[512];
				int length = in.read(buffer);// read �Լ��� ���� �Է� �޴´�.
				if (length == -1)
					throw new IOException();
				String message = new String(buffer, 0, length, "UTF-8");
				if (message.equals("��������")) {
					Platform.runLater(() -> {
						textArea.appendText("�����Ͻ� ID�� ��� �����մϴ�.\n");
					});
					return true;
				} else {
					Platform.runLater(() -> {
						textArea.appendText("�Է� �Ͻ� ID�� ��� �Ұ��� �մϴ�. �ٸ� ID�� ����ϼ���\n");
					});
					return false;
				}
			} catch (Exception e) {
				stopClient();
				return false;
			}
		}
	}

	// �����κ��� �޼����� ���� �޴� �޼ҵ�
	public void receiveChat() {
		while (true) {
			// ��� ���� ����
			try {
				InputStream in = ChatSocket.getInputStream();// �����κ��� ���� ����
				byte[] buffer = new byte[512];
				int length = in.read(buffer);// read �Լ��� ���� �Է� �޴´�.
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

	// ������ �޼����� �����ϴ� �޼ҵ�
	public void sendLogin(String message) {
		// ������ �����ϱ� ���ؼ��� thread �ʿ�, receive thread�� �ٸ�
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

	// ������ �޼����� �����ϴ� �޼ҵ�
	public void sendChat(String message) {
		// ������ �����ϱ� ���ؼ��� thread �ʿ�, receive thread�� �ٸ�
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

	// ������ ���α׷��� ���۽�Ű�� �޼ҵ��Դϴ�.
	@Override
	public void start(Stage primaryStage) {
		BorderPane root = new BorderPane();
		root.setPadding(new Insets(5));

		HBox hbox = new HBox(); // BorderPane ���� �ϳ� �� layout�� �����.
		hbox.setSpacing(5);

		TextField userName = new TextField();
		userName.setPrefWidth(150);
		userName.setPromptText("�г����� �Է��ϼ���.");
		HBox.setHgrow(userName, Priority.ALWAYS);

		TextField IPText = new TextField("127.0.0.1");
		TextField portText = new TextField("9876");
		portText.setPrefWidth(80);

		// hbox ���� ������ �� �� �ֵ���
		hbox.getChildren().addAll(userName, IPText, portText);
		root.setTop(hbox);// border padding ���� �־���

		textArea = new TextArea();
		textArea.setEditable(false);// ������ �� ������
		root.setCenter(textArea);

		TextField input = new TextField();
		input.setPrefWidth(Double.MAX_VALUE);
		input.setDisable(true);// ���� ������ �ȵǵ���

		input.setOnAction(event -> {
			sendChat(userName.getText());
			input.setText("");// ���� ������ �޼��� ���� ĭ ����.
			input.requestFocus();// �ٽ� ���� �� �ֵ��� Focus �������ش�.
		});

		Button sendButton = new Button("������");
		sendButton.setDisable(true);

		sendButton.setOnAction(event -> {
			sendChat(userName.getText() + ": " + input.getText() + "\n");
			input.setText("");
			input.requestFocus();
		});
		Button connectionButton = new Button("�����ϱ�");
		connectionButton.setOnAction(event -> {
			if (connectionButton.getText().equals("�����ϱ�")) {
				int port = 9876;

				// send("/w "+userName.getText());//����Ű ����
				try {
					port = Integer.parseInt(portText.getText());
				} catch (Exception e) {
					e.printStackTrace();
				}
				startClient(userName.getText(), IPText.getText(), port);
				Platform.runLater(() -> {
					textArea.appendText("[ ä�ù� ����]\n");
				});
				connectionButton.setText("�����ϱ�");// ������ �̷�� ������
				input.setDisable(false);
				sendButton.setDisable(false);
				input.requestFocus();
			} else {
				// �����ϱ⿴�ٸ�
				stopClient();
				Platform.runLater(() -> {
					textArea.appendText("[ ä�ù� ����]\n");
				});
				connectionButton.setText("�����ϱ�");
				input.setDisable(true);
				sendButton.setDisable(true);
			}
		});
		////////// �ӽ�///////////
		Button startButton = new Button("���ӽ���\n");
		startButton.setOnAction(event -> {
			if (startButton.getText().equals("���ӽ���\n")) {
				int port = 9877;
				try {
					port = Integer.parseInt(portText.getText());
				} catch (Exception e) {
					e.printStackTrace();
				}
				startLiarGame();
				Platform.runLater(() -> {
					textArea.appendText("[���ӽ���]\n");
				});
				input.setDisable(false);
				sendButton.setDisable(false);
				input.requestFocus();
			} else {
				// �����ϱ⿴�ٸ�
				endLiarGame();
				Platform.runLater(() -> {
					textArea.appendText("[��������]\n");
				});
				input.setDisable(true);
				sendButton.setDisable(true);
			}
		});
//////////�ӽ�///////////
		Button voteButton = new Button("��ǥ�ϱ�");
		voteButton.setOnAction(event -> {
			if (voteButton.getText().equals("��ǥ�ϱ�")) {
				int port = 9876;
				try {
					port = Integer.parseInt(portText.getText());
				} catch (Exception e) {
					e.printStackTrace();
				}
				// ��ǥ�ϴ� �Լ� ����
				Platform.runLater(() -> {
					textArea.appendText("[��ǥ ����]\n");
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
		primaryStage.setTitle("[ ä�� Ŭ���̾�Ʈ]");
		primaryStage.setScene(scene);
		primaryStage.setOnCloseRequest(event -> stopClient());
		primaryStage.show();

		connectionButton.requestFocus();
	}

	// ���α׷��� �������̴�.
	public static void main(String[] args) {
		launch(args);
	}
}
