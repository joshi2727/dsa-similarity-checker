package com.yogesh.dsa_similarity_check.controller;

import com.yogesh.dsa_similarity_check.service.GeminiAI;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class SimilarityController {

    private final GeminiAI ai;

    public SimilarityController(GeminiAI ai) {
        this.ai = ai;
    }

    @PostMapping("/check-similarity")
    public Map<String, String> checkSimilarity(@RequestBody ProblemRequest request) {
        Map<String, String> response = new HashMap<>();

        // 1. Validate the API Key
        if (request.apiKey() == null || request.apiKey().trim().isEmpty()) {
            response.put("status", "error");
            response.put("message", "Gemini API Key is required.");
            return response;
        }

        // 2. Validate the Problem Statement
        if (request.problemStatement() == null || request.problemStatement().trim().isEmpty()) {
            response.put("status", "error");
            response.put("message", "Problem statement cannot be empty.");
            return response;
        }

        Map<String, String> variables = new HashMap<>();
        variables.put("problem", request.problemStatement());

        try {
            System.out.println("Processing request with user-provided API key...");

            String result = ai.runTask("web_similarity_check", variables, request.apiKey());

            response.put("status", "success");
            response.put("analysis", result);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "Task failed: Please check if your API key is valid.");
        }

        return response;
    }
}

// Update the Record to expect the apiKey from the frontend
record ProblemRequest(String problemStatement, String apiKey) {}