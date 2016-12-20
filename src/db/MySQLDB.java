package db;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


public class MySQLDB implements DATA_BASE {
	private Connection sqlCon = null;
	
	
	  public MySQLDB() {
			this(DBUtil.URL);
		}

		public MySQLDB(String url) {
			try {
				
				// initialize data base	
				Class.forName("com.mysql.jdbc.Driver").newInstance();
				sqlCon = DriverManager.getConnection(url);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		   
		@Override
		public void close() {
			// TODO Auto-generated method stub
			if (sqlCon != null) {
				try {
					sqlCon.close();
				} catch (Exception e) { /* ignored */
				}
			}
		}
		
		
		
	@Override
	public Boolean verifyLogin(String userId, String password) {
		try {
			
			String sqlCmd = "SELECT user_id from users WHERE user_id=? and password=?";
			PreparedStatement pstmt = sqlCon.prepareStatement( sqlCmd );
			pstmt.setString( 1, userId); 
			pstmt.setString( 2, password); 
			
			ResultSet rs = pstmt.executeQuery();
			
			//check the user id is in the data base or not
			if (rs.next()) {
				return true;
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return false;
	}


	@Override
	public String getFirstLastName(String userId) {
		String name = "";
		try {
			if (sqlCon != null) {
				String sqlCmd = "SELECT first_name, last_name from users WHERE user_id = ?";
				PreparedStatement statement = sqlCon.prepareStatement(sqlCmd);
				statement.setString(1, userId);
				
				ResultSet rs = statement.executeQuery();
				if (rs.next()) {
					name += rs.getString("first_name") + " "
							+ rs.getString("last_name");
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return name;
	}
	
	
	public static void main(String[] args) {
		
		/*CREATE TABLE users (user_id VARCHAR(255) NOT NULL, 
		password VARCHAR(255) NOT NULL, 
		first_name VARCHAR(255), 
		last_name VARCHAR(255), 
		PRIMARY KEY ( user_id ));*/
		
		DATA_BASE sqlTest = new MySQLDB();
		System.out.print(sqlTest.verifyLogin("thomas","1234"));
	}
	
}