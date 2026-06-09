package jmalvin.modsync.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FolderChecklist extends ObjectSelectionList<FolderChecklist.FolderEntry> {

    private final List<Button> buttons;
    private final HashMap<Path, Boolean> paths;

    public FolderChecklist(Minecraft minecraft, int width, int screenWidth, int height, int posY, int spacing, Path modFolders) {
        super(minecraft, width, height, posY, height+posY, spacing);
        int posX = (screenWidth - width)/2;
        this.setLeftPos(posX);
        buttons = new ArrayList<>();
        paths = new HashMap<>();
        if (!modFolders.toFile().isDirectory())
            throw new IllegalArgumentException("Not a directory: " + modFolders.getFileName());
        try (DirectoryStream<Path> folders = Files.newDirectoryStream(modFolders)) {
            for (Path folder : folders) {
                if (folder.toFile().isDirectory() && !folder.toFile().getName().equals(".git")) {
                    this.addEntry(new FolderEntry(minecraft.font, folder, posX, buttons, paths, width));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected int getScrollbarPosition() {
        return x0 + width;
    }

    public List<Button> getButtons() {
        return buttons;
    }

    public HashMap<Path, Boolean> getFolderMap() {
        return paths;
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
        return;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        super.render(guiGraphics, i, j, f);
    }

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        for (Button btn : buttons) {
            btn.mouseClicked(d, e, i);
        }
        return super.mouseClicked(d, e, i);
    }

    @Override
    public boolean isMouseOver(double d, double e) {
        for (Button btn : buttons) {
            btn.setFocused(btn.isMouseOver(d, e));
        }
        return super.isMouseOver(d, e);
    }

    protected static class FolderEntry extends ObjectSelectionList.Entry<FolderEntry> {
        private final Font font;
        private final int listX;
        private final Path folder;
        private Button option;
        protected FolderEntry(Font font, Path folder, int listX, List<Button> btns, HashMap<Path, Boolean> folderMap, int listWidth) {
            if (!folder.toFile().isDirectory()) {
                throw new IllegalArgumentException();
            }
            this.font = font;
            this.folder = folder;
            folderMap.put(folder, true);
            this.listX = listX;// + 10;
            this.option = Button.builder(Component.literal("Keep"), (btn) -> {
                if (folderMap.get(folder) == null) {
                    throw new IllegalArgumentException("No such folder exists in map");
                }
                option.setMessage(Component.literal(folderMap.get(folder) ? "Ignore" : "Keep"));
                folderMap.put(folder, !folderMap.get(folder));
            }).bounds(font.width(folder.getFileName().toString()) + listX + 10,0,45,15).build();
            btns.add(this.option);
        }

        @Override
        public void render(GuiGraphics guiGraphics, int x, int y, int k, int l, int m, int n, int o, boolean bl, float f) {
            // Apply indent and translation from list position
            String str = folder.getFileName().toString();
            guiGraphics.drawString(this.font, str, listX+5, y + 2, 0xFFFFFF);

            option.setY(y - 2);
            option.render(guiGraphics, x, y, f);
        }

        @Override
        public Component getNarration() {
            return null;
        }
    }
}
