package com.cosmicstones.event;

import com.cosmicstones.item.CosmicSiphonItem;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class PassiveHandler {
    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                ItemStack siphon = findSiphon(player);
                if (siphon.isEmpty() || !CosmicSiphonItem.isPassiveEnabled(siphon)) continue;
                String stone = CosmicSiphonItem.getSocketedStone(siphon);
                if ("space".equals(stone)) player.resetFallDistance();
            }
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!world.isClientSide() && hand == InteractionHand.MAIN_HAND && entity instanceof LivingEntity living && world instanceof ServerLevel sw) {
                ItemStack siphon = findSiphon(player);
                if (!siphon.isEmpty() && CosmicSiphonItem.isPassiveEnabled(siphon)) {
                    if ("power".equals(CosmicSiphonItem.getSocketedStone(siphon))) {
                        int stacks = getStacks(player);
                        if (stacks < 10) setStacks(player, stacks + 1);
                        living.hurtServer(sw, sw.damageSources().playerAttack(player), stacks * 1.5F);
                        if (stacks == 10) {
                            sw.sendParticles(ParticleTypes.EXPLOSION, living.getX(), living.getY() + 1, living.getZ(), 1, 0, 0, 0, 0);
                        }
                    }
                }
            }
            return InteractionResult.PASS;
        });
    }

    private static int getStacks(Player player) {
        return player.getTags().stream()
                .filter(t -> t.startsWith("overcharge_"))
                .mapToInt(t -> Integer.parseInt(t.replace("overcharge_", "")))
                .findFirst().orElse(0);
    }

    private static void setStacks(Player player, int val) {
        player.getTags().removeIf(t -> t.startsWith("overcharge_"));
        player.addTag("overcharge_" + val);
    }

    private static ItemStack findSiphon(Player player) {
        if (player.getMainHandItem().getItem() instanceof CosmicSiphonItem) return player.getMainHandItem();
        if (player.getOffhandItem().getItem() instanceof CosmicSiphonItem) return player.getOffhandItem();
        for (int i = 0; i < 9; i++) {
            ItemStack s = player.getInventory().getItem(i);
            if (s.getItem() instanceof CosmicSiphonItem) return s;
        }
        return ItemStack.EMPTY;
    }
}
