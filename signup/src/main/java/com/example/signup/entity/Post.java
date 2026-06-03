package com.example.signup.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "posts")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long authorId;

    @Column(nullable = false)
    private String author;

    //@Column(nullable = false, length = 2000)
    @Column(length = 2000)
    private String content;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private int likes;

    @Column(name = "like_count", nullable = false)
    private Integer likeCount = 0;

    @Column(nullable = false)
    private int dislikes;

    @Column(name = "dislike_count", nullable = false)
    private Integer dislikeCount = 0;

    @Column(name = "comment_count", nullable = false)
    private Integer commentCount = 0;

    @Column(nullable = false)
    private boolean deleted = false;

    // Optional attachments
    @Column(length = 4000)
    private String code;

    @Column(length = 1000)
    private String imageUrl;

    @Column(length = 1000)
    private String linkUrl;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }

    public int getDislikes() { return dislikes; }
    public void setDislikes(int dislikes) { this.dislikes = dislikes; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getLinkUrl() { return linkUrl; }
    public void setLinkUrl(String linkUrl) { this.linkUrl = linkUrl; }


    public Integer getLikeCount() {
    return likeCount;
}

public void setLikeCount(Integer likeCount) {
    this.likeCount = likeCount;
}

public Integer getDislikeCount() {
    return dislikeCount;
}

public void setDislikeCount(Integer dislikeCount) {
    this.dislikeCount = dislikeCount;
}

public Integer getCommentCount() {
    return commentCount;
}

public void setCommentCount(Integer commentCount) {
    this.commentCount = commentCount;
}

@Transient
public String getPostType() {
    if (code != null && !code.trim().isEmpty()) return "CODE";
    if (imageUrl != null && !imageUrl.trim().isEmpty()) return "IMAGE";
    return "TEXT";
}

}
