package host_app_lib;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBManager {
	String file;
	public DBManager(String file) throws ClassNotFoundException{
	// TODO 自動生成されたコンストラクター・スタブ
		this.file = file;
		Class.forName("org.sqlite.JDBC");
	}
	public Connection createConnection(){
		try {
			Connection con = DriverManager.getConnection("jdbc:sqlite:" + file);
			return con;

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	public void update(Connection con,String sql) throws SQLException{
		Statement stmt = con.createStatement();
		//String sql = "UPDATE QEOL.TOKMSP SET TKADR1 = '東京都中央区' WHERE TKBANG = '01010'";

		// SQL文の実行 (更新)
		int num = stmt.executeUpdate(sql);
		//System.out.println("更新件数 :" + num);
		// SQLコンテナのクローズ
		stmt.close();		
	}
	
	public void insert(Connection con,String sql) throws SQLException{
		// SQLコンテナの作成
		Statement stmt = con.createStatement();
		//String sql = "INSERT INTO QEOL.TOKMSP(TKBANG,TKNAKJ,TKADR1) VALUES('00001','テスト興業','神奈川県茅ヶ崎市')";

		// SQL文の実行 (挿入)
		int num = stmt.executeUpdate(sql);
		//System.out.println("登録件数 : " + num);

		// SQLコンテナのクローズ
		stmt.close();		
	}
}
