package jmalvin.modsync.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class GitErrorScreen extends Screen {
    private Screen lastScreen;
    protected GitErrorScreen(Screen lastScreen) {
        super(Component.literal("Git Error Screen"));
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        Button buttonWidget = new Button.Builder(
                Component.literal("Try Again"),
                (btn) -> this.minecraft.setScreen(lastScreen))
                .bounds(this.width / 2 - 50, 130, 100, 20).build();

        this.addRenderableWidget(buttonWidget);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        super.render(guiGraphics, i, j, f);
        guiGraphics.drawCenteredString(this.font, Component.literal("That repository is invalid or does not exist"), this.width / 2, 115, 16777215);
    }
}
