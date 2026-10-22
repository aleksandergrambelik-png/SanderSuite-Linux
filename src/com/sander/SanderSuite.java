package com.sander;
import com.sander.core.ConfigManager;
import com.sander.modules.BeaconEngine;

public class SanderSuite {
    public static void main(String[] args) {
        if (args.length == 0) { printHelp(); return; }
        ConfigManager.Profile profile = ConfigManager.Profile.LITE;
        for (String arg : args) { if (arg.equalsIgnoreCase("--profile=full")) profile = ConfigManager.Profile.FULL; }
        ConfigManager config = new ConfigManager(profile);
        String mode = args[0].toLowerCase();
        
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║     SanderSuite v3.0 (Ultimate AI + WAF Edition)     ║");
        System.out.println("║     Profile: " + profile.name() + " | Mode: " + mode.toUpperCase() + "               ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
        
        switch (mode) {
            case "beacon": BeaconEngine.start(args, config); break;
            default: printHelp();
        }
    }
    private static void printHelp() {
        System.out.println("USAGE: java -cp bin com.sander.SanderSuite beacon [OPTIONS]");
        System.out.println("OPTIONS: --profile=full | --profile=lite");
    }
}
