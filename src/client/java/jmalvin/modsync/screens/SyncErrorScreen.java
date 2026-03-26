package jmalvin.modsync.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;

public class SyncErrorScreen extends Screen{
    private final String message;
    public SyncErrorScreen(String message) {
        super(Component.literal("Sync Error Screen"));
        this.message = message;
    }

    @Override
    protected void init() {
        Button buttonWidget = new Button.Builder(
                Component.literal("Back"),
                (btn) -> this.minecraft.setScreen(new TitleScreen()))
                .bounds(this.width / 2 - 50, 130, 100, 20).build();

        this.addRenderableWidget(buttonWidget);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        super.render(guiGraphics, i, j, f);
        guiGraphics.drawCenteredString(this.font, Component.literal(message), this.width / 2, 115, 16777215);
    }
}
