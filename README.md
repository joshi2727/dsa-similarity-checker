# AI-Powered DSA Problem Similarity Checker 

An Applied Generative AI web application built with Spring Boot and the Google Gemini API. This tool helps Data Structures and Algorithms (DSA) problem setters ensure their drafted questions are truly unique by analyzing the core algorithmic pattern and searching the live internet for existing matches.

## 💡 The Problem

Creating unique competitive programming problems is difficult because the underlying algorithms (like Dynamic Programming, Monotonic Stacks, or Dijkstra's) are finite. It is incredibly easy to mask a classic problem with a new "story" (e.g., changing spaceships to apples), but ensuring the underlying mathematical logic is unique across platforms like Codeforces or LeetCode is highly manual.

This application acts as an AI gatekeeper. It strips away the narrative of a drafted problem, identifies the core algorithmic requirement, and actively searches for similar logic online.

## ✨ Key Features

* **Bring Your Own Key (BYOK) Architecture:** Designed for infinite scalability and zero hosting liability. The backend acts as a stateless proxy; users input their own Gemini API key in the UI, which is securely passed to Google and immediately discarded.
* **Live Web Search Grounding:** Bypasses standard LLM hallucinations by granting the AI tool access to live Google Search. It returns verified, clickable URLs to similar existing problems.
* **Modular Prompt Architecture:** The core Java logic acts as a generic AI wrapper. All system instructions, prompt templates, and model parameters (`gemini-2.5-flash`) are dynamically loaded from a JSON configuration file, adhering to the Open-Closed Principle.
* **Modern UI:** A responsive, lightweight HTML/JS frontend featuring animated loaders, step-by-step progress tracking, and dynamic Markdown rendering via `marked.js`.

## 🛠️ Tech Stack

* **Backend:** Java 17+, Spring Boot (Spring Web), Gson
* **AI Integration:** Google Gemini API (`gemini-2.5-flash`), Google Search Grounding
* **Frontend:** HTML5, CSS3, Vanilla JavaScript
* **Build Tool:** Maven

## 📂 Architecture Highlight

This project strictly follows the **Spring MVC Layered Architecture** for clean separation of concerns:
* `controller/`: Handles REST endpoints and HTTP request/response mapping.
* `service/`: Contains the core business logic and `GeminiAI` API integration (`@Service`).
* `model/`: Data structures and records (e.g., `TaskConfig`, `ProblemRequest`).
* `util/`: Helper classes for text processing and prompt templating.

## 💻 Running the Project Locally

### Prerequisites
* Java 17 or higher installed
* Maven installed
* A free [Google Gemini API Key](https://aistudio.google.com/) (Required for usage in the browser)

### Setup Steps
1. **Clone the repository:**
   ```bash
   git clone https://github.com/joshi2727/dsa-similarity-checker.git
   cd dsa-similarity-checker
2. **Build and Run the Spring Boot Server:**
```bash
./mvnw spring-boot:run
```
or if using IntelliJ IDEA just click on the ▶ button
(Note: Because of the BYOK architecture, you do not need to set up any local environment variables for API keys before running the server!)

3. **Access the Dashboard:**
Open your browser and navigate to http://localhost:8080.

4. **Test the Application:**
  - Paste your Gemini API Key in the top input field.
  - Paste a drafted DSA problem in the main text area.
  -  Click "Analyze for Similarity" and watch the AI break down your algorithm!

🔮 Future Enhancements
  - Database Caching: Integrate Spring Data JPA with PostgreSQL to hash problem statements (SHA-256) and cache AI responses, completely bypassing API calls for previously checked problems.
  - Async Batch Processing: Allow setters to upload a full contest set (4-5 problems) and analyze them in parallel using @Async CompletableFutures.
