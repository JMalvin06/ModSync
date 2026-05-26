package jmalvin.modsync.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.LoadingDotsWidget;
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
        LoadingDotsWidget dots = new LoadingDotsWidget(this.font, Component.literal("Pulling mods.."));
        dots.setSize(40, 40);
        dots.setPosition(this.width / 2 - 20, this.height / 2 - 20);
        addRenderableWidget(dots);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        super.render(guiGraphics, i, j, f);
        if (future.isDone()) {
            if (future.isCompletedExceptionally()) {
                minecraft.setScreen(new SyncErrorScreen(future.exceptionNow().getLocalizedMessage()));
            } else {
                minecraft.setScreen(nextScreen);
            }
        }
    }
}
