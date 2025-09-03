package com.opsara.sodagent.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opsara.sodagent.dto.ProblematicCheckpoint;
import com.opsara.sodagent.entities.OrganisationChecklist;
import com.opsara.sodagent.entities.UserChecklistData;
import com.opsara.sodagent.services.SODAgentService;
import com.opsara.sodagent.util.URLGenerationUtil;
import dev.langchain4j.agent.tool.Tool;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import static com.opsara.sodagent.constants.Constants.CDN_BASE_URL;

@Data
public class SODAgentTools {


    private SODAgentService service;
    private String organisationId;
    private static final Logger logger = LoggerFactory.getLogger(SODAgentTools.class);
    private static final List<String> CHECK_POINTS = List.of("Have doors been unlocked and the alarm system disabled?", "Have overnight security alerts or messages been reviewed?", "Is the CCTV system functioning and recording?", "Have all floors been swept and mopped?", "Have surfaces, shelves, mannequins, and display units been dusted?", "Are mirrors, glass doors, and fitting room areas clean?", "Have trash bins been emptied and liners replaced?", "Is the air-conditioning or ventilation working and set to a comfortable level?", "Is all store lighting switched on and functioning?", "Is background music playing at the correct volume?", "Are window displays clean, neat, and on theme?", "Are in-store displays set as per the planogram or promotional guidelines?", "Are mannequins dressed and positioned correctly?", "Are shelves and racks fully stocked with no empty gaps?", "Are all garments steamed/ironed and presentable?", "Have overnight deliveries been checked and reconciled with invoices?", "Are new arrivals tagged and priced?", "Are high-margin or new collection items placed in prime locations?", "Are adequate sizes and colors available for fast-moving SKUs?", "Are all POS systems powered on and operational?", "Is the sales software logged in and connected?", "Has the opening cash float been counted and prepared?", "Are receipt printers, barcode scanners, and payment terminals working?", "Has the team briefing been conducted for daily sales targets and promotions?", "Have staff roles and floor coverage been assigned?", "Have important updates from head office been shared?", "Are fire exits checked and clear?", "Are fire extinguishers accessible?", "Have fitting room emergency buttons been tested?", "Are there no tripping hazards on the shop floor?", "Is welcome signage and promotional material placed and visible?", "Are fitting rooms stocked with hangers, hooks, and clean seating?", "Are shopping bags, tissue paper, and gift wraps ready?", "Is hand sanitizer available at entrance and cash counter?");

    public SODAgentTools(SODAgentService service) {
        this.service = service;
    }

    public SODAgentTools(SODAgentService service, String organisationId) {
        this.service = service;
        this.organisationId = organisationId;
    }



    @Tool("Initialise SOD Agent with a list of sample check points to track daily.")
    public String init() {

        logger.info("Init called .... organisation ID is " + organisationId);

        Integer orgId = Integer.valueOf(organisationId);
        List<String> checklist = new ArrayList<>();
        checklist.addAll(CHECK_POINTS);
        // Represent as JSON: { "checklist": [ ... ] }
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> jsonMap = Map.of("checklist", checklist);
        String checkListJson = "{}";
        try {
            checkListJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonMap);
        } catch (JsonProcessingException e) {
            logger.error("Error converting checklist to JSON", e);
        }

        logger.info("Init called .... 3");


