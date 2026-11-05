package com.cosmicstones.block;

import com.cosmicstones.CosmicStonesMod;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

public class MeteorCoreBlock extends Block {
    private static final Map<BlockPos, Item> PENDING_DROPS = new HashMap<>();

    public MeteorCoreBlock(Properties properties) {
        super(properties);
    }

    public static void setStoneForLocation(BlockPos pos, Item stone) {
        PENDING_DROPS.put(pos.immutable(), stone);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && !player.isCreative() && level instanceof ServerLevel serverLevel) {
            Item dropItem = PENDING_DROPS.remove(pos);
            if (dropItem == null) dropItem = CosmicStonesMod.MIND_STONE;
            ItemEntity drop = new ItemEntity(serverLevel, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(dropItem));
            drop.setGlowingTag(true);
            drop.setUnlimitedLifetime();
            serverLevel.addFreshEntity(drop);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
