package com.opsara.sodagent.controller;

import com.opsara.sodagent.tools.SODAgentTools;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@RestController
@RequestMapping("/sodagent")

public class SODAgentController {

    private static final Logger logger = LoggerFactory.getLogger(SODAgentController.class);
    private static final String OPENAI_API_KEY = "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";




    @PostMapping("/chat")
    public ResponseEntity<AgentResponse> chat(@RequestBody AgentRequest request) {
        logger.info("/chat called with : {}", request.getQuery());


        return ResponseEntity.ok(new AgentResponse(true, mainExecution(request.getQuery())));

        //return ResponseEntity.status(401).body(new CalculatorResponse(false, "Invalid email or password"));
    }

    @PostMapping("/upload")
    public ResponseEntity<UploadResponse> handleFileUpload(
            @RequestParam("file") MultipartFile file) throws IOException {
        logger.info("Handling file upload: {}", file.getOriginalFilename());
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    new UploadResponse(false, "File is empty")
            );
        }

        parseGenerateJSONStoreAgainstOrg(file.getInputStream());

        logger.info("Your Checklist is Stored in Database successfully");



        UploadResponse response = new UploadResponse(
                true,
                "Your Checklist is Stored in Database successfully"
        );

        return ResponseEntity.ok(response);
    }


    private void parseGenerateJSONStoreAgainstOrg(InputStream inputStream) {
        logger.info("Parsing the file against org");
    }

    private String mainExecution(String query) {

        OpenAiChatModel model = OpenAiChatModel.builder()
                .apiKey(OPENAI_API_KEY)
                .modelName("gpt-4o-mini")
                // https://docs.langchain4j.dev/integrations/language-models/open-ai#structured-outputs-for-tools
                .build();

        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                .tools(new SODAgentTools())
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .build();
        String answer = assistant.chat(query);

        return answer;
    }

    @Data
    private static class AgentRequest {
        private String query;
    }

    @Data
    @AllArgsConstructor
    private static class AgentResponse {
        private boolean success;
        private String answer;
    }


    interface Assistant {

        String chat(String userMessage);
    }


    @Data
    @AllArgsConstructor
    public static class UploadResponse {
        private boolean success;
        private String message;
    }

}
