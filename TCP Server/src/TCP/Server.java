package TCP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

class Server {
	static final int port = 4321; // 상수값으로 port 번호 설정

	static String url = "jdbc:oracle:thin:@localhost:1521:oraknu";
	static String user = "kdhong";
	static String pass = "kdhong";
	static Connection conn = null;
	static PreparedStatement pstmt; 
	static Statement stmt = null;
	static String sql = null;
	static String query = null;
	int result;

	/*
	 * Thread를 통해 서버가 클라이언트가 죽어도 계속 동작하도록 함 **건드리지 말것.**
	 */

	public static void main(String[] args) {
		
		//jdbc jdbc_source = new jdbc(); 
		
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			System.out.println("드라이버 검색 성공!");

		} catch (ClassNotFoundException e) {
			System.err.println("error = " + e.getMessage());
			System.exit(1);
		}

		try {
			conn = DriverManager.getConnection(url, user, pass);
			conn.setAutoCommit(false);
			System.out.println("연결 및 setAutoCommit(false)설정!");

		} catch (SQLException e) {
			System.err.println("sql error = " + e.getMessage());
			System.exit(1);
		}

		

		try (ServerSocket server = new ServerSocket(port)) {

			while (true) {
				try {
					Socket client = server.accept();
					Thread task = new DaytimeThread(client);
					task.start();
				} catch (IOException e) {
				}
			}

		} catch (IOException e) {
			System.err.println("error : " + e.getMessage());
		}
	}

	Server() {
	}

	private static class DaytimeThread extends Thread {
		private Socket client; // 클라이언트 소켓
		private PrintWriter out; // 클라이언트에 데이터를 씀
		private BufferedReader in; // 클라이언트로부터 데이터를 읽음

		DaytimeThread(Socket client) {
			this.client = client;
		}

		public void run() {
			try {

				System.out.println("thread runs");
				out = new PrintWriter(client.getOutputStream(), true);
				in = new BufferedReader(new InputStreamReader(client.getInputStream()));

				// receive data from client
				String message = readData();

				// modify data
				String response = queryData(message);

				// send data to client
				sendData(response);

				out.close();
				in.close();

			} catch (Exception e) {
				System.err.println(e);
			} finally {
				try {
					client.close();
				} catch (IOException e) {
				}
			}
		}

		public String readData() throws IOException {
			
			String rcvMessage = in.readLine();
			System.out.println("Receive : " + rcvMessage);
			return rcvMessage;
		}

		public void sendData(String message) {
			out.println(message);
			System.out.println("Send : " + message);
		}

		@SuppressWarnings("unchecked")
		public String queryData(String message) throws Exception {

			int fcount=0;
			// 1. Parse String
			JSONParser parser = new JSONParser();
			JSONObject obj = (JSONObject) parser.parse(message);
			JSONObject result = new JSONObject();
			JSONArray list = new JSONArray();

			// 2. Get data
			String searchFor = (String) obj.get("searchFor");
			System.out.println("search for what? " + searchFor);


			// 3. Modify data
			if (searchFor.equals("Season")) {
				
				String searchForSeason = (String) obj.get("search");
				
				System.out.println(searchForSeason);	
					
				stmt = conn.createStatement();

				pstmt = conn.prepareStatement("select f.fid, fname, f_location, to_char(f_start_date, 'YYYY-MM-DD') f_start_date, to_char(f_finish_date, 'YYYY-MM-DD')  f_finish_date, oname, h_address, tourist_attraction "
						+ "from festival f, organization o, host_of h, region r "
						+ "where h.oid=o.oid and f.fid=h.fid and "
						+ "((f_start_date >= ? and f_finish_date< ? ) "
						+ "or (f_start_date >= ? and f_finish_date< ? ) "
						+ "or (f_start_date >= ? and f_finish_date< ?))");
						
						
				if(searchForSeason.equals("봄"))
				{
					pstmt.setString(1, "2015/03/01");
					pstmt.setString(2, "2015/06/01");
					pstmt.setString(3, "2016/03/01");
					pstmt.setString(4, "2016/06/01");
					pstmt.setString(5, "2017/03/01");
					pstmt.setString(6, "2017/06/01");
				}
				else if(searchForSeason.equals("여름"))
				{
					pstmt.setString(1, "2015/06/01");
					pstmt.setString(2, "2015/09/01");
					pstmt.setString(3, "2016/06/01");
					pstmt.setString(4, "2016/09/01");
					pstmt.setString(5, "2017/06/01");
					pstmt.setString(6, "2017/09/01");
					
				}
				else if(searchForSeason.equals("가을"))
				{
					pstmt.setString(1, "2015/09/01");
					pstmt.setString(2, "2015/12/01");
					pstmt.setString(3, "2016/09/01");
					pstmt.setString(4, "2016/12/01");
					pstmt.setString(5, "2017/09/01");
					pstmt.setString(6, "2017/12/01");
				}
				else if(searchForSeason.equals("겨울"))
				{
					pstmt.setString(1, "2015/01/01");
					pstmt.setString(2, "2015/03/01");
					pstmt.setString(3, "2015/12/01");
					pstmt.setString(4, "2016/03/01");
					pstmt.setString(5, "2016/12/01");
					pstmt.setString(6, "2017/03/01");
					
				}
				
				ResultSet rs = pstmt.executeQuery();
		
				
				
				while (rs.next() && fcount < 20) {
				
				
					fcount++;
					JSONObject temp = new JSONObject();
					int fid = rs.getInt("fid");
					String fname = rs.getString("fname");
					String start_date = rs.getString("f_start_date");
					String end_date = rs.getString("f_finish_date");
					String address = rs.getString("f_location");
					String oname = rs.getString("oname");
					String h_address = rs.getString("h_address");
					String tour = rs.getString("tourist_attraction");
					System.out.println("fid = " + fid + ",fname= " + fname + ", end_date= " + end_date+ ",location ="+ address);
					temp.put("fname", fname);
					temp.put("address", address);
					temp.put("start_date", start_date);
					temp.put("end_date", end_date);
					temp.put("oname", oname);
					temp.put("h_address", h_address);
					temp.put("tourist_attraction", tour);
					list.add(temp);
					
				}
				rs.close();
		

				
			} else if (searchFor.equals("Region")) {
		
				String searchForRegion = (String) obj.get("search");
				System.out.println(searchForRegion);		
				
				pstmt = conn.prepareStatement("select fname, f_location, to_char(f_start_date, 'YYYY-MM-DD') f_start_date, to_char(f_finish_date, 'YYYY-MM-DD')  f_finish_date, oname, h_address, tourist_attraction "
						+ "from festival f, organization o, region r, host_of h "
						+ "where h.oid=o.oid and f.fid=h.fid and r.rid=f.rid and r.state= ?"); 
				
				pstmt.setString(1, searchForRegion);
				
				ResultSet rs = pstmt.executeQuery();

			
				
				while (rs.next()) {
				
					
					JSONObject temp = new JSONObject();
					String fname = rs.getString("fname");
					String start_date = rs.getString("f_start_date");
					String end_date = rs.getString("f_finish_date");
					String address = rs.getString("f_location");
					String oname = rs.getString("oname");
					String h_address = rs.getString("h_address");
					
					System.out.println("fname= " + fname + ", start_date= " + start_date+ ",location ="+ address);
					temp.put("fname", fname);
					temp.put("address", address);
					temp.put("start_date", start_date);
					temp.put("end_date", end_date);
					temp.put("oname", oname);
					temp.put("h_address", h_address);
					list.add(temp);
					
				}
				rs.close();
			
				
			} else if (searchFor.equals("Search")){
				
			
				pstmt = conn.prepareStatement("select fname, f_location, to_char(f_start_date, 'YYYY-MM-DD') f_start_date, to_char(f_finish_date, 'YYYY-MM-DD') f_finish_date, oname, h_address, tourist_attraction "
						+ "from festival f, organization o, host_of h, region r "
						+ "where h.oid=o.oid and f.fid=h.fid and "
						+ "f.rid=r.rid and "
						+ "fname like ?");
						
				
				String searchWord = (String) obj.get("search");
				pstmt.setString(1, "%"+searchWord+"%");
				
				ResultSet rs = pstmt.executeQuery();

			
			
				while (rs.next() ) {
					
					
					JSONObject temp = new JSONObject();
					String fname = rs.getString("fname");
					String start_date = rs.getString("f_start_date");
					String end_date = rs.getString("f_finish_date");
					String address = rs.getString("f_location");
					String oname = rs.getString("oname");
					String h_address = rs.getString("h_address");
					String tour = rs.getString("tourist_attraction");
					System.out.println("fname= " + fname + ", start_date= " + start_date+ ",location ="+ address);
					temp.put("fname", fname);
					temp.put("address", address);
					temp.put("start_date", start_date);
					temp.put("end_date", end_date);
					temp.put("oname", oname);
					temp.put("h_address", h_address);
					temp.put("tourist_attraction", tour);
					list.add(temp);
					
				}
				rs.close();
			
			}		
			
			result.put("results", list);

			String[] headString = {
					"축제명","개최장소","시작날짜","종료날짜 ", "주최기관", "주최기관사이트"
			};
			
			JSONArray tableHeads = new JSONArray();
			for (String a : headString) {
				JSONObject tableHead = new JSONObject();
				tableHead.put("name", a);
				tableHeads.add(tableHead);
			}
			
			if (searchFor.equals("Season") || searchFor.equals("Search") ) {
				JSONObject tableHead = new JSONObject();
				tableHead.put("name", "관광명소");
				tableHeads.add(tableHead);
			}
			result.put("tablehead", tableHeads);
			
			result.put("link_name", "https://www.google.co.kr/");
			
			// 4. return JSON String
			return result.toJSONString();
		}

	}
}