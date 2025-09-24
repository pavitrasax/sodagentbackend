package com.opsara.sodagent.util;

import java.util.Iterator;
import java.util.List;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;


public class GeneralUtil {

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

    public static String validateMobileNumbersAndRemoveInvalid(List<String> mobileNumbers) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        StringBuilder returnMessage = new StringBuilder();
        Iterator<String> iterator = mobileNumbers.iterator();
        while (iterator.hasNext()) {
            String mobile = iterator.next();
            String input = "+" + mobile;
            boolean isValid = true;
            try {
                PhoneNumber number = phoneUtil.parse(input, null);
                isValid = phoneUtil.isValidNumber(number);
            } catch (Exception e) {
                isValid = false;
            }
            if (!isValid) {
                returnMessage.append(mobile).append(", ");
                iterator.remove(); // Safely remove invalid number
            }
        }
        int returnMessageLength = returnMessage.length();
        if (returnMessageLength > 0) {
            returnMessage.setLength(returnMessageLength - 2); // Remove last comma and space
        }
        return returnMessage.toString();
    }
}
