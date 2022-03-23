package com.example.demo.config;


import com.example.demo.domain.user.Role;
import com.example.demo.domain.user.User;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
public class OAuthAttributes {

    private Map<String, Object> attributes;
    private String nameAttributeKey;
    private String name;
    private String email;
    private String picture;
    private String platform;

    @Builder
    //public OAuthAttributes(Map<String, Object> attributes, String nameAttributeKey, String name, String email, String picture)
    public OAuthAttributes(Map<String, Object> attributes, String nameAttributeKey, String name, String email, String picture, String platform)
    {
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.name = name;
        this.email = email;
        this.picture = picture;
        this.platform = platform;
    }

    public static OAuthAttributes of(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {

        if("naver".equals(registrationId)) {
            return ofNaver("id", attributes);
        }
        if("kakao".equals(registrationId)) {
            return ofKakao("id", attributes);
        }
        return ofGoogle(userNameAttributeName, attributes);
    }

    private static OAuthAttributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
        System.out.println("###Google attributes### : "+attributes);
        System.out.println("###attributename### : "+userNameAttributeName);

        return OAuthAttributes.builder()
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .picture((String) attributes.get("picture"))
                .platform("google")
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    private static OAuthAttributes ofNaver(String userNameAttributeName, Map<String, Object> attributes){
        Map<String , Object> response = (Map<String, Object>) attributes.get("response");

        System.out.println("###naver attributes### : "+attributes);
        System.out.println("###naver response### : "+response);

        return OAuthAttributes.builder()
                .name((String) response.get("name"))
                .email((String) response.get("email"))
                .picture((String) response.get("profile_image"))
                .platform("naver")
                .attributes(response)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    private static OAuthAttributes ofKakao(String userNameAttributeName, Map<String, Object> attributes){
        Map<String , Object> response = (Map<String, Object>) attributes.get("kakao_account");
        Map<String , Object> properties = (Map<String, Object>) attributes.get("properties");//조회해본 결과 profile은 propseties안에 있음.
        /*
        System.out.println("####attributes_key### : "+attributes.keySet());
        System.out.println("####attributes_value### : "+attributes.values());
        System.out.println("####response_key### : "+response.keySet());
        System.out.println("####response_value### : "+response.values());
*/

        /*
        실제로 profile이 있기는 한데
        properties안에 profile이 있어서 이중으로 받아야 가능함. 이전 자바파일에서도 profile로 받은게 아니라 properties로 받음
        */
        return OAuthAttributes.builder()
                .name((String) properties.get("nickname"))
                .email((String) response.get("email"))
                .picture((String) properties.get("profile_image"))
                .platform("kakao")
                .attributes(attributes)//key set으로 id를 가지고 있는게 attributes밖에 없었음. 그래서 CustomOAuth2UserService에서 id를 받아야 하기 때문에 attributes를 연결
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    public User toEntity() {
        return User.builder().name(name).email(email).picture(picture).platform(platform).role(Role.USER).build();
    }
}
