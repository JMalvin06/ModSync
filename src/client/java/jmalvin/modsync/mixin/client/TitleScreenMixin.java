package jmalvin.modsync.mixin.client;

import jmalvin.modsync.ModSync;
import jmalvin.modsync.ModSyncClient;
import jmalvin.modsync.screens.FolderSelectScreen;
import jmalvin.modsync.screens.RepositoryInputScreen;
import jmalvin.modsync.screens.RepositoryView;
import jmalvin.modsync.screens.SyncErrorScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.eclipse.jgit.api.errors.TransportException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Component component) {
        super(component);
    }

    @Inject(at = @At("RETURN"), method = "createNormalMenuOptions")
    public void addCustomButton(int i, int j, CallbackInfo ci) {
        ImageButton modSyncButton = new ImageButton(this.width / 2 - 100 + 205, i, 20, 20, 0, 0, 20, new ResourceLocation(ModSync.MOD_ID, "textures/icon/sync.png"), 32, 64, (button) -> setModsync());
        this.addRenderableWidget(modSyncButton);

    }

    @Unique
    private void setModsync() {
        if (ModSyncClient.DOWNLOADER.getGitDir() == null) {
            this.minecraft.setScreen(new RepositoryInputScreen(this));
        } else {
            try {
                ModSyncClient.DOWNLOADER.fetch();
                this.minecraft.setScreen(new RepositoryView(this));
            } catch (TransportException e) {
                minecraft.setScreen(new SyncErrorScreen("There was a connection error, please try again."));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

}
