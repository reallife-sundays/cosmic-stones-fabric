package com.cosmicstones.event;

import com.cosmicstones.item.CosmicSiphonItem;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public class PassiveHandler {
    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                ItemStack siphon = findSiphon(player);
                if (siphon.isEmpty() || !CosmicSiphonItem.isPassiveEnabled(siphon)) continue;
                String stone = CosmicSiphonItem.getSocketedStone(siphon);
                if ("space".equals(stone)) player.onLanding();
            }
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!world.isClient && hand == Hand.MAIN_HAND && entity instanceof LivingEntity living) {
                ItemStack siphon = findSiphon(player);
                if (!siphon.isEmpty() && CosmicSiphonItem.isPassiveEnabled(siphon)) {
                    if ("power".equals(CosmicSiphonItem.getSocketedStone(siphon))) {
                        int stacks = getStacks(player);
                        if (stacks < 10) setStacks(player, stacks + 1);
                        living.damage(world.getDamageSources().playerAttack(player), stacks * 1.5F);
                        if (stacks == 10 && world instanceof ServerWorld sw) {
                            sw.spawnParticles(ParticleTypes.EXPLOSION, living.getX(), living.getY() + 1, living.getZ(), 1, 0, 0, 0, 0);
                        }
                    }
                }
            }
            return ActionResult.PASS;
        });
    }

    private static int getStacks(PlayerEntity player) {
        return player.getScoreboardTags().stream().filter(t -> t.startsWith("overcharge_")).mapToInt(t -> Integer.parseInt(t.replace("overcharge_", ""))).findFirst().orElse(0);
    }

    private static void setStacks(PlayerEntity player, int val) {
        player.getScoreboardTags().removeIf(t -> t.startsWith("overcharge_"));
        player.addScoreboardTag("overcharge_" + val);
    }

    private static ItemStack findSiphon(PlayerEntity player) {
        if (player.getMainHandStack().getItem() instanceof CosmicSiphonItem) return player.getMainHandStack();
        if (player.getOffHandStack().getItem() instanceof CosmicSiphonItem) return player.getOffHandStack();
        for (int i = 0; i < 9; i++) {
            ItemStack s = player.getInventory().getStack(i);
            if (s.getItem() instanceof CosmicSiphonItem) return s;
        }
        return ItemStack.EMPTY;
    }
}
