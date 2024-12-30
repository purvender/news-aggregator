package com.happynews.newsaggregator.repository;

import com.happynews.newsaggregator.model.NewsArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NewsArticleRepository extends JpaRepository<NewsArticle, UUID> {
    Optional<NewsArticle> findByUrl(String url);
}
