package com.gokul.trustdesk.application.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiEmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Value("${ai.gemini.api-key}")
    private String geminiApiKey;

    @Bean
    public EmbeddingModel embeddingModel() {
        return GoogleAiEmbeddingModel.builder()
                .apiKey(geminiApiKey)
                .modelName("gemini-embedding-001")
                .outputDimensionality(768) // Forces 768 dims to match pgvector
                .build();
    }

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore(
            @Value("${spring.datasource.username}") String user,
            @Value("${spring.datasource.password}") String password) {

        // will automatically create a table named 'embeddings'
        // with a vector column of 768 dimensions in our Postgres database!
        return PgVectorEmbeddingStore.builder()
                .host("localhost")
                .port(5432)
                .database("trustdesk")
                .user(user)
                .password(password)
                .table("embeddings")
                .dimension(768)
                .build();
    }

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return GoogleAiGeminiChatModel.builder()
                .apiKey(geminiApiKey)
                .modelName("gemini-2.5-flash") // if rate limit exceeded, use gemini-3.5-flash-lite
                .temperature(0.0)
                .build();
    }
}