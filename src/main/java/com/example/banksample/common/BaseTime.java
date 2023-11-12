package com.example.banksample.common;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * {@code @MappedSuperclass}
 * - JPA Entity 클래스들이 해당 클래스에 선언된 필드를 인식하게 한다.
 * {@code @EntityListeners}
 * - AuditingEntityListener.class 가 콜백 리스너로 지정된다.
 * - Entity 에서 어떤 이벤트가 발생할 때 특정 로직을 수행한다.
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseTime implements Serializable {

    @CreatedDate
    @Column(name="createdAt", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name="updatedAt", nullable = false)
    private LocalDateTime modifiedAt;

}