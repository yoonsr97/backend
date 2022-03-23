package com.example.demo.config;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.example.demo.filter.JwtAuthenticationFilter;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity(debug=false)
public class MySecurityConfig extends WebSecurityConfigurerAdapter {

	private static final Logger logger = LogManager.getLogger(MySecurityConfig.class);

	@Autowired
    private CustomOAuth2SuccessHandler customOAuth2SuccessHandler;
	
	@Autowired
	private CustomLogoutSuccessHandler customLogoutSuccessHandler;

	private final CustomOAuth2UserService customOAuth2UserService;

	@Override
	protected void configure(HttpSecurity http) throws Exception {	
		
		 http
		 .authorizeRequests()
		     .antMatchers("/index.html").permitAll()
		     .antMatchers("/static/**").permitAll()
		     .antMatchers("/oauth2Login").permitAll()
		     .anyRequest().authenticated()
		     .and()
		 .csrf().disable()
		 .oauth2Login()
		    .loginPage("/oauth2Login")
		       .redirectionEndpoint()
		          .baseUri("/oauth2/callback/*") // 디폴트는 login/oauth2/code/*
		          .and()
				 //로그인이 성공하고, 아래 서비스 클래스에서 유저 정보를 받아와서 저장한다.
		       .userInfoEndpoint().userService(customOAuth2UserService)
		          .and()
				 //그리고 아래 로그인성공 핸들러에서 서비스에서 만들어진 user의 정보를 받아서 토큰을 발급해주는 것.
		       .successHandler(customOAuth2SuccessHandler)
		       .failureUrl("/main.do")
		       .and()
				 //JWT는 세션을 이용하지 않는다 -> 아래는 세션 비활성화 설정
		    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
		       .and()
		    .logout()
		       .deleteCookies("JSESSIONID")
				 //이제 아래 로그아웃 핸들러에서 토큰 받아와서 리소스서버 로그아웃도 해주는거 해야되는데 이제 그게 가장 큰 문제. 토큰 관리를 어떻게 할 것인가.
		       .logoutSuccessHandler(customLogoutSuccessHandler)
		       .and()
		  .addFilterBefore(jwtAuthenticationFilter(), OAuth2AuthorizationRequestRedirectFilter.class);


	}

	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
				
		PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();		
		auth.inMemoryAuthentication()
		    .withUser("user")
		    .password(encoder.encode("1234"))
		    .roles("USER");
	}
	/*
	@Bean
	public OAuth2UserService<OAuth2UserRequest, OAuth2User> customOAuth2UserService() {		
		return new CustomOAuth2UserService();
	}

	 */

	@Bean
	public JwtAuthenticationFilter jwtAuthenticationFilter() {
		return new JwtAuthenticationFilter();
	}
	
	

}
