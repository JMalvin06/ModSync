package jmalvin.modsync.screens;

import jmalvin.modsync.widgets.LoadingDotsWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.concurrent.CompletableFuture;

public class LoadingScreen extends Screen {
    private Screen nextScreen;
    private CompletableFuture<Boolean> future;
    protected LoadingScreen(Screen nextScreen, CompletableFuture<Boolean> future) {
        super(Component.literal("Loading Screen"));
        this.nextScreen = nextScreen;
        this.future = future;
        if (future == null) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    protected void init() {
        LoadingDotsWidget dots = new LoadingDotsWidget("Pulling mods..", this.width/2, this.height/2, this.font);
        //dots.setSize(40, 40);
        dots.setPosition(this.width / 2, this.height / 2 - 40);
        addRenderableWidget(dots);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, i, j, f);
        if (future.isDone()) {
            if (future.isCompletedExceptionally()) {
                String message = future.handle((r, ex) -> ex != null ? ex.getLocalizedMessage() : null).join();
                minecraft.setScreen(new SyncErrorScreen(message));
            } else {
                minecraft.setScreen(nextScreen);
            }
        }
    }
}
