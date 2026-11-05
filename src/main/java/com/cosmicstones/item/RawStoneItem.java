package com.cosmicstones.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Rarity;
import net.minecraft.world.World;

public class RawStoneItem extends Item {
    private final String stoneType;

    public RawStoneItem(String stoneType) {
        super(new Item.Settings().maxCount(1).fireproof().rarity(Rarity.EPIC));
        this.stoneType = stoneType;
    }

    public String getStoneType() { return stoneType; }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!world.isClient && entity instanceof PlayerEntity player && !player.isCreative()) {
            if ((selected || player.getOffHandStack() == stack) && world.getTime() % 40 == 0) {
                player.damage(world.getDamageSources().magic(), 2.0F);
                player.addExhaustion(3.0F);
                if (world instanceof ServerWorld sw) {
                    sw.spawnParticles(ParticleTypes.LARGE_SMOKE, player.getX(), player.getY() + 1.0, player.getZ(), 8, 0.2, 0.3, 0.2, 0.02);
                }
                player.sendMessage(Text.literal("⚠ Raw Cosmic Radiation sears your hands! Socket into a Siphon.").formatted(Formatting.RED), true);
            }
        }
    }
}
