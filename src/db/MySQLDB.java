package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import yelpModel.Restaurant;
import yelp.YelpAPI;

public class MySQLDB implements DATA_BASE {
	private Connection sqlCon = null;
	private static final int MAX_RECOMMENDED_RESTAURANTS = 10;

	private Set<String> getCategories(String businessId) {
		try {
			String sql = "SELECT categories from restaurants WHERE business_id = ? ";
			PreparedStatement statement = sqlCon.prepareStatement(sql);
			statement.setString(1, businessId);

			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				Set<String> categoriesSet = new HashSet<>();
				String[] categories = rs.getString("categories").split(",");
				for (String category : categories) {
					categoriesSet.add(category.trim());
				}
				return categoriesSet;
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return new HashSet<String>();
	}

	private List<String> getBusinessId(Set<String> categories) {
		List<String> list = new ArrayList<>();
		try {

			// combine all the categories and sort by star
			int n = categories.size();

			// mach patter: category = Burgers == categories = American,Burgers,
			// Beer,
			String sql = "SELECT business_id, stars from restaurants WHERE categories LIKE ? ";

			for (int i = 0; i < n - 1; i++) {
				sql += "union SELECT business_id, stars from restaurants WHERE categories LIKE ? ";
			}

			// sort by stars
			sql += "ORDER BY stars DESC";
			int inputNum = 1;
			PreparedStatement statement = sqlCon.prepareStatement(sql);
			for (String category : categories) {
				statement.setString(inputNum++, "%" + category + "%");
			}

			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				String businessId = rs.getString("business_id");
				list.add(businessId);
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return list;
	}

	@Override
	public JSONArray getRecommendRestaurants(String userId) {
		try {
			if (sqlCon == null) {
				return null;
			}

			List<String> bookMarkedRestaurants = getBookMarkRestaurants(userId);
			Set<String> categories = new HashSet<>();

			// get all the categories
			for (String restaurant : bookMarkedRestaurants) {
				categories.addAll(getCategories(restaurant));
			}

			// get all the businessId by categories pattern
			List<String> businessIds = new ArrayList<>();

			businessIds = getBusinessId(categories);

			List<JSONObject> result = new ArrayList<>();
			int count = 0;
			for (String businessId : businessIds) {
				// we want to filter the restaurant already in the bookMarked
				if (!bookMarkedRestaurants.contains(businessId)) {
					result.add(getRestaurantsById(businessId));
					count++;
					if (count >= MAX_RECOMMENDED_RESTAURANTS) {
						break;
					}
				}
			}
			return new JSONArray(result);
		} catch (Exception e) {
			// we want to normal return the status
			System.out.println(e.getMessage());
		}
		return null;
	}

	@Override
	public JSONObject getRestaurantsById(String businessId) {

		try {
			String sql = "SELECT * from restaurants where business_id = ?";
			PreparedStatement statement = sqlCon.prepareStatement(sql);
			statement.setString(1, businessId);

			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				Restaurant restaurant = new Restaurant(rs.getString("business_id"), rs.getString("name"),
						rs.getString("categories"), rs.getString("city"), rs.getString("state"), rs.getFloat("stars"),
						rs.getString("full_address"), rs.getFloat("latitude"), rs.getFloat("longitude"),
						rs.getString("image_url"), rs.getString("url"));
				JSONObject obj = restaurant.toJSONObject();
				return obj;
			}
		} catch (Exception e) {
			// we want to normal return the status
			System.out.println(e.getMessage());
		}
		return null;
	}

	@Override
	public List<String> getBookMarkRestaurants(String userId) {
		List<String> bookMarkList = new ArrayList<>();
		try {

			String sql = "SELECT business_id from book_mark WHERE user_id = ?";
			PreparedStatement statement = sqlCon.prepareStatement(sql);
			statement.setString(1, userId);
			ResultSet rs = statement.executeQuery();

			while (rs.next()) {
				String visitedRestaurant = rs.getString("business_id");
				bookMarkList.add(visitedRestaurant);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bookMarkList;
	}

	private boolean bookMarkExist(String userId, String businessId) {
		String query = "select user_id_business_id FROM book_mark WHERE user_id_business_id=?";
		try {
			PreparedStatement statement = sqlCon.prepareStatement(query);
			statement.setString(1, userId + ':' + businessId);
			ResultSet rs = statement.executeQuery();

			if (rs.next()) {
				return true;
			} else {
				return false;
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}

	@Override
	public boolean bookMarkRestaurants(String userId, List<String> businessIds, boolean value) {

		try {

			String query = value == true
					? "INSERT INTO book_mark (user_id, business_id, user_id_business_id) VALUES (?,?,?)"
					: "DELETE FROM book_mark WHERE user_id_business_id = ?";

			PreparedStatement statement = sqlCon.prepareStatement(query);
			for (String businessId : businessIds) {

				if (value == true) {
					statement.setString(1, userId);
					statement.setString(2, businessId);
					statement.setString(3, userId + ':' + businessId);
				} else {

					if (!bookMarkExist(userId, businessId)) {
						return false;
					}
					statement.setString(1, userId + ':' + businessId);
				}
				statement.execute();

			}

			return true;
		} catch (SQLException e) {
			// we want to normal return the status
			System.out.println(e.getMessage());
			return false;

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
				Boolean isBooked = bookMarkExist(userId, businessId);

				if (isBooked == null || isBooked == false) {
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

				// insert data
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

				// the update data
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

				// finally get the result > <
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

	public static void main(String[] args) throws JSONException {

		MySQLDB sqlTest = new MySQLDB();
		//Set<String> categories = sqlTest.getCategories("charley-noodle-and-grill-los-altos-2");
		// System.out.println(categories);
		// System.out.println(sqlTest.getBusinessId(categories));
		JSONArray list = sqlTest.getRecommendRestaurants("thomas");
		for (int i = 0; i < list.length(); i++) {
			System.out.println(list.getJSONObject(i).getString("business_id"));
		}
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
		 * CREATE TABLE restaurants (business_id VARCHAR(255) NOT NULL, name
		 * VARCHAR(255), categories VARCHAR(255), city VARCHAR(255), state
		 * VARCHAR(255), stars FLOAT, full_address VARCHAR(255), latitude FLOAT,
		 * longitude FLOAT, image_url VARCHAR(255), url VARCHAR(255), PRIMARY
		 * KEY ( business_id ))
		 */

		JSONArray list = sqlTest.searchRestaurants("thomas", 37.38, -122.08, "dinner");

		for (int i = 0; i < list.length(); i++) {
			try {
				System.out.println(list.get(i));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	// simplify the table by combine two data to primary key
	private static void bookMarkTestV2(boolean value) {
		DATA_BASE sqlTest = new MySQLDB();
		/*
		 * CREATE TABLE book_mark (user_id_business_id VARCHAR(255) NOT NULL ,
		 * user_id VARCHAR(255) NOT NULL , business_id VARCHAR(255) NOT NULL,
		 * PRIMARY KEY (user_id_business_id ), FOREIGN KEY (business_id)
		 * REFERENCES restaurants(business_id), FOREIGN KEY (user_id) REFERENCES
		 * users(user_id));
		 */
		List<String> testList = new ArrayList<>();
		testList.add("asian-box-mountain-view");
		testList.add("bowl-of-heaven-mountain-view-2");
		testList.add("eureka-mountain-view-2");
		testList.add("vaso-azzurro-ristorante-mountain-view");
		testList.add("srasa-kitchen-mountain-view-3");
		System.out.println(sqlTest.bookMarkRestaurants("thomas", testList, value));
	}

	private static void getBookMarkTest() {
		DATA_BASE sqlTest = new MySQLDB();
		System.out.println(sqlTest.getBookMarkRestaurants("thomas"));
	}

}