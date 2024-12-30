package com.happynews.newsaggregator.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsApiOrgArticle {
    private NewsApiOrgSource source;
    private String author;
    private String title;
    private String description;
    private String url;
    private String urlToImage;
    private String publishedAt;
    private String content;
}
