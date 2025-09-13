package com.opsara.sodagent.util;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.io.IOException;
import static com.opsara.sodagent.constants.Constants.WHATSAPP_ACCESS_TOKEN;

public class WhatsappUtil {

    public static String PHONE_NUMBER_ID = "717287918142013";
    public static String RECIPIENT_PHONE = "+919632542332"; // Must be in international format

    public static void main(String[] args) {
        // Replace with your API credentials

        sendDirectMessage("hello");
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
                .header("Authorization", "Bearer " + WHATSAPP_ACCESS_TOKEN)
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


    public static void sendDirectMessage(String message) {

        // API URL
        String url = "https://graph.facebook.com/v18.0/" + PHONE_NUMBER_ID + "/messages";

        // JSON payload for message
        String jsonPayload = "{"
                + "\"messaging_product\": \"whatsapp\","
                + "\"to\": \"" + RECIPIENT_PHONE + "\","
                + "\"type\": \"text\","
                + "\"text\": {\"body\": \"" + message + "\"}"
                + "}";

        // Create HTTP client
        HttpClient client = HttpClient.newHttpClient();

        // Create HTTP request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + WHATSAPP_ACCESS_TOKEN)
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