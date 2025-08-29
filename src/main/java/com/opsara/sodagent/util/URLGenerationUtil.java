package com.opsara.sodagent.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class URLGenerationUtil {


    private static final String ALGORITHM = "AES";
    private static final SecretKeySpec secretKey = new SecretKeySpec("OPSARA_SECRET_12".getBytes(), ALGORITHM);;


    public static String generateHash(String userCredential, String credentialType, String filledFor, String organisationChecklistId) throws Exception {
        String combined = userCredential + "|" + credentialType + "|" + filledFor + "|" + organisationChecklistId;
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encrypted = cipher.doFinal(combined.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public static String[] reverseHash(String hash) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decoded = Base64.getDecoder().decode(hash);
        String decrypted = new String(cipher.doFinal(decoded));
        return decrypted.split("\\|", 4);
    }
}
