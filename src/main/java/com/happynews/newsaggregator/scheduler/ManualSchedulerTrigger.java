package com.happynews.newsaggregator.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * This class manually triggers the NewsScheduler's scheduleNewsFetch method
 * for debugging purposes. It runs once when the application starts.
 *
 * Ensure to disable or remove this in production to prevent unintended executions.
 */
@Component
public class ManualSchedulerTrigger implements CommandLineRunner {

    private final NewsScheduler newsScheduler;

    @Autowired
    public ManualSchedulerTrigger(NewsScheduler newsScheduler) {
        this.newsScheduler = newsScheduler;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Manually triggering the scheduled news fetch...");
        newsScheduler.scheduleNewsFetch();
        System.out.println("Manual trigger completed.");
    }
}
