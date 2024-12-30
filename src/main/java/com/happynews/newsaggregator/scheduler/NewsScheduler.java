package com.happynews.newsaggregator.scheduler;

import com.happynews.newsaggregator.service.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class NewsScheduler {

    private final NewsService newsService;

    @Autowired
    public NewsScheduler(NewsService newsService) {
        this.newsService = newsService;
    }

    //@Scheduled(cron = "0 0 2 * * ?") // Runs daily at 2 AM
    public void scheduleNewsFetch() {
        log.info("Starting scheduled news fetch...");
        newsService.fetchAndStoreNews();
        log.info("News fetch completed.");
    }
}
