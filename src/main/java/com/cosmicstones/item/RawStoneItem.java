package com.cosmicstones.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class RawStoneItem extends Item {
    private final String stoneType;

    public RawStoneItem(String stoneType, Properties properties) {
        super(properties);
        this.stoneType = stoneType;
    }

    public String getStoneType() {
        return stoneType;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        if (entity instanceof Player player && !player.isCreative()) {
            boolean inHands = player.getMainHandItem() == stack || player.getOffhandItem() == stack;
            if (inHands && level.getGameTime() % 40 == 0) {
                player.hurtServer(level, level.damageSources().magic(), 2.0F);
                player.causeFoodExhaustion(3.0F);
                level.sendParticles(ParticleTypes.LARGE_SMOKE, player.getX(), player.getY() + 1.0, player.getZ(), 8, 0.2, 0.3, 0.2, 0.02);
                player.displayClientMessage(Component.literal("⚠ Raw Cosmic Radiation sears your hands! Socket into a Siphon.").withStyle(ChatFormatting.RED), true);
            }
        }
    }
}
