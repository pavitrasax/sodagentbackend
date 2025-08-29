package com.opsara.sodagent.util;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.io.IOException;

public class WhatsappUtil {
    public static void main(String[] args) {
        // Replace with your API credentials
        String ACCESS_TOKEN = "EAATuYX1XbRABPdcZBMYl75yZCZB26ZBmO8vp1SyJObL71oPT0VmaIqVS2EOyTYRipyYZBVcDsDeMmPaBnQSsTZCAFr9T8EXoTqUUJ04JaMB1QZCtRp4ujZBGnzUTxfMcz3G1pjKr98fkkZAOnMrSbbQ71SjKAuRYQ3nNut5eAnQbpkIF4dAjZBzdypoupf1gkJBSrZClrPCffvgMzIZBdlq58P60N7phmDvd2knzBPwWb4PLZBIsZD";
        String PHONE_NUMBER_ID = "717287918142013";
        String RECIPIENT_PHONE = "+919632542332"; // Must be in international format

        // API URL
        String url = "https://graph.facebook.com/v18.0/" + PHONE_NUMBER_ID + "/messages";

        // JSON payload for message
        String jsonPayload = "{"
                + "\"messaging_product\": \"whatsapp\","
                + "\"to\": \"" + RECIPIENT_PHONE + "\","
                + "\"type\": \"template\","
                + "\"template\": {\"name\": \"collect_sod_data\"}"
                + "\"language\": {\"code\": \"en_US\"}"
                + "}";

        // Create HTTP client
        HttpClient client = HttpClient.newHttpClient();

        // Create HTTP request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + ACCESS_TOKEN)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        // Send request and handle response
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Response Code: " + response.statusCode());
            System.out.println("Response Body: " + response.body());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}