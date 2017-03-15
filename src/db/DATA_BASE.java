
package db;



import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

//just in case we have to use the other kind of database
public interface DATA_BASE {
	
	
	
	
	 /**
     * Recommend restaurants based on userId
     * @param userId
     * @return
     */
    public JSONArray getRecommendRestaurants(String userId);
	
	

	 /**
     * Insert the book mark value in the data base.
     * @param userId
     * @param businessIds
     */
    public boolean bookMarkRestaurants(String userId, List<String> businessIds, boolean value);
	

    
   
    
	
	/**
     * Search restaurants.
     * @param userId
     * @param lat
     * @param lon
     * @return
     */
    public JSONArray searchRestaurants(String userId, double lat, double lon, String term);
	
	
	  /**
     * Close the connection.
     */
    public void close() ;
  

   /**
     * Verify if the userId matches the password.
     * @param userId
     * @param password
     * @return
     */
    public Boolean verifyLogin(String userId, String password);

    /**
     * Get user's name for the userId.
     * @param userId
     * @return First and Last Name
     */
    public String getFirstLastName(String userId);
    
    
    
    /**
     * Get the bookMarked restaurants for a user.
     * @param userId
     * @return
     */
    public List<String> getBookMarkRestaurants(String userId);
    
    
    
    /**
     * Get the restaurant json by id.
     * @param businessId
     * @return
     */
    public JSONObject getRestaurantsById(String businessId);


}
