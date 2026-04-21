package com.MyFarmerApp.MyFarmer.ai.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

public class GroqModels {

    @Data
    @Builder
    public static class ChatRequest {
        private String model;
        private List<Message> messages;
        private Double temperature;
        
        @JsonProperty("response_format")
        private Map<String, String> responseFormat;
    }

    @Data
    @Builder
    public static class Message {
        private String role;
        private String content;
    }

    @Data
    public static class ChatResponse {
        private List<Choice> choices;
    }

    @Data
    public static class Choice {
        private Message message;
    }
}
