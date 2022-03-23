package com.example.demo.config;

import java.util.HashMap;
import java.util.Map;

import com.example.demo.domain.user.Role;
import com.example.demo.domain.user.User;
import lombok.Builder;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * OAuth2의 인증 공급자로부터 취득한 사용자 정보를 다시 애플리케이션의 목적에 맞도록 구성 
 * 사용자 정보는 다음 세가지 정보만 보유하기로 한다.
 * userId, userName, userEmail
 * -> 22.3.3/ 다시 재구성해야됨.
 *
 * @author kate
 *
 */
@Component
@Getter
public class OAuth2UserAttribute {
	
	private static final Logger logger = LogManager.getLogger(OAuth2UserAttribute.class);
	
	// TODO 권한정보는?
	/*
	public static final String id = "userId";
	public static final String name = "userName";
	public static final String email = "userEmail";

	 */
	//public Map<String, Object> attributes;
	public static final String id = "userId";
	public static final String name = "userName";
	public static final String email = "userEmail";
	public static final String picture= "userPicture";

	/*
	@Builder
	public OAuth2UserAttribute(Map<String, Object> attributes, String name, String email, String picture) {
		this.attributes = attributes;
		this.name = name;
		this.email = email;
		this.picture = picture;
	}
*/


	public Map<String, Object> getOAuth2UserAttributes(String registrationId, String response)
			throws JsonMappingException, JsonProcessingException {
		
		Map<String, Object> attributes = new HashMap<>();
		
		if ("naver".equals(registrationId)) {
			
			//응답 형태
			//애플리케이션 등록시 제공정보 항목에 따라 차이가 있음
			/*
			{
			  "resultcode":"00",
			  "message":"success",
			  "response":{"id":"55XXXXXX","gender":"M","email":"foo@naver.com"}
			}
			*/
			/*
			response:
			{
				"id":"QpWXQ1Dgb32nhVqLXrZCOGH8pQK5ujlJUYHHGfo1e1U"->토큰?
				,"profile_image":"https://ssl.pstatic.net/static/pwe/address/img_profile.png"
				,"email":"ypd05172@naver.com"
				,"name":"이찬근"
			}
			 */
			
			JsonNode node = new ObjectMapper().readTree(response);
			//System.out.println("###NODE###"+node+"\n###END NODE###");
			attributes.put(id, node.get("response").get("id").toString().replaceAll("\"", ""));
			attributes.put(name, node.get("response").get("name"));//nickname->name 변경,
			attributes.put(email, node.get("response").get("email"));
			attributes.put(picture, node.get("response").get("profile_image"));


			
		} else if ("google".equals(registrationId)){
			
			//응답형태
			/*
			{
			  "sub": "11XXXXXXXXXXXXXXXXXXX",
			  "name": "Foo Robert",
			  "given_name": "Foo",
			  "family_name": "Robert",
			  "picture": "https://lh6.googleusercontent.com/XXX/XXX/photo.jpg",
			  "email": "foo@gmail.com",
			  "email_verified": true,
			  "locale": "ko"
			}
            */
		
			
			ObjectMapper mapper = new ObjectMapper();
			Map<String, String> responseMap = mapper.readValue(response, new TypeReference<Map<String, String>>() {});
			System.out.println("###RESPONSE###"+responseMap+"\n###END RESPONSE###");
			attributes.put(id, responseMap.get("sub"));
			attributes.put(name, responseMap.get("family_name") + "" + responseMap.get("given_name"));
			attributes.put(email, responseMap.get("email"));
			attributes.put(picture, responseMap.get("picture"));


		} else if("kakao".equals(registrationId)){

			//System.out.println("###kakao###!!!!!!!!!!!!!!!!!!");
			//카카오 로그가 안 구해져 온다. 어디서 잘못됐는지 알아야함 -> post로 받아오는거 설정 안해줘서 안됐던거임.
			/*
			"id":2063499049,"connected_at":"2022-01-05T16:45:30Z"
			,"properties":{
						   "nickname":"이찬근"
						  ,"profile_image":"http://k.kakaocdn.net/dn/bE3Nwi/btrmZlPztJQ/Yr2LC1OOL56KU6iKnNZ5Uk/img_640x640.jpg"
						  ,"thumbnail_image":"http://k.kakaocdn.net/dn/bE3Nwi/btrmZlPztJQ/Yr2LC1OOL56KU6iKnNZ5Uk/img_110x110.jpg"
						  }
			,"kakao_account":{
						   "profile_nickname_needs_agreement":false
						   ,"profile_image_needs_agreement":false
						   ,"profile":{
						   				"nickname":"이찬근"
						   				,"thumbnail_image_url":"http://k.kakaocdn.net/dn/h3mO0/btrtIUkC6Hu/GASklHcM0HMbFv8lQZOKUk/img_110x110.jpg"
						   				,"profile_image_url":"http://k.kakaocdn.net/dn/h3mO0/btrtIUkC6Hu/GASklHcM0HMbFv8lQZOKUk/img_640x640.jpg"
						  				,"is_default_image":false
						 			  }
						   ,"has_email":true
						   ,"email_needs_agreement":false
						   ,"is_email_valid":true
						   ,"is_email_verified":true
						   ,"email":"ypd05172@naver.com"
						   }
			 */


			JsonNode node = new ObjectMapper().readTree(response);
			//System.out.println("###NODE###"+node+"\n###END NODE###");

			attributes.put(id, node.get("id").toString().replaceAll("\"", ""));
			attributes.put(name, node.get("properties").get("nickname"));
			attributes.put(email, node.get("kakao_account").get("email"));
			attributes.put(picture, node.get("properties").get("profile_image"));

		}

		if (logger.isDebugEnabled()) {
			logger.debug(attributes);
		}


		return attributes;

	}


}
