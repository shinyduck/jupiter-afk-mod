package me.jupiter.afk;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;

public class AFKMod implements ModInitializer {

    @Override
    public void onInitialize() {
        AFKManager.config = AFKConfig.load();

        // /afk command — toggles the player's own AFK state
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(Commands.literal("afk").executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    AFKManager.setAfk(player, !AFKManager.isAfk(player));
                    return 1;
                }))
        );

        // Seed last-action time when a player joins so they don't instantly time out
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                AFKManager.addPlayer(handler.getPlayer())
        );

        // Clean up maps when a player disconnects to prevent memory leaks
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                AFKManager.removePlayer(handler.getPlayer())
        );

        // Reset AFK on chat messages
        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, params) -> {
            if (AFKManager.config.resetOnChat) AFKManager.updateActivity(sender);
        });

        // Reset AFK on block interactions (right-clicking blocks)
        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            if (!level.isClientSide() && AFKManager.config.resetOnBlockInteractions) {
                if (player instanceof ServerPlayer serverPlayer) {
                    AFKManager.updateActivity(serverPlayer);
                }
            }
            return InteractionResult.PASS;
        });

        System.out.println("Jupiter AFK Mod has been initialized!");
    }
}
