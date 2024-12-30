package com.happynews.newsaggregator.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "api_usage")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String apiProvider;
    private int requestCount;
    private LocalDate date;
}
