package com.opsara.sodagent.controller;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.opsara.aaaservice.services.UserService;
import com.opsara.aaaservice.util.AWSUtil;
import com.opsara.aaaservice.util.URLGenerationUtil;
import com.opsara.sodagent.services.SODAgentService;
import com.opsara.sodagent.tools.SODAgentTools;
import com.opsara.sodagent.util.SODAGeneralUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;

import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

import com.fasterxml.jackson.databind.ObjectMapper;


import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.opsara.sodagent.entities.OrganisationChecklist;


import static com.opsara.sodagent.constants.Constants.*;


@RestController
@RequestMapping("/sodagent")

public class SODAgentController {

    private static final Logger logger = LoggerFactory.getLogger(SODAgentController.class);
    private static final InputStream inputStream = SODAgentController.class.getClassLoader().getResourceAsStream("static/SODChecks.txt");
    private static final String checkListJson = parseGenerateJSONStoreAgainstOrg(inputStream);

    @Autowired
    SODAgentService sodagentService;

    @Autowired
    UserService userService;

    /**
     * Initializes the SOD Agent for the requesting organization.
     * Loads the default checklist template, stores it if not already present,
     * and returns a message indicating the initialization status.
     *
     * @param httpRequest the HTTP request containing organization and user attributes
     * @return ResponseEntity with a status message about the initialization
     * @throws IOException if reading the checklist template fails
     */
    @PostMapping("/initialise")
    public ResponseEntity<?> initialise(HttpServletRequest httpRequest) throws IOException {
        logger.info("/initialise called ...");


        String organisationId = (String) httpRequest.getAttribute("organisationId");
        Integer orgId = Integer.valueOf(organisationId);
        String hashtoken = null;
        try {
            hashtoken = URLGenerationUtil.generateHash("preview@opsara.io", "email", "01092025-00:00", organisationId);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        OrganisationChecklist existingChecklist = sodagentService.fetchLatestActiveChecklist(orgId);
        String responseString = "";
        if (existingChecklist == null) {
            sodagentService.saveChecklist(orgId, checkListJson);
            logger.info("Checklist is Stored in Database successfully.");
            String downloadChecklistURL = downloadDefaultChecklist(organisationId);


            responseString = SODAGeneralUtil.generateInitMessage(downloadChecklistURL, hashtoken);
        } else {
            int status = existingChecklist.getStatus() != null ? existingChecklist.getStatus() : 0;
            switch (status) {
                case 1:
                    responseString += new String("SOD Agent is already initialised with default checklist. Click here to download , edit and upload the checklist as per your choice. \n");
                    responseString += downloadDefaultChecklist(organisationId) + "\n";
                    responseString += "If you do not want to edit and want to stay with default checklist, please let us know. I will not remind you again. \n";
                    responseString += "You can always preview how it looks to store managers here. \n";
                    responseString += "<a href=\"" + "/fillsodchecklist?hashtoken=" + hashtoken + "\" target=\"_blank\">View Checklist</a>" + " \n";
                    break;
                case 2:
                    responseString += new String("Your SOD is initialised as per your template. Roll it out now \n");
                    responseString += "For rolling out use the prompt like Roll out to Name at mobile. \n";
                    responseString += "You can always preview how it looks to store managers here. \n";
                    responseString += "<a href=\"" + "/fillsodchecklist?hashtoken=" + hashtoken + "\" target=\"_blank\">View Checklist</a>" + " \n";
                    responseString += "You can also download the checklist you uploaded here. \n";
                    responseString += downloadDefaultChecklist(organisationId) + "\n";
                    break;
                case 3:
                    responseString += new String("Your default checklist is already rolled out to few users. How ever I recommend you see it once , may be you want to edit and upload again.\n");
                    responseString += "Click here to download , edit and upload \n";
                    responseString += downloadDefaultChecklist(organisationId) + "\n";
                    responseString += "If you do not want to edit and want to stay with default checklist, please let us know. I will not remind you again. \n";
                    responseString += "You can always preview how it looks to store managers here. \n";
                    responseString += "<a href=\"" + "/fillsodchecklist?hashtoken=" + hashtoken + "\" target=\"_blank\">View Checklist</a>" + " \n";
                    break;
                case 5:
                    responseString += new String("Hello. You can start querying the data received from people you rolled it out to.\n");
                    responseString +=new String("See sample prompts to query insights in prompts guide.\n");
                    responseString += "You can always preview how it looks to store managers here. \n";
                    responseString += "<a href=\"" + "/fillsodchecklist?hashtoken=" + hashtoken + "\" target=\"_blank\">View Checklist</a>" + " \n";
                    responseString += "You can also download the checklist you uploaded here. \n";
                    responseString += downloadDefaultChecklist(organisationId) + "\n";

                    break;
                default:
                    responseString += new String("Hello. See sample prompts to query insights in prompts guide.\n");
                    responseString += "You can always preview how it looks to store managers here. \n";
                    responseString += "<a href=\"" + "/fillsodchecklist?hashtoken=" + hashtoken + "\" target=\"_blank\">View Checklist</a>" + " \n";
                    responseString += "You can also download the checklist you uploaded here. \n";
                    responseString += downloadDefaultChecklist(organisationId) + "\n";

            }
        }

        return ResponseEntity.ok(new AgentResponse(true, responseString));

    }


    /**
     * Handles chat requests by processing the user's query,
     * validating organization and user credentials, and returning the AI-generated response.
     *
     * @param request     the chat request containing the user's query
     * @param httpRequest the HTTP request with organization and user attributes
     * @return ResponseEntity containing the agent's respons
     */
    @PostMapping("/chat")
    public ResponseEntity<AgentResponse> chat(@RequestBody AgentRequest request, HttpServletRequest httpRequest) {
        logger.info("/chat called with : {}", request.getMessage());
        // TODO : Validate claims. Get organisation. Get Purchaed Agent. Find SOD. Not found throw UnAuthorised

        String organisationId = (String) httpRequest.getAttribute("organisationId");
        String userCredentials = (String) httpRequest.getAttribute("userCredentials");


        logger.info("/chat called with : {} {}", organisationId, userCredentials);

        return ResponseEntity.ok(new AgentResponse(true, mainExecution(request.getMessage(), organisationId, userCredentials)));
    }
    @Data
    private static class AgentRequest {
        private String message;
    }

    @Data
    @AllArgsConstructor
    private static class AgentResponse {
        private boolean success;
        private String answer;
    }

    /**
     * Handles the upload of a checklist file for an organization.
     * Validates the file, parses its contents into checklist JSON,
     * saves it to the database, and returns a response indicating success or failure.
     *
     * @param file the uploaded checklist file
     * @param httpRequest the HTTP request containing organization attributes
     * @return ResponseEntity with the upload result
     * @throws IOException if reading the file fails
     */
    @PostMapping("/upload")
    public ResponseEntity<UploadResponse> handleFileUpload(@RequestParam("file") MultipartFile file, HttpServletRequest httpRequest) throws IOException {
        logger.info("Handling file upload: {}", file.getOriginalFilename());
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(new UploadResponse(false, "File is empty"));
        }

        String checkListJson = parseGenerateJSONStoreAgainstOrg(file.getInputStream());
        String organisationId = (String) httpRequest.getAttribute("organisationId");
        Integer orgId = Integer.valueOf(organisationId);
        sodagentService.saveChecklist(orgId, checkListJson);
        logger.info("Your Checklist is Stored in Database successfully");

        String responseMessage = "Your Checklist is Stored in Database successfully. \n";
        responseMessage += "Now you may want to roll it out to store managers. Use prompt like \"Roll out to name at mobile\". It would roll out to given person at given mobile number. \n";
        responseMessage += "Or you can say \"Roll out to every one\". It would roll out to all store managers already existing in database.\n";
        UploadResponse response = new UploadResponse(true, responseMessage);
        return ResponseEntity.ok(response);
    }
    @Data
    @AllArgsConstructor
    public static class UploadResponse {
        private boolean success;
        private String message;
    }



