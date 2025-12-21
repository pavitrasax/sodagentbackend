package com.opsara.sodagent.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opsara.aaaservice.entities.StoreUser;
import com.opsara.aaaservice.services.UserService;
import com.opsara.aaaservice.util.AWSUtil;
import com.opsara.aaaservice.util.GeneralUtil;
import com.opsara.aaaservice.util.MSG91WhatsappUtil;
import com.opsara.aaaservice.util.URLGenerationUtil;
import com.opsara.sodagent.dto.ProblematicCheckpoint;
import com.opsara.sodagent.entities.OrganisationChecklist;
import com.opsara.sodagent.entities.RolloutUser;
import com.opsara.sodagent.entities.UserChecklistData;
import com.opsara.sodagent.services.SODAgentService;
import com.opsara.sodagent.util.SODAGeneralUtil;
import dev.langchain4j.agent.tool.Tool;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.opsara.sodagent.constants.Constants.*;
import static com.opsara.sodagent.constants.DefaultChecklist.ASSESSMENT_URL;
import static com.opsara.sodagent.constants.DefaultChecklist.DEFAULT_CHECKLIST_JSON;

@Data
public class SODAgentTools {


    private SODAgentService sodAgentService;
    private UserService userService;
    private String organisationId;
    private URLGenerationUtil urlGenerationUtil;
    private MSG91WhatsappUtil msg91WhatsappUtil;
    private static final Logger logger = LoggerFactory.getLogger(SODAgentTools.class);

    public SODAgentTools(SODAgentService sodAgentService) {
        this.sodAgentService = sodAgentService;
    }

    public SODAgentTools(MSG91WhatsappUtil msg91WhatsappUtil, URLGenerationUtil urlGenerationUtil, SODAgentService sodAgentService, UserService userService, String organisationId) {
        this.msg91WhatsappUtil = msg91WhatsappUtil;
        this.urlGenerationUtil = urlGenerationUtil;
        this.sodAgentService = sodAgentService;
        this.userService = userService;
        this.organisationId = organisationId;
    }


