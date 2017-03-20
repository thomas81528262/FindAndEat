package servletService;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;

import api.RpcHelper;
import db.DATA_BASE;
import db.MySQLDB;


/**
 * Servlet implementation class RecommendRestaurants
 */
@WebServlet("/RecommendationServlet")
public class RecommendServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
   

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
    private static DATA_BASE db = new MySQLDB();


    protected void doGet(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException {

    	JSONArray array = null;

    	if (request.getParameterMap().containsKey("user_id")) {
    		String userId = request.getParameter("user_id");
    		array = db.getRecommendRestaurants(userId);
    	}
    	RpcHelper.writeOutput(response, array);
    }


	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
