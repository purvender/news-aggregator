package com.happynews.newsaggregator.repository;

import com.happynews.newsaggregator.model.ApiUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApiUsageRepository extends JpaRepository<ApiUsage, UUID> {
    Optional<ApiUsage> findByApiProviderAndDate(String apiProvider, LocalDate date);
}
