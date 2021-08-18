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

	public static ExecutorService threadPool; // ExecutorService threadPool -> 다양한 클라이언트가 접속 했을 때 쓰레드들을 효과적으로 관리
												// 갑작스럽게 클라이언트의 수가 폭증하더라도 쓰레드들의 숫자의 제안이 있기 때문에 서버에 성능 저하를 방지할 수 있다.
	public static Vector<Client> clients = new Vector<Client>(); // Vector ->

	ServerSocket serverSocket;

	// 서버를 구동시켜서 클라이언트의 연결을 기다리는 메소드입니다.
	public void startServer(String IP, int port) { // IP , Port를 열어 클라이언트랑 통신
		try {
			serverSocket = new ServerSocket(); //서버 소켓 객체 생성 
			serverSocket.bind(new InetSocketAddress(IP, port)); // bind를 사용하여 서버컴퓨터 역할을 수행하는 그 컴퓨터가 자신의 ip주소 port번호로 특정한 클라이언트의접속을 기다리도록 할 수 있다.  
		} catch (Exception e) { // 오류가 발생할 경우 
			e.printStackTrace();
			if (!serverSocket.isClosed()) { //서버소켓이 닫혀있는 상태가 아니라면  stopServer()를 통해서 서버를 종료할 수 있게 만듬 
				stopServer(); 
			}
			return;
		}

		// 클라이언트가 접속할 때까지 계속 기다리는 쓰레드 입니다.
		Runnable thread = new Runnable() {
			@Override
			public void run() { 
				while (true) {	//while을 통해서 계속해서 새로운 클라이언트가 접속할 수 있게 만들어준다. 
					try {
						Socket socket = serverSocket.accept(); //클라이언트 접속 
						clients.add(new Client(socket)); //  clients <- 이 클라이언트에 새롭게 접속한 클라이언트를 추가해 주는 것  
						System.out.println("[클라이언트 접속] " //로그 표시  출력 
								+ socket.getRemoteSocketAddress() + ": " // 접속한 클라이언트에 주소를 출력 
								+ Thread.currentThread().getName()); //해당 쓰레드 정보를 출력 할 수 있도록 한다. 
					} catch (Exception e) { //오류가 발생하면 서버소켓에 문제가 발생한거기때문에  서버를 작동 중지 시킨다. 
						if (!serverSocket.isClosed()) {
							stopServer();
						}
						break; // 빠져나온다. 
					}
				}
			}
		};
		threadPool = Executors.newCachedThreadPool(); // threadpool 초기화 
		threadPool.submit(thread); //클라이언트를 기달리는 쓰레드를 담을 수있도록 처리를해서 성공적으로 쓰레드 풀을 먼저 초기화를 해주고 그 안에 첫번째 쓰레드로서 클라이언트의 접속을 기다리는 쓰레드를 넣어준 것 . 
	}

	// 서버의 작동을 중지시키는 메소드입니다.
	public void stopServer() {
		try {
			// 현재 작동 중인 모든 소켓 닫기
			Iterator<Client> iterator = clients.iterator(); //  iterator를 이용해서 모든 클라이언트의 개별적으로 접근할 수 있도록 만들어준 것 
			while (iterator.hasNext()) { //각각 클라이언트에 접근하는 것 
				Client client = iterator.next(); // 특정한 클라이언트의 접근을 해서 그 클라이언트의 소켓을 닫아버리는 것 
				client.socket.close();
				iterator.remove();
			}
			// 서버 소켓 객체 닫기
			if (serverSocket != null && !serverSocket.isClosed()) { //serverSocket값이 null값이 아니고 소켓이 열려있는 상태이면  해당 소켓을 닫아주는 것 
				serverSocket.close();
			}
			// 쓰레드 풀 종료하기
			if (threadPool != null && !threadPool.isShutdown()) { //쓰레드 풀 또한 어떠한 쓰레드도 존재하지 않기 때문에 therad pool을 shutdown() 해줌으로써 자원을 할당 해제 해준다. 
				threadPool.shutdown();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// UI를 생성하고, 실질적으로 프로그램을 동작시키는 메소드입니다.
	@Override
	public void start(Stage primaryStage) {
		//BorderPane  - > setCenter(중간위치 ) , setBottom(아래위치 ) 설정 가능 
		BorderPane root = new BorderPane(); //BorderPane  전체적인  틀 (레이아웃 ) 
		root.setPadding(new Insets(5)); // 패딩 5만큼 줌

		TextArea textArea = new TextArea(); // 긴문장 담을 수 있는 공간 
		textArea.setEditable(false); // 어떠한 문장을 출력만 하고 수정이 불가능하도록 만드는것 
		textArea.setFont(new Font("나눔고딕", 15)); //textArea안에 글씨체 설정 
		root.setCenter(textArea); //  textArea 를 중간(setCenter)에 담을 수 있도록 설정 

		Button toggleButton = new Button("시작하기"); // '시작하기' 버튼 제작 
		toggleButton.setMaxWidth(Double.MAX_VALUE); // toggleButton 스위치 라고 생각 , 시작과 종료  -> 버튼의 내용이 바뀌는 식 
		BorderPane.setMargin(toggleButton, new Insets(1, 0, 0, 0)); //BorderPane에  마진 ->Insets(top,right,bottom,left)을 준다.
		root.setBottom(toggleButton); // root 를 이용해서 버튼을 담을 수 있도록 한다.

		String IP = "127.0.0.1"; //자기자신의 컴퓨터 주소 =로컬 주소= 루프백 주소 라고도 한다 .
		int port = 9876; // port번호 
		

		//  toggleButton 버튼을 눌렀을 때  Action 처리 
		toggleButton.setOnAction(event -> { 
			if (toggleButton.getText().equalsIgnoreCase("시작하기")) { // "시작하기" 라는 문자열을 포함하고 있는 상태면  서버를 시작해주면된다. 
				startServer(IP, port);
				Platform.runLater(() -> { //  runLater이용해서  GUI요소를 출력할 수 있도록 만들어준다. 
					String message = String.format("[서버 시작]\n", IP, port); //"서버시작" 메시지 출력 
					textArea.appendText(message); //이후에 textArea에 메시지를 출력할 수 있다. 
					toggleButton.setText("종료하기"); // " 시작하기" 에서 -> 종료하기 로 버튼 바뀜. 
				});
			} else {
				stopServer();
				Platform.runLater(() -> {
					String message = String.format("[서버 종료]\n", IP, port);
					textArea.appendText(message);
					toggleButton.setText("시작하기");
				});
			}
		});

		Scene scene = new Scene(root, 400, 400); //화면 크기 조정 Scene(Parent root,double width,double height)
		primaryStage.setTitle("[ 채팅 서버 ]"); // 프로그램의 정보 출력 
		primaryStage.setOnCloseRequest(event -> stopServer()); // 프로그램의 종료 버튼을 누렀다면  stopServer()를 시행한 후에 종료 
		primaryStage.setScene(scene); //화면에 출력 설정 
		primaryStage.show(); //화면에 출력할 수 있도록 설정
	} 

	// 프로그램의 진입접입니다.
	public static void main(String[] args) {
		launch(args);
	}
}