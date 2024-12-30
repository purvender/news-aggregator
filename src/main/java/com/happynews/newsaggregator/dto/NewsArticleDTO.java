package com.happynews.newsaggregator.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsArticleDTO {
    private UUID id;
    private String title;
    private String description;
    private String url;
    private String imageUrl; // Local path
    private LocalDateTime publishedAt;
    private String sourceName;
    private String content;
    private String language;
    private String category;
}
