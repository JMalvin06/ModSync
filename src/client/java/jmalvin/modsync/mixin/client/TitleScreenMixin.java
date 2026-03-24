package jmalvin.modsync.mixin.client;

import jmalvin.modsync.ModSync;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import jmalvin.modsync.screens.ModSyncScreen;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Component component) {
        super(component);
    }

    @Inject(at = @At("RETURN"), method = "createNormalMenuOptions")
    public void addCustomButton(int i, int j, CallbackInfo ci) {
        //SpriteIconButton.builder(Component.translatable("options.language"), () -> this.minecraft.setScreen(new ModSyncScreen(this), true).width(i).sprite(ResourceLocation.withDefaultNamespace("icon/language"), 15, 15).build();
        ModSync.LOGGER.info("HELLO: " + ResourceLocation.fromNamespaceAndPath(ModSync.MOD_ID, "sync.png").toString());
        SpriteIconButton modSync = SpriteIconButton.builder(Component.literal("Mod Sync"), (button) -> this.minecraft.setScreen(new ModSyncScreen(this)), true).width(20).sprite(ResourceLocation.fromNamespaceAndPath(ModSync.MOD_ID, "icon/sync"), 15, 15).build();//.bounds(this.width / 2 - 100 + 205, i, 75, 20).build();
        modSync.setPosition(this.width / 2 - 100 + 205, i);
        this.addRenderableWidget(modSync);

    }

}
