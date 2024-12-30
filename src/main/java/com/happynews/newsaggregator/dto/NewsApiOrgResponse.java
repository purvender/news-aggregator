package com.happynews.newsaggregator.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsApiOrgResponse {
    private String status;
    private int totalResults;
    private List<NewsApiOrgArticle> articles;
}
