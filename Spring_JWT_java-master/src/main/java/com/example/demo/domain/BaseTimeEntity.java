package com.example.demo.domain;

import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@Getter
@MappedSuperclass// ? : 자바 엔티티 클래스들이 BAseTimeEntity를 상속할 경우 필드(아래 두 변수)들도 칼럼으로 인식하게 한다.
@EntityListeners(AuditingEntityListener.class)//? : 아래 클래스에 Auditing기능을 포함시킴.
public abstract class BaseTimeEntity {

    @CreatedDate//? : Entity생성되고 저장될 때 시간 자동 저장
    private LocalDateTime createdDate;

    @LastModifiedDate//? : 값 변경시 시간 자동 저장
    private LocalDateTime modifiedDate;
}
