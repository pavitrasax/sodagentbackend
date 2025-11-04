package com.opsara.sodagent.util;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.opsara.aaaservice.util.AWSUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.opsara.sodagent.constants.Constants.AWS_BUCKET_NAME;


public class SODAGeneralUtil {

    private static final Logger logger = LoggerFactory.getLogger(SODAGeneralUtil.class);

    public static String generateInitMessage(String csvUrl, String hashtoken) {
        String responseString = new String();
        responseString += new String("SOD Agent is successfully initialised for your organisation.\n");
        responseString += "A default template is copied. \n";
        responseString += "You can see how the form would look to your store managers here: \n";
        responseString += "<a href=\"" + "/fillsodchecklist?hashtoken=" + hashtoken + "\" target=\"_blank\">View Checklist</a>" + " \n";
        responseString += "\n If you want to change it, you can download the checklist from here\n";
        responseString += csvUrl;
        responseString += "\n After download, edit it and upload back the modified checklist. \n";
        responseString += "\n Or you may directly want to roll out to your store managers. \n";
        responseString += "For rolling out use the prompt like \"Roll out to name at mobile\". It would roll out to given person at given mobile number.  \n";
        responseString += "Or you can say \"Roll out to every one\". It would roll out to all store managers already existing in database.\n";

        return responseString;
    }



    public static String replaceUrlsNamedUrlWithSignedUrls(String dataJson) {
        if (dataJson == null || dataJson.trim().isEmpty()) {
            return dataJson;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(dataJson);
            traverseAndReplaceUrlFields(root);
            return mapper.writeValueAsString(root);
        } catch (Exception e) {
            logger.error("Error replacing `url` fields with signed URLs", e);
            return dataJson;
        }
    }

    private static void traverseAndReplaceUrlFields(com.fasterxml.jackson.databind.JsonNode node) {
        if (node == null) return;

        if (node.isObject()) {
            com.fasterxml.jackson.databind.node.ObjectNode obj = (com.fasterxml.jackson.databind.node.ObjectNode) node;
            Iterator<Map.Entry<String, com.fasterxml.jackson.databind.JsonNode>> fields = obj.fields();
            while (fields.hasNext()) {
                Map.Entry<String, com.fasterxml.jackson.databind.JsonNode> entry = fields.next();
                String key = entry.getKey();
                com.fasterxml.jackson.databind.JsonNode child = entry.getValue();
                if ("url".equals(key) && child != null && child.isTextual()) {
                    String original = child.asText();
                    String signed = generateSignedUrlFromAwsUrl(original);
                    if (signed != null && !signed.equals(original)) {
                        obj.put(key, signed);
                    }
                } else {
                    traverseAndReplaceUrlFields(child);
                }
            }
        } else if (node.isArray()) {
            com.fasterxml.jackson.databind.node.ArrayNode arr = (com.fasterxml.jackson.databind.node.ArrayNode) node;
            for (int i = 0; i < arr.size(); i++) {
                traverseAndReplaceUrlFields(arr.get(i));
            }
        }
    }

    private static String generateSignedUrlFromAwsUrl(String originalUrl) {
        if (originalUrl == null || originalUrl.trim().isEmpty()) return originalUrl;
        // skip already-signed URLs
        if (originalUrl.contains("X-Amz-") || originalUrl.contains("x-amz-")) return originalUrl;

        int bucketIdx = originalUrl.indexOf(AWS_BUCKET_NAME);
        if (bucketIdx == -1) {
            return originalUrl;
        }

        // find the slash that begins the key/path after the domain portion
        int keyStartIdx = originalUrl.indexOf("/", bucketIdx);
        if (keyStartIdx == -1 || keyStartIdx + 1 >= originalUrl.length()) {
            return originalUrl;
        }

        String key = originalUrl.substring(keyStartIdx + 1);
        try {
            return AWSUtil.generatePresignedUrl(AWS_BUCKET_NAME, key);
        } catch (Exception e) {
            logger.warn("Failed to generate presigned URL for key={} : {}", key, e.getMessage());
            return originalUrl;
        }
    }
}
