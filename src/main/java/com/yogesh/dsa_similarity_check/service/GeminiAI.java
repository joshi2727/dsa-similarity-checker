package com.yogesh.dsa_similarity_check.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.yogesh.dsa_similarity_check.model.TaskConfig;
import com.yogesh.dsa_similarity_check.util.TemplateEngine;
import org.springframework.stereotype.Service;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class GeminiAI {

    private final HttpClient httpClient;
    private final Gson gson;
    private final Map<String, TaskConfig> tasks;

    public GeminiAI() {
        // No API key needed here anymore -> (BYOK Architecture)
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new Gson();
        this.tasks = new HashMap<>();
        loadTasks();
    }

    private void loadTasks() {
        try (Reader reader = new InputStreamReader(
                Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("tasks.json")))) {

            TaskConfig[] taskArray = gson.fromJson(reader, TaskConfig[].class);

            for (TaskConfig task : taskArray) {
                tasks.put(task.name(), task);
            }

            System.out.println("Successfully loaded " + tasks.size() + " tasks from config.");

        } catch (Exception e) {
            System.err.println("Failed to load tasks.json. Make sure it exists in src/main/resources/");
            e.printStackTrace();
        }
    }

    // Accepts userApiKey as a parameter
    public String runTask(String taskName, Map<String, String> variables, String userApiKey) throws Exception {
        TaskConfig task = tasks.get(taskName);
        if (task == null) {
            throw new IllegalArgumentException("Unknown task: " + taskName);
        }

        String prompt = TemplateEngine.render(task.template(), variables);
        String payload = buildJsonPayload(task, prompt);

        String url = String.format(
                "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s",
                task.model(),
                userApiKey
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("API Error: " + response.body());
        }

        return extractTextFromResponse(response.body());
    }

    private String buildJsonPayload(TaskConfig task, String prompt) {
        JsonObject root = new JsonObject();

        JsonObject systemInstruction = new JsonObject();
        JsonArray sysParts = new JsonArray();
        JsonObject sysText = new JsonObject();
        sysText.addProperty("text", task.systemPrompt());
        sysParts.add(sysText);
        systemInstruction.add("parts", sysParts);
        root.add("systemInstruction", systemInstruction);

        JsonArray contents = new JsonArray();
        JsonObject contentObj = new JsonObject();
        JsonArray parts = new JsonArray();
        JsonObject textObj = new JsonObject();
        textObj.addProperty("text", prompt);
        parts.add(textObj);
        contentObj.add("parts", parts);
        contents.add(contentObj);
        root.add("contents", contents);

        JsonObject config = new JsonObject();
        config.addProperty("temperature", task.temperature());
        root.add("generationConfig", config);

        if (task.enableGoogleSearch()) {
            JsonArray tools = new JsonArray();
            JsonObject toolObj = new JsonObject();
            toolObj.add("googleSearch", new JsonObject());
            tools.add(toolObj);
            root.add("tools", tools);
        }

        return gson.toJson(root);
    }

    // FIXED: Catches the full response including web links
    private String extractTextFromResponse(String jsonResponse) {
        JsonObject responseObj = gson.fromJson(jsonResponse, JsonObject.class);
        try {
            StringBuilder fullResponse = new StringBuilder();

            JsonArray parts = responseObj.getAsJsonArray("candidates")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("content")
                    .getAsJsonArray("parts");

            for (int i = 0; i < parts.size(); i++) {
                JsonObject part = parts.get(i).getAsJsonObject();
                if (part.has("text")) {
                    fullResponse.append(part.get("text").getAsString());
                }
            }

            return fullResponse.toString();

        } catch (Exception e) {
            System.err.println("JSON Parsing Error. Raw API Response: " + jsonResponse);
            return "Failed to parse the full response. Please check your terminal console for details.";
        }
    }
}