    /**
     * Handles the submission of a filled checklist by a user.
     * Validates the hashtoken, checks the submitted data against the checklist template,
     * saves the data if valid, and returns a response indicating success or failure.
     *
     * @param body the request body containing hashtoken and data
     * @return ResponseEntity with the submission result
     */

    @PostMapping("/public/fillchecklist")
    public ResponseEntity<?> fill(@RequestBody Map<String, Object> body) {

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

        if("email".equals(userCredentialType)) {
            logger.info("admin was trying to fill just to try. Returning with out saving.");
            return ResponseEntity.ok("Information submitted successfully. Since it was for your trial, it will not show in any reports.");
        }

        OrganisationChecklist checklistEntity = sodagentService.fetchLatestActiveChecklist(Integer.valueOf(orgId));
        String checklistTemplateJson = new String(checklistEntity.getChecklistJson().getBytes(), StandardCharsets.UTF_8);

        // 4. Validate dataJson against checklistTemplateJson
        boolean isValid = validateAgainstTemplate(dataJson, checklistTemplateJson);

        if (!isValid) {
            // TODO : check the logic of validateAgainstTemplate and once properly tested start throwing following exception.
            //return ResponseEntity.badRequest().body("Submitted data does not match checklist template");
        }

        sodagentService.saveUserChecklistData(userCredential, userCredentialType, filledForPeriod, checklistEntity.getId(), dataJson);
        return ResponseEntity.ok("Information submitted successfully");
    }

