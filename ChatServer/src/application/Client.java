package application;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

// 한명의 클라이언트와 통신을 하기 위한 기능들을 정의

public class Client {

	Socket socket; // 소켓 생성 - 어떠한 컴퓨터와 네트워크 상에서 통신할 수 있기때문이다.

	// 생성자 -> 어떠한 변수에 초기화를 위해서 만들어 준다.
	public Client(Socket socket) {
		this.socket = socket;
		receive();
	}

	// 클라이언트로부터 메시지를 전달 받는 메소드입니다.
	public void receive() {
		Runnable thread = new Runnable() { // 일반적으로 하나의 스레드를 생성할 때 보통 Runnable을 많이 사용함

			@Override
			public void run() {
				try {
					while (true) { //반복적으로 클라이언트로부터 내용을 전달 받을 수 있도록 만든다. 
						InputStream in = socket.getInputStream(); // 어떠한 내용을 전달받을 수 있도록 InputStream객체를 생성 in 
						byte[] buffer = new byte[512]; // 한번에 512바이트 만큼 전달 받을 수 있도록 만든다.
						int length = in.read(buffer); //클라이언트로부터 내용을 전달받아서 이 버퍼에 담아주도록 하기 위해 만듬 
						while (length == -1)  //length는 담긴 메세지의 크기를 의미 이고 -1은 어떠한 메시지를 읽을 떄 오류가 발생했다면 알려주는 것
							throw new IOException();
						System.out.println("[메시지 수신 성공]" 
								+ socket.getRemoteSocketAddress() // getRemoteSocketAddress 현재 접속한 클라이언트의 ip주소와 같은 주소정보를 출력하도록 만듬  
								+ ": " + Thread.currentThread().getName()); //쓰레드의 고유한 정보  출력 , getName() 쓰레드의 이름 값을 출력 
						String message = new String(buffer, 0, length, "UTF-8"); //buffer에서 전달 받은 값을 받아서 message에 넣는다. , 한글도 가능하게  UTF-8
						
						// 다른 클라이언트들에도 내용 전송하기  
						for (Client client : Main.clients) { 
							client.send(message); // 클라이언트의 send 함수 
						}
					}
				} catch (Exception e) { // 오류가 발생했을 때 처리 
					try { //메시지를 수신 중에서 오류가 발생했을 때 
						System.out.println("[메시지 수신 오류] " + socket.getRemoteSocketAddress() + ": "
								+ Thread.currentThread().getName()); //해당 쓰레드 고유 이름을 출력하도록  만들어 줌
					} catch (Exception e2) {
						e2.printStackTrace();
					}
				}
			}
		};
		Main.threadPool.submit(thread); //메인함수에 있는 쓰레드풀에 submit을 해준다.즉 , 쓰레드 풀의  하나의 thread를 등록
	}

	// 클라이언트에게 메시지를 전송하는 메소드입니다.
	public void send(String message) {
		Runnable thread = new Runnable() {

			@Override
			public void run() {
				try {
					OutputStream out = socket.getOutputStream(); // OutputStream 메시지를 보내주고자 할 때 
					byte[] buffer = message.getBytes("UTF-8");
					out.write(buffer); // 오류가 발생하지 않았을 경우  : 버퍼에 담긴 내용을 서버에서 클라이언트로 전송해주겠다는 것 
					out.flush(); 		// 반드시 flush 까지해줘야지 성공적으로 전송했다는 것을 알려줄 수 있기때문..
				} catch (Exception e) {
					try {// 전달받는 도중 오류가 났다면 오류 처리 
						System.out.println("[메시지 송신 오류]" 
								+ socket.getRemoteSocketAddress() + ": "
								+ Thread.currentThread().getName());
						Main.clients.remove(Client.this); // 해당 클라이언트가 서버로부터 접속이 끊겼을 때, 정보를 처리한다. 즉, 클라이언트 배열에서 해당 오류가 생긴 클라이언트를 제거해주는 것 
						socket.close();
					} catch (Exception e2) {
						e2.printStackTrace();
					}
				}
			}
		};
		Main.threadPool.submit(thread);
	}
}