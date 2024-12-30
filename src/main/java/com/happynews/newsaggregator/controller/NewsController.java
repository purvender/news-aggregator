package com.happynews.newsaggregator.controller;

import com.happynews.newsaggregator.model.NewsArticle;
import com.happynews.newsaggregator.repository.NewsArticleRepository;
import com.happynews.newsaggregator.dto.NewsArticleDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/news")
public class NewsController {

    private final NewsArticleRepository newsArticleRepository;

    @Autowired
    public NewsController(NewsArticleRepository newsArticleRepository) {
        this.newsArticleRepository = newsArticleRepository;
    }

    @GetMapping
    public ResponseEntity<List<NewsArticleDTO>> getLatestNews(
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(defaultValue = "publishedAt") String sortBy) {

        List<NewsArticle> articles = newsArticleRepository.findAll()
                .stream()
                .sorted((a, b) -> b.getPublishedAt().compareTo(a.getPublishedAt()))
                .limit(limit)
                .collect(Collectors.toList());

        List<NewsArticleDTO> dtoList = articles.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtoList);
    }

    private NewsArticleDTO convertToDTO(NewsArticle article) {
        return new NewsArticleDTO(
                article.getId(),
                article.getTitle(),
                article.getDescription(),
                article.getUrl(),
                article.getLocalImagePath(),
                article.getPublishedAt(),
                article.getSourceName(),
                article.getContent(),
                article.getLanguage(),
                article.getCategory()
        );
    }
}

