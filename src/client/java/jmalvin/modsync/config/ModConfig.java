package jmalvin.modsync.config;

import jmalvin.modsync.ModSync;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class ModConfig {
    private final File configFile;
    private HashMap<String, String> configData;

    public ModConfig() {
        configFile = FabricLoader.getInstance().getConfigDir().resolve(ModSync.MOD_ID + ".properties").toFile();
        if (!configFile.exists()) {
            try {
                Files.createFile(configFile.toPath());
            } catch (Exception e) {
                // TODO handle exception properly
                throw new RuntimeException();
            }
        }
        setConfigData();
    }

    private void setConfigData() {
        configData = new HashMap<>();
        try {
            if (configFile.exists()) {
                Scanner scnr = new Scanner(configFile);
                while (scnr.hasNext()) {
                    String config = scnr.nextLine().trim();
                    if (!config.startsWith("#") && !config.isBlank()) {
                        String[] list = config.split("=");
                        configData.put(list[0].trim(), list[1].trim());
                    }
                }
            } else {
                Files.createFile(configFile.toPath());
            }
        } catch (Exception e) {
            // TODO handle exception properly
            throw new RuntimeException();
        }
    }

    public void setConfig(String key, String data) {
        try {
            if (!configData.containsKey(key)) {
                Files.write(configFile.toPath(), (key + " = " + data).getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } else {

                List<String> lines = Files.readAllLines(configFile.toPath(), StandardCharsets.UTF_8);
                for (int i = 0; i < lines.size(); i++) {
                    if (lines.get(i).trim().contains(key + "=")) {
                        lines.set(i, key + " = " + data);
                        break;
                    }
                }
                Files.write(configFile.toPath(), lines, StandardCharsets.UTF_8);

            }
        } catch (Exception e) {
            // TODO handle
            throw new RuntimeException();
        }
        configData.put(key, data);
    }

    public String getConfig(String key) {
        System.out.println(configData);
        return configData.get(key);
    }

    public void delete(String key) {
        if (configData.containsKey(key)) {
            try {
                List<String> lines = Files.readAllLines(configFile.toPath(), StandardCharsets.UTF_8);
                for (int i = 0; i < lines.size(); i++) {
                    if (lines.get(i).trim().contains(key + "=")) {
                        lines.remove(i);
                        break;
                    }
                }
                Files.write(configFile.toPath(), lines, StandardCharsets.UTF_8);
                configData.remove(key);
            } catch (Exception e) {
                // TODO handle this
                throw new RuntimeException();
            }
        }
    }

    public void clear() {
        try {
            Files.write(configFile.toPath(), "".getBytes());
            configData.clear();
        } catch (Exception e) {
            // TODO handle
            throw new RuntimeException();
        }
    }
}
