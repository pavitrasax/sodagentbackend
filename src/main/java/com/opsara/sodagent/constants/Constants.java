package com.opsara.sodagent.constants;


public final class Constants {

   public static final String OPENAI_API_KEY = System.getenv("OPENAI_API_KEY");
   public static final String CDN_BASE_URL = System.getenv("CDN_BASE_URL");
   public static final String MY_CODE = "SOD";
    public static final String AWS_BUCKET_NAME = "opsara-sod";
    public static final String OPSARA_WEB_URL = "https://opsara.io";

    public static final String[] PUBLIC_ENDPOINTS = {
            "/sodagent/getuniqueURL",
            "/sodagent/public/fillchecklist",
            "/sodagent/public/gettemplate"
    };

    public static final String[] AUTH_ENDPOINTS = {
            "/sodagent/chat",
            "/sodagent/upload",
            "/sodagent/initialise"
    };

    public static final String[] ALLOWED_ORIGINS = {
            "http://localhost:3001",
            "https://opsara.io",
            "https://www.opsara.io",
            "https://*.replit.app",
            "https://replit.app",
            "https://*.replit.dev",
            "https://replit.dev",
            "http://localhost:3000",
            "https://d83104de-8dc1-4e95-ad1a-f2d62d63f7d6-00-1tq1p0cz1y1el.janeway.replit.dev"
    };



}
