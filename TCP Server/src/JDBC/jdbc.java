package JDBC;

import java.sql.*;

import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;

public class jdbc {
	private String execell_impormation = "C:\\NewInformation.xls";
	private String url = "jdbc:oracle:thin:@localhost:1521:oraknu";
	private String user = "kdhong";
	private String pass = "kdhong";
	private Connection conn = null;
	private String sql = null;
	private HSSFRow row;
	private HSSFCell cell;

	public jdbc() {

		

	}

	public void connection() {
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			System.out.println("드라이버 검색 성공!");
		} catch (ClassNotFoundException e) {
			System.err.println("error = " + e.getMessage());
			System.exit(1);
		}
		try {
			System.out.println("연결");
			conn = DriverManager.getConnection(url, user, pass);
		} catch (SQLException e) {
			System.err.println("sql error = " + e.getMessage());
			System.exit(1);
		}
		try {
			conn.setAutoCommit(false);
			System.out.println("statement 생성.");
		} catch (Exception e) {
			System.err.println("sql error = " + e.getMessage());
		}
	}

	public void connection_close() {

		try {

			conn.setAutoCommit(true);
			conn.close();
		} catch (Exception e) {
			System.err.println("sql error = " + e.getMessage());
		}
	}

	public void create_table() {
		try {
			// region 테이블
			Statement stmt = conn.createStatement();
			sql = "create table region(" + "rid			int," + "state			varchar2(20),"
					+ "tourist_attraction	varchar2(100)," + "primary key(rid)," + "unique(state)" + ")";

			stmt.executeUpdate(sql);
			System.out.println("region create\n");
			
			// organization 테이블
			sql = "create table organization(" + "oid int," + "oname   varchar2(100)," + "h_address   varchar2(70),"
					+ "phonenumber varchar2(20)," + "primary key(oid)," + "unique(oname)" + ")";
			stmt.executeUpdate(sql);
			System.out.println("organization create\n");

			// festival 테이블 생성
			sql = "create table festival(" + "fid		int," + "fname		varchar2(100)," + "f_start_date 	date,"
					+ "f_finish_date 	date," + "f_location	varchar2(50)," + "rid		int," + "primary key(fid),"
					+ "foreign key(rid) references region(rid)" + ")";
			stmt.executeUpdate(sql);
			System.out.println("region create\n");
			
			// host_of 테이블 생성
			sql = "create table host_of(" + "fid	int," + "oid	int," + "foreign key(fid) references festival(fid),"
					+ "foreign key(oid) references organization(oid)" + ")";
			stmt.executeUpdate(sql);
			System.out.println("host_of creat\n");

			conn.commit();
			stmt.close();
		} catch (Exception e) {
			System.err.println("sql(create) error = " + e.getMessage());
		}
	}

	public void insert_table() {

		String f_name = null;
		int fid = 0;
		String f_start_date = null;
		String f_finish_date = null;
		String f_location = null;
		int rid = 0;
		String state = null;
		String tourist_attraction = null;
		int oid = 0;
		String oname = null;
		String phonenumber = null;
		String h_address = null;

		int sc;
		try {
			FileInputStream inputStream = new FileInputStream(new File(execell_impormation));
			POIFSFileSystem fileSystem = new POIFSFileSystem(inputStream);
			HSSFWorkbook workbook = new HSSFWorkbook(fileSystem);
			HSSFSheet sheet;
			int row_length, row_index;
			int sheetcount = workbook.getNumberOfSheets();
			Statement stmt = conn.createStatement();
			for (sc = 0; sc < sheetcount; sc++) {
				sheet = workbook.getSheetAt(sc);
				row_length = sheet.getPhysicalNumberOfRows();
				for (row_index = 1; row_index < row_length; row_index++) {
					row = sheet.getRow(row_index);
					if (row != null) {
						if (sc == 0) {
							for (int cell_index = 0; cell_index < 4; cell_index++) {
								cell = row.getCell(cell_index);
								if (cell != null) {
									switch (cell_index) {
									case 0:
										oname = cell.getStringCellValue();
										break;
									case 1:
										oid = (int) cell.getNumericCellValue();
										break;
									case 2:
										h_address = cell.getStringCellValue();
										break;
									case 3:
										phonenumber = cell.getStringCellValue();
										break;
									}
								} else {
									phonenumber = "010-3564-3258";
								}
							}

							try {
								if (row_index == 433) {

									/*
									 * System.out.println("error occurred"); throw new Exception("error");
									 */
								} else {
									// insert 문
									sql = "insert into organization values(" + oid + ",'" + oname + "','" + h_address
											+ "','" + phonenumber + "')";
									stmt.executeUpdate(sql);

									conn.commit();
								}
							} catch (Exception e) {
								System.err.println("sql(organiztion) error = " + e.getMessage());
							}
						} else if (sc == 1) {

							for (int cell_index = 0; cell_index < 3; cell_index++) {
								cell = row.getCell(cell_index);
								if (cell != null) {
									switch (cell_index) {
									case 0:
										state = cell.getStringCellValue();
										break;
									case 1:
										rid = (int) cell.getNumericCellValue();
										break;
									case 2:
										tourist_attraction = cell.getStringCellValue();
										break;
									}
								}
							}

							try {

								// insert 문
								sql = "insert into region values(" + rid + ",'" + state + "','" + tourist_attraction
										+ "')";
								stmt.executeUpdate(sql);

								conn.commit();

							} catch (Exception e) {
								System.err.println("sql(region) error = " + e.getMessage());
							}
						}

						else if (sc == 2) {
							for (int cell_index = 0; cell_index < 6; cell_index++) {
								cell = row.getCell(cell_index);
								if (cell != null) {
									switch (cell_index) {
									case 0:
										f_name = cell.getStringCellValue();
										break;
									case 1:
										fid = (int) cell.getNumericCellValue();
										break;
									case 2:
										f_start_date = cell.getStringCellValue();
										break;
									case 3:
										f_finish_date = cell.getStringCellValue();
										break;
									case 4:
										rid = (int) cell.getNumericCellValue();
										break;
									case 5:
										f_location = cell.getStringCellValue();
										break;
									}
								}
							}

							try {

								// insert 문
								sql = "insert into festival values(" + fid + ",'" + f_name + "','" + f_start_date
										+ "','" + f_finish_date + "','" + f_location + "'," + rid + ")";

								stmt.executeUpdate(sql);
								conn.commit();

							} catch (Exception e) {
								System.err.println("sql(insert_festival)error = " + e.getMessage());
							}
						} else if (sc == 3) {

							for (int cell_index = 0; cell_index < 2; cell_index++) {
								cell = row.getCell(cell_index);
								if (cell != null) {
									switch (cell_index) {
									case 0:
										fid = (int) cell.getNumericCellValue();
										break;
									case 1:
										oid = (int) cell.getNumericCellValue();
										break;
									}
								}
							}
							try {

								// insert 문
								sql = "insert into host_of values(" + fid + ",'" + oid + "')";
								stmt.executeUpdate(sql);

								conn.commit();

							} catch (Exception e) {
								System.err.println("sql(hostof) error = " + e.getMessage());
							}
						}

					}
				}
			}
			workbook.close();
			stmt.close();
		} catch (Exception e) {
			System.err.println("sql error = " + e.getMessage());
		}
	}

	public static void main(String[] args) {
		jdbc atb = new jdbc();
		
		try {
			atb.connection();
			atb.create_table();
			atb.insert_table();
			atb.connection_close();
		} catch (Exception e) {
			System.err.println("sql error = " + e.getMessage());
			System.exit(1);
		}
	}
}
