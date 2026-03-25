package jmalvin.modsync.screens;

import jmalvin.modsync.ModSync;
import jmalvin.modsync.ModSyncClient;
import jmalvin.modsync.widgets.CommitList;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class RepositoryView extends Screen {
    private Screen lastScreen;
    private CommitList list;
    public RepositoryView(Screen lastScreen) {
        super(Component.literal("Repository View"));
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        list = new CommitList(this.minecraft, this.width, 3*this.height/4, 0, 30, 50);
        addRenderableWidget(list);

        Button refresh = new Button.Builder(Component.literal("Refresh"),
                (button) -> {
                    ModSyncClient.DOWNLOADER.fetch();
                    minecraft.setScreen(new RepositoryView(lastScreen));
                })
                .bounds(this.width - 85 - 115, 5, 80, 20).build();
        addRenderableWidget(refresh);

        Button reset = new Button.Builder(Component.literal("Change Repository"),
                (button) -> {
                    minecraft.setScreen(new RepositoryInputScreen(this));
                })
                .bounds(this.width - 115, 5, 110, 20).build();
        addRenderableWidget(reset);

        Button back = new Button.Builder(Component.literal("Back"),
                (button) -> minecraft.setScreen(lastScreen))
                .bounds(this.width / 2 - 105, this.height - 25, 100, 20).build();
        addRenderableWidget(back);

        Button pull = new Button.Builder(Component.literal("Update Mods"),
                (button) -> {
                    CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(ModSyncClient.DOWNLOADER::pull);
                    minecraft.setScreen(new SuccessScreen(lastScreen, future));
                })
                .bounds(this.width / 2 + 5, this.height - 25, 100, 20).build();
        try {
            if (ModSyncClient.DOWNLOADER.upToDate()) {
                pull.active = false;
                pull.setMessage(Component.literal("(Up to Date)"));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        addRenderableWidget(pull);


    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        super.render(guiGraphics, i, j, f);
        try {
            boolean upToDate = ModSyncClient.DOWNLOADER.upToDate();
            guiGraphics.drawString(this.minecraft.font,  upToDate ? "Mods are up to date" : "Update available: " + ModSyncClient.DOWNLOADER.getCommitsAhead().size() + " versions behind",
                                5, 15,  upToDate ? 0x3BB143 : 0xEDC001);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
