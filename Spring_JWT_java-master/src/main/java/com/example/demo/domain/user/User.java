package com.example.demo.domain.user;


import com.example.demo.domain.BaseTimeEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@NoArgsConstructor
@Entity
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = true)//프사가 없는 사람이면 NUll값 들어가서 오류 뜨기 때문에 true로 변경했음
    private String picture;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private String platform;//유저의 플랫폼명

    @Builder
    //public User(String userid, String name, String email, String picture, Role role) {
    public User(String name, String email, String picture, Role role, String platform) {

        this.name = name;
        this.email = email;
        this.picture = picture;
        this.role = role;
        this.platform = platform;

    }

    public User update(String name, String picture) {
        this.name = name;
        this.picture = picture;

        return this;
    }

    public String getRoleKey() {
        return this.role.getKey();
    }
}
