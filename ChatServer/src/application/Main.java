package application;


import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class Main extends Application {

	public static ExecutorService threadPool; // ExecutorService threadPool -> �پ��� Ŭ���̾�Ʈ�� ���� ���� �� ��������� ȿ�������� ����
												// ���۽����� Ŭ���̾�Ʈ�� ���� �����ϴ��� ��������� ������ ������ �ֱ� ������ ������ ���� ���ϸ� ������ �� �ִ�.
	public static Vector<Client> clients = new Vector<Client>(); // Vector ->

	ServerSocket serverSocket;

	// ������ �������Ѽ� Ŭ���̾�Ʈ�� ������ ��ٸ��� �޼ҵ��Դϴ�.
	public void startServer(String IP, int port) { // IP , Port�� ���� Ŭ���̾�Ʈ�� ���
		try {
			serverSocket = new ServerSocket(); //���� ���� ��ü ���� 
			serverSocket.bind(new InetSocketAddress(IP, port)); // bind�� ����Ͽ� ������ǻ�� ������ �����ϴ� �� ��ǻ�Ͱ� �ڽ��� ip�ּ� port��ȣ�� Ư���� Ŭ���̾�Ʈ�������� ��ٸ����� �� �� �ִ�.  
		} catch (Exception e) { // ������ �߻��� ��� 
			e.printStackTrace();
			if (!serverSocket.isClosed()) { //���������� �����ִ� ���°� �ƴ϶��  stopServer()�� ���ؼ� ������ ������ �� �ְ� ���� 
				stopServer(); 
			}
			return;
		}

		// Ŭ���̾�Ʈ�� ������ ������ ��� ��ٸ��� ������ �Դϴ�.
		Runnable thread = new Runnable() {
			@Override
			public void run() { 
				while (true) {	//while�� ���ؼ� ����ؼ� ���ο� Ŭ���̾�Ʈ�� ������ �� �ְ� ������ش�. 
					try {
						Socket socket = serverSocket.accept(); //Ŭ���̾�Ʈ ���� 
						clients.add(new Client(socket)); //  clients <- �� Ŭ���̾�Ʈ�� ���Ӱ� ������ Ŭ���̾�Ʈ�� �߰��� �ִ� ��  
						System.out.println("[Ŭ���̾�Ʈ ����] " //�α� ǥ��  ��� 
								+ socket.getRemoteSocketAddress() + ": " // ������ Ŭ���̾�Ʈ�� �ּҸ� ��� 
								+ Thread.currentThread().getName()); //�ش� ������ ������ ��� �� �� �ֵ��� �Ѵ�. 
					} catch (Exception e) { //������ �߻��ϸ� �������Ͽ� ������ �߻��Ѱű⶧����  ������ �۵� ���� ��Ų��. 
						if (!serverSocket.isClosed()) {
							stopServer();
						}
						break; // �������´�. 
					}
				}
			}
		};
		threadPool = Executors.newCachedThreadPool(); // threadpool �ʱ�ȭ 
		threadPool.submit(thread); //Ŭ���̾�Ʈ�� ��޸��� �����带 ���� ���ֵ��� ó�����ؼ� ���������� ������ Ǯ�� ���� �ʱ�ȭ�� ���ְ� �� �ȿ� ù��° ������μ� Ŭ���̾�Ʈ�� ������ ��ٸ��� �����带 �־��� �� . 
	}

	// ������ �۵��� ������Ű�� �޼ҵ��Դϴ�.
	public void stopServer() {
		try {
			// ���� �۵� ���� ��� ���� �ݱ�
			Iterator<Client> iterator = clients.iterator(); //  iterator�� �̿��ؼ� ��� Ŭ���̾�Ʈ�� ���������� ������ �� �ֵ��� ������� �� 
			while (iterator.hasNext()) { //���� Ŭ���̾�Ʈ�� �����ϴ� �� 
				Client client = iterator.next(); // Ư���� Ŭ���̾�Ʈ�� ������ �ؼ� �� Ŭ���̾�Ʈ�� ������ �ݾƹ����� �� 
				client.socket.close();
				iterator.remove();
			}
			// ���� ���� ��ü �ݱ�
			if (serverSocket != null && !serverSocket.isClosed()) { //serverSocket���� null���� �ƴϰ� ������ �����ִ� �����̸�  �ش� ������ �ݾ��ִ� �� 
				serverSocket.close();
			}
			// ������ Ǯ �����ϱ�
			if (threadPool != null && !threadPool.isShutdown()) { //������ Ǯ ���� ��� �����嵵 �������� �ʱ� ������ therad pool�� shutdown() �������ν� �ڿ��� �Ҵ� ���� ���ش�. 
				threadPool.shutdown();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// UI�� �����ϰ�, ���������� ���α׷��� ���۽�Ű�� �޼ҵ��Դϴ�.
	@Override
	public void start(Stage primaryStage) {
		//BorderPane  - > setCenter(�߰���ġ ) , setBottom(�Ʒ���ġ ) ���� ���� 
		BorderPane root = new BorderPane(); //BorderPane  ��ü����  Ʋ (���̾ƿ� ) 
		root.setPadding(new Insets(5)); // �е� 5��ŭ ��

		TextArea textArea = new TextArea(); // �乮�� ���� �� �ִ� ���� 
		textArea.setEditable(false); // ��� ������ ��¸� �ϰ� ������ �Ұ����ϵ��� ����°� 
		textArea.setFont(new Font("�������", 15)); //textArea�ȿ� �۾�ü ���� 
		root.setCenter(textArea); //  textArea �� �߰�(setCenter)�� ���� �� �ֵ��� ���� 

		Button toggleButton = new Button("�����ϱ�"); // '�����ϱ�' ��ư ���� 
		toggleButton.setMaxWidth(Double.MAX_VALUE); // toggleButton ����ġ ��� ���� , ���۰� ����  -> ��ư�� ������ �ٲ�� �� 
		BorderPane.setMargin(toggleButton, new Insets(1, 0, 0, 0)); //BorderPane��  ���� ->Insets(top,right,bottom,left)�� �ش�.
		root.setBottom(toggleButton); // root �� �̿��ؼ� ��ư�� ���� �� �ֵ��� �Ѵ�.

		String IP = "127.0.0.1"; //�ڱ��ڽ��� ��ǻ�� �ּ� =���� �ּ�= ������ �ּ� ��� �Ѵ� .
		int port = 9876; // port��ȣ 
		

		//  toggleButton ��ư�� ������ ��  Action ó�� 
		toggleButton.setOnAction(event -> { 
			if (toggleButton.getText().equalsIgnoreCase("�����ϱ�")) { // "�����ϱ�" ��� ���ڿ��� �����ϰ� �ִ� ���¸�  ������ �������ָ�ȴ�. 
				startServer(IP, port);
				Platform.runLater(() -> { //  runLater�̿��ؼ�  GUI��Ҹ� ����� �� �ֵ��� ������ش�. 
					String message = String.format("[���� ����]\n", IP, port); //"��������" �޽��� ��� 
					textArea.appendText(message); //���Ŀ� textArea�� �޽����� ����� �� �ִ�. 
					toggleButton.setText("�����ϱ�"); // " �����ϱ�" ���� -> �����ϱ� �� ��ư �ٲ�. 
				});
			} else {
				stopServer();
				Platform.runLater(() -> {
					String message = String.format("[���� ����]\n", IP, port);
					textArea.appendText(message);
					toggleButton.setText("�����ϱ�");
				});
			}
		});

		Scene scene = new Scene(root, 400, 400); //ȭ�� ũ�� ���� Scene(Parent root,double width,double height)
		primaryStage.setTitle("[ ä�� ���� ]"); // ���α׷��� ���� ��� 
		primaryStage.setOnCloseRequest(event -> stopServer()); // ���α׷��� ���� ��ư�� �����ٸ�  stopServer()�� ������ �Ŀ� ���� 
		primaryStage.setScene(scene); //ȭ�鿡 ��� ���� 
		primaryStage.show(); //ȭ�鿡 ����� �� �ֵ��� ����
	} 

	// ���α׷��� �������Դϴ�.
	public static void main(String[] args) {
		launch(args);
	}
}