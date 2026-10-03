package com.gokul.trustdesk.application.config;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
@Profile("test")
public class TestDependencyConfig {

    @Bean
    @Primary
    public EmbeddingModel mockEmbeddingModel() {
        return new EmbeddingModel() {
            @Override
            public Response<List<Embedding>> embedAll(List<TextSegment> textSegments) {
                List<Embedding> dummyEmbeddings = textSegments.stream()
                        .map(segment -> Embedding.from(new float[768]))
                        .collect(Collectors.toList());
                return Response.from(dummyEmbeddings);
            }
        };
    }

    @Bean
    @Primary
    public EmbeddingStore<TextSegment> mockEmbeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }

    @Bean
    @Primary
    public ChatLanguageModel mockChatLanguageModel() {
        // A simple lambda to satisfy the dependency without hitting the network
        return messages -> Response.from(AiMessage.from("Fallback mock response"));
    }
}