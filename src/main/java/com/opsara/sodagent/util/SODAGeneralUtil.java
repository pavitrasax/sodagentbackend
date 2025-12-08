package com.opsara.sodagent.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.opsara.aaaservice.util.AWSUtil;
import com.opsara.sodagent.entities.OrganisationChecklist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import static com.opsara.sodagent.constants.Constants.AWS_BUCKET_NAME;
import static com.opsara.sodagent.constants.Constants.CDN_BASE_URL;
import static com.opsara.sodagent.constants.DefaultChecklist.ASSESSMENT_URL;


public class SODAGeneralUtil {

    private static final Logger logger = LoggerFactory.getLogger(SODAGeneralUtil.class);

    public static String generateInitMessage(String csvUrl, String hashtoken) {
        String responseString = new String();
        responseString += new String("SOD Agent is successfully initialised for your organisation.\n");
        responseString += "A default template is copied. \n";
        responseString += "You can see how the form would look to your store managers here: \n";
        responseString += "<a href=\"" + ASSESSMENT_URL + hashtoken + "\" target=\"_blank\">View Checklist</a>" + " \n";
        responseString += "\n If you want to change it, you can download the checklist from here\n";
        responseString += csvUrl;
        responseString += "\n After download, edit it and upload back the modified checklist. \n";
        responseString += "\n Or you may directly want to roll out to your store managers. \n";
        responseString += "For rolling out use the prompt like \"Roll out to name at mobile\". It would roll out to given person at given mobile number.  \n";
        responseString += "Or you can say \"Roll out to every one\". It would roll out to all store managers already existing in database.\n";

        return responseString;
    }

