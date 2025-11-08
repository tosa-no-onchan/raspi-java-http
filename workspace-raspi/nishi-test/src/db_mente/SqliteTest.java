package db_mente;

import host_app_lib.DBManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SqliteTest {

	// TODO 自動生成されたメソッド・スタブ
	public static void main(String[] args) throws ClassNotFoundException {

		Connection con = null;
		Statement smt = null;
		ResultSet rs = null;

		try {
			con = new DBManager("data/node_data.db").createConnection();
			smt = con.createStatement();

			smt.executeUpdate("DROP TABLE IF EXISTS DRAGONTB");
			smt.executeUpdate("CREATE TABLE DRAGONTB(NAME TEXT, SENTOURYOKU INTEGER);");
			PreparedStatement prep = con
					.prepareStatement("INSERT INTO DRAGONTB VALUES (?, ?);");

			prep.setString(1, "GOKU");
			prep.setInt(2, 5000000);
			prep.addBatch();
			prep.setString(1, "BEJITA");
			prep.setInt(2, 4000000);
			prep.addBatch();
			prep.setString(1, "KURIRIN");
			prep.setInt(2, 100000);
			prep.addBatch();

			con.setAutoCommit(false);
			prep.executeBatch();

			con.commit();

			rs = smt.executeQuery("SELECT * FROM DRAGONTB;");
			while (rs.next()) {
				System.out.println("NAME = " + rs.getString("NAME"));
				System.out.println("SENTOURYOKU = " + rs.getInt("SENTOURYOKU"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				con.close();
				smt.close();
				rs.close();
			} catch (SQLException e) {
			// TODO: handle exception
				e.printStackTrace();
			}
		}
	}

}
