package com.opsara.sodagent.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opsara.sodagent.controller.SODAgentController;
import com.opsara.sodagent.entities.OrganisationChecklist;
import com.opsara.sodagent.services.SODAgentService;
import dev.langchain4j.agent.tool.Tool;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.opsara.sodagent.constants.Constants.CDN_BASE_URL;

@Data
public class SODAgentTools {


    private SODAgentService service;
    private String organisationId;

    public SODAgentTools(SODAgentService service) {
        this.service = service;
    }

    public SODAgentTools(SODAgentService service, String organisationId) {
        this.service = service;
        this.organisationId = organisationId;
    }


    private static final Logger logger = LoggerFactory.getLogger(SODAgentTools.class);
    private static final List<String> CHECK_POINTS = List.of(
            "Have doors been unlocked and the alarm system disabled?",
            "Have overnight security alerts or messages been reviewed?",
            "Is the CCTV system functioning and recording?",
            "Have all floors been swept and mopped?",
            "Have surfaces, shelves, mannequins, and display units been dusted?",
            "Are mirrors, glass doors, and fitting room areas clean?",
            "Have trash bins been emptied and liners replaced?",
            "Is the air-conditioning or ventilation working and set to a comfortable level?",
            "Is all store lighting switched on and functioning?",
            "Is background music playing at the correct volume?",
            "Are window displays clean, neat, and on theme?",
            "Are in-store displays set as per the planogram or promotional guidelines?",
            "Are mannequins dressed and positioned correctly?",
            "Are shelves and racks fully stocked with no empty gaps?",
            "Are all garments steamed/ironed and presentable?",
            "Have overnight deliveries been checked and reconciled with invoices?",
            "Are new arrivals tagged and priced?",
            "Are high-margin or new collection items placed in prime locations?",
            "Are adequate sizes and colors available for fast-moving SKUs?",
            "Are all POS systems powered on and operational?",
            "Is the sales software logged in and connected?",
            "Has the opening cash float been counted and prepared?",
            "Are receipt printers, barcode scanners, and payment terminals working?",
            "Has the team briefing been conducted for daily sales targets and promotions?",
            "Have staff roles and floor coverage been assigned?",
            "Have important updates from head office been shared?",
            "Are fire exits checked and clear?",
            "Are fire extinguishers accessible?",
            "Have fitting room emergency buttons been tested?",
            "Are there no tripping hazards on the shop floor?",
            "Is welcome signage and promotional material placed and visible?",
            "Are fitting rooms stocked with hangers, hooks, and clean seating?",
            "Are shopping bags, tissue paper, and gift wraps ready?",
            "Is hand sanitizer available at entrance and cash counter?"
    );

    @Tool("Initialise SOD Agent with a list of sample check points to track daily.")
    public String init() {

        logger.info("Init called .... organisation ID is " + organisationId);

        Integer orgId = Integer.valueOf(organisationId);
        List<String> checklist = new ArrayList<>();
        checklist.addAll(CHECK_POINTS);
        // Represent as JSON: { "checklist": [ ... ] }
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> jsonMap = Map.of("checklist", checklist);
        logger.info("Init called .... 2");
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
            responseString += new String("SOD Agent is successfully initialised for your organisation. A default template is copied. \n");
            responseString += "You can see how the form would look to your store managers here: \n";
            responseString += "https://sod-frontend.opsara.com/fillchecklist/?userCredential=userCredentials&userCredentialType=email&forPeriod=2023-Q3 \n";
            responseString += "If you want to change it, you can download the checklist from here, edit it and upload back the modified checklist. \n";
            responseString += "https://sod-frontend.opsara.com/downloadchecklist \n";
            responseString += "Or you may directly want to roll out to your store managers. \n";
            responseString += "For rolling out use the promt like Roll out to Name at mobile/email. \n";
        } else {
            int status = existingChecklist.getStatus() != null ? existingChecklist.getStatus() : 0;
            switch (status) {
                case 0:
                    logger.info("case 0 called  ....");
                    responseString += new String("SOD Agent is successfully initialised for your organisation. A default template is copied. \n");
                    responseString += "You can see how the form would look to your store managers here: \n";
                    responseString += "https://sod-frontend.opsara.com/fillchecklist/?userCredential=userCredentials&userCredentialType=email&forPeriod=2023-Q3 \n";
                    responseString += "If you want to change it, you can download the checklist from here, edit it and upload back the modified checklist. \n";
                    responseString += "https://sod-frontend.opsara.com/downloadchecklist \n";
                    responseString += "Or you may directly want to roll out to your store managers. \n";
                    responseString += "For rolling out use the promt like Roll out to Name at mobile/email. \n";
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
            // Use existingChecklist.getStatus() or other fields as needed
        }
        logger.info("Response String: " + responseString);

        return responseString;
    }