    /**
     * Parses an InputStream in the same format as `SODChecksNewTemplate.txt` and returns a JSON string
     * that follows the schema of `DEFAULT_CHECKLIST_JSON`.
     * On validation or parse failure returns a message starting with "ERROR:".
     */
    public static String parseGenerateJSONStoreAgainstOrg(InputStream inputStream) {
        if (inputStream == null) {
            return "ERROR: input stream is null";
        }
        ObjectMapper mapper = new ObjectMapper();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setTrim(true)
                    .setCommentMarker('#')
                    .build();

            try (CSVParser parser = new CSVParser(br, format)) {
                ArrayNode sections = mapper.createArrayNode();
                ObjectNode section = mapper.createObjectNode();
                section.put("id", "");
                section.put("title", "");
                ArrayNode questions = mapper.createArrayNode();

                Set<String> ids = new HashSet<>();
                int lineNo = 0;

                for (CSVRecord rec : parser) {
                    lineNo = (int) rec.getRecordNumber();
                    if (rec == null || rec.size() == 0) {
                        continue;
                    }

                    if (rec.size() < 4) {
                        return "ERROR: invalid format at record starting around line " + lineNo + " - expected at least 4 columns";
                    }

                    String id = rec.get(0).trim();

                    // prompt spans from index 1 to rec.size() - 3 (inclusive)
                    String prompt;
                    if (rec.size() == 4) {
                        prompt = rec.get(1).trim();
                    } else {
                        StringBuilder p = new StringBuilder();
                        for (int i = 1; i <= rec.size() - 3; i++) {
                            if (i > 1) p.append(",");
                            p.append(rec.get(i));
                        }
                        prompt = p.toString().trim();
                    }

                    String questionMandatory = rec.get(rec.size() - 2).trim().toUpperCase();
                    String attachmentNeeded = rec.get(rec.size() - 1).trim().toUpperCase();

                    if (id.isEmpty()) {
                        return "ERROR: empty id at record around line " + lineNo;
                    }
                    if (prompt.isEmpty()) {
                        return "ERROR: empty prompt at record around line " + lineNo;
                    }
                    if (!(questionMandatory.equals("Y") || questionMandatory.equals("N"))) {
                        return "ERROR: third column must be Y or N at record around line " + lineNo;
                    }
                    if (!(attachmentNeeded.equals("Y") || attachmentNeeded.equals("N") || attachmentNeeded.equals("M"))) {
                        return "ERROR: fourth column must be Y, N or M at record around line " + lineNo;
                    }
                    if (!ids.add(id)) {
                        return "ERROR: duplicate id '" + id + "' at record around line " + lineNo;
                    }

                    ObjectNode q = mapper.createObjectNode();
                    q.put("id", id);
                    q.put("type", "mcq-single");
                    q.put("prompt", prompt);

                    // options: always Yes, No. If question-mandatory == N include Not Applicable
                    ArrayNode options = mapper.createArrayNode();
                    options.add("Yes");
                    options.add("No");
                    if (questionMandatory.equals("N")) {
                        options.add("Not Applicable");
                    }
                    q.set("options", options);

                    // business: require users to answer (allow Not Applicable if N) -> keep required true
                    q.put("required", true);

                    ObjectNode attachment = mapper.createObjectNode();
                    boolean attachEnabled = attachmentNeeded.equals("Y") || attachmentNeeded.equals("M");
                    boolean attachRequired = attachmentNeeded.equals("M");
                    // If attachmentNeeded == N -> enabled false, required false
                    attachment.put("enabled", attachEnabled);
                    attachment.put("required", attachRequired);
                    q.set("attachment", attachment);

                    questions.add(q);
                }

                if (questions.isEmpty()) {
                    return "ERROR: no valid question lines found";
                }

                section.set("questions", questions);
                sections.add(section);

                ObjectNode root = mapper.createObjectNode();
                root.put("title", "SOD Agent Template");
                root.put("version", "1.0");
                root.set("sections", sections);
                root.put("agentScope", "SOD");
                root.put("description", "This is SOD Checklist Template");

                return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
            }
        } catch (IOException e) {
            logger.warn("Failed to parse template", e);
            return "ERROR: failed to parse template";
        }
    }

    // java
    private static String quoteCsv(String field) {
        if (field == null) return "\"\"";
        String escaped = field.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }


    public static String generateCSVStringFromJSONTemplate(String jsonTemplate) {
        if (jsonTemplate == null || jsonTemplate.isBlank()) {
            return "";
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(jsonTemplate);

            StringBuilder sb = new StringBuilder();
            // Preserve the same comment/header block as SODChecksNewTemplate.txt
            sb.append("# This is an easy to use format to configure a dynamic SOD checks form for a retail store.\n");
            sb.append("# Each line represents a different check.\n");
            sb.append("# Please make sure not to change the ID of any existing question. This is important for data consistency.\n");
            sb.append("# You can remove areas by deleting the corresponding line.\n");
            sb.append("# You can add new areas by appending new lines at the end of the file. Make sure to use unique Ids and Names for new areas.\n");
            sb.append("# question-mandatory is a key word , write Y or N as third parameter.\n");
            sb.append("# attachment-needed is a key word , write Y or N or M as third parameter. N=No, Y=Yes, M=Yes and Mandatory.\n");
            sb.append("# id,name,question-mandatory [N/Y], attachment-needed [N/Y/M] \n");

            com.fasterxml.jackson.databind.JsonNode sections = root.path("sections");
            if (sections.isMissingNode() || !sections.isArray()) {
                return sb.toString();
            }

            for (com.fasterxml.jackson.databind.JsonNode section : sections) {
                com.fasterxml.jackson.databind.JsonNode questions = section.path("questions");
                if (questions.isMissingNode() || !questions.isArray()) {
                    continue;
                }
                for (com.fasterxml.jackson.databind.JsonNode q : questions) {
                    String id = q.path("id").asText("").trim();
                    String prompt = q.path("prompt").asText("").trim();
                    com.fasterxml.jackson.databind.JsonNode attachmentNode = q.path("attachment");

                    if (id.isEmpty() || prompt.isEmpty()) {
                        // skip malformed entries
                        continue;
                    }

                    // Determine question-mandatory: if options contain "Not Applicable" -> N else Y
                    boolean hasNotApplicable = false;
                    com.fasterxml.jackson.databind.JsonNode optionsNode = q.path("options");
                    if (!optionsNode.isMissingNode() && optionsNode.isArray()) {
                        for (com.fasterxml.jackson.databind.JsonNode opt : optionsNode) {
                            if ("Not Applicable".equalsIgnoreCase(opt.asText())) {
                                hasNotApplicable = true;
                                break;
                            }
                        }
                    }
                    String questionMandatory = hasNotApplicable ? "N" : "Y";

                    // Determine attachment-needed: N / Y / M
                    String attachmentNeeded = "N";
                    if (!attachmentNode.isMissingNode() && attachmentNode.isObject()) {
                        boolean enabled = attachmentNode.path("enabled").asBoolean(false);
                        boolean required = attachmentNode.path("required").asBoolean(false);
                        if (enabled) {
                            attachmentNeeded = required ? "M" : "Y";
                        } else {
                            attachmentNeeded = "N";
                        }
                    }

                    // Quote and escape the prompt to handle commas and quotes correctly
                    sb.append(id)
                            .append(",")
                            .append(quoteCsv(prompt))
                            .append(",")
                            .append(questionMandatory)
                            .append(",")
                            .append(attachmentNeeded)
                            .append("\n");
                }
            }
            return sb.toString();
        } catch (Exception e) {
            logger.error("Failed to generate SOD CSV from JSON template", e);
            return "";
        }
    }



    public static String downloadDefaultChecklist(OrganisationChecklist existingChecklist) {
        logger.info("Download Called.");

        String organisationId = String.valueOf(existingChecklist.getOrgId());

        if (existingChecklist != null && existingChecklist.getChecklistJson() != null) {
            String checklistJson = existingChecklist.getChecklistJson();

            String csvContent = generateCSVStringFromJSONTemplate(checklistJson);

            // Upload to S3
            String fileName = "Checklist_" + organisationId + ".csv";
            InputStream csvStream = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));
            try {
                AWSUtil.getInstance().uploadFileToS3(AWS_BUCKET_NAME, fileName, csvStream, csvContent.getBytes(StandardCharsets.UTF_8).length, "text/csv");
                String signedS3Url = AWSUtil.getInstance().generatePresignedUrl(AWS_BUCKET_NAME, fileName);
                return "<a href=\"" + signedS3Url + "\" target=\"_blank\">Download Checklist CSV</a>";
            } catch (Exception e) {
                logger.error("Error uploading checklist CSV to S3", e);
                return "Error Generating Template Download Link.";

            }

        } else {
            // fallback to hardcoded file
            return "Error Generating Template Download Link.";

        }
    }

}
