package mac.ilike2moveit.mixin;

import mac.ilike2moveit.MoveItCore;
import mac.ilike2moveit.config.MobModelOptionsScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Exposes iLike2MoveIt's own settings directly from the Resource Packs screen. */
@Mixin(PackSelectionScreen.class)
public abstract class PackSelectionScreenMixin extends Screen {
    @Unique
    private static boolean ilike2moveit$loggedPackOptions;

    protected PackSelectionScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void ilike2moveit$addPackOptionsButton(CallbackInfo ci) {
        addRenderableWidget(Button.builder(
                        Component.translatable("config.ilike2moveit.resource_pack_options.button"),
                        button -> {
                            if (minecraft != null) {
                                minecraft.setScreen(new MobModelOptionsScreen((Screen) (Object) this));
                            }
                        })
                .bounds(width - 156, 6, 150, 20)
                .build());
        if (!ilike2moveit$loggedPackOptions) {
            ilike2moveit$loggedPackOptions = true;
            MoveItCore.LOGGER.info("[Resource Pack Config] iLike2MoveIt settings button added to Resource Packs.");
        }
    }
}
