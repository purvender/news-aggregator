package com.happynews.newsaggregator.service.apiClients;

import com.happynews.newsaggregator.dto.NewsApiOrgArticle;
import com.happynews.newsaggregator.dto.NewsApiOrgResponse;
import com.happynews.newsaggregator.exception.ImageProcessingException;
import com.happynews.newsaggregator.model.NewsArticle;
import com.happynews.newsaggregator.repository.NewsArticleRepository;
import com.happynews.newsaggregator.service.image.ImageService;
import com.happynews.newsaggregator.util.NewsConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class NewsApiOrgClientImpl implements NewsApiClient {

    @Value("${newsapi.org.apiKey}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final NewsArticleRepository newsArticleRepository;
    private final ImageService imageService;

    // Constructor Injection
    public NewsApiOrgClientImpl(RestTemplate restTemplate,
                                NewsArticleRepository newsArticleRepository,
                                ImageService imageService) {
        this.restTemplate = restTemplate;
        this.newsArticleRepository = newsArticleRepository;
        this.imageService = imageService;
    }

    @Override
    public List<NewsArticle> fetchNews() throws Exception {
        log.info("NewsApiOrgClientImpl: Starting to fetch news with queries.");
        // Define your queries based on priority
        List<NewsArticle> allArticles = new ArrayList<>();

        // 1. Breaking News Top 10
        allArticles.addAll(fetchArticles("breaking news", NewsConstants.BREAKING_NEWS_LIMIT, "breaking"));

        // 2. India Breaking News Top 10
        allArticles.addAll(fetchArticles("India breaking news", NewsConstants.INDIA_BREAKING_NEWS_LIMIT, "india"));

        // 3. Positive and Other Keywords - 80
        String positiveQuery = String.join(" OR ", NewsConstants.POSITIVE_KEYWORDS);
        allArticles.addAll(fetchArticles(positiveQuery, NewsConstants.POSITIVE_NEWS_LIMIT, "positive"));

        log.info("NewsApiOrgClientImpl: Fetched a total of {} articles.", allArticles.size());
        return allArticles;
    }

    private List<NewsArticle> fetchArticles(String query, int limit, String category) {
        String encodedQuery = java.net.URLEncoder.encode(query, java.nio.charset.StandardCharsets.UTF_8);
        String url = "https://newsapi.org/v2/everything?q=" + encodedQuery +
                "&pageSize=" + limit +
                "&apiKey=" + apiKey +
                "&language=en&sortBy=publishedAt";

        log.debug("NewsApiOrgClientImpl: Making API request to URL: {}", url);

        NewsApiOrgResponse response;
        try {
            response = restTemplate.getForObject(url, NewsApiOrgResponse.class);
        } catch (Exception e) {
            log.error("NewsApiOrgClientImpl: Failed to fetch news from NewsAPI.org for query '{}': {}", query, e.getMessage(), e);
            return Collections.emptyList();
        }

        List<NewsArticle> articles = new ArrayList<>();

        if (response != null && "ok".equals(response.getStatus())) {
            log.info("NewsApiOrgClientImpl: Received {} articles for query '{}'.", response.getArticles().size(), query);
            for (NewsApiOrgArticle apiArticle : response.getArticles()) {
                try {
                    // Check if the article already exists
                    Optional<NewsArticle> existingArticle = newsArticleRepository.findByUrl(apiArticle.getUrl());

                    if (existingArticle.isPresent()) {
                        log.debug("NewsApiOrgClientImpl: Article with URL '{}' already exists. Skipping.", apiArticle.getUrl());
                        continue; // Skip duplicates
                    }

                    NewsArticle article = new NewsArticle();
                    article.setTitle(apiArticle.getTitle());
                    article.setDescription(apiArticle.getDescription());
                    article.setUrl(apiArticle.getUrl());
                    article.setImageUrl(apiArticle.getUrlToImage());
                    article.setPublishedAt(LocalDateTime.parse(apiArticle.getPublishedAt(), DateTimeFormatter.ISO_DATE_TIME));
                    article.setSourceName(apiArticle.getSource().getName());
                    article.setContent(apiArticle.getContent());
                    article.setLanguage("en"); // Assuming English
                    article.setCategory(category);
                    article.setFetchedAt(LocalDateTime.now());

                    // Download and process image
                    String localImagePath = null;
                    try {
                        localImagePath = imageService.downloadAndProcessImage(article.getImageUrl());
                        log.debug("NewsApiOrgClientImpl: Image processed at {}", localImagePath);
                    } catch (ImageProcessingException e) { // Create a custom exception if needed
                        log.error("NewsApiOrgClientImpl: Failed to process image for article URL '{}': {}", article.getUrl(), e.getMessage(), e);
                        // Optionally, set a default image or leave it null
                    }

                    article.setLocalImagePath(localImagePath);

                    articles.add(article);
                } catch (Exception e) {
                    log.error("NewsApiOrgClientImpl: Exception while processing article '{}': {}", apiArticle.getUrl(), e.getMessage(), e);
                    // Continue with the next article
                }
            }
        } else {
            log.error("NewsApiOrgClientImpl: Failed to fetch news from NewsAPI.org. Status: {}", response != null ? response.getStatus() : "NULL");
        }

        return articles;
    }

    @Override
    public String getApiProvider() {
        return "NewsAPI.org";
    }
}
