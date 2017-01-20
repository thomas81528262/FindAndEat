package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import yelpModel.Restaurant;
import yelp.YelpAPI;

public class MySQLDB implements DATA_BASE {
	private Connection sqlCon = null;


	private Boolean getLastBookedMark(String userId, String businessId) {
		String query = "SELECT is_booked FROM book_mark where user_id=? and business_id=? "
				+ "ORDER BY book_mark.book_mark_time DESC "
				+ "limit 1;";
		try {
			PreparedStatement statement = sqlCon.prepareStatement(query);
			statement.setString(1,  userId);
			statement.setString(2, businessId);
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				
				return rs.getString("is_booked") != null;
			}
			
			System.out.println("can not find the restaurant");
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	
	}
	
	
	
	@Override
	public void bookMarkRestaurants(String userId, List<String> businessIds) {
		
	
		
		
		try {
			
			String query = "INSERT INTO book_mark (user_id, business_id, is_booked) VALUES (?, ?, 1)";
			PreparedStatement statement = sqlCon.prepareStatement(query);
			for (String businessId : businessIds) {
				Boolean result = getLastBookedMark(userId, businessId);
				if (result == null || result == false) {
					statement.setString(1,  userId);
					statement.setString(2, businessId);
					System.out.println(statement);
					statement.execute();
				} 
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	
	
	@Override
	public JSONArray searchRestaurants(String userId, double lat, double lon, String Sterm) {
		try {

			YelpAPI yApi = new YelpAPI();
			JSONObject response = new JSONObject(yApi.searchForBusinessesByLocation(lat, lon, Sterm));
			JSONArray array = (JSONArray) response.get("businesses");

			List<JSONObject> list = new ArrayList<JSONObject>();
			

			for (int i = 0; i < array.length(); i++) {
				JSONObject object = array.getJSONObject(i);
				Restaurant restaurant = new Restaurant(object);
				String businessId = restaurant.getBusinessId();
				String name = restaurant.getName();
				String categories = restaurant.getCategories();
				String city = restaurant.getCity();
				String state = restaurant.getState();
				String fullAddress = restaurant.getFullAddress();
				double stars = restaurant.getStars();
				double latitude = restaurant.getLatitude();
				double longitude = restaurant.getLongitude();
				String imageUrl = restaurant.getImageUrl();
				String url = restaurant.getUrl();
				JSONObject obj = restaurant.toJSONObject();
				
				System.out.println(businessId);
				Boolean isBooked = getLastBookedMark(userId, businessId);
				
				if(isBooked == null || isBooked == false) {
					obj.put("is_booked", false);
				} else {
					obj.put("is_booked", true);
				}
				
				// if the primary key is alreay in the data base just update it
				String insertCom = "INSERT INTO restaurants VALUES ";
				String updateCom = "ON DUPLICATE KEY UPDATE ";
				String sql = insertCom + "(?,?,?,?,?,?,?,?,?,?,?)" + updateCom + "name=?," + "categories=?," + "city=?,"
						+ "state=?," + "stars=?," + "full_address=?," + "latitude=?," + "longitude=?," + "image_url=?,"
						+ "url=?";

				// String sqlUpdate= "ON DUPLICATE KEY UPDATE"";

				PreparedStatement statement = sqlCon.prepareStatement(sql);
				statement.setString(1, businessId);
				statement.setString(2, name);
				statement.setString(3, categories);
				statement.setString(4, city);
				statement.setString(5, state);
				statement.setDouble(6, stars);
				statement.setString(7, fullAddress);
				statement.setDouble(8, latitude);
				statement.setDouble(9, longitude);
				statement.setString(10, imageUrl);
				statement.setString(11, url);

				statement.setString(12, name);
				statement.setString(13, categories);
				statement.setString(14, city);
				statement.setString(15, state);
				statement.setDouble(16, stars);
				statement.setString(17, fullAddress);
				statement.setDouble(18, latitude);
				statement.setDouble(19, longitude);
				statement.setString(20, imageUrl);
				statement.setString(21, url);

				statement.execute();
				
				//finally get the result > <
				list.add(obj);
				
			}

			return new JSONArray(list);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		return null;
	}

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
			} catch (Exception e) { 
				// TODO
				e.printStackTrace();
			}
		}
	}

	@Override
	public Boolean verifyLogin(String userId, String password) {
		try {

			String sqlCmd = "SELECT user_id from users WHERE user_id=? and password=?";
			PreparedStatement pstmt = sqlCon.prepareStatement(sqlCmd);
			pstmt.setString(1, userId);
			pstmt.setString(2, password);

			ResultSet rs = pstmt.executeQuery();

			// check the user id is in the data base or not
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
					name += rs.getString("first_name") + " " + rs.getString("last_name");
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return name;
	}

	public static void main(String[] args) {

		MySQLDB sqlTest = new MySQLDB();
		
		/*
		CREATE TABLE book_mark
			 (book_mark_id bigint(20) unsigned NOT NULL AUTO_INCREMENT,
			 user_id VARCHAR(255) NOT NULL ,
			 business_id VARCHAR(255) NOT NULL,
			 book_mark_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
			 PRIMARY KEY (book_mark_id),
			 FOREIGN KEY (business_id) REFERENCES restaurants(business_id),
			 FOREIGN KEY (user_id) REFERENCES users(user_id));
		*/
		
		
		List<String> testList = new ArrayList<>();
		testList.add("asian-box-mountain-view");
		testList.add("bowl-of-heaven-mountain-view-2");
		testList.add("eureka-mountain-view-2");
		testList.add("vaso-azzurro-ristorante-mountain-view");
		testList.add("srasa-kitchen-mountain-view-3");
		sqlTest.bookMarkRestaurants("thomas", testList);
		
		
		
	}
	
	
	private void logInTest() {
		DATA_BASE sqlTest = new MySQLDB();
		/*
		 * CREATE TABLE users (user_id VARCHAR(255) NOT NULL, password
		 * VARCHAR(255) NOT NULL, first_name VARCHAR(255), last_name
		 * VARCHAR(255), PRIMARY KEY ( user_id ));
		 */
		System.out.print(sqlTest.verifyLogin("thomas", "1234"));
		
	}
	
	private void searchTest() {
		DATA_BASE sqlTest = new MySQLDB();
		/*
		CREATE TABLE restaurants 
			 (business_id VARCHAR(255) NOT NULL, 
			 name VARCHAR(255),
			 categories VARCHAR(255), 
			 city VARCHAR(255), 
			 state VARCHAR(255), 
			 stars FLOAT, 
			 full_address VARCHAR(255), 
			 latitude FLOAT,
			 longitude FLOAT,
			 image_url VARCHAR(255),
			 url VARCHAR(255),
			 PRIMARY KEY ( business_id ))
		*/
		
		JSONArray list = sqlTest.searchRestaurants("thomas",37.38, -122.08, "dinner");
		
		for (int i = 0; i < list.length(); i++) {
			try {
				System.out.println(list.get(i));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}