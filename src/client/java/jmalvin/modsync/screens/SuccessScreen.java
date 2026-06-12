package jmalvin.modsync.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;

import java.util.concurrent.CompletableFuture;

public class SuccessScreen extends Screen {
    private CompletableFuture<Boolean> future;

    private Button button;

    protected SuccessScreen() {
        super(Component.literal("Success Screen"));
    }

    @Override
    protected void init() {
        this.button = new Button.Builder(
                Component.literal("Quit Game"),
                (btn) -> this.minecraft.stop())
                .bounds(this.width / 2 - 50, 130, 100, 20).build();

        this.addRenderableWidget(button);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, i, j, f);
        guiGraphics.drawCenteredString(this.font, Component.literal("Success!"), this.width / 2, 100, 16777215);
        guiGraphics.drawCenteredString(this.font, Component.literal("Please restart the game to use these mods."), this.width / 2, 115, 16777215);
    }
}