    @Tool("Initialise SOD Agent with a list of sample check points to track daily.")
    public String init() {

        logger.info("Init called .... organisation ID is " + organisationId);

        Integer orgId = Integer.valueOf(organisationId);



        OrganisationChecklist existingChecklist = sodAgentService.fetchLatestActiveChecklist(orgId);
        String responseString = "";

        String hashtoken = null;
        try {
            hashtoken = urlGenerationUtil.generateHash("preview@opsara.io", "email", "01092025-00:00", organisationId);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (existingChecklist == null) {
            sodAgentService.saveChecklist(orgId, DEFAULT_CHECKLIST_JSON);
            logger.info("service.saveChecklist called  ....");
            String downloadChecklistURL = downloadDefaultChecklist();

            responseString = SODAGeneralUtil.generateInitMessage(downloadChecklistURL, hashtoken);
        } else {
            int status = existingChecklist.getStatus() != null ? existingChecklist.getStatus() : 0;
            switch (status) {
                case 0:
                    logger.info("case 0 called  ....");
                    String downloadChecklistURL = downloadDefaultChecklist();
                    responseString = SODAGeneralUtil.generateInitMessage(downloadChecklistURL, hashtoken);
                    break;
                case 1:
                    logger.info("case 1 called  ....");
                    responseString += new String("SOD Agent is already initialised with default checklist. Click here to download , edit and upload the checklist as per your choice. \n\n");
                    responseString += downloadDefaultChecklist() + "\n\n";
                    responseString += "If you do not want to edit and want to stay with default checklist, please let us know. I will not remind you again. \n\n";
                    responseString += "You can always preview how it looks to store managers here. \n\n";
                    responseString += "[View Checklist](" + OPSARA_WEB_URL + ASSESSMENT_URL + hashtoken + ")" + " \n\n";
                    break;
                case 2:
                    logger.info("case 2 called  ....");
                    responseString += new String("Your SOD is initialised as per your template. Roll it out now \n\n");
                    responseString += "For rolling out use the prompt like Roll out to Name at mobile. \n\n";
                    responseString += "You can always preview how it looks to store managers here. \n\n";
                    responseString += "[View Checklist](" + OPSARA_WEB_URL + ASSESSMENT_URL + hashtoken + ")" + " \n\n";
                    responseString += "You can also download the checklist you uploaded here. \n\n";
                    responseString += downloadDefaultChecklist() + "\n\n";
                    break;
                case 3:
                    logger.info("case 3 called  ....");
                    responseString += new String("Your default checklist is already rolled out to few users. How ever I recommend you see it once , may be you want to edit and upload again.\n\n");
                    responseString += "Click here to download , edit and upload";
                    responseString += downloadDefaultChecklist() + "\n\n";
                    responseString += "If you do not want to edit and want to stay with default checklist, please let us know. I will not remind you again. \n\n";
                    responseString += "You can always preview how it looks to store managers here. \n\n";
                    responseString += "[View Checklist](" + OPSARA_WEB_URL + ASSESSMENT_URL + hashtoken + ")" + " \n\n";
                    break;
                case 5:
                    logger.info("case 5 called  ....");
                    responseString += new String("Hello. You can start querying the data received from people you rolled it out to.\n\n");
                    responseString += new String("See sample prompts to query insights in prompts guide.\n\n");
                    responseString += "You can always preview how it looks to store managers here. \n\n";
                    responseString += "[View Checklist](" + OPSARA_WEB_URL + ASSESSMENT_URL + hashtoken + ")" + " \n\n";
                    responseString += "You can also download the checklist you uploaded here. \n\n";
                    responseString += downloadDefaultChecklist() + "\n\n";
                    break;
                default:
                    logger.info("case default called  ....");
                    responseString += new String("See sample prompts to query insights in prompts guide.\n\n");
                    responseString += "You can always preview how it looks to store managers here. \n\n";
                    responseString += "[View Checklist](" + OPSARA_WEB_URL + ASSESSMENT_URL + hashtoken + ")" + " \n\n";
                    responseString += "You can also download the checklist you uploaded here. \n\n";
                    responseString += downloadDefaultChecklist() + "\n\n";
            }

        }

        logger.info("Response String: " + responseString);

        return responseString;
    }


    @Tool("Download a default SOD checklist file containing all the check points.")
    public String downloadDefaultChecklist() {
        logger.info("Download Called.");

        OrganisationChecklist existingChecklist = sodAgentService.fetchLatestActiveChecklist(Integer.valueOf(organisationId));
        return SODAGeneralUtil.downloadDefaultChecklist(existingChecklist);
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

    @Tool("Rolls out the checklist to every store user existing in database.")
    public String rolloutToEveryOne(List<String> mobileNumbers) {
        logger.info("Rollout to everyone is called ....");
        List<StoreUser> allUsersInDB = userService.getAllStoreUsersByOrgId(Integer.valueOf(organisationId));

        Map<String, String> mobileNameMaps = new HashMap<>();
        for (StoreUser user : allUsersInDB) {
            mobileNameMaps.put(user.getMobileNumber(), user.getName());
        }
        return rolloutToMobileNameMap(mobileNameMaps);
    }

    @Tool("Rolls out the checklist to provided mobile numbers.")
    public String rolloutToMobiles(List<String> mobileNumbers) {
        logger.info("Rollout is called with mobile numbers. + mobileNumbers: " + mobileNumbers);
        if (mobileNumbers.isEmpty()) {
            return "No mobile numbers provided. Please provide a list of mobile numbers to send the checklist.";
        }

        String validationMessage = GeneralUtil.getInstance().validateMobileNumbersAndRemoveInvalid(mobileNumbers);

        logger.info("fetchLatestActiveChecklist getting called with orgId: " + organisationId);
        OrganisationChecklist checklist = sodAgentService.fetchLatestActiveChecklist(Integer.valueOf(organisationId));
        if (checklist != null) {
            if (checklist.getStatus() != 2) {
                checklist.setStatus(3);
            } else {
                checklist.setStatus(5);
            }
            sodAgentService.saveChecklist(checklist);
        }




        String sodaChecklistUrl = "fillsodchecklist?hashtoken=";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy-00:00");
        LocalDate today = LocalDate.now();
        String dateString = today.format(formatter);

        mobileNumbers.forEach(mobile -> {
            mobile = GeneralUtil.getInstance().convertMobileNumberToE164Standard(mobile);
            StoreUser savedUser = userService.saveOrUpdateStoreUser(mobile, null, Integer.valueOf(organisationId));
            sodAgentService.saveOrUpdateRolloutUser(mobile, null, Integer.valueOf(organisationId));

            String hash = "";

            try {
                hash = urlGenerationUtil.generateHash(mobile, "mobile", dateString, organisationId);
            } catch (Exception e) {
                //throw new RuntimeException(e);
            }


            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH);
            String formattedDate = today.format(outputFormatter);

            String whatsappUtilResponse = msg91WhatsappUtil.sendGenericFillFormMessageNewTemplate(mobile, savedUser.getName(), "sod form", formattedDate, sodaChecklistUrl + hash, savedUser.getOrgId());
            logger.info("whatsappUtilResponse " + whatsappUtilResponse);
        });

        String returnMessage = "";
        if (!validationMessage.isEmpty()) {
            returnMessage = "The following mobile numbers were invalid and ignored: " + validationMessage + ". ";
            returnMessage += "We have stored remaining valid mobile numbers and would send them whatsapp messages to fill in checklist";
        } else {
            returnMessage = "We have stored the mobile numbers and would send them whatsapp messages to fill in checklist";
        }

        return returnMessage;
    }

    @Tool("Roll out the checklist to one or more users by their mobile numbers and name with key as mobile number and value as name.")
    public String rolloutToMobileNameMap(Map<String, String> mobileNameMaps) {
        logger.info("Rollout is called with mobile numbers. mobileNameMaps: " + mobileNameMaps);

        if (mobileNameMaps.isEmpty()) {
            return "No mobile numbers provided. Please provide a list of mobile numbers to send the checklist.";
        }

        List<String> listOfMobiles = new ArrayList<>(mobileNameMaps.keySet());
        String validationMessage = GeneralUtil.getInstance().validateMobileNumbersAndRemoveInvalid(listOfMobiles);
        mobileNameMaps.keySet().retainAll(listOfMobiles);


        OrganisationChecklist checklist = sodAgentService.fetchLatestActiveChecklist(Integer.valueOf(organisationId));
        if (checklist != null) {
            if (checklist.getStatus() != 2) {
                checklist.setStatus(3);
            } else {
                checklist.setStatus(5);
            }
            sodAgentService.saveChecklist(checklist);
        }

        String sodaChecklistUrl = "fillsodchecklist?hashtoken=";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy-00:00");
        LocalDate today = LocalDate.now();
        String dateString = today.format(formatter);

        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH);
        String formattedDate = today.format(outputFormatter);

        mobileNameMaps.forEach((mobile, name) -> {
            mobile = GeneralUtil.getInstance().convertMobileNumberToE164Standard(mobile);
            StoreUser savedUser = userService.saveOrUpdateStoreUser(mobile, name, Integer.valueOf(organisationId));
            sodAgentService.saveOrUpdateRolloutUser(mobile, name, Integer.valueOf(organisationId));

            String hash = "";

            try {
                hash = urlGenerationUtil.generateHash(mobile, "mobile", dateString, organisationId);
            } catch (Exception e) {
                //throw new RuntimeException(e);
            }


            String whatsappUtilResponse = msg91WhatsappUtil.sendGenericFillFormMessageNewTemplate(mobile, savedUser.getName(), "sod form", formattedDate, sodaChecklistUrl + hash, savedUser.getOrgId());
            logger.info("whatsappUtilResponse " + whatsappUtilResponse);
        });








        String returnMessage = "";
        if (!validationMessage.isEmpty()) {
            returnMessage = "The following mobile numbers were invalid and ignored: " + validationMessage + ". ";
            returnMessage += "We have stored remaining valid mobile numbers and would send them whatsapp messages to fill in checklist";
        } else {
            returnMessage = "We have stored the mobile numbers and would send them whatsapp messages to fill in checklist";
        }

        return returnMessage;
    }


