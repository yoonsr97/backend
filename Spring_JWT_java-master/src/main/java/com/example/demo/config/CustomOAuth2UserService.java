package com.example.demo.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.example.demo.domain.user.User;
import com.example.demo.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService 를 참조하여 작성
 * @author kate
 *
 */
/*
	얻어온 유저 엑세스 토큰을 통해 리소스 서버에 정보를 요청해서 따오는 서비스 클래스
 */

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
	
	private static final Logger logger = LogManager.getLogger(CustomOAuth2UserService.class);
	
	private static final String MISSING_USER_INFO_ERROR_CODE = "missing_redirect_uri_access_code";

	@Autowired
	private final UserRepository repository;

	@Autowired
	private OAuth2UserAttribute oauth2UserAttribute;

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

		//이거로 정보 받아올 수 있나
		OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
		OAuth2User oAuth2User = delegate.loadUser(userRequest);
		//System.out.println("###userAttributes" + oAuth2User.getAttributes());



		String clientRegistrationId = userRequest.getClientRegistration().getRegistrationId();
		String resourceServerUri = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUri();
		String accessToken = userRequest.getAccessToken().getTokenValue();
		//여기까지 기존 세션 코드에서 가져온 것. loaduser이라는 메소드는 공통으로 오버라이딩해서 쓰이기 때문에 같이 쓸 수 있다고 판별.

		System.out.println("##log-엑세스토큰##");
		if (logger.isDebugEnabled()) {
			logger.debug(userRequest.getAccessToken().getTokenValue());//여기서 디버깅?
		}//userRequest.getAccessToken().getExpiresAt() , .getIssuedAt()

		System.out.println("##log-scopes###");
		logger.debug(userRequest.getClientRegistration().getScopes());


		//리프레쉬 토큰 어딨는지 알아야함



		//OAuth2User user = null;

		//TODO 시큐리티에서 제공하는 기본 권한 형식은 ROLE_XXX
		// 인증에 성공한 사용자는 ROLE_USER 권한을 부여하기로 하자.
		List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

		//사용자 정보 중 사용자 아이디로 삼을 key
		String userNameAttributeName = OAuth2UserAttribute.id;

		//유저 정보 담을 map생성
		Map<String, Object> attributes = null;
		//OAuth2UserAttribute attributes = null;

		OAuth2User user = null;

		if (resourceServerUri != null && !"".equals(resourceServerUri)
				&& accessToken != null && !"".equals(accessToken)) {

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
			headers.set("Authorization", "Bearer " + accessToken);

			// 헤더에만 접근 코드를 넣어서 전송하므로 파라미터로 넘길 값은 없다.
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

			HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
			RestTemplate restTemplate = new RestTemplate();
			restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());

			System.out.println("##log-httpheaders##");
			if (logger.isDebugEnabled()) {
				logger.debug(headers);
			}


			System.out.println("##log-TokenRequest###");
			logger.debug(request);

			try {
				// 리소스 서버에게 사용자 정보 요청
				String response = restTemplate.postForObject(resourceServerUri, request, String.class);//이거로 받아서

				//로그는 진짜 로그일 뿐임.
				if (logger.isDebugEnabled()) {
					logger.debug(response);
				}

				//System.out.println("###RESPONSE_service###"+response+"\n###END RESPONSE###");
				//TODO 권한 정보도 넘겨야 할듯?
				attributes = oauth2UserAttribute.getOAuth2UserAttributes(clientRegistrationId, response);

			} catch (OAuth2AuthorizationException ex) {
				OAuth2Error oauth2Error = ex.getError();
				throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString(), ex);
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			//System.out.println("###authorities### : " + authorities);
			//System.out.println("###attributes### : " + attributes);
			//System.out.println("###userNameAttributeName### : " + userNameAttributeName);

			user = new DefaultOAuth2User(authorities, attributes, userNameAttributeName);

			OAuthAttributes userAttributes = OAuthAttributes.of(clientRegistrationId, userNameAttributeName, oAuth2User.getAttributes());
			User dataUser = saveOrUpdate(userAttributes);

		} else {
			OAuth2Error oauth2Error = new OAuth2Error(MISSING_USER_INFO_ERROR_CODE,
					"Missing required redirect uri or access token for Client Registration: "
							+ userRequest.getClientRegistration().getRegistrationId(),
					null);
			throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString());
		}

		//System.out.println("###User### : " + user);

		return user;
		//return new DefaultOAuth2User(authorities, attributes, userNameAttributeName);

	}


	//private User saveOrUpdate(OAuth2UserAttribute attributes) {
	private User saveOrUpdate(OAuthAttributes attributes) {
		//User user = repository.findByEmail(attributes.getEmail())
		User user = repository.findByEmailAndPlatform(attributes.getEmail(), attributes.getPlatform())
				//.map(entity -> entity.update(attributes.getName(), attributes.getPicture()))
				.map(entity -> entity.update(attributes.getName(), attributes.getPicture()))
				.orElse(attributes.toEntity());

		return repository.save(user);
	}


}
