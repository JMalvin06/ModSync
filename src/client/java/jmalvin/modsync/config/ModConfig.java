package jmalvin.modsync.config;

import jmalvin.modsync.ModSync;
import net.fabricmc.loader.api.FabricLoader;
import org.eclipse.jgit.util.IO;

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
                setConfigData();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
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
            Files.createFile(configFile.toPath());
        }
    }

    public void setConfig(String key, String data) throws IOException {
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
        configData.put(key, data);
    }

    public String getConfig(String key) {
        System.out.println(configData);
        return configData.get(key);
    }

    public void delete(String key) throws IOException {
        if (configData.containsKey(key)) {
            List<String> lines = Files.readAllLines(configFile.toPath(), StandardCharsets.UTF_8);
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).trim().contains(key + "=")) {
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
