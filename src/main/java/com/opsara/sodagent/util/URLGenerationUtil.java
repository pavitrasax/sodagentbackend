package com.opsara.sodagent.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;


import java.net.URLEncoder;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class URLGenerationUtil {


    private static final String ALGORITHM = "AES";
    private static final SecretKeySpec secretKey = new SecretKeySpec("OPSARA_SECRET_12".getBytes(), ALGORITHM);;



    public static String generateHash(String userCredential, String credentialType, String filledFor, String organisationId) throws Exception {
        String combined = userCredential + "|" + credentialType + "|" + filledFor + "|" + organisationId;
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encrypted = cipher.doFinal(combined.getBytes());
        String base64Hash = Base64.getEncoder().encodeToString(encrypted);
        return URLEncoder.encode(base64Hash, StandardCharsets.UTF_8.toString());
    }

    public static String[] reverseHash(String hash) throws Exception {
        //String decodedHash = URLDecoder.decode(hash, StandardCharsets.UTF_8.toString());
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decoded = Base64.getDecoder().decode(hash);
        String decrypted = new String(cipher.doFinal(decoded));
        return decrypted.split("\\|", 4);
    }

    public static void main(String[] args) throws Exception {
        String sodaChecklistUrl = "https://opsara.io/fillsodchecklist?hashtoken=";

        List<String> dateStrings = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy-00:00");
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 5; i++) {
            LocalDate date = today.minusDays(i);
            dateStrings.add(date.format(formatter));
        }

        for (String dateString : dateStrings) {
            String hash = generateHash(args[0], args[1], dateString, args[2]);
            System.out.println(sodaChecklistUrl + hash);
        }
    }

}
