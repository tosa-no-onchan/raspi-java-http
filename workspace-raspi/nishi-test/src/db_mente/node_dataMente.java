package db_mente;

import host_app_lib.DBManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class node_dataMente {

	public static void main(String[] args) throws ClassNotFoundException {
		// TODO 自動生成されたメソッド・スタブ
		Connection con = null;
		Statement smt = null;
		ResultSet rs = null;
		DBManager dbm= new DBManager("data/node_data.db");
		System.out.println("node_dataMente: #1 start ");
		try {
			con = dbm.createConnection();
			smt = con.createStatement();

			smt.executeUpdate("DROP TABLE IF EXISTS DRAGONTB");
			smt.executeUpdate("DROP TABLE IF EXISTS node_tmp");
			smt.executeUpdate("CREATE TABLE node_tmp(host TEXT,dtime DATETIME,tmp REAL);");

			//con.commit();
			System.out.println("node_dataMente: #9 end");

		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		finally {
			try {
				con.close();
				smt.close();
				//rs.close();
			} catch (SQLException e) {
			// TODO: handle exception
				e.printStackTrace();
			}
		}
	}

}