    @Tool("Returns Completion Status. It tells how many stores out of how many have completed the checklist.")
    public String returnCompletionStatus() {

        List<String> allUserCredentials = sodAgentService.getAllRolloutUserCredentialsByOrgId(Integer.valueOf(organisationId));
        int totalUsers = allUserCredentials.size();

        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddMMyyyy"));
        List<String> filledUserCredentials = sodAgentService.getUserChecklistDataFilledForPeriodAndOrg(today, Integer.valueOf(organisationId));

        Set<String> filledSet = new HashSet<>(filledUserCredentials);
        List<String> notFilled = new ArrayList<>();
        for (String user : allUserCredentials) {
            if (!filledSet.contains(user)) {
                notFilled.add(user);
            }
        }

        int notFilledCount = notFilled.size();
        String notFilledList = String.join(", ", notFilled);

        if (notFilledCount == 0) {
            return "All " + totalUsers + " stores have completed the checklist for today.";
        }

        return notFilledCount + " out of " + totalUsers + " stores have not yet completed the checklist. "
                + (notFilledCount > 0 ? "Pending: " + notFilledList + ". " : "")
                + "Let me know if you want to send a reminder to them.";
    }

    @Tool("Reminder for completion. It sends a reminder to stores that have not completed the checklist.")
    public String sendCompletionReminder() {
        List<RolloutUser> allUsers = sodAgentService.getAllRolloutUsersByOrgId(Integer.valueOf(organisationId));
        Map<String, String> mobileToName = new HashMap<>();
        List<String> allUserCredentials = new ArrayList<>();
        for (RolloutUser user : allUsers) {
            mobileToName.put(user.getMobileNumber(), user.getName());
            allUserCredentials.add(user.getMobileNumber());
        }

        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddMMyyyy"));
        List<String> filledUserCredentials = sodAgentService.getUserChecklistDataFilledForPeriodAndOrg(today, Integer.valueOf(organisationId));
        Set<String> filledSet = new HashSet<>(filledUserCredentials);

        List<String> notFilled = allUserCredentials.stream()
                .filter(mobile -> !filledSet.contains(mobile))
                .toList();

        if (notFilled.isEmpty()) {
            return "All stores have already completed the checklist for today. No reminders sent.";
        }

        logger.info("Sending reminders to not filled users: " + notFilled);
        String sodaChecklistUrl = "fillsodchecklist?hashtoken=";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy-00:00");
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH);
        LocalDate date = LocalDate.now();
        String dateString = date.format(formatter);
        String formattedDate = date.format(outputFormatter);

