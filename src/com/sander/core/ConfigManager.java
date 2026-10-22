package com.sander.core;
public class ConfigManager {
    public enum Profile { FULL, LITE }
    private Profile profile;
    private int maxThreads;
    private boolean enableCsv, enableJson, enableTxt, enableTarpit, enableRecon;
    public ConfigManager(Profile profile) {
        this.profile = profile;
        if (profile == Profile.FULL) { maxThreads = 250; enableCsv = true; enableJson = true; enableTxt = true; enableTarpit = true; enableRecon = true; }
        else { maxThreads = 50; enableCsv = false; enableJson = false; enableTxt = true; enableTarpit = false; enableRecon = false; }
    }
    public int getMaxThreads() { return maxThreads; }
    public boolean isCsvEnabled() { return enableCsv; }
    public boolean isJsonEnabled() { return enableJson; }
    public boolean isTxtEnabled() { return enableTxt; }
    public boolean isTarpitEnabled() { return enableTarpit; }
    public boolean isReconEnabled() { return enableRecon; }
}
