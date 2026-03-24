package jmalvin.modsync.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class SuccessScreen extends Screen {
    private Screen lastScreen;
    protected SuccessScreen(Screen lastScreen) {
        super(Component.literal("Success Screen"));
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        Button buttonWidget = new Button.Builder(
                Component.literal("Quit Game"),
                (btn) -> this.minecraft.stop())
                .bounds(this.width / 2 - 50, 130, 100, 20).build();

        this.addRenderableWidget(buttonWidget);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        super.render(guiGraphics, i, j, f);
        guiGraphics.drawCenteredString(this.font, Component.literal("Success!"), this.width / 2, 100, 16777215);
        guiGraphics.drawCenteredString(this.font, Component.literal("Please restart the game to use these mods."), this.width / 2, 115, 16777215);
    }
}