    @Tool("Download a default SOD checklist file containing all the check points.")
    public String download() {
        logger.info("Download Called.");
        String fileName = "SODChecks.txt";
        String completeUrl = CDN_BASE_URL + fileName;
        String downLoadLink = "<a href=\"" + completeUrl + "\" target=\"_blank\">Download SODChecks.txt</a>";
        return downLoadLink;
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

        mobileNumbers.forEach(mobile -> service.saveWhatsappUser(mobile, null));


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

        mobileNameMaps.forEach((mobile, name) -> service.saveWhatsappUser(mobile, name));

        return "We have stored the mobile numbers and user names and would send them whatsapp messages to fill in checklist";
    }


    @Tool("Returns Completion Status. It tells how many stores out of how many have completed the checklist.")
    public String returnCompletionStatus() {
        return "3 out of 19 stores have not yet completed the checklist. Let me know if you want to send a reminder to them.";
    }

    @Tool("Reminder for completion. It sends a reminder to stores that have not completed the checklist.")
    public String sendCompletionReminder() {
        return "Reminder sent to stores that have not completed the checklist. They will receive a WhatsApp message shortly.";
    }

    @Tool("Returns Leaderboard from top. It returns the list of top stores based on score they got as per checklist. Parameter numberofStoresToReturn represents how many stores from top would be returned")
    public String giveLeaderBoardfFomTop(int numberofStoresToReturn) {
        String returnString = "Saket, Koramangala and Indiranagar are the top 3 stores based on the checklist score. <img src='" + CDN_BASE_URL + "leaderboard.png' alt='Leaderboard Image' />";
        logger.info(returnString);
        return returnString;
    }

    @Tool("Returns Detailed Report for a store. It returns the detailed report for a store with name storeName.")
    public String giveOneDetailedReport(String storeName) {
        String message = "Detailed report for " + storeName + ":\n";
        String completeUrl = CDN_BASE_URL + "Koramangala_LatestReport.pdf";
        String downLoadLink = "<a href=\"" + completeUrl + "\" target=\"_blank\">Koramangala_LatestReport.pdf</a>";
        return message + downLoadLink;
    }

    @Tool("Returns Most Problematic Check Points. It returns the list of most problematic check points based on the checklist filled by stores.")
    public String giveMostProblematicCheckPoints() {
        return "Backroom light not working has been the most problematic check point for 5 stores.";
    }

    @Tool("Download Report. It generates a csv file report based on the date range provided and returns a download link.")
    public String downloadReport(String fromDate, String toDate) {
        // Logic to generate and download report based on date range
        // This is a placeholder implementation
        String message = "Report generated from " + fromDate + " to " + toDate + " for all stores. Please download here.";
        String completeUrl = CDN_BASE_URL + "sodreport.csv";
        String downLoadLink = "<a href=\"" + completeUrl + "\" target=\"_blank\">sodreport.csv</a>";
        return message + downLoadLink;
    }

    @Tool("Skip Edit Checklist. Skips the edit checklist reminder and starts for next stage which is rollout.")
    public void skipEditReminder() {
        logger.info("User chose to skip editing the checklist and proceed with rollout.");
        service.markChecklistStatusAsTwo(Integer.valueOf(organisationId));

    }

}
