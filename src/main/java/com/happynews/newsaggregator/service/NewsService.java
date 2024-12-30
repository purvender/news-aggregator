package com.happynews.newsaggregator.service;

import com.happynews.newsaggregator.model.ApiUsage;
import com.happynews.newsaggregator.model.NewsArticle;
import com.happynews.newsaggregator.repository.NewsArticleRepository;
import com.happynews.newsaggregator.repository.ApiUsageRepository;
import com.happynews.newsaggregator.service.apiClients.NewsApiClient;
import com.happynews.newsaggregator.service.image.ImageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class NewsService {

    private final List<NewsApiClient> newsApiClients;
    private final NewsArticleRepository newsArticleRepository;
    private final ApiUsageRepository apiUsageRepository;
    private final ImageService imageService;

    @Autowired
    public NewsService(List<NewsApiClient> newsApiClients,
                       NewsArticleRepository newsArticleRepository,
                       ApiUsageRepository apiUsageRepository,
                       ImageService imageService) {
        this.newsApiClients = newsApiClients;
        this.newsArticleRepository = newsArticleRepository;
        this.apiUsageRepository = apiUsageRepository;
        this.imageService = imageService;
    }

    @Transactional
    public void fetchAndStoreNews() {
        log.info("NewsService: Starting fetchAndStoreNews()");

        // Set to track processed URLs within this run
        Set<String> processedUrls = new HashSet<>();

        for (NewsApiClient client : newsApiClients) {
            try {
                log.info("NewsService: Processing client - {}", client.getApiProvider());

                // Check API usage limits
                if (canFetch(client.getApiProvider())) {
                    List<NewsArticle> articles = client.fetchNews();
                    log.info("NewsService: Fetched {} articles from {}", articles.size(), client.getApiProvider());

                    for (NewsArticle article : articles) {
                        String url = article.getUrl();

                        // Check if the URL has already been processed in this run
                        if (processedUrls.contains(url)) {
                            log.debug("NewsService: Duplicate URL '{}' found in current run. Skipping.", url);
                            continue;
                        }

                        // Check if the article already exists in the database
                        boolean exists = newsArticleRepository.findByUrl(url).isPresent();
                        if (exists) {
                            log.debug("NewsService: Article with URL '{}' already exists in the database. Skipping.", url);
                            continue;
                        }

                        // Mark the URL as processed
                        processedUrls.add(url);

                        log.debug("NewsService: Processing article - {}", article.getTitle());

                        // Download and process image
                        String localImagePath = imageService.downloadAndProcessImage(article.getImageUrl());
                        article.setLocalImagePath(localImagePath);
                        log.debug("NewsService: Image processed at {}", localImagePath);

                        // Save to database
                        newsArticleRepository.save(article);
                        log.debug("NewsService: Saved article - {}", article.getTitle());
                    }

                    // Update API usage
                    incrementApiUsage(client.getApiProvider(), 1); // Assuming one request per client
                    log.info("NewsService: Updated API usage for {}", client.getApiProvider());
                } else {
                    log.warn("NewsService: API usage limit reached for {}", client.getApiProvider());
                }
            } catch (Exception e) {
                log.error("NewsService: Exception while fetching news from {}: {}", client.getApiProvider(), e.getMessage(), e);
            }
        }

        log.info("NewsService: Completed fetchAndStoreNews()");
    }

    private boolean canFetch(String apiProvider) {
        // Example: Limit to 100 requests per day
        int freeTierLimit = 100;
        var usage = apiUsageRepository.findByApiProviderAndDate(apiProvider, java.time.LocalDate.now())
                .orElse(new ApiUsage(null, apiProvider, 0, java.time.LocalDate.now()));
        return usage.getRequestCount() < freeTierLimit;
    }

    private void incrementApiUsage(String apiProvider, int count) {
        var usage = apiUsageRepository.findByApiProviderAndDate(apiProvider, java.time.LocalDate.now())
                .orElse(new ApiUsage(null, apiProvider, 0, java.time.LocalDate.now()));
        usage.setRequestCount(usage.getRequestCount() + count);
        apiUsageRepository.save(usage);
        log.debug("NewsService: API usage incremented for {}", apiProvider);
    }
}
