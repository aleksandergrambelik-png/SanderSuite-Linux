package com.sander.ai;
import com.sander.core.ConfigManager;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Set;

public class AIBrain {
    private static final int SCORE_SQLI = 40, SCORE_XSS = 30, SCORE_CMD_INJ = 50, SCORE_PORT_SCAN = 20, SCORE_TIMEOUT = 5;
    private static final int SCORE_LFI = 45, SCORE_SENSITIVE = 80, SCORE_JAVA = 90, SCORE_BRAINFUCK = 35, SCORE_JSFUCK = 25;
    private static final int THRESHOLD_TARPIT = 30, THRESHOLD_BLOCK = 70;
    
    private static final Map<String, Integer> threatScores = new ConcurrentHashMap<>();
    private static final Map<String, Set<Integer>> ipPortHistory = new ConcurrentHashMap<>();
    private static final Set<String> permanentBlacklist = ConcurrentHashMap.newKeySet();

    public enum Decision { ALLOW, MONITOR, TARPIT, BLOCK }

    public static Decision analyze(String ip, String attackType, int port, ConfigManager config) {
        if (permanentBlacklist.contains(ip)) return Decision.BLOCK;
        
        int scoreToAdd = getScoreForType(attackType);
        
        // Anti-Scan: Kalau nembak >= 3 port berbeda, skor bonus
        if (attackType.equals("PORT_SCAN")) {
            ipPortHistory.putIfAbsent(ip, ConcurrentHashMap.newKeySet());
            ipPortHistory.get(ip).add(port);
            if (ipPortHistory.get(ip).size() >= 3) scoreToAdd += 50;
        }

        int newScore = threatScores.getOrDefault(ip, 0) + scoreToAdd;
        threatScores.put(ip, newScore);

        if (newScore >= THRESHOLD_BLOCK) {
            permanentBlacklist.add(ip);
            threatScores.remove(ip);
            ipPortHistory.remove(ip);
            return Decision.BLOCK;
        } else if (newScore >= THRESHOLD_TARPIT && config.isTarpitEnabled()) {
            return Decision.TARPIT;
        } else if (newScore > 0) return Decision.MONITOR;
        return Decision.ALLOW;
    }

    private static int getScoreForType(String type) {
        switch (type.toUpperCase()) {
            case "SQL_INJECTION": return SCORE_SQLI;
            case "XSS": return SCORE_XSS;
            case "CMD_INJECTION": return SCORE_CMD_INJ;
            case "LFI": return SCORE_LFI;
            case "SENSITIVE_PATH": return SCORE_SENSITIVE;
            case "JAVA_INJECTION": return SCORE_JAVA;
            case "BRAINFUCK": return SCORE_BRAINFUCK;
            case "JSFUCK": return SCORE_JSFUCK;
            case "PORT_SCAN": return SCORE_PORT_SCAN;
            case "TIMEOUT_SCAN": return SCORE_TIMEOUT;
            default: return 10;
        }
    }
    public static int getScore(String ip) { return threatScores.getOrDefault(ip, 0); }
}
