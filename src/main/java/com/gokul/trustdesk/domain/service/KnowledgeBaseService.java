package com.gokul.trustdesk.domain.service;

import com.gokul.trustdesk.application.rest.dto.DocumentSearchResponse;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class KnowledgeBaseService {

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;

    public KnowledgeBaseService(EmbeddingModel embeddingModel, EmbeddingStore<TextSegment> embeddingStore) {
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
    }

    public List<DocumentSearchResponse> search(String query) {
        Embedding queryEmbedding = embeddingModel.embed(query).content();

        EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(3)
                .minScore(0.6)
                .build();

        EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(searchRequest);

        return searchResult.matches().stream()
                .map(match -> {
                    TextSegment segment = match.embedded();
                    return new DocumentSearchResponse(
                            segment.metadata().getString("doc_id"),
                            segment.metadata().getString("title"),
                            segment.text().substring(0, Math.min(segment.text().length(), 200)) + "...",
                            match.score()
                    );
                })
                .collect(Collectors.toList());
    }
}