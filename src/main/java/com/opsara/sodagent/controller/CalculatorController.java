package com.opsara.sodagent.controller;

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

import static org.springframework.ai.openai.api.OpenAiApi.ChatModel.GPT_4_O_MINI;
import static org.springframework.ai.openai.api.OpenAiApi.ChatModel.GPT_3_5_TURBO;
import static com.opsara.sodagent.constants.Constants.OPENAI_API_KEY;

@RestController
@RequestMapping("/api")

public class CalculatorController {

    private static final Logger logger = LoggerFactory.getLogger(CalculatorController.class);

    @PostMapping("/auth/askme")
    public ResponseEntity<CalculatorResponse> askMe(@RequestBody CalculatorRequest request) {
        logger.info("/login called with : {}", request.getQuery());


        return ResponseEntity.ok(new CalculatorResponse(true, mainExecution(request.getQuery())));

        //return ResponseEntity.status(401).body(new CalculatorResponse(false, "Invalid email or password"));
    }

    public String mainExecution(String query) {

        OpenAiChatModel model = OpenAiChatModel.builder()
                .apiKey(OPENAI_API_KEY)
                .modelName("gpt-4o-mini")
                // https://docs.langchain4j.dev/integrations/language-models/open-ai#structured-outputs-for-tools
                .build();

        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                .tools(new Calculator())
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .build();

        //String question = "What is the square root of the sum of the numbers of letters in the words \"hello\" and \"world\"?";

        String answer = assistant.chat(query);

        return answer;
    }


    @Data
    public static class CalculatorRequest {
        private String query;
    }

    @Data
    @AllArgsConstructor
    public static class CalculatorResponse {
        private boolean success;
        private String answer;
    }



    static class Calculator {

        @Tool("Calculates the length of a string")
        int stringLength(String s) {
            System.out.println("Called stringLength with s='" + s + "'");
            return s.length();
        }

        @Tool("Calculates the sum of two numbers")
        int add(int a, int b) {
            System.out.println("Called add with a=" + a + ", b=" + b);
            return a + b;
        }

        @Tool("Calculates the square root of a number")
        double sqrt(int x) {
            System.out.println("Called sqrt with x=" + x);
            return Math.sqrt(x);
        }
    }

    interface Assistant {

        String chat(String userMessage);
    }

}
