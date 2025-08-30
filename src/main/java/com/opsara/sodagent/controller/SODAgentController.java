package com.opsara.sodagent.controller;

import com.opsara.sodagent.services.SODAgentService;
import com.opsara.sodagent.tools.SODAgentTools;
import com.opsara.sodagent.util.URLGenerationUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
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

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.opsara.sodagent.constants.Constants.OPENAI_API_KEY;
import com.opsara.sodagent.entities.OrganisationChecklist;


@RestController
@RequestMapping("/sodagent")

public class SODAgentController {

    private static final Logger logger = LoggerFactory.getLogger(SODAgentController.class);
    private static final String MY_CODE = "SOD";

    @Autowired
    SODAgentService sodagentService;

    @PostMapping("/initialise")
    public ResponseEntity<?> initialise(HttpServletRequest httpRequest) throws IOException {
        logger.info("/initialise called with ");


        InputStream inputStream = SODAgentController.class.getClassLoader().getResourceAsStream("static/SODChecks.txt");
        String checkListJson = parseGenerateJSONStoreAgainstOrg(inputStream);


        String organisationId = (String) httpRequest.getAttribute("organisationId");

        Integer orgId = Integer.valueOf(organisationId);


        OrganisationChecklist existingChecklist = sodagentService.fetchLatestActiveChecklist(orgId);
        String responseString = "";
        if (existingChecklist == null) {
            sodagentService.saveChecklist(orgId, checkListJson);
            String userCredentials = (String) httpRequest.getAttribute("userCredentials");
            responseString += new String("SOD Agent is successfully initialised for your organisation. A default template is copied. \n");
            responseString += "You can see how the form would look to your store managers here: \n";
            responseString += "https://sod-frontend.opsara.com/fillchecklist/?userCredential=" + userCredentials + "&userCredentialType=email&forPeriod=2023-Q3 \n";
            responseString += "If you want to change it, you can download the checklist from here, edit it and upload back the modified checklist. \n";
            responseString += "https://sod-frontend.opsara.com/downloadchecklist \n";
            responseString += "Or you may directly want to roll out to your store managers. \n";
            responseString += "For rolling out use the promt like Roll out to Name at mobile/email. \n";
        } else {
            int status = existingChecklist.getStatus() != null ? existingChecklist.getStatus() : 0;
            switch (status){
                case 1:
                    responseString += new String("SOD Agent is already initialised with default checklist. Click here to download , edit and upload the checklist as per your choice. \n");
                    responseString += "Otherwise Tell you do not want to edit and want to stay with default checklist. I will not remind you again. \n";
                    break;
                case 2:
                    responseString += new String("Your SOD is initialised as per your template. Roll it out now \n");
                    responseString += "For rolling out use the promt like Roll out to Name at mobile/email. \n";
                    break;
                case 3:
                    responseString += new String("Your default checklist is already rolled out to few users. How ever I recommend you see it once , may be you want to edit and upload.\n");
                    responseString += "Otherwise Tell you do not want to edit and want to stay with default checklist. I will not remind you again. \n";
                    break;
                case 5:
                    responseString += new String("Hello Again. 3 of 5 people have already filled data. you can query for same.\n");
                    responseString += new String("See sample prompts to query insights in tray on left hand side.\n");
                    break;
                default:
                    responseString += new String("Hello. See sample prompts to query insights in tray on left hand side. \n");
            }
            // Use existingChecklist.getStatus() or other fields as needed
        }
        logger.info("Your Checklist is Stored in Database successfully");
        // Validate claims. Get organisatin. Get Purchaed Agent. Find SOD. Not found throw UnAuthorised



        return ResponseEntity.ok(responseString);

        //return ResponseEntity.status(401).body(new CalculatorResponse(false, "Invalid email or password"));
    }



