package TCP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

class Server {
	static final int port = 5000; // 상수값으로 port 번호 설정
	
	public static void main(String[] args) {
		try(ServerSocket server = new ServerSocket(port)){
			while(true){
				try{
					Socket client = server.accept();
					Thread task = new DaytimeThread(client);
					task.start();
				}catch(IOException e){}
			}
		}catch(IOException e){
			System.err.println("error : " + e.getMessage());
		}
	}

	Server() {}
	
	private static class DaytimeThread extends Thread{
		private Socket client;
		
		DaytimeThread(Socket client){
			this.client = client;
		}
		
		public void run(){
			try{
				PrintWriter out = new PrintWriter(client.getOutputStream(), true); 
				BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

				// receive data from client
				String rcvMessage = in.readLine();
				System.out.println("Receive : " + rcvMessage);
				
				// send data to client
				out.println(rcvMessage);
				System.out.println("Send : " + rcvMessage);
				
				out.close();
				in.close();
			}catch(IOException e){
				System.err.println(e);
			}finally{
				try{
					client.close();
				}catch(IOException e){}
			}
		}
	}
}