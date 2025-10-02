package com.opsara.sodagent.util;

import java.util.Iterator;
import java.util.List;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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

    /**
     * Validates a list of mobile numbers and removes any invalid numbers from the list.
     * <p>
     * Each number is expected to be in international format (without the leading '+').
     * Invalid numbers are removed from the input list, and their values are returned as a comma-separated string.
     * <p>
     * Uses Google's libphonenumber for validation.
     *
     * @param mobileNumbers the list of mobile numbers to validate and clean; invalid numbers will be removed from this list
     * @return a comma-separated string of invalid mobile numbers, or an empty string if all numbers are valid
     */
    // Note: This method modifies the input list by removing invalid numbers.
    // Note: I tried to move all utility methods to AAA Service to that all Agents could use them through exported jar
    // But, some how when Tools are invoked by Langchain, it is not able to invoke the method from GeneralUtil of exported jar.
    // Forced to keep this duplicated method in all Agents where ever needed.

    public static String validateMobileNumbersAndRemoveInvalid(List<String> mobileNumbers) {
        logger.info("Validating mobile numbers: " + mobileNumbers);
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        StringBuilder returnMessage = new StringBuilder();
        Iterator<String> iterator = mobileNumbers.iterator();
        while (iterator.hasNext()) {
            String mobile = iterator.next();
            String input = "+" + mobile;
            boolean isValid = true;
            try {
                logger.info("Validating number with phoneUtil: " + input);
                Phonenumber.PhoneNumber number = phoneUtil.parse(input, null);
                isValid = phoneUtil.isValidNumber(number);
            } catch (Exception e) {
                logger.info("Exception while validating number: " + e.getMessage());
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
        logger.info("Invalid mobile numbers: " + returnMessage.toString());
        return returnMessage.toString();
    }

}
