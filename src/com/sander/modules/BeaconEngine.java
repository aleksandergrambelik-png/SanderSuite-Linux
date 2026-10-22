package com.sander.modules;
import com.sander.core.ConfigManager;
import com.sander.core.OutputManager;
import com.sander.ai.AIBrain;
import com.sander.ai.WafEngine;
import java.io.*;
import java.net.*;
import java.util.Random;

public class BeaconEngine {
    private static final int[] PORTS = {2121, 2222, 3306, 8080, 33890};
    private static final Random RAND = new Random();
    private static final int MAX_TARPIT_DURATION_MS = 120000;
    private static final long TARPIT_DELAY_MS = 1500;
    private static OutputManager logger;

    public static void start(String[] args, ConfigManager config) {
        logger = new OutputManager(config);
        System.out.println("[BEACON] Starting Ultimate Honeypot on ports: 2121, 2222, 3306, 8080, 33890");
        for (int p : PORTS) new Thread(() -> listen(p, config), "Listener-" + p).start();
    }

    private static void listen(int port, ConfigManager config) {
        try (ServerSocket ss = new ServerSocket(port)) {
            logger.log("LISTEN", "0.0.0.0", port, "Port active");
            while (true) {
                Socket s = ss.accept();
                new Thread(() -> handle(s, port, config), "Handler-" + port).start();
            }
        } catch (Exception e) { logger.log("ERROR", "0.0.0.0", port, e.getMessage()); }
    }

    private static void handle(Socket s, int port, ConfigManager config) {
        String ip = s.getInetAddress().getHostAddress();
        try {
            s.setSoTimeout(1500);
            InputStream in = s.getInputStream();
            byte[] buf = new byte[2048];
            int len = in.read(buf);
            
            if (len == -1) {
                AIBrain.Decision decision = AIBrain.analyze(ip, "TIMEOUT_SCAN", port, config);
                executeDecision(s, ip, port, decision, "SILENT_SCAN", config);
                return; 
            }
            
            if (len > 0) {
                String rawReq = new String(buf, 0, len);
                
                // Panggil WAF Engine
                String threatType = WafEngine.inspect(rawReq);
                
                AIBrain.Decision decision = AIBrain.Decision.ALLOW;
                String type = "UNKNOWN";

                if (!threatType.equals("CLEAN")) {
                    decision = AIBrain.analyze(ip, threatType, port, config);
                    type = threatType;
                }
                
                executeDecision(s, ip, port, decision, type, config);
                return;
            }
            
            AIBrain.Decision decision = AIBrain.analyze(ip, "TIMEOUT_SCAN", port, config);
            executeDecision(s, ip, port, decision, "SILENT_SCAN", config);

        } catch (SocketTimeoutException e) {
            AIBrain.Decision decision = AIBrain.analyze(ip, "TIMEOUT_SCAN", port, config);
            executeDecision(s, ip, port, decision, "TIMEOUT_SCAN", config);
        } catch (Exception e) {
        } finally { try { s.close(); } catch(Exception e){} }
    }

    private static void executeDecision(Socket s, String ip, int port, AIBrain.Decision dec, String type, ConfigManager config) {
        switch (dec) {
            case BLOCK: logger.log("AI_BLOCK", ip, port, type + " (Score exceeded)"); break;
            case TARPIT: logger.log("AI_TARPIT", ip, port, type + " (Slowing down)"); tarpit(s, ip); break;
            case MONITOR: logger.log("AI_MONITOR", ip, port, type + " (Score: " + AIBrain.getScore(ip) + ")"); sendBanner(s, port); break;
            default: sendBanner(s, port);
        }
    }

    private static void tarpit(Socket s, String ip) {
        try {
            OutputStream out = s.getOutputStream();
            InputStream in = s.getInputStream();
            out.write("220 SanderTrap Tarpit\r\n".getBytes()); out.flush();
            long start = System.currentTimeMillis();
            byte[] b = new byte[1];
            while (in.read(b) != -1) {
                if (System.currentTimeMillis() - start > MAX_TARPIT_DURATION_MS) break;
                out.write('.'); out.flush();
                Thread.sleep(TARPIT_DELAY_MS);
            }
        } catch (Exception e) {}
    }

    private static void sendBanner(Socket s, int port) {
        try {
            String banner = "";
            if (port == 2222) banner = "SSH-2.0-OpenSSH_" + RAND.nextInt(9) + ".0\r\n";
            else if (port == 8080) banner = "HTTP/1.1 200 OK\r\nServer: Fake\r\n\r\n<html>Login</html>";
            else banner = "220 Service Ready\r\n";
            s.getOutputStream().write(banner.getBytes()); s.getOutputStream().flush();
        } catch (Exception e) {}
    }
}
