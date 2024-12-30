package com.happynews.newsaggregator.service.image;

import com.happynews.newsaggregator.exception.ImageProcessingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
@Profile("local")
@Slf4j
public class LocalImageServiceImpl implements ImageService {

    @Value("${image.storage.path}")
    private String imageStoragePath;

    private final RestTemplate restTemplate;

    public LocalImageServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String downloadAndProcessImage(String imageUrl) throws ImageProcessingException {
        if (imageUrl == null || imageUrl.isEmpty()) {
            log.warn("LocalImageService: Received null or empty imageUrl");
            return null;
        }

        // Generate a unique filename
        String fileExtension = StringUtils.getFilenameExtension(imageUrl);
        String fileName = UUID.randomUUID().toString() + "." + (fileExtension != null ? fileExtension : "jpg");

        log.debug("LocalImageService: Downloading image from URL: {}", imageUrl);

        // Download the image
        byte[] imageBytes;
        try {
            imageBytes = restTemplate.getForObject(imageUrl, byte[].class);
            if (imageBytes == null) {
                throw new ImageProcessingException("Failed to download image: " + imageUrl);
            }
        } catch (Exception e) {
            log.error("LocalImageService: Failed to download image from URL: {}", imageUrl, e);
            throw new ImageProcessingException("Failed to download image: " + imageUrl, e);
        }

        // Define the path
        File imageFile = new File(imageStoragePath + fileName);

        // Create directories if they don't exist
        File directory = new File(imageStoragePath);
        if (!directory.exists()) {
            boolean dirsCreated = directory.mkdirs();
            if (dirsCreated) {
                log.debug("LocalImageService: Created image storage directory at {}", imageStoragePath);
            } else {
                log.warn("LocalImageService: Failed to create image storage directory at {}", imageStoragePath);
            }
        }

        // Resize and save the image
        try {
            Thumbnails.of(new java.io.ByteArrayInputStream(imageBytes))
                    .size(800, 600) // Set desired size
                    .outputFormat("jpg")
                    .toFile(imageFile);
            log.info("LocalImageService: Image downloaded and processed at {}", imageFile.getPath());
        } catch (IOException e) {
            log.error("LocalImageService: Error processing image from URL '{}': {}", imageUrl, e.getMessage(), e);
            throw new ImageProcessingException("Error processing image from URL: " + imageUrl, e);
        }

        return imageFile.getPath();
    }
}
