package com.cluely.audio_chunks.config;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Path;

@Configuration
public class StorageConfig {

    @Value("${cluely.storage.audio-chunks-path:${user.home}/cluely/audio-chunks}")
    private String audioChunksPath;

    @Bean
    public Path audioChunksStorageLocation() {
        Path path = Paths.get(audioChunksPath);
        try {
            Files.createDirectories(path);
            return path;
        } catch (IOException e) {
            throw new RuntimeException("Could not create audio chunks storage directory: " + audioChunksPath, e);
        }
    }
}