    /**
     * Validates the submitted checklist data against the checklist template.
     * Ensures all questions in the data exist in the template and answers are valid.
     *
     * @param dataJson      the JSON string of submitted checklist data
     * @param templateJson  the JSON string of the checklist template
     * @return true if data is valid against the template, false otherwise
     */
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

    /**
     * Parses the provided input stream containing checklist items,
     * generates a JSON string * with a "checklist" array, and returns it.
     * If an error occurs, returns an empty string. * *
     *
     * @param inputStream the input stream to read checklist items from
     * @return a pretty-printed JSON string with the checklist, or an empty string on error
     *
     */
    private static String parseGenerateJSONStoreAgainstOrg(InputStream inputStream) {
        try {
            List<String> checklist = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty()) {
                        checklist.add(line);
                    }
                }
            }
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(Map.of("checklist", checklist));
        } catch (Exception e) {
            return "";
        }
    }


    private String mainExecution(String query, String organisationId, String userCredentials) {

        // TODO : play with various models and check impact on accuracy etc. And understand cost also.
        OpenAiChatModel model = OpenAiChatModel.builder().apiKey(OPENAI_API_KEY).modelName("gpt-4o-mini")
                .build();

        logger.info("OrganisationId: " + organisationId);
        SODAgentTools tools = new SODAgentTools(sodagentService, userService, organisationId);

        Assistant assistant = AiServices.builder(Assistant.class).chatModel(model).tools(tools).chatMemory(MessageWindowChatMemory.withMaxMessages(10)).build();
        String answer = assistant.chatAndInvoke(query);

        return answer;
    }




    interface Assistant {
        String chatAndInvoke(String userMessage);
    }



    /**
     * Retrieves the checklist template for the organization associated with the provided hashtoken.
     *
     * @param body the request body containing the hashtoken
     * @return ResponseEntity with the checklist template JSON or an error message
     */
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


    private String downloadDefaultChecklist(String organisationId) {
        logger.info("Download Called.");

        OrganisationChecklist existingChecklist = sodagentService.fetchLatestActiveChecklist(Integer.valueOf(organisationId));
        if (existingChecklist != null && existingChecklist.getChecklistJson() != null) {
            // Parse checklist_json to extract questions
            ObjectMapper mapper = new ObjectMapper();
            List<String> questions = new ArrayList<>();
            try {
                Map<String, Object> checklistMap = mapper.readValue(existingChecklist.getChecklistJson(), new TypeReference<Map<String, Object>>() {
                });
                questions = (List<String>) checklistMap.getOrDefault("checklist", Collections.emptyList());
            } catch (Exception e) {
                logger.error("Error parsing checklist_json", e);
                // fallback to hardcoded file
                String fileName = "SODChecks.txt";
                String completeUrl = CDN_BASE_URL + fileName;
                String downLoadLink = "<a href=\"" + completeUrl + "\" target=\"_blank\">Download SODChecks.txt</a>";
                return downLoadLink;
            }

            // Generate CSV content
            StringBuilder csv = new StringBuilder();
            for (String q : questions) {
                csv.append(q.replaceAll(",", " ")).append("\n");
            }
            String csvContent = csv.toString();

            // Upload to S3
            String fileName = "Checklist_" + organisationId + ".csv";
            InputStream csvStream = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));
            try {
                AWSUtil.uploadFileToS3(AWS_BUCKET_NAME, fileName, csvStream, csvContent.getBytes(StandardCharsets.UTF_8).length, "text/csv");
                String signedS3Url = AWSUtil.generatePresignedUrl(AWS_BUCKET_NAME, fileName);
                return "<a href=\"" + signedS3Url + "\" target=\"_blank\">Download Checklist CSV</a>";
            } catch (Exception e) {
                logger.error("Error uploading checklist CSV to S3", e);
                // fallback to hardcoded file
                String fallbackFileName = "SODChecks.txt";
                String fallbackUrl = CDN_BASE_URL + fallbackFileName;
                String fallbackLink = "<a href=\"" + fallbackUrl + "\" target=\"_blank\">Download SODChecks.txt</a>";
                return fallbackLink;
            }

        } else {
            // fallback to hardcoded file
            String fileName = "SODChecks.txt";
            String completeUrl = CDN_BASE_URL + fileName;
            String downLoadLink = "<a href=\"" + completeUrl + "\" target=\"_blank\">Download SODChecks.txt</a>";
            return downLoadLink;
        }
    }

    @PostMapping("/gettask")
    public ResponseEntity<GetTaskResponse> getTask(@RequestBody GetTaskRequest request, HttpServletRequest httpRequest) {
        logger.info("/gettask called with timezone: {}", request != null ? request.getUserMobileTimezone() : "null");

        if (request == null || request.getUserMobileTimezone() == null
                || request.getUserMobileTimezone().isEmpty()) {
            return ResponseEntity.badRequest().body(
                    new GetTaskResponse("false", "Missing required fields: userMobileTimezone in body.", null, null)
            );
        }

        ZoneId zoneId;
        try {
            zoneId = ZoneId.of(request.getUserMobileTimezone());
        } catch (DateTimeException e) {
            logger.warn("Invalid timezone provided: {}", request.getUserMobileTimezone());
            return ResponseEntity.badRequest().body(new GetTaskResponse("failure", "Invalid userMobileTimezone", null, null));
        }

        ZonedDateTime nowInUserZone = ZonedDateTime.now(zoneId);
        LocalTime cutoff = LocalTime.of(14, 0); // 2:00 PM

        if (nowInUserZone.toLocalTime().isAfter(cutoff)) {
            // After 2:00 PM in user's timezone -> return the crossed message and nulls for links
            return ResponseEntity.ok(new GetTaskResponse("success", "time to fill SOD for today is crossed.", null, null));
        }

        String sodaChecklistUrl = "fillsodchecklist?hashtoken=";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy-00:00");
        LocalDate today = LocalDate.now();
        String dateString = today.format(formatter);

        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH);
        String formattedDate = today.format(outputFormatter);

        String hash = "";

        String organisationId = (String) httpRequest.getAttribute("organisationId");
        String userCredentials = (String) httpRequest.getAttribute("userCredentials");

        try {
            hash = URLGenerationUtil.generateHash(userCredentials, "mobile", dateString, organisationId);
        } catch (Exception e) {
            // ignore and continue with empty hash
        }

        // Calculate remaining time until today's 2:00 PM in user's timezone (rounded up to nearest hour)
        ZonedDateTime cutoffZdt = ZonedDateTime.of(today, cutoff, zoneId);
        long secondsLeft = Duration.between(nowInUserZone, cutoffZdt).getSeconds();
        if (secondsLeft < 0) secondsLeft = 0;
        long hoursLeft = (secondsLeft + 3599) / 3600; // round up
        String expiryHoursText = String.valueOf(hoursLeft);

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);
        String cutoffTimeFormatted = cutoff.format(timeFormatter) + " " + zoneId.toString();

        String text = String.format("Dear User,\nRequest you to fill the SOD form for %s. It takes just 2 minutes.\nThe link will expire in %s hours.\nThank you!",
                formattedDate, expiryHoursText);

        String oneTimeUrl = sodaChecklistUrl + hash;

        GetTaskResponse response = new GetTaskResponse("success", text, oneTimeUrl, cutoffTimeFormatted);
        return ResponseEntity.ok(response);
    }

    @Data
    @AllArgsConstructor
    private static class GetTaskResponse {
        private String status;
        private String text;

        @JsonProperty("Onetimesignedurl")
        private String onetimesignedurl;

        @JsonProperty("Expiry-time")
        private String expiryTime;
    }

    @Data
    private static class GetTaskRequest {
        private String userMobileTimezone;
    }


    // java
    @PostMapping("/getuserchecklistdata")
    public ResponseEntity<UserChecklistDataResponse> getUserChecklistData(@RequestBody UserChecklistDateRangeRequest request, HttpServletRequest httpRequest) {
        logger.info("/getuserchecklistdata called with dateRange: from={} to={}",
                request != null ? request.getFromDate() : "null",
                request != null ? request.getToDate() : "null");

        String userCredentials = (String) httpRequest.getAttribute("userCredentials");
        String organisationId = (String) httpRequest.getAttribute("organisationId");

        // Resolve date range defaults
        LocalDate resolvedToDate;
        LocalDate resolvedFromDate;
        if (request == null || request.getFromDate() == null || request.getToDate() == null) {
            resolvedToDate = LocalDate.now();
            resolvedFromDate = resolvedToDate.minusWeeks(1);
        } else {
            resolvedFromDate = request.getFromDate();
            resolvedToDate = request.getToDate();
        }

        List<UserChecklistData> checklistDataList = new ArrayList<>();

        try {
            // Placeholder logic: populate sample entry (replace with real fetch using resolvedFromDate/resolvedToDate)
            String dateFilled = resolvedToDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH));

            String dateStringForHash = resolvedToDate.format(DateTimeFormatter.ofPattern("ddMMyyyy-00:00"));
            String hash = "";
            try {
                hash = URLGenerationUtil.generateHash(userCredentials == null ? "unknown" : userCredentials, "mobile", dateStringForHash, organisationId == null ? "" : organisationId);
            } catch (Exception e) {
                logger.warn("Unable to generate hash for checklist link", e);
            }

            String link = "/fillsodchecklist?hashtoken=" + hash;

            UserChecklistData sample = new UserChecklistData(dateFilled,
                    organisationId == null ? "" : organisationId,
                    "",
                    link);

            checklistDataList.add(sample);
        } catch (Exception e) {
            logger.error("Error building user checklist data", e);
            return ResponseEntity.ok(new UserChecklistDataResponse("failure", "SOD", "SOD Agent", Collections.emptyList()));
        }

        UserChecklistDataResponse response = new UserChecklistDataResponse("success", "SOD", "SOD Agent", checklistDataList);
        return ResponseEntity.ok(response);
    }

    @Data
    private static class UserChecklistDateRangeRequest {
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private LocalDate fromDate;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private LocalDate toDate;
    }

    @Data
    @AllArgsConstructor
    private static class UserChecklistDataResponse {
        private String status;
        private String agentCode;
        private String agentName;

        @JsonProperty("UserChecklistData")
        private List<UserChecklistData> userChecklistData;
    }

    @Data
    @AllArgsConstructor
    private static class UserChecklistData {
        private String dateFilled;
        private String organisationId;

        @JsonProperty("Organisation Name")
        private String organisationName;

        @JsonProperty("link")
        private String link;
    }

    @RequestMapping(value = "/sodagent/upload", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> handleOptions() {
        logger.info("OPTIONS preflight request received for /upload");
        return ResponseEntity.ok().build();
    }
}
