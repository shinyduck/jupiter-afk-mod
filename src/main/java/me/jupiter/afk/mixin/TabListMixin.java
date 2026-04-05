package me.jupiter.afk.mixin;

import me.jupiter.afk.AFKManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin targeting ServerPlayer to override the tab-list display name.
 * When a player is AFK, returns a formatted name (e.g. §7[AFK] §rPlayerName)
 * instead of null (which tells the client to use the default name).
 * <p>
 * AFKManager.refreshTabList() sends a ClientboundPlayerInfoUpdatePacket to all
 * clients whenever AFK state changes, causing this method to be re-queried immediately.
 */
@Mixin(ServerPlayer.class)
public class TabListMixin {

    @Inject(method = "getTabListDisplayName", at = @At("HEAD"), cancellable = true)
    private void onGetTabListDisplayName(CallbackInfoReturnable<Component> cir) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        if (AFKManager.isAfk(self)) {
            String formatted = AFKManager.config.afkPlayerName
                    .replace("%player%", self.getScoreboardName());
            cir.setReturnValue(Component.literal(formatted));
        }
    }
}
