package com.happynews.newsaggregator.repository;

import com.happynews.newsaggregator.model.Source;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SourceRepository extends JpaRepository<Source, UUID> {
    Optional<Source> findByName(String name);
}

