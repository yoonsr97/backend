package com.example.demo.domain.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {//여기를 좀 수정해서 역할들 나눠야 될 것 같은데

    GUEST("ROLE_GUEST", "손님"),
    USER("ROLE_USER", "일반 사용자"),
    ADMIN("ROLE_ADMIN", "관계자");//요놈 관련된거 추가해야 함

    private final String key;
    private final String title;
}
