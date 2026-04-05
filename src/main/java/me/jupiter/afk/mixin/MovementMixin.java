package me.jupiter.afk.mixin;

import me.jupiter.afk.AFKManager;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin targeting the server-side packet listener to intercept player movement packets.
 * Handles both position changes (walking) and rotation changes (looking around).
 */
@Mixin(ServerGamePacketListenerImpl.class)
public class MovementMixin {

    @Shadow
    public ServerPlayer player;

    @Inject(method = "handleMovePlayer", at = @At("HEAD"))
    private void onMove(ServerboundMovePlayerPacket packet, CallbackInfo ci) {

        // Position change: only reset if the player moved more than 0.01 blocks,
        // filtering out sub-tick physics jitter that isn't real player movement.
        if (AFKManager.config.resetOnMovement && packet.hasPosition()) {
            double dx = Math.abs(packet.getX(player.getX()) - player.getX());
            double dy = Math.abs(packet.getY(player.getY()) - player.getY());
            double dz = Math.abs(packet.getZ(player.getZ()) - player.getZ());

            if (dx > 0.01 || dy > 0.01 || dz > 0.01) {
                AFKManager.updateActivity(this.player);
            }
        }

        // Rotation change: any look movement resets the timer.
        if (AFKManager.config.resetOnLook && packet.hasRotation()) {
            AFKManager.updateActivity(this.player);
        }
    }
}
