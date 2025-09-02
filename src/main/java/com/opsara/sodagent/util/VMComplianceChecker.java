package com.opsara.sodagent.util;

import java.io.OutputStream;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public class VMComplianceChecker {

    private static final String API_KEY = ""; // replace with your key
    private static final String ENDPOINT = "https://api.openai.com/v1/chat/completions";

    public static void main(String[] args) throws Exception {
        // ---- Step 1: Define inputs ----
        String referenceImagePath = "/Users/sanskritisaxena/SODAgent/src/main/resources/static/reference.png";
        String actualImagePath = "/Users/sanskritisaxena/SODAgent/src/main/resources/static/actual.png";

        String instructions = "There should be 3 mannequins. "
                + "2 mannequins should wear sunglasses. "
                + "All mannequins should wear orange shirts.";

        // ---- Step 2: Encode images in Base64 and wrap as data URLs ----
        String referenceImageB64 = encodeFileToBase64(referenceImagePath);
        String actualImageB64 = encodeFileToBase64(actualImagePath);

        String referenceDataUrl = "data:image/png;base64," + referenceImageB64;
        String actualDataUrl = "data:image/png;base64," + actualImageB64;

        // ---- Step 3: Build request JSON ----
        String jsonPayload = "{\n" +
                "  \"model\": \"gpt-4o\",\n" +
                "  \"messages\": [\n" +
                "    {\n" +
                "      \"role\": \"user\",\n" +
                "      \"content\": [\n" +
                "        {\"type\": \"text\", \"text\": \"" + instructions.replace("\"", "\\\"") + "\"},\n" +
                "        {\"type\": \"image_url\", \"image_url\": {\"url\": \"" + referenceDataUrl + "\"}},\n" +
                "        {\"type\": \"image_url\", \"image_url\": {\"url\": \"" + actualDataUrl + "\"}}\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        // ---- Step 4: Call OpenAI API using HttpURLConnection ----
        URL url = new URL(ENDPOINT);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonPayload.getBytes());
            os.flush();
        }

        int responseCode = conn.getResponseCode();
        InputStream is = (responseCode >= 200 && responseCode < 300) ? conn.getInputStream() : conn.getErrorStream();
        String responseBody = readInputStream(is);

        if (responseCode < 200 || responseCode >= 300) {
            throw new RuntimeException("Failed: HTTP error code : " + responseCode + "\n" + responseBody);
        }

        System.out.println("=== Compliance Report ===");
        System.out.println(responseBody);

        conn.disconnect();
    }

    // Utility: Encode image file to Base64
    private static String encodeFileToBase64(String filePath) throws Exception {
        byte[] fileContent = Files.readAllBytes(Path.of(filePath));
        return Base64.getEncoder().encodeToString(fileContent);
    }

    // Utility: Read InputStream to String
    private static String readInputStream(InputStream is) throws Exception {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = is.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString("UTF-8");
    }
}