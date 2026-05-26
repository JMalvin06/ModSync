package jmalvin.modsync.screens;

import jmalvin.modsync.ModSync;
import jmalvin.modsync.ModSyncClient;
import jmalvin.modsync.tools.ModDownloader;
import jmalvin.modsync.widgets.FolderChecklist;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FolderSelectScreen extends Screen {
    private final Screen lastScreen;

    public FolderSelectScreen(Screen lastScreen) {
        super(Component.literal("Folder Select"));
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        FolderChecklist checklist = new FolderChecklist(this.minecraft, this.width/3, this.width, this.height/4, this.height/2 - this.height/4, 20, Path.of("modsync"));
        addRenderableWidget(checklist);

        Button back = new Button.Builder(Component.literal("Back"),
                (button) -> {
                    if (new File("modsync").exists()) {
                        try {
                            ModDownloader.deleteDirectory(Path.of("modsync"));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    ModSyncClient.DOWNLOADER.resetGit();
                    minecraft.setScreen(lastScreen);
                })
                .bounds(this.width / 2 - 105, this.height - 105, 100, 20).build();
        addRenderableWidget(back);

        Button next = new Button.Builder(Component.literal("Next"),
                (button) -> {
                    CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
                        try {
                            ArrayList<Path> ignoredPaths = new ArrayList<>();
                            checklist.getFolderMap().forEach((path, bool) -> {
                                if (!bool) {
                                    ignoredPaths.add(Path.of("modsync").relativize(path));
                                }
                            });
                            if (!ModSyncClient.CONFIG.fileExists()) {
                                ModSyncClient.CONFIG.createFile();
                            }
                            if (ignoredPaths.isEmpty()) {
                                ModSyncClient.CONFIG.delete("ignored");
                            } else {
                                ModSyncClient.CONFIG.setConfig("ignored", ignoredPaths);
                            }
                            return ModSyncClient.DOWNLOADER.moveFolders(checklist.getFolderMap());
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
                    minecraft.setScreen(new LoadingScreen(new SuccessScreen(), future));
                })
                .bounds(this.width / 2 + 5, this.height - 105, 100, 20).build();
        addRenderableWidget(next);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, i, j, f);

        String text = "Select the folders you want to pull from: ";
        guiGraphics.drawString(this.minecraft.font, text, (this.width - this.minecraft.font.width(text))/ 2, 35, 0xFFFFFF);
    }
}
