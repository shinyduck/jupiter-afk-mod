package me.jupiter.afk.mixin;

import me.jupiter.afk.AFKManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin targeting the base Player class.
 * Used for hooks that exist on both client and server players,
 * guarded with an instanceof check to only run server-side logic.
 */
@Mixin(Player.class)
public abstract class PlayerMixin {

    /**
     * Piggybacks on the player tick to check if the idle timeout has been exceeded.
     * Runs 20 times per second but is cheap — just a map lookup and a timestamp compare.
     */
    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        if ((Object) this instanceof ServerPlayer serverPlayer) {
            AFKManager.checkTimeout(serverPlayer);
        }
    }

    /**
     * Cancels hunger exhaustion while the player is AFK and freezeHunger is enabled.
     * This covers all hunger drain sources (sprinting, jumping, taking damage, etc.).
     */
    @Inject(method = "causeFoodExhaustion", at = @At("HEAD"), cancellable = true)
    private void onHungerDrain(float amount, CallbackInfo ci) {
        if ((Object) this instanceof ServerPlayer serverPlayer) {
            if (AFKManager.config.freezeHunger && AFKManager.isAfk(serverPlayer)) {
                ci.cancel();
            }
        }
    }

    /**
     * Cancels all incoming damage while the player is AFK and freezeDamage is enabled.
     * hurtServer is the server-side damage entry point in this MC version.
     */
    @Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
    private void onHurt(ServerLevel level, DamageSource source, float damage, CallbackInfoReturnable<Boolean> cir) {
        if ((Object)this instanceof ServerPlayer serverPlayer) {
            if (AFKManager.config.freezeDamage && AFKManager.isAfk(serverPlayer)) {
                cir.setReturnValue(false);
            }
        }
    }
}
