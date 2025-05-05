package org.almagestauth.domain.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
public class BaseTime {
    @CreatedDate
    @Column(updatable = false)
    private String createdDate;
    @LastModifiedDate
    private String lastModifiedDate;

//    @PrePersist
//    public void onPrePersist() {
//        this.createdDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:SS:SSS"));
//        this.lastModifiedDate  = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:SS:SSS"));
//    }
//
//    @PreUpdate
//    public void onPreUpdate() {
//        this.lastModifiedDate  = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:SS:SSS"));
//    }


}