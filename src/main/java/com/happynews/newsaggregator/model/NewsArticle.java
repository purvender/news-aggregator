package com.happynews.newsaggregator.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "news_articles", uniqueConstraints = {
        @UniqueConstraint(columnNames = "url", name = "uk6bnn6ac0wal697kn73hb3kb8l")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(length = 512)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(unique = true, length = 1024)
    private String url;

    @Column(length = 1024)
    private String imageUrl;

    @Column(length = 1024)
    private String localImagePath;

    private LocalDateTime publishedAt;
    private String sourceName;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    private String language;
    private String category;
    private LocalDateTime fetchedAt;

    @Column(length = 2048)
    private String s3ImageUrl; // For future S3 integration
}
