package com.happynews.newsaggregator.service.image;

import com.happynews.newsaggregator.exception.ImageProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.UUID;

@Service
@Profile("prod")
@Slf4j
public class S3ImageServiceImpl implements ImageService {

    private final String bucketName;
    private final Region region;
    private final S3Client s3Client;

    @Autowired
    public S3ImageServiceImpl(
            @Value("${aws.s3.bucketName}") String bucketName,
            @Value("${aws.region}") String region,
            @Value("${newsapi.org.apiKey}") String apiKey // Assuming you need this
    ) {
        this.bucketName = bucketName;
        this.region = Region.of(region);
        this.s3Client = S3Client.builder()
                .region(this.region)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    @Override
    public String downloadAndProcessImage(String imageUrl) throws ImageProcessingException {
        if (imageUrl == null || imageUrl.isEmpty()) {
            log.warn("S3ImageService: Received null or empty imageUrl");
            return null;
        }

        URL url;
        try {
            // Encode the URL properly to handle non-ASCII characters
            URI uri = new URI(imageUrl);
            URI encodedUri = new URI(
                    uri.getScheme(),
                    uri.getAuthority(),
                    uri.getPath(),
                    uri.getQuery(),
                    uri.getFragment());
            url = encodedUri.toURL();
        } catch (URISyntaxException | MalformedURLException e) {
            log.error("S3ImageService: Invalid URL syntax: {}", imageUrl, e);
            throw new ImageProcessingException("Invalid URL syntax: " + imageUrl, e);
        }

        // Generate a unique filename
        String fileExtension = getFileExtension(imageUrl);
        String fileName = "images/" + UUID.randomUUID().toString() + "." + fileExtension;

        log.debug("S3ImageService: Downloading image from URL: {}", imageUrl);

        // Download the image
        byte[] imageBytes;
        try (InputStream in = url.openStream()) {
            imageBytes = in.readAllBytes();
        } catch (IOException e) {
            log.error("S3ImageService: Failed to download image from URL: {}", imageUrl, e);
            throw new ImageProcessingException("Failed to download image: " + imageUrl, e);
        }

        // Upload to S3 without ACL since ACLs are disallowed
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType("image/jpeg") // Adjust based on actual image type
                    .build();

            s3Client.putObject(putObjectRequest, software.amazon.awssdk.core.sync.RequestBody.fromBytes(imageBytes));

            String s3ImageUrl = "https://" + bucketName + ".s3." + region.toString() + ".amazonaws.com/" + fileName;
            log.info("S3ImageService: Image uploaded to S3 at {}", s3ImageUrl);
            return s3ImageUrl;
        } catch (S3Exception e) {
            log.error("S3ImageService: Failed to upload image to S3: {}", e.awsErrorDetails().errorMessage(), e);
            throw new ImageProcessingException("Failed to upload image to S3: " + fileName, e);
        }
    }

    /**
     * Helper method to extract file extension from URL.
     * Defaults to 'jpg' if extraction fails.
     */
    private String getFileExtension(String imageUrl) {
        String fileExtension = "jpg"; // default
        int lastDot = imageUrl.lastIndexOf('.');
        if (lastDot != -1 && lastDot < imageUrl.length() - 1) {
            fileExtension = imageUrl.substring(lastDot + 1);
            // Optionally, validate the extension against allowed types
        }
        return fileExtension;
    }
}
