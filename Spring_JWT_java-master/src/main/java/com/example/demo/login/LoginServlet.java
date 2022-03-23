package com.example.demo.login;

import com.example.demo.config.MySecurityConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Deprecated
public class LoginServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger logger = LogManager.getLogger(LoginServlet.class);

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		String flag = req.getParameter("flag");

		System.out.println("##servlet-req##");//이쪽은 접근도 안하나?
		logger.debug(req.authenticate(resp));

		System.out.println("##servlet-resp##");
		logger.debug(resp);
	}

	
	
	
	

}
