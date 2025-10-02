package com.opsara.sodagent.util;

import com.opsara.aaaservice.util.GeneralUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static com.opsara.aaaservice.constants.Constants.MSG91_ACCESS_TOKEN;
import static com.opsara.aaaservice.constants.Constants.MSG91_WHATSAPP_URL;

public class MSG91WhatsappUtil {

    private static final Logger logger = LoggerFactory.getLogger(MSG91WhatsappUtil.class);


    public static String sendWhatsappOTP(String mobile, String otp) {


        if (!GeneralUtil.validateMobileNumber(mobile)) {
            return "mobile number is invalid " + mobile;
        }

        if (mobile.startsWith("+")) {
            mobile = mobile.substring(1);
        }

        // Build JSON body

        String jsonPayload = "{"
                + "\"integrated_number\": \"919663814972\","
                + "\"content_type\": \"template\","
                + "\"payload\": {"
                + "\"messaging_product\": \"whatsapp\","
                + "\"type\": \"template\","
                + "\"template\": {"
                + "\"name\": \"ops_otp\","
                + "\"language\": {\"code\": \"en\", \"policy\": \"deterministic\"},"
                + "\"namespace\": \"5f787ab3_3922_4296_88eb_4f238a397c20\","
                + "\"to_and_components\": [{"
                + "\"to\": [" + mobile + "],"
                + "\"components\": {"
                + "\"body_1\": {\"type\": \"text\", \"value\": \"" + otp + "\"},"
                + "\"button_1\": {\"subtype\": \"url\", \"type\": \"text\", \"value\": \"" + otp + "\"}"
                + "}"
                + "}]"
                + "}"
                + "}"
                + "}";

        return invokeWhatsappAPI(jsonPayload);
    }


    public static String sendGenericFillFormMessageOTP(String mobile, String name, String formName, String toBeFilledFor, String expiryHours, String formUrl) {


        if (!GeneralUtil.validateMobileNumber(mobile)) {
            return "mobile number is invalid " + mobile;
        }

        if (mobile.startsWith("+")) {
            mobile = mobile.substring(1);
        }

        // Build JSON body

        String jsonPayload = "{"
                + "\"integrated_number\": \"919663814972\","
                + "\"content_type\": \"template\","
                + "\"payload\": {"
                + "\"messaging_product\": \"whatsapp\","
                + "\"type\": \"template\","
                + "\"template\": {"
                + "\"name\": \"fill_generic\","
                + "\"language\": {\"code\": \"en\", \"policy\": \"deterministic\"},"
                + "\"namespace\": \"5f787ab3_3922_4296_88eb_4f238a397c20\","
                + "\"to_and_components\": [{"
                + "\"to\": [\"" + mobile + "\"],"
                + "\"components\": {"
                + "\"body_1\": {\"type\": \"text\", \"value\": \"" + name + "\"},"
                + "\"body_2\": {\"type\": \"text\", \"value\": \"" + formName + "\"},"
                + "\"body_3\": {\"type\": \"text\", \"value\": \"" + toBeFilledFor + "\"},"
                + "\"body_4\": {\"type\": \"text\", \"value\": \"" + expiryHours + "\"},"
                + "\"button_1\": {\"subtype\": \"url\", \"type\": \"text\", \"value\": \"" + formUrl + "\"}"
                + "}"
                + "}]"
                + "}"
                + "}"
                + "}";

        logger.info("Generic form filling payload \n\n {} \n\n " , jsonPayload);
        return invokeWhatsappAPI(jsonPayload);
    }


    public static String invokeWhatsappAPI(String jsonPayload) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(MSG91_WHATSAPP_URL))
                .header("Content-Type", "application/json")
                .header("authkey", MSG91_ACCESS_TOKEN)
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return "Response: " + response.body();
        } catch (IOException | InterruptedException e) {
            return "Error: " + e.getMessage();
        }
    }
}