        OrganisationChecklist existingChecklist = service.fetchLatestActiveChecklist(orgId);
        String responseString = "";
        if (existingChecklist == null) {
            service.saveChecklist(orgId, checkListJson);
            logger.info("service.saveChecklist called  ....");
            String downloadChecklistURL = downloadDefaultChecklist();
            String hashtoken = null;
            try {
                hashtoken = URLGenerationUtil.generateHash("preview@opsara.io", "email", "01092025-00:00", organisationId);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            responseString += new String("SOD Agent is successfully initialised for your organisation. A default template is copied. \n");
            responseString += "You can see how the form would look to your store managers here: \n";
            responseString += "fillsodchecklist?hashtoken=" + hashtoken + " \n";
            responseString += "If you want to change it, you can download the checklist from here, edit it and upload back the modified checklist. \n";
            responseString += downloadChecklistURL;
            responseString += "Or you may directly want to roll out to your store managers. \n";
            responseString += "For rolling out use the prompt like Roll out to Name at mobile\n";
        } else {
            int status = existingChecklist.getStatus() != null ? existingChecklist.getStatus() : 0;
            switch (status) {
                case 0:
                    logger.info("case 0 called  ....");
                    String downloadChecklistURL = downloadDefaultChecklist();
                    String hashtoken = null;
                    try {
                        hashtoken = URLGenerationUtil.generateHash("preview@opsara.io", "email", "01092025-00:00", organisationId);
                        logger.info("Hash Token generated with in INIT as " + hashtoken);

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    responseString += new String("SOD Agent is successfully initialised for your organisation. A default template is copied. \n");
                    responseString += "You can see how the form would look to your store managers here: \n";
                    responseString += "fillsodchecklist?hashtoken=" + hashtoken + " \n";
                    responseString += "If you want to change it, you can download the checklist from here, edit it and upload back the modified checklist. \n";
                    responseString += downloadChecklistURL;
                    responseString += "Or you may directly want to roll out to your store managers. \n";
                    responseString += "For rolling out use the prompt like Roll out to Name at mobile\n";
                    break;
                case 1:
                    logger.info("case 1 called  ....");
                    responseString += new String("SOD Agent is already initialised with default checklist. Click here to download , edit and upload the checklist as per your choice. \n");
                    responseString += "Otherwise Tell you do not want to edit and want to stay with default checklist. I will not remind you again. \n";
                    break;
                case 2:
                    logger.info("case 2 called  ....");
                    responseString += new String("Your SOD is initialised as per your template. Roll it out now \n");
                    responseString += "For rolling out use the promt like Roll out to Name at mobile/email. \n";
                    break;
                case 3:
                    logger.info("case 3 called  ....");
                    responseString += new String("Your default checklist is already rolled out to few users. How ever I recommend you see it once , may be you want to edit and upload.\n");
                    responseString += "Otherwise Tell you do not want to edit and want to stay with default checklist. I will not remind you again. \n";
                    break;
                case 5:
                    logger.info("case 5 called  ....");
                    responseString += new String("Hello Again. 3 of 5 people have already filled data. you can query for same.\n");
                    responseString += new String("See sample prompts to query insights in tray on left hand side.\n");
                    break;
                default:
                    logger.info("case default called  ....");
                    responseString += new String("Hello. See sample prompts to query insights in tray on left hand side. \n");
            }

        }

        logger.info("Response String: " + responseString);

        return responseString;
    }


    @Tool("Download a default SOD checklist file containing all the check points.")
    public String downloadDefaultChecklist() {
        logger.info("Download Called.");

        OrganisationChecklist existingChecklist = service.fetchLatestActiveChecklist(Integer.valueOf(organisationId));
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
            S3Client s3Client = S3Client.builder().region(Region.of("us-east-1")).endpointOverride(URI.create("https://s3.us-east-1.amazonaws.com")).build();
            String bucketName = "opsara-sod";
            String key = fileName;
            PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(bucketName).key(key).contentType("text/csv").build();
            try {
                s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(csvStream, csvContent.getBytes(StandardCharsets.UTF_8).length));
            } catch (Exception e) {
                logger.error("Error uploading checklist CSV to S3", e);
                // fallback to hardcoded file
                String fallbackFileName = "SODChecks.txt";
                String fallbackUrl = CDN_BASE_URL + fallbackFileName;
                String fallbackLink = "<a href=\"" + fallbackUrl + "\" target=\"_blank\">Download SODChecks.txt</a>";
                return fallbackLink;
            }
            String s3Url = "https://" + bucketName + ".s3.amazonaws.com/" + key;
            return "<a href=\"" + s3Url + "\" target=\"_blank\">Download Checklist CSV</a>";
        } else {
            // fallback to hardcoded file
            String fileName = "SODChecks.txt";
            String completeUrl = CDN_BASE_URL + fileName;
            String downLoadLink = "<a href=\"" + completeUrl + "\" target=\"_blank\">Download SODChecks.txt</a>";
            return downLoadLink;
        }
    }

    @Tool("Parses CSV and extracts mobile numbers to return a List of Strings containing mobile numbers.")
    public List<String> extractMobileNumbersFromCSV(String csvMobiles) {
        logger.info("Extract Mobile Numbers Called with csvMobiles: " + csvMobiles);
        List<String> mobileNumbers = new ArrayList<>();
        String[] lines = csvMobiles.split("\n");
        for (String line : lines) {
            String[] parts = line.split(",");
            for (String part : parts) {
                part = part.trim();
                if (!part.isEmpty() && part.matches("\\d{10}")) { // Assuming 10-digit mobile numbers
                    mobileNumbers.add(part);
                }
            }
        }
        return mobileNumbers;
    }

    @Tool("Rolls out the checklist to provided mobile numbers.")
    public String rolloutToMobiles(List<String> mobileNumbers) {
        logger.info("Rollout is called with mobile numbers. + mobileNumbers: " + mobileNumbers);
        if (mobileNumbers.isEmpty()) {
            return "No mobile numbers provided. Please provide a list of mobile numbers to send the checklist.";
        }

        logger.info("fetchLatestActiveChecklist getting called with orgId: " + organisationId);
        OrganisationChecklist checklist = service.fetchLatestActiveChecklist(Integer.valueOf(organisationId));
        if (checklist != null) {
            if (checklist.getStatus() != 2) {
                checklist.setStatus(3);
            } else {
                checklist.setStatus(5);
            }
            service.saveChecklist(checklist);
        }

        mobileNumbers.forEach(mobile -> service.saveWhatsappUser(mobile, null, Integer.valueOf(organisationId)));


        return "We have stored the mobile numbers and would send them whatsapp messages to fill in checklist";
    }

    @Tool("Rolls out the checklist to provided map of mobile numbers and user names.")
    public String rolloutToMobileNameMap(Map<String, String> mobileNameMaps) {
        logger.info("Rollout is called with mobile numbers. mobileNameMaps: " + mobileNameMaps);

        mobileNameMaps.forEach((k, v) -> logger.info("Key : " + k + " Value : " + v));

        OrganisationChecklist checklist = service.fetchLatestActiveChecklist(Integer.valueOf(organisationId));
        if (checklist != null) {
            if (checklist.getStatus() != 2) {
                checklist.setStatus(3);
            } else {
                checklist.setStatus(5);
            }
            service.saveChecklist(checklist);
        }

        mobileNameMaps.forEach((mobile, name) -> service.saveWhatsappUser(mobile, name, Integer.valueOf(organisationId)));

        return "We have stored the mobile numbers and user names and would send them whatsapp messages to fill in checklist";
    }


    @Tool("Returns Completion Status. It tells how many stores out of how many have completed the checklist.")
    public String returnCompletionStatus() {

        List<String> allUserCredentials = service.getAllWhatsappUserCredentialsByOrgId(Integer.valueOf(organisationId));
        int totalUsers = allUserCredentials.size();

        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddMMyyyy"));
        List<String> filledUserCredentials = service.getUserChecklistDataFilledForPeriodAndOrg(today, Integer.valueOf(organisationId));

        Set<String> filledSet = new HashSet<>(filledUserCredentials);
        List<String> notFilled = new ArrayList<>();
        for (String user : allUserCredentials) {
            if (!filledSet.contains(user)) {
                notFilled.add(user);
            }
        }

        int notFilledCount = notFilled.size();
        String notFilledList = String.join(", ", notFilled);

        return notFilledCount + " out of " + totalUsers + " stores have not yet completed the checklist. "
                + (notFilledCount > 0 ? "Pending: " + notFilledList + ". " : "")
                + "Let me know if you want to send a reminder to them.";
    }

    @Tool("Reminder for completion. It sends a reminder to stores that have not completed the checklist.")
    public String sendCompletionReminder() {


        List<String> allUserCredentials = service.getAllWhatsappUserCredentialsByOrgId(Integer.valueOf(organisationId));
        int totalUsers = allUserCredentials.size();

        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddMMyyyy"));
        List<String> filledUserCredentials = service.getUserChecklistDataFilledForPeriodAndOrg(today, Integer.valueOf(organisationId));

        Set<String> filledSet = new HashSet<>(filledUserCredentials);
        List<String> notFilled = new ArrayList<>();
        for (String user : allUserCredentials) {
            if (!filledSet.contains(user)) {
                notFilled.add(user);
            }
        }

        // send whatsapp message to notFilled users
        return "Reminder sent to stores that have not completed the checklist. They will receive a WhatsApp message shortly.";
    }

    @Tool("Returns Leaderboard based on descipline of filling checklist from top. It returns the list of top {numberofStoresToReturn} users who are regular in filling.")
    public String giveLeaderBoardfFomTop(int numberofStoresToReturn) {
        String returnString = "";
        List<Object[]> results = service.getTopActiveUsersString(Integer.valueOf(organisationId), numberofStoresToReturn);

        StringBuilder sb = new StringBuilder("Top " + numberofStoresToReturn + " users in terms of descipline to fill over last month:\n");
        for (Object[] row : results) {
            sb.append(row[0]).append(": ").append(row[1]).append(" entries\n");
        }
        return sb.toString();
    }

    @Tool("Returns Detailed Report for a store. It returns the detailed report for a store with name storeName.")
    public String giveOneDetailedReport(String storeName) {
        String message = "Detailed report for " + storeName + ":\n";
        String downLoadLink = "<a href=\"store360?storename=" + storeName + "\" target=\"_blank\">storeName 360</a>";
        return message + downLoadLink;
    }

    @Tool("Returns Most Problematic Check Points. It returns the list of most problematic check points based on the checklist filled by stores.")
    public List<ProblematicCheckpoint> giveMostProblematicCheckPoints() {
        return service.giveMostProblematicCheckPoints();
    }

    @Tool("Download Report. It generates a csv file report based on the date range provided and returns a download link.")
    public String downloadReport(LocalDateTime fromDateTime, LocalDateTime toDateTime) {
        logger.info("Download Report Called with fromDateTime: " + fromDateTime + " toDateTime: " + toDateTime);

        // Fetch data
        List<UserChecklistData> checklistDataList = service.getUserChecklistDataBetweenDatesAndOrg(Integer.valueOf(organisationId), fromDateTime, toDateTime);

        ObjectMapper objectMapper = new ObjectMapper();

        // 1. Collect all unique questions
        Set<String> allQuestions = new LinkedHashSet<>();
        List<Map<String, String>> answersList = new ArrayList<>();

        for (UserChecklistData data : checklistDataList) {
            Map<String, String> answers = new LinkedHashMap<>();
            try {
                Map<String, Object> root = objectMapper.readValue(data.getDataJson(), new TypeReference<Map<String, Object>>() {
                });
                List<Map<String, Object>> responses = (List<Map<String, Object>>) root.get("checklist_responses");
                if (responses != null) {
                    for (Map<String, Object> resp : responses) {
                        String question = (String) resp.get("question");
                        String answer = (String) resp.get("answer");
                        answers.put(question, answer);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            allQuestions.addAll(answers.keySet());
            answersList.add(answers);
        }

        // 2. Write CSV header
        List<String> header = new ArrayList<>();
        header.add("userCredential");
        header.add("filledForPeriod");
        header.addAll(allQuestions);

        StringBuilder csv = new StringBuilder();
        csv.append(String.join(",", header)).append("\n");

        // 3. Write each row
        for (int i = 0; i < checklistDataList.size(); i++) {
            UserChecklistData data = checklistDataList.get(i);
            Map<String, String> answers = answersList.get(i);
            List<String> row = new ArrayList<>();
            row.add(data.getUserCredential());
            row.add(data.getFilledForPeriod());
            for (String question : allQuestions) {
                row.add(answers.getOrDefault(question, ""));
            }
            csv.append(String.join(",", row)).append("\n");
        }

        String csvContent = csv.toString();

        // Upload to S3 (pseudo code, replace with your S3 client)
        String fileName = "UserChecklistReport_" + fromDateTime + "_to_" + fromDateTime + ".csv";
        InputStream csvStream = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));
        S3Client s3Client = S3Client.builder().region(Region.of("us-east-1")).endpointOverride(URI.create("https://s3.us-east-1.amazonaws.com")).build();

        String bucketName = "opsara-sod";
        String key = fileName;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(bucketName).key(key).contentType("text/csv").build();

        try {
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(csvStream, csvContent.getBytes(StandardCharsets.UTF_8).length));
        } catch (Exception e) {
            logger.error("Error uploading report to S3", e);
        }


        // Construct the S3 URL (public access or presigned URL as per your setup)
        // TODO - Generate presigned URL with expiry
        String s3Url = "https://" + bucketName + ".s3.amazonaws.com/" + key;


        return "Download your report here: " + s3Url;
    }

    @Tool("Skip Edit Checklist. Skips the edit checklist reminder and starts for next stage which is rollout.")
    public void skipEditReminder() {
        logger.info("User chose to skip editing the checklist and proceed with rollout.");
        service.markChecklistStatusAsTwo(Integer.valueOf(organisationId));

    }

}
