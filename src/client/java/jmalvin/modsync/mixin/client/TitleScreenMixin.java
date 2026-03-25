package jmalvin.modsync.mixin.client;

import jmalvin.modsync.ModSync;
import jmalvin.modsync.ModSyncClient;
import jmalvin.modsync.screens.RepositoryInputScreen;
import jmalvin.modsync.screens.RepositoryView;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
        SpriteIconButton modSync = SpriteIconButton.builder(Component.literal("Mod Sync"), (button) -> setModsync(), true).width(20).sprite(ResourceLocation.fromNamespaceAndPath(ModSync.MOD_ID, "icon/sync"), 15, 15).build();//.bounds(this.width / 2 - 100 + 205, i, 75, 20).build();
        modSync.setPosition(this.width / 2 - 100 + 205, i);
        this.addRenderableWidget(modSync);

    }

    @Unique
    private void setModsync() {
        if (ModSyncClient.DOWNLOADER.getGitDir() == null) {
            this.minecraft.setScreen(new RepositoryInputScreen(this));
        } else {
            try {
                ModSyncClient.DOWNLOADER.fetch();
                this.minecraft.setScreen(new RepositoryView(this));
            } catch (Exception e) {
                throw new RuntimeException();
            }
        }
    }

}
