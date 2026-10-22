package com.sander.ai;
import java.util.regex.Pattern;

public class WafEngine {
    // Standard Patterns
    private static final Pattern SQLI = Pattern.compile("(?i)(union\\s+select|select\\s+.*\\s+from|'.*or.*'='|sleep\\(|benchmark\\()");
    private static final Pattern XSS = Pattern.compile("(?i)(<script|javascript:|onerror\\s*=|onload\\s*=|alert\\()");
    private static final Pattern LFI = Pattern.compile("(?i)(\\.\\./|\\.\\.\\\\|/etc/passwd|/etc/shadow|c:\\\\windows)");
    private static final Pattern RCE = Pattern.compile("(?i)(;|\\||&&|\\$\\(|`)");
    private static final Pattern SENSITIVE_PATH = Pattern.compile("(?i)(/\\.env|/\\.git/|/actuator|/wp-config\\.php|/phpinfo\\.php)");
    private static final Pattern NUCLEI = Pattern.compile("(?i)(user-agent:\\s*nuclei|x-nuclei-id)");

    // Esoteric Languages
    private static final Pattern BRAINFUCK = Pattern.compile("[+\\-<>.,\\[\\]]{8,}");
    private static final Pattern JSFUCK = Pattern.compile("[\\[\\]()!+]{12,}");
    private static final Pattern JAVA_INJ = Pattern.compile("(?i)(Runtime\\.getRuntime|ProcessBuilder|System\\.exit|Class\\.forName|jshell|ScriptEngine|eval\\()");
    private static final Pattern FUCKING_LANG = Pattern.compile("(?i)(fuck|kcuf|feck|brain|f\\*ck){2,}");

    public static String inspect(String rawRequest) {
        if (rawRequest == null || rawRequest.isEmpty()) return "UNKNOWN";

        String decoded;
        try { decoded = java.net.URLDecoder.decode(rawRequest, "UTF-8"); } 
        catch (Exception e) { decoded = rawRequest; }

        // Priority: Sensitive > RCE > Java > Nuclei > SQLi > XSS > LFI > Esoteric
        if (SENSITIVE_PATH.matcher(decoded).find()) return "SENSITIVE_PATH";
        if (RCE.matcher(decoded).find()) return "CMD_INJECTION";
        if (JAVA_INJ.matcher(decoded).find()) return "JAVA_INJECTION";
        if (NUCLEI.matcher(decoded).find()) return "NUCLEI_SCANNER";
        if (SQLI.matcher(decoded).find()) return "SQL_INJECTION";
        if (XSS.matcher(decoded).find()) return "XSS";
        if (LFI.matcher(decoded).find()) return "LFI";
        if (BRAINFUCK.matcher(decoded).find()) return "BRAINFUCK";
        if (JSFUCK.matcher(decoded).find()) return "JSFUCK";
        if (FUCKING_LANG.matcher(decoded).find()) return "FUCKING_LANG";

        return "CLEAN";
    }
}
