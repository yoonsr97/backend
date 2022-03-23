package com.example.demo.config;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.stereotype.Component;

import com.example.demo.utils.CookieUtils;

@Component
public class CustomLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {

	private static final Logger logger = LogManager.getLogger(CustomLogoutSuccessHandler.class);

	@Autowired
	private Environment env;
	
	@Autowired
	private CookieUtils cookieUtils;
	
	@Override
	public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
			throws IOException, ServletException {
		
		String frontendAppEntryPage = env.getProperty("frontend-app.entry");



		System.out.println("logout cookieUtils");
		if (logger.isDebugEnabled()) {
			logger.debug(cookieUtils);
		}

		response.addCookie(cookieUtils.generateRemoveJwtCookie(env.getProperty("jwt.token-name"), ""));
		response.addCookie(cookieUtils.generateRemoveJwtCookie(env.getProperty("jwt.token-name") + "-flag", ""));

		getRedirectStrategy().sendRedirect(request, response, frontendAppEntryPage);



		//logout success service made

		if (logger.isDebugEnabled()) {
			logger.debug("뭐가 뭔지 알아야 뭘 하지 시ㅣㅣㅣ발");
		}

		String reqURL = "https://kauth.kakao.com/oauth/logout";

		String URL2 = "https://kauth.kakao.com/oauth/logout?client_id=53c5f9b54ad9450a6cc811fdccf65417&logout_redirect_uri=http://localhost:8080/logout&response_type=code";

		try {
			//URL url = new URL(reqURL);
			URL url = new URL(URL2);

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();

			//conn.setRequestMethod("GET");// 카카오는 필수 파라미터 값들을 담아 POST로만 요청이 가능하다.
			conn.setDoOutput(true);       // POST 요청을 위해 기본값이 false인 setDoOutput을 true로

			//    POST 요청에 필요로 요구하는 파라미터 스트림을 통해 전송
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
			StringBuilder sb = new StringBuilder();
			sb.append("https://kauth.kakao.com/oauth/logout");
			sb.append("?client_id=$53c5f9b54ad9450a6cc811fdccf65417");  //본인이 발급받은 key
			sb.append("&logout_redirect_uri=$http://localhost:8080/logout");     // 본인이 설정해 놓은 경로
			sb.append("&response_type=code");
			bw.write(sb.toString());

			if (logger.isDebugEnabled()) {
				logger.debug(sb.toString());
			}

			bw.flush();//url로 카카오 로그아웃 보내버리기

			//https://kauth.kakao.com/oauth/logout?client_id=53c5f9b54ad9450a6cc811fdccf65417&logout_redirect_uri=http://localhost:8080/logout&response_type=code

			int responseCode = conn.getResponseCode();
			if (logger.isDebugEnabled()) {
				logger.debug(responseCode);
			}

			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void logout(String access_Token) {
		String reqURL = "https://kapi.kakao.com/v1/user/logout";
		try {
			URL url = new URL(reqURL);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Authorization", "Bearer " + access_Token);
			int responseCode = conn.getResponseCode();
			System.out.println("responseCode : " + responseCode);
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String result = "";
			String line = "";
			while ((line = br.readLine()) != null) {
				result += line;
			}
			logger.debug(result);
		} catch (IOException e) { e.printStackTrace(); } }

}
