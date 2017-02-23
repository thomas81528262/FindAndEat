package servletService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import db.MySQLDB;
import api.RpcHelper;
import db.DATA_BASE;



/**
 * Servlet implementation class VisitHistory
 */
@WebServlet("/bookMark")
public class bookMarkServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       

	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
            DATA_BASE db = new MySQLDB();
			JSONArray array = null;
			
			if (request.getParameterMap().containsKey("user_id")) {
				String userId = request.getParameter("user_id");
				List<String> booked_business_id = db.getBookMarkRestaurants(userId);
				array = new JSONArray();
				for (String id : booked_business_id) {
					JSONObject dbObject = db.getRestaurantsById(id).put("is_booked", true);
					array.put(dbObject);
				}
				
				RpcHelper.writeOutput(response, array);
				
			} else {
				RpcHelper.writeOutput(response, new JSONObject().put("status", "Error"));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	private static final DATA_BASE db = new MySQLDB();
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
				throws ServletException, IOException {
			try {
				
				boolean status = false;
				
				List<String> business_ids = new ArrayList<>();
				JSONObject input = RpcHelper.parseInput(request);
				if (input.has("user_id") && input.has("is_booked") && input.has("business_id")) {
					String userId = (String) input.get("user_id");
					boolean value = (boolean) input.get("is_booked");
					business_ids.add ((String) input.get("business_id"));
					status = db.bookMarkRestaurants(userId, business_ids, value);
				} 
				
				if (status) {
					RpcHelper.writeOutput(response, new JSONObject().put("status", "OK"));
				}else{
					RpcHelper.writeOutput(response, new JSONObject().put("status", "InvalidParameter"));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	
	}


