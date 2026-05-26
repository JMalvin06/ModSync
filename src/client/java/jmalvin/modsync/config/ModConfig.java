package jmalvin.modsync.config;

import jmalvin.modsync.ModSync;
import net.fabricmc.loader.api.FabricLoader;
import org.eclipse.jgit.util.IO;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class ModConfig {
    private final File configFile;
    private HashMap<String, String> configData;

    public ModConfig() {
        configFile = FabricLoader.getInstance().getConfigDir().resolve(ModSync.MOD_ID + ".properties").toFile();
        try {
            if (!configFile.exists()) {
                Files.createFile(configFile.toPath());
            }
            setConfigData();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean fileExists() {
        return configFile.exists();
    }

    public void createFile() throws IOException {
        Files.createFile(configFile.toPath());
    }

    private void setConfigData() throws IOException{
        configData = new HashMap<>();
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
            createFile();
        }
    }

    public void setConfig(String key, String data) throws IOException {
        if (!configData.containsKey(key)) {
            Files.write(configFile.toPath(), (key + " = " + data).getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } else {
            List<String> lines = Files.readAllLines(configFile.toPath(), StandardCharsets.UTF_8);
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).contains(key + " =") || lines.get(i).contains(key + "=")) {
                    lines.set(i, key + " = " + data);
                    break;
                }
            }
            Files.write(configFile.toPath(), lines, StandardCharsets.UTF_8);
        }
        configData.put(key, data);
    }

    public <T> void setConfig(String key, ArrayList<T> data) throws IOException {
        StringBuilder configString = new StringBuilder();
        for (T entry : data) {
            configString.append(entry.toString());
            configString.append(",");
        }
        configString.deleteCharAt(configString.length()-1);
        setConfig(key, configString.toString());
    }

    public String getStringConfig(String key) {
        return configData.get(key);
    }

    public String[] getListConfig(String key) {
        if (configData.containsKey(key)) {
            return configData.get(key).split(",");
        } else {
            return null;
        }
    }

    public void delete(String key) throws IOException {
        if (configData.containsKey(key)) {
            List<String> lines = Files.readAllLines(configFile.toPath(), StandardCharsets.UTF_8);
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).contains(key + " =") || lines.get(i).contains(key + "=")) {
                    lines.remove(i);
                    break;
                }
            }
            Files.write(configFile.toPath(), lines, StandardCharsets.UTF_8);
            configData.remove(key);
        }
    }

    public void clear() throws IOException {
        Files.write(configFile.toPath(), "".getBytes());
        configData.clear();
    }
}
