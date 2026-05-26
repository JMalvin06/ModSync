package jmalvin.modsync.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.eclipse.jgit.revwalk.RevCommit;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class FolderChecklist extends AbstractSelectionList<FolderChecklist.FolderEntry> {

    private final List<Button> buttons;
    private final HashMap<Path, Boolean> paths;
    public FolderChecklist(Minecraft minecraft, int width, int screenWidth, int height, int posY, int spacing, Path modFolders) {
        super(minecraft, width, height, posY, spacing);
        int posX = (screenWidth - width)/2;
        this.setX(posX);
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

    public List<Button> getButtons() {
        return buttons;
    }

    public HashMap<Path, Boolean> getFolderMap() {
        return paths;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        return;
    }

    protected static class FolderEntry extends Entry<FolderEntry> {
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
            }).bounds(0,0,45,15).build();
            btns.add(this.option);
        }

        @Override
        public void render(GuiGraphics guiGraphics, int x, int y, int k, int l, int m, int n, int o, boolean bl, float f) {
            // Apply indent and translation from list position
            x = listX + 5;
            String str = folder.getFileName().toString();
            guiGraphics.drawString(this.font, str, x, y, 0xFFFFFF);

            option.setPosition(font.width(str) + x + 5,y - 4);
        }
    }
}
