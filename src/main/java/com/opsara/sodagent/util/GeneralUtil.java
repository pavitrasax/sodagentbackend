package com.opsara.sodagent.util;

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
}
