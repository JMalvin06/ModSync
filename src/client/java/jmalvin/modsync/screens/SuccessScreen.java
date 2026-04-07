package jmalvin.modsync.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.LoadingDotsWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;

import java.util.concurrent.CompletableFuture;

public class SuccessScreen extends Screen {
    private final Screen lastScreen;
    private CompletableFuture<Boolean> future;

    private LoadingDotsWidget dots;
    private Button button;

    protected SuccessScreen(Screen lastScreen) {
        super(Component.literal("Success Screen"));
        this.lastScreen = lastScreen;
    }

    protected SuccessScreen(Screen lastScreen, CompletableFuture<Boolean> future) {
        super(Component.literal("Success Screen"));
        this.lastScreen = lastScreen;
        this.future = future;
    }

    @Override
    protected void init() {
        this.button = new Button.Builder(
                Component.literal("Return to Menu"),
                (btn) -> this.minecraft.setScreen(new TitleScreen()))
                .bounds(this.width / 2 - 50, 130, 100, 20).build();

        this.addRenderableWidget(button);

        if (future != null) {
            dots = new LoadingDotsWidget(this.font, Component.literal("Pulling mods.."));
            dots.setSize(40, 40);
            dots.setPosition(this.width / 2 - 20, this.height / 2 - 20);
            addRenderableWidget(dots);
            button.visible = false;
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        super.render(guiGraphics, i, j, f);
        if (future == null) {
            guiGraphics.drawCenteredString(this.font, Component.literal("Success!"), this.width / 2, 100, 16777215);
            guiGraphics.drawCenteredString(this.font, Component.literal("Please restart the game to use these mods."), this.width / 2, 115, 16777215);
        } else if (future.isDone()) {
            if (future.isCompletedExceptionally()) {
                minecraft.setScreen(new SyncErrorScreen(future.exceptionNow().getMessage()));
            }
            future = null;
            dots.visible = false;
            button.visible = true;
        }
    }
}
