package com.cosmicstones.block;

import com.cosmicstones.CosmicStonesMod;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public class MeteorCoreBlock extends Block {
    private static final Map<BlockPos, Item> PENDING_DROPS = new HashMap<>();

    public MeteorCoreBlock(Settings settings) { super(settings); }

    public static void setStoneForLocation(BlockPos pos, Item stone) {
        PENDING_DROPS.put(pos.toImmutable(), stone);
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient && !player.isCreative()) {
            ServerWorld serverWorld = (ServerWorld) world;
            Item dropItem = PENDING_DROPS.remove(pos);
            if (dropItem == null) dropItem = CosmicStonesMod.MIND_STONE;
            ItemEntity drop = new ItemEntity(serverWorld, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(dropItem));
            drop.setGlowing(true);
            drop.setNeverDespawn();
            serverWorld.spawnEntity(drop);
        }
        return super.onBreak(world, pos, state, player);
    }
}
