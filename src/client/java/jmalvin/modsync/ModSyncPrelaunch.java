package jmalvin.modsync;

import jmalvin.modsync.widgets.FolderChecklist;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

public class ModSyncPrelaunch implements PreLaunchEntrypoint {
    @Override
    public void onPreLaunch() {
        // Windows specific BS
        if (SystemUtils.IS_OS_WINDOWS) {
            ArrayList<String> notDeleted = new ArrayList<>();
            if (ModSyncClient.CONFIG.getListConfig("to_delete") != null) {
                for (String fileName : ModSyncClient.CONFIG.getListConfig("to_delete")) {
                    try {
                        if (new File(fileName).exists())
                            Files.delete(Path.of(fileName));
                    } catch (Exception e) {
                        System.out.println("COULD NOT DELETE: " + fileName);
                        notDeleted.add(fileName);
                    }
                }
            }
            try {
                if (notDeleted.isEmpty()) {
                    ModSyncClient.CONFIG.delete("to_delete");
                } else {
                    ModSyncClient.CONFIG.setConfig("to_delete", notDeleted);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            if (new File("modsync_temp").exists()) {
                try {
                    moveFiles(new File("modsync_temp"), new File("."));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static void moveFiles(File currFolder, File newFolder) throws IOException {
        for (File file : currFolder.listFiles()) {
            if (file.isDirectory()) {
                moveFiles(file, new File(newFolder, file.getName()));
            } else {
                Path newPath = newFolder.toPath().resolve(file.getName());
                if (newPath.toFile().exists()) {
                    Files.delete(file.toPath());
                } else {
                    if (!newPath.getParent().toFile().exists()) {
                        Files.createDirectories(newPath.getParent());
                    }
                    Files.move(file.toPath(), newPath);
                }
            }
        }
        Files.delete(currFolder.toPath());
    }
}
