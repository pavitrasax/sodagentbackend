package com.opsara.sodagent.util;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class EmailUtil {

    // Configurable Constants
    private static final String API_URL = "https://control.msg91.com/api/v5/email/send";
    private static final String AUTH_KEY = "466359AraNNyb94H68aea73eP1";
    private static final String CONTENT_TYPE = "application/json";
    private static final String FROM_EMAIL = "no-reply@opsara.io";
    private static final String DOMAIN = "mail.opsara.io";
    private static final String TEMPLATE_ID = "global_otp";

    public static void sendMessage(String toEmail, String toName, String companyName, String otp) throws Exception {
        String payload = String.format(
                "{ \"recipients\": [ { \"to\": [ { \"email\": \"%s\", \"name\": \"%s\" } ], \"variables\": { \"company_name\": \"%s\", \"otp\": \"%s\" } } ], \"from\": { \"email\": \"%s\" }, \"domain\": \"%s\", \"template_id\": \"%s\" }",
                toEmail, toName, companyName, otp, FROM_EMAIL, DOMAIN, TEMPLATE_ID
        );

        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", CONTENT_TYPE);
        conn.setRequestProperty("authkey", AUTH_KEY);
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(payload.getBytes());
            os.flush();
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != 200 && responseCode != 202) {
            throw new RuntimeException("Failed to send email: HTTP error code : " + responseCode);
        }
        conn.disconnect();
    }


    public static void main(String[] args) {
        try {
            sendMessage("pavitrasaxena@yahoo.com", "Pavitra", "Opsara", "121212");
            System.out.println("Email sent successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}