    @PostMapping("/chat")
    public ResponseEntity<AgentResponse> chat(@RequestBody AgentRequest request, HttpServletRequest httpRequest) {
        logger.info("/chat called with : {}", request.getQuery());

        // Validate claims. Get organisatin. Get Purchaed Agent. Find SOD. Not found throw UnAuthorised

        String organisationId = (String) httpRequest.getAttribute("organisationId");
        String userCredentials = (String) httpRequest.getAttribute("userCredentials");

        return ResponseEntity.ok(new AgentResponse(true, mainExecution(request.getQuery())));

        //return ResponseEntity.status(401).body(new CalculatorResponse(false, "Invalid email or password"));
    }

    @PostMapping("/upload")
    public ResponseEntity<UploadResponse> handleFileUpload(
            @RequestParam("file") MultipartFile file, HttpServletRequest httpRequest) throws IOException {
        logger.info("Handling file upload: {}", file.getOriginalFilename());
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    new UploadResponse(false, "File is empty")
            );
        }

        String checkListJson = parseGenerateJSONStoreAgainstOrg(file.getInputStream());


        String organisationId = (String) httpRequest.getAttribute("organisationId");

        Integer orgId = Integer.valueOf(organisationId);

        sodagentService.saveChecklist(orgId, checkListJson);

        logger.info("Your Checklist is Stored in Database successfully");


        UploadResponse response = new UploadResponse(
                true,
                "Your Checklist is Stored in Database successfully"
        );

        return ResponseEntity.ok(response);
    }


    @GetMapping("/getuniqueURL")
    public ResponseEntity<String> getUniqueURL(HttpServletRequest httpRequest) {
        logger.info("/getuniqueURL called");

        String userCredential = (String) httpRequest.getParameter("userCredential");
        String userCredentialType = (String) httpRequest.getParameter("userCredentialType");
        String forPeriod = (String) httpRequest.getParameter("forPeriod");

        try {
            logger.info("calling URLGenerationUtil.generateHash with userCredential: {}, userCredentialType: {}, forPeriod: {}",
                    userCredential, userCredentialType, forPeriod);
            String hashValue = URLGenerationUtil.generateHash(userCredential, userCredentialType, forPeriod, "1");
            logger.info("/getuniqueURL called with hashValue: {}", hashValue);
            String[] decryptedValues = URLGenerationUtil.reverseHash(hashValue);
            Arrays.stream(decryptedValues).forEach(str -> logger.info("Decrypted Value: " + str));

            String uniqueURL = "/public/fillchecklist/" + hashValue;

            return ResponseEntity.ok(uniqueURL);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @PostMapping("/public/fillchecklist")
    public ResponseEntity<?> fill(@RequestBody Map<String, Object> body, HttpServletRequest httpRequest) {

        logger.info("/public/fillchecklist/ called");


        String hashtoken = (String) body.get("hashtoken");
        Object dataObj = body.get("data");

        ObjectMapper mapper = new ObjectMapper();
        boolean isValidJson = false;
        String dataJson = null;

        try {
            // Convert dataObj to JSON string and validate
            dataJson = mapper.writeValueAsString(dataObj);
            mapper.readTree(dataJson); // Throws exception if not valid JSON
            isValidJson = true;
        } catch (Exception e) {
            isValidJson = false;
        }

        logger.info("Hashtoken: " + hashtoken);
        logger.info("Data JSON: " + dataJson);
        logger.info("Is data valid JSON? " + isValidJson);

        String userCredential = null;
        String userCredentialType = null;
        String filledForPeriod = null;
        String orgId = null;



        try {
            String[] decryptedValues = URLGenerationUtil.reverseHash(hashtoken);
            Arrays.stream(decryptedValues).forEach(str -> logger.info("Decrypted Value: " + str));
            userCredential = decryptedValues[0];
            userCredentialType = decryptedValues[1];
            filledForPeriod = decryptedValues[2];
            orgId = decryptedValues[3];
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid hashtoken");
        }

        OrganisationChecklist checklistEntity = sodagentService.fetchLatestActiveChecklist(Integer.valueOf(orgId));
        String checklistTemplateJson = new String(checklistEntity.getChecklistJson().getBytes(), StandardCharsets.UTF_8);

        // 4. Validate dataJson against checklistTemplateJson
        boolean isValid = validateAgainstTemplate(dataJson, checklistTemplateJson);

        if (!isValid) {
            //return ResponseEntity.badRequest().body("Submitted data does not match checklist template");
        }

        sodagentService.saveUserChecklistData(userCredential, userCredentialType, filledForPeriod,
                checklistEntity.getId(), dataJson);
        return ResponseEntity.ok("Information submitted successfully");
    }

    private boolean validateAgainstTemplate(String dataJson, String templateJson) {
        try {

            logger.info("validateAgainstTemplate : \n " + templateJson);
            logger.info("data : \n " + dataJson);
            ObjectMapper mapper = new ObjectMapper();

            // Parse template checklist
            Map<String, Object> templateMap = mapper.readValue(templateJson, Map.class);
            List<String> templateQuestions = (List<String>) templateMap.get("checklist");

            // Parse dataJson
            Map<String, Object> dataMap = mapper.readValue(dataJson, Map.class);

            for (String question : dataMap.keySet()) {
                // 1. Question must exist in template
                if (!templateQuestions.contains(question)) {
                    return false;
                }
                // 2. Answer must be Yes, No, or Not Applicable
                Object answer = dataMap.get(question);
                if (!(answer instanceof String)) {
                    return false;
                }
                String ans = ((String) answer).trim();
                if (!ans.equals("Yes") && !ans.equals("No") && !ans.equals("Not Applicable")) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String parseGenerateJSONStoreAgainstOrg(InputStream inputStream) throws IOException {
        List<String> checklist = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                // Validation: non-empty, reasonable length
                if (!line.isEmpty()) {
                    checklist.add(line);
                }
            }
        }
        // Represent as JSON: { "checklist": [ ... ] }
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> jsonMap = Map.of("checklist", checklist);
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonMap);
    }


    private String mainExecution(String query) {

        OpenAiChatModel model = OpenAiChatModel.builder()
                .apiKey(OPENAI_API_KEY)
                .modelName("gpt-4o-mini")
                // https://docs.langchain4j.dev/integrations/language-models/open-ai#structured-outputs-for-tools
                .build();

        SODAgentTools tools = new SODAgentTools(sodagentService);
        tools.setOrgId(String.valueOf(4));
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                .tools(tools)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .build();
        String answer = assistant.chatAndInvoke(query);

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
        String chatAndInvoke(String userMessage);
    }


    @Data
    @AllArgsConstructor
    public static class UploadResponse {
        private boolean success;
        private String message;
    }



@PostMapping("/public/gettemplate")
    public ResponseEntity<String> getTemplate(@RequestBody Map<String, Object> body) {
        logger.info("/gettemplate called");

    String hashToken = (String) body.get("hashtoken");


    ObjectMapper mapper = new ObjectMapper();
    boolean isValidJson = false;
    String dataJson = null;


    logger.info("Hashtoken: " + hashToken);


    String orgId = null;



    try {
        String[] decryptedValues = URLGenerationUtil.reverseHash(hashToken);
        Arrays.stream(decryptedValues).forEach(str -> logger.info("Decrypted Value: " + str));
        orgId = decryptedValues[3];
    } catch (Exception e) {
        return ResponseEntity.badRequest().body("Invalid hashtoken");
    }

    OrganisationChecklist checklistEntity = sodagentService.fetchLatestActiveChecklist(Integer.valueOf(orgId));
    String checklistTemplateJson = new String(checklistEntity.getChecklistJson().getBytes(), StandardCharsets.UTF_8);

        return ResponseEntity.ok(checklistTemplateJson);
    }

}
