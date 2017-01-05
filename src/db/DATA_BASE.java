
package db;



import org.json.JSONArray;

//just incase we have to use the other kind of database
public interface DATA_BASE {

	
	

	
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

}
