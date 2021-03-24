//package com.load.pgm.socket;

import java.io.DataInputStream;
import java.io.InputStream;
import java.net.Socket;

public class SocketClientExample {
	public static void main(String[] args) throws Exception{
		Socket sk = new Socket("127.0.0.1", 8889);
		System.out.println("서버와 접속되었습니다");
		
		InputStream inputStream = sk.getInputStream();
		DataInputStream dataInputStream = new DataInputStream(inputStream);
		
		String str = dataInputStream.readUTF();
		int number = dataInputStream.readInt();
		System.out.println("서버에서 전송된 문자 : " +str);
		System.out.println("서버에서 전송된 숫자 : " +number);
		
		dataInputStream.close();
		inputStream.close();
		sk.close();
	}
}
