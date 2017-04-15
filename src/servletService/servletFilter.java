package servletService;


import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class servletFilter implements Filter {

	
	
	
	private static final Set<String> passPath = new HashSet<>();
    static {
    	
    	String[] passingPath = {
    			"home",
    			"home.css",
    			"home.html",
    			"home.js",
    			"LoginServlet"
    			};
    	
        
        for (String s : passingPath) {
        	passPath.add (s);
        }
    }
	
	@Override
	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException {
		
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		String path = request.getRequestURI();
		HttpSession session = request.getSession();
  		String[] check = path.split("/");
  		
		//if the url request just access the certain path the othe will be redirct
  		//if the client get session just pass
		if (check.length >= 3 && passPath.contains(check[2]) ||  session.getAttribute("user_id")!= null) {
			chain.doFilter(req, res);
		}else {
			response.sendRedirect("home.html");
		}		
		 
	}
	
	@Override
	public void init(FilterConfig config) throws ServletException {
		
		//check the server initial the server
		String checker = config.getInitParameter("checker");
		System.out.println(checker);
	}
	
	@Override
	public void destroy() {
		//TODO
	}
}
