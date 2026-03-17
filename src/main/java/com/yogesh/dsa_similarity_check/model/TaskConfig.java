package com.yogesh.dsa_similarity_check.model;

public record TaskConfig(
        String name,
        String systemPrompt,
        String template,
        String model,
        double temperature,
        boolean enableGoogleSearch
) {}