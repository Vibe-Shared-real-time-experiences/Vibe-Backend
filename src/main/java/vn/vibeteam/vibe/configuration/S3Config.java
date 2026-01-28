package vn.vibeteam.vibe.configuration;

import lombok.Getter;
import lombok.Setter;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
@ConfigurationProperties(prefix = "minio")
@Getter
@Setter
public class S3Config {
    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String region;

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(region))
                // Redirect S3 client to MinIO server endpoint
                .endpointOverride(URI.create(endpoint))
                 // Force to use path-style access for MinIO
                .forcePathStyle(true)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)
                ))
                .build();
    }

    @Bean
    public Tika tika() {
        return new Tika();
    }
}