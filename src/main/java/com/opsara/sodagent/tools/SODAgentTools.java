package com.opsara.sodagent.tools;

import com.opsara.sodagent.controller.SODAgentController;
import dev.langchain4j.agent.tool.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import static com.opsara.sodagent.constants.Constants.CDN_BASE_URL;

public class SODAgentTools {


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
    String init() {

       String response = "Thank you. Lets understand what all you want to track. Showing you a few of sample check points. Let me know if you want to go ahead with these or edit them.";
       response += "\n 1. Have doors been unlocked and the alarm system disabled? ☐ Yes ☐ No ☐ N/A";
       response += "\n 2. Have overnight security alerts or messages been reviewed? ☐ Yes ☐ No ☐ N/A";
       response += "\n 3. Is the CCTV system functioning and recording? ☐ Yes ☐ No ☐ N/A";
        response += "\n 4. Have all floors been swept and mopped? ☐ Yes ☐ No ☐ N/A";
        response += "\n 5. Have surfaces, shelves, mannequins, and display units been dusted? ☐ Yes ☐ No ☐ N/A";
        response += "\n 6. Are mirrors, glass doors, and fitting room areas clean? ☐ Yes ☐ No ☐ N/A";
        response += "\n 20. Are all POS systems powered on and operational? ☐ Yes ☐ No ☐ N/A";
        response += "\n 33. Are shopping bags, tissue paper, and gift wraps ready? ☐ Yes ☐ No ☐ N/A";
        response += "\n 34. Is hand sanitizer available at entrance and cash counter? ☐ Yes ☐ No ☐ N/A";
        response += "\n\n ... and many more check points to track daily.";
        response += "\n Please let me know if you want to download this list, to pick the check points you want to track. We recommend you to pick around 10-15 check points to track daily. You can also edit the list and add your own check points, and upload.";
        return response;
    }


    @Tool("Download a SOD checklist file containing all the check points.")
    String download() {
        // Path where static files are served from (Spring Boot default: src/main/resources/static)
        //String staticDir = "src/main/resources/static";
        String fileName = "SODChecks.txt";
        //Path filePath = Paths.get(staticDir, fileName);

        // Write CHECK_POINTS to the file
        /*try {
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, CHECK_POINTS);
        } catch (IOException e) {
            return "Error generating checklist file.";
        }*/

        // Construct the public URL (adjust baseUrl as needed)
        // Change to your actual domain if deployed
        String completeUrl = CDN_BASE_URL + fileName;
        String downLoadLink = "<a href=\"" + completeUrl + "\" target=\"_blank\">Download SODChecks.txt</a>";
        return downLoadLink;
    }

    @Tool("Parses CSV and extracts mobile numbers to return a List of Strings containing mobile numbers.")
    List<String> extractMobileNumbersFromCSV(String csvMobiles) {
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
    String rollout(List<String> mobileNumbers) {
        if(mobileNumbers.isEmpty()) {
            return "No mobile numbers provided. Please provide a list of mobile numbers to send the checklist.";
        }
        return "We have stored the mobile numbers and would send them whatsapp messages to fill in checklist";
    }


    @Tool ("Returns Completion Status. It tells how many stores out of how many have completed the checklist.")
    String returnCompletionStatus()
    {
        return "3 out of 19 stores have not yet completed the checklist. Let me know if you want to send a reminder to them.";
    }

    @Tool ("Reminder for completion. It sends a reminder to stores that have not completed the checklist.")
    String sendCompletionReminder()
    {
        return "Reminder sent to stores that have not completed the checklist. They will receive a WhatsApp message shortly.";
    }

    @Tool( "Returns Leaderboard from top. It returns the list of top stores based on score they got as per checklist. Parameter numberofStoresToReturn represents how many stores from top would be returned")
    String giveLeaderBoardfFomTop(int numberofStoresToReturn)
    {
        String returnString =  "Saket, Koramangala and Indiranagar are the top 3 stores based on the checklist score. <img src='"+CDN_BASE_URL+"leaderboard.png' alt='Leaderboard Image' />";
        logger.info(returnString);
        return returnString;
    }

    @Tool("Returns Detailed Report for a store. It returns the detailed report for a store with name storeName.")
    String giveOneDetailedReport (String storeName) {
        String message = "Detailed report for " + storeName + ":\n";
        String completeUrl = CDN_BASE_URL + "Koramangala_LatestReport.pdf";
        String downLoadLink = "<a href=\"" + completeUrl + "\" target=\"_blank\">Koramangala_LatestReport.pdf</a>";
        return message+downLoadLink;
    }

    @Tool("Returns Most Problematic Check Points. It returns the list of most problematic check points based on the checklist filled by stores.")
    String giveMostProblematicCheckPoints() {
        return "Backroom light not working has been the most problematic check point for 5 stores.";
    }

    @Tool("Download Report. It generates a csv file report based on the date range provided and returns a download link.")
    String downloadReport(String fromDate, String toDate) {
        // Logic to generate and download report based on date range
        // This is a placeholder implementation
        String message =  "Report generated from " + fromDate + " to " + toDate + " for all stores. Please download here.";
        String completeUrl = CDN_BASE_URL + "sodreport.csv";
        String downLoadLink = "<a href=\"" + completeUrl + "\" target=\"_blank\">sodreport.csv</a>";
        return message + downLoadLink;
    }
}
