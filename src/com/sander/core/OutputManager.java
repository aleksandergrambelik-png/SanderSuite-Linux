package com.sander.core;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class OutputManager {
    private static final String CSV_FILE = "sander_log.csv";
    private static final String JSON_FILE = "sander_log.jsonl";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final BlockingQueue<String> csvQueue = new LinkedBlockingQueue<>(1000);
    private static final BlockingQueue<String> jsonQueue = new LinkedBlockingQueue<>(1000);
    private final ConfigManager config;

    public OutputManager(ConfigManager config) {
        this.config = config;
        initFiles();
        new Thread(() -> writeLoop(csvQueue, CSV_FILE), "CSV-Writer").start();
        new Thread(() -> writeLoop(jsonQueue, JSON_FILE), "JSON-Writer").start();
    }

    private void initFiles() {
        try {
            File csv = new File(CSV_FILE);
            if (!csv.exists()) { csv.createNewFile(); writeSync(CSV_FILE, "timestamp,event,ip,port,details\n"); }
            new File(JSON_FILE).createNewFile();
        } catch (Exception e) {}
    }

    private void writeLoop(BlockingQueue<String> q, String file) {
        while(true) { try { writeSync(file, q.take()); } catch (Exception e) {} }
    }

    private synchronized void writeSync(String file, String content) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) { bw.write(content); } catch (Exception e) {}
    }

    public void log(String event, String ip, int port, String details) {
        String time = DATE_FORMAT.format(new Date());
        String safeDetails = details.replace("\"", "'").replace("\\", "\\\\");
        
        // Real-time output ke layar
        System.out.print("\r[LIVE] [" + event + "] " + ip + ":" + port + " - " + details + "          \n");
        System.out.flush(); 
        
        if (config.isCsvEnabled()) csvQueue.offer(time + "," + event + "," + ip + "," + port + ",\"" + safeDetails + "\"\n");
        if (config.isJsonEnabled()) jsonQueue.offer("{\"timestamp\":\"" + time + "\",\"event\":\"" + event + "\",\"ip\":\"" + ip + "\",\"port\":" + port + ",\"details\":\"" + safeDetails + "\"}\n");
    }
}
