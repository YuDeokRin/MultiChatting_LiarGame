package application;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

// �Ѹ��� Ŭ���̾�Ʈ�� ����� �ϱ� ���� ��ɵ��� ����

public class Client {

	Socket socket; // ���� ���� - ��� ��ǻ�Ϳ� ��Ʈ��ũ �󿡼� ����� �� �ֱ⶧���̴�.

	// ������ -> ��� ������ �ʱ�ȭ�� ���ؼ� ����� �ش�.
	public Client(Socket socket) {
		this.socket = socket;
		receive();
	}

	// Ŭ���̾�Ʈ�κ��� �޽����� ���� �޴� �޼ҵ��Դϴ�.
	public void receive() {
		Runnable thread = new Runnable() { // �Ϲ������� �ϳ��� �����带 ������ �� ���� Runnable�� ���� �����

			@Override
			public void run() {
				try {
					while (true) { //�ݺ������� Ŭ���̾�Ʈ�κ��� ������ ���� ���� �� �ֵ��� �����. 
						InputStream in = socket.getInputStream(); // ��� ������ ���޹��� �� �ֵ��� InputStream��ü�� ���� in 
						byte[] buffer = new byte[512]; // �ѹ��� 512����Ʈ ��ŭ ���� ���� �� �ֵ��� �����.
						int length = in.read(buffer); //Ŭ���̾�Ʈ�κ��� ������ ���޹޾Ƽ� �� ���ۿ� ����ֵ��� �ϱ� ���� ���� 
						while (length == -1)  //length�� ��� �޼����� ũ�⸦ �ǹ� �̰� -1�� ��� �޽����� ���� �� ������ �߻��ߴٸ� �˷��ִ� ��
							throw new IOException();
						System.out.println("[�޽��� ���� ����]" 
								+ socket.getRemoteSocketAddress() // getRemoteSocketAddress ���� ������ Ŭ���̾�Ʈ�� ip�ּҿ� ���� �ּ������� ����ϵ��� ����  
								+ ": " + Thread.currentThread().getName()); //�������� ������ ����  ��� , getName() �������� �̸� ���� ��� 
						String message = new String(buffer, 0, length, "UTF-8"); //buffer���� ���� ���� ���� �޾Ƽ� message�� �ִ´�. , �ѱ۵� �����ϰ�  UTF-8
						
						// �ٸ� Ŭ���̾�Ʈ�鿡�� ���� �����ϱ�  
						for (Client client : Main.clients) { 
							client.send(message); // Ŭ���̾�Ʈ�� send �Լ� 
						}
					}
				} catch (Exception e) { // ������ �߻����� �� ó�� 
					try { //�޽����� ���� �߿��� ������ �߻����� �� 
						System.out.println("[�޽��� ���� ����] " + socket.getRemoteSocketAddress() + ": "
								+ Thread.currentThread().getName()); //�ش� ������ ���� �̸��� ����ϵ���  ����� ��
					} catch (Exception e2) {
						e2.printStackTrace();
					}
				}
			}
		};
		Main.threadPool.submit(thread); //�����Լ��� �ִ� ������Ǯ�� submit�� ���ش�.�� , ������ Ǯ��  �ϳ��� thread�� ���
	}

	// Ŭ���̾�Ʈ���� �޽����� �����ϴ� �޼ҵ��Դϴ�.
	public void send(String message) {
		Runnable thread = new Runnable() {

			@Override
			public void run() {
				try {
					OutputStream out = socket.getOutputStream(); // OutputStream �޽����� �����ְ��� �� �� 
					byte[] buffer = message.getBytes("UTF-8");
					out.write(buffer); // ������ �߻����� �ʾ��� ���  : ���ۿ� ��� ������ �������� Ŭ���̾�Ʈ�� �������ְڴٴ� �� 
					out.flush(); 		// �ݵ�� flush ����������� ���������� �����ߴٴ� ���� �˷��� �� �ֱ⶧��..
				} catch (Exception e) {
					try {// ���޹޴� ���� ������ ���ٸ� ���� ó�� 
						System.out.println("[�޽��� �۽� ����]" 
								+ socket.getRemoteSocketAddress() + ": "
								+ Thread.currentThread().getName());
						Main.clients.remove(Client.this); // �ش� Ŭ���̾�Ʈ�� �����κ��� ������ ������ ��, ������ ó���Ѵ�. ��, Ŭ���̾�Ʈ �迭���� �ش� ������ ���� Ŭ���̾�Ʈ�� �������ִ� �� 
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