package jmalvin.modsync.screens;

import jmalvin.modsync.tools.ModDownloader;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;

@Environment(EnvType.CLIENT)
public class ModSyncScreen extends Screen {
    private Screen lastScreen;
    private EditBox textField;
    private String repository;
    private Button submit;

    public ModSyncScreen(Screen lastScreen) {
        super(Component.literal("Mod Sync"));
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        this.textField = new EditBox(this.font, this.width / 2 - 100, 100, 200, 20, this.textField, Component.literal("Github Repo"));
        this.textField.setMaxLength(Integer.MAX_VALUE);
        this.textField.setResponder(this::updateTextBox);
        this.addRenderableWidget(textField);

        this.submit = new Button.Builder(
                Component.literal("Submit"), (btn) -> downloadMods()).bounds(this.width / 2 - 40 + 45, 125, 80, 20).build();
        this.addRenderableWidget(submit);

        Button buttonWidget = new Button.Builder(
            Component.literal("Back"),
                (btn) -> this.minecraft.setScreen(lastScreen))
                .bounds(this.width / 2 - 40 - 45, 125, 80, 20).build();
        this.addRenderableWidget(buttonWidget);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        super.render(guiGraphics, i, j, f);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 16777215);
        guiGraphics.drawCenteredString(this.font, Component.literal("Enter Link to GitHub Repository:"), this.width / 2, 90, 16777215);
    }

    protected void updateTextBox(String text) {
        this.repository = text;
    }

    private void downloadMods() {
        if (repository != null && !repository.isBlank() && repository.startsWith("https://github.com")) {
            try {
                ModDownloader.setupRepo(repository);
                this.minecraft.setScreen(new SuccessScreen(lastScreen));
            } catch (GitAPIException | JGitInternalException e) {
                this.minecraft.setScreen(new GitErrorScreen(this));
            }
        }

    }
}
