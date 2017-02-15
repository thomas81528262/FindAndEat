package servletService;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONException;
import org.json.JSONObject;

import api.RpcHelper;
import db.DATA_BASE;
import db.MySQLDB;

/**
 * Servlet implementation class LoginServlet
 */
@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final DATA_BASE db = new MySQLDB();

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		try {
			JSONObject res = new JSONObject();
			HttpSession session = request.getSession();
			if (session.getAttribute("user_id") == null) {

				// http 403 Forbidden
				response.setStatus(403);
				res.put("status", "Session Invalid");
			} else {
				String user = (String) session.getAttribute("user");
				String name = db.getFirstLastName(user);
				res.put("status", "OK");
				res.put("user_id", user);
				res.put("name", name);

			}

			RpcHelper.writeOutput(response, res);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */

	// the user will get in by post method to get session
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			JSONObject rst = new JSONObject();

			// get the the user_id and pwd from URL
			String user_id = request.getParameter("user_id");
			String pwd = request.getParameter("password");

			if (db.verifyLogin(user_id, pwd)) {
				HttpSession session = request.getSession();
				session.setAttribute("user_id", user_id);

				// session will expire in 30 minutes
				session.setMaxInactiveInterval(30 * 60);
				// Get user name the user can see the name on the page
				String userName = db.getFirstLastName(user_id);

				rst.put("user_id", user_id);
				rst.put("name", userName);
				rst.put("status", "OK");
				RpcHelper.writeOutput(response, rst);
			} else {
				// http 401 Unauthorized
				response.setStatus(401);
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
