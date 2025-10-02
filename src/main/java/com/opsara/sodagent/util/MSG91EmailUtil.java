package com.opsara.sodagent.util;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.opsara.aaaservice.constants.Constants.*;

public class MSG91EmailUtil {
    private static final String CONTENT_TYPE = "application/json";

    public static String sendEmailOTP(String toEmail, String toName, String otp) {

        // Email validation (simple regex)
        if (toEmail == null || !toEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            return "Invalid email address";
        }
        // OTP validation
        if (otp == null || otp.trim().isEmpty()) {
            return "OTP cannot be null or empty";
        }
        // Name defaulting
        if (toName == null || toName.trim().isEmpty()) {
            toName = "Recipient";
        }

        String payload = String.format(
                "{ \"recipients\": [ { \"to\": [ { \"email\": \"%s\", \"name\": \"%s\" } ], \"variables\": { \"company_name\": \"%s\", \"otp\": \"%s\" } } ], \"from\": { \"email\": \"%s\" }, \"domain\": \"%s\", \"template_id\": \"%s\" }",
                toEmail, toName, MSG91_FROM_COMPANYNAME, otp, MSG91_FROM_EMAIL, MSG91_EMAIL_DOMAIN, MSG91_EMAIL_OTP_TEMPLATE_ID
        );

        try {
            return sendPostRequest(payload);
        } catch (IOException e) {
            return "error sending email: " + e.getMessage();
        }

    }

    private static String sendPostRequest(String payload)  throws IOException {
        URL url = new URL(MSG91_EMAIL_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", CONTENT_TYPE);
        conn.setRequestProperty("authkey", MSG91_ACCESS_TOKEN);
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(payload.getBytes());
            os.flush();
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != 200 && responseCode != 202) {
            return "error sending email: HTTP error code : " + responseCode;
        }
        conn.disconnect();
        return "Email sent successfully";
    }
}
