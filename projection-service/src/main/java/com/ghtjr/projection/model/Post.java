package com.ghtjr.projection.model;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "posts")
public class Post {
    @Id
    private String id;

    @Indexed(unique = true)
    private String uuid;
    private String userUuid;
    private String nickname;
    private String title;
    private String content;
    private Long createdDate;
    private Long updatedDate;
}
