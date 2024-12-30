package com.happynews.newsaggregator.service.image;

import java.io.IOException;

public interface ImageService {
    String downloadAndProcessImage(String imageUrl) throws IOException;
}

