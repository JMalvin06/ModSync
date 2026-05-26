package jmalvin.modsync.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.LoadingDotsText;
import net.minecraft.network.chat.Component;

public class LoadingDotsWidget extends AbstractWidget {
    private String loadingString;
    private String text;
    private boolean filling;
    private final Font font;
    private int tick;

    public LoadingDotsWidget(String text, int x, int y, Font font) {
        super(x, y, 0, 0, null);
        this.text = text;
        this.loadingString = "......";
        this.font = font;

        filling = true;
        tick = 0;
    }


    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
        if (tick % 4 == 0) {
            updateLoadingText();
        }

        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(getX() - getX() * 1.2, getY() - getY() * 1.2, 0);
        pose.scale(1.2f, 1.2f, 1);
        guiGraphics.drawString(font, loadingString, getX() - font.width(loadingString)/2, getY(), 0xFFFFFF);
        guiGraphics.drawString(font, text, getX() - font.width(text)/2, getY() + 20, 0xFFFFFF);
        tick++;
        pose.popPose();
    }

    private void updateLoadingText() {
        if (!filling) {
            this.loadingString = new StringBuilder(loadingString).reverse().toString();
        }
        int index = loadingString.indexOf(filling ? '.' : '|');
        if (index == -1) {
            this.loadingString = loadingString.substring(0,loadingString.length()-1) + (filling ? "." : "|");
        } else {
            this.loadingString = loadingString.substring(0, index) + (filling ? "|" : ".") + loadingString.substring(index + 1);
        }
        if (!filling) {
            this.loadingString = new StringBuilder(loadingString).reverse().toString();
        }

        if(index == -1) {
            filling = !filling;
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        return;
    }
}
