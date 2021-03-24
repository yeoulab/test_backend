//package com.load.pgm.socket;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketServerExample {
	public static void main(String[] args) throws IOException{
		
		int port = 8889;
		
		int number = Integer.parseInt(args[0]);
		String str = new String(args[1]);
		
		ServerSocket serverSocket = new ServerSocket(port);
		
		System.out.println("접속 대기중");
		
		while(true) {
			Socket socket = serverSocket.accept();
			
			System.out.println("사용자가 접속했습니다.");
			System.out.println("Client IP : "+socket.getInetAddress());
			
			OutputStream outputStream = socket.getOutputStream();
			DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
			
			dataOutputStream.writeUTF(str);
			dataOutputStream.writeInt(number);
			dataOutputStream.flush();
			
			dataOutputStream.close();
			socket.close();
		}
	}

}
