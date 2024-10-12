package com.ghtjr.post.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "POST")
@Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 데이터베이스 기본 키

    @Column(unique = true, nullable = false)
    private String uuid; // 서비스 간 유니크 ㅅ ㅣㄱ별자

    private String userUuid;
    private String nickname;
    private String title;
    private String content;
    private Date createdDate;
    private Date updatedDate;


    @PrePersist
    public void prePersist() {
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
        }
    }
}
