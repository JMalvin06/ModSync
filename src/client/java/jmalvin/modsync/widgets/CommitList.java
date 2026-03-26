package jmalvin.modsync.widgets;

import jmalvin.modsync.ModSync;
import jmalvin.modsync.ModSyncClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

public class CommitList extends AbstractSelectionList<CommitList.CommitEntry> {

    public CommitList(Minecraft minecraft, int width, int height, int posX, int posY, int spacing) {
        super(minecraft, width, height, posY, spacing);
        setX(posX);
        try {
            for (RevCommit commit : ModSyncClient.DOWNLOADER.getCommitsAhead()) {
                this.addEntry(new CommitEntry(minecraft.font, commit, false, posX));
            }
            this.addEntry(new CommitEntry(minecraft.font, ModSyncClient.DOWNLOADER.getCurrentCommit(), true, posX));
            for (RevCommit commit : ModSyncClient.DOWNLOADER.getCommitsBehind()) {
                this.addEntry(new CommitEntry(minecraft.font, commit, false, posX));
            }
        } catch (IOException | GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        return;
    }

    @Override
    protected int getScrollbarPosition() {
        return this.width - 5;
    }

    @Override
    protected void renderListItems(GuiGraphics guiGraphics, int i, int j, float f) {
        super.renderListItems(guiGraphics, i, j, f);
    }

    protected static class CommitEntry extends ContainerObjectSelectionList.Entry<CommitEntry> {
        private final RevCommit commit;
        private final Font font;
        private final boolean isCurrent;
        private final int listX;
        protected CommitEntry(Font font, RevCommit commit, boolean isCurrent, int listX) {
            this.font = font;
            this.commit = commit;
            this.isCurrent = isCurrent;
            this.listX = listX;
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return Collections.emptyList();
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return Collections.emptyList();
        }

        @Override
        public void render(GuiGraphics guiGraphics, int x, int y, int k, int l, int m, int n, int o, boolean bl, float f) {
            int color = isCurrent ? 0xFFFF00 : 0xFFFFFF;
            // Apply indent and translation from list position
            x = 10 + listX;
            guiGraphics.drawString(this.font, "commit " + commit.name() +  (isCurrent ? " -> (you are here)" : ""), x, y, color);
            guiGraphics.drawString(this.font, commit.getShortMessage().trim(), x, y + 10, color);
            guiGraphics.drawString(this.font, "Author: " + commit.getAuthorIdent().getName() + " <" + commit.getAuthorIdent().getEmailAddress() + ">", x, y + 20, color);
            Instant instant = Instant.ofEpochSecond(commit.getCommitTime());
            ZonedDateTime zonedDateTime = instant.atZone(ZoneId.systemDefault());
            guiGraphics.drawString(this.font, "Date:    " + zonedDateTime.format(DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss yyyy z")), x, y + 30, color);

        }
    }


}
