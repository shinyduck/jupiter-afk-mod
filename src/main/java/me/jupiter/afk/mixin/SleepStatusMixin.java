package me.jupiter.afk.mixin;

import me.jupiter.afk.AFKManager;
import net.minecraft.server.players.SleepStatus;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin targeting SleepStatus to exclude AFK players from the sleep calculation.
 * Vanilla counts all online players when determining how many need to sleep.
 * This override subtracts AFK players so they don't block the night skip.
 */
@Mixin(SleepStatus.class)
public class SleepStatusMixin {

    /** The vanilla count of active (online) players tracked by SleepStatus. */
    @Shadow
    private int activePlayers;

    @Inject(method = "sleepersNeeded", at = @At("HEAD"), cancellable = true)
    private void onSleepersNeeded(int sleepPercentageNeeded, CallbackInfoReturnable<Integer> cir) {
        if (!AFKManager.config.bypassSleep) return;

        // Subtract AFK players from the total so they don't block the night skip.
        int actualActive = Math.max(0, this.activePlayers - AFKManager.getAfkCount());

        // Re-apply vanilla's percentage math with the adjusted player count.
        int needed = Mth.ceil((float) (actualActive * sleepPercentageNeeded) / 100.0F);

        // Always require at least 1 sleeper so night doesn't skip with nobody in bed.
        cir.setReturnValue(Math.max(1, needed));
    }
}
