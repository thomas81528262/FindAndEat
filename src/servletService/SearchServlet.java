package servletService;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import api.RpcHelper;
import db.DATA_BASE;
import db.MySQLDB;

/**
 * Servlet implementation class SearchRestaurants
 */
@WebServlet("/SearchServlet")
public class SearchServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		HttpSession session = request.getSession();
		if (session.getAttribute("user_id") == null) {
			// http 401 Unauthorized
			response.setStatus(401);
			return;
		}

		String term = "dinner";
		JSONArray array = new JSONArray();
		DATA_BASE db = new MySQLDB();
		if (request.getParameterMap().containsKey("lat") && request.getParameterMap().containsKey("lon")) {

			//if the client did not put the term in the url just use defaultt term
			String reqTerm = request.getParameter("term");
			term = reqTerm == null? term : reqTerm;
			String userId = (String) session.getAttribute("user_id");

			double latitude = Double.parseDouble(request.getParameter("lat"));
			double lontitude = Double.parseDouble(request.getParameter("lon"));
			array = db.searchRestaurants(userId, latitude, lontitude, term);
		}
		RpcHelper.writeOutput(response, array);
		servletService.WebSocketLog.sendMessage();

	}

}
