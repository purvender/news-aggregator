package com.happynews.newsaggregator.service.apiClients;

import com.happynews.newsaggregator.model.NewsArticle;
import java.util.List;

public interface NewsApiClient {
    List<NewsArticle> fetchNews() throws Exception;
    String getApiProvider();
}