        for (String mobile : notFilled) {
            String name = mobileToName.get(mobile);
            String hash = "";
            try {
                hash = urlGenerationUtil.generateHash(mobile, "mobile", dateString, organisationId);
            } catch (Exception e) {
                // handle exception
            }
            String whatsappUtilResponse = msg91WhatsappUtil.sendGenericFillFormMessageNewTemplate(mobile, "User", "sod form", formattedDate, sodaChecklistUrl + hash, Integer.valueOf(organisationId));

            logger.info("Reminder whatsappUtilResponse " + whatsappUtilResponse);
        }

        return "Reminder sent to stores that have not completed the checklist. They will receive a WhatsApp message shortly.";
    }

    @Tool("Returns Most Problematic Check Points. It returns the list of most problematic check points based on the checklist filled by stores.")
    public List<ProblematicCheckpoint> giveMostProblematicCheckPoints() {
        return sodAgentService.giveMostProblematicCheckPoints(Integer.valueOf(organisationId));
    }

    @Tool("Download Report. It generates a csv file report based on the date range provided and returns a download link.")
    public String downloadReport(LocalDateTime fromDateTime, LocalDateTime toDateTime) {
        logger.info("Download Report Called with fromDateTime: " + fromDateTime + " toDateTime: " + toDateTime);

        // Fetch data
        List<UserChecklistData> checklistDataList = sodAgentService.getUserChecklistDataBetweenDatesAndOrg(Integer.valueOf(organisationId), fromDateTime, toDateTime);

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

        String fileName = "UserChecklistReport_" + fromDateTime + "_to_" + fromDateTime + ".csv";
        InputStream csvStream = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));

        String bucketName = "opsara-sod";


        try {
            String s3Url =  AWSUtil.getInstance().uploadFileToS3(AWS_BUCKET_NAME, fileName, csvStream, csvContent.getBytes(StandardCharsets.UTF_8).length, "text/csv");
            String preSignedUrl = AWSUtil.getInstance().generatePresignedUrl(AWS_BUCKET_NAME, fileName);
            return "Download your report here: " + preSignedUrl;
        } catch (Exception e) {
            logger.error("Error uploading report to S3", e);
            return "Error in generating your report.";
        }

    }

    @Tool("Skip Edit Checklist. Skips the edit checklist reminder and starts for next stage which is rollout.")
    public void skipEditReminder() {
        logger.info("User chose to skip editing the checklist and proceed with rollout.");
        sodAgentService.markChecklistStatusAsTwo(Integer.valueOf(organisationId));

    }

    @Tool("Previews a checklist by providing a direct link to open it.")
    public String previewChecklist() {
        logger.info("Returning link to preview checklist");
        String hashtoken = null;
        try {
            hashtoken = urlGenerationUtil.generateHash("preview@opsara.io", "email", "01092025-00:00", organisationId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        String message = "You can see how the form would look to your store managers here: \n\n";

        String downLoadLink = "[Here](" + OPSARA_WEB_URL + ASSESSMENT_URL + hashtoken + ")";
        String finalMesssage = message + downLoadLink;

        logger.info("Final Message: " + finalMesssage);

        return finalMesssage;
    }

}
