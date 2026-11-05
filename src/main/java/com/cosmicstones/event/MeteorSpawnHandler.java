package com.cosmicstones.event;

import com.cosmicstones.CosmicStonesMod;
import com.cosmicstones.block.MeteorCoreBlock;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

import java.util.Random;

public class MeteorSpawnHandler {
    private static final Random RANDOM = new Random();
    private static BlockPos pendingPos = null;
    private static int countdownTicks = 0;
    private static boolean autoEnabled = false;

    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("cosmicmeteor")
                    .requires(s -> s.hasPermissionLevel(2))
                    .then(CommandManager.literal("auto")
                            .then(CommandManager.literal("start").executes(ctx -> {
                                autoEnabled = true;
                                ctx.getSource().sendFeedback(() -> Text.literal("✔ Meteor auto-spawn ENABLED.").formatted(Formatting.GREEN), true);
                                return 1;
                            }))
                            .then(CommandManager.literal("stop").executes(ctx -> {
                                autoEnabled = false;
                                ctx.getSource().sendFeedback(() -> Text.literal("✖ Meteor auto-spawn DISABLED.").formatted(Formatting.RED), true);
                                return 1;
                            }))
                    )
            );
        });

        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (world.getRegistryKey() != World.OVERWORLD) return;
            if (pendingPos == null && autoEnabled && world.isNight() && world.getTime() % 24000 == 14000) {
                if (!world.getPlayers().isEmpty() && RANDOM.nextFloat() < 0.20F) {
                    ServerPlayerEntity anchor = world.getPlayers().get(RANDOM.nextInt(world.getPlayers().size()));
                    int tx = anchor.getBlockX() + (RANDOM.nextBoolean() ? 1 : -1) * (50 + RANDOM.nextInt(60));
                    int tz = anchor.getBlockZ() + (RANDOM.nextBoolean() ? 1 : -1) * (50 + RANDOM.nextInt(60));
                    int ty = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, tx, tz);

                    pendingPos = new BlockPos(tx, ty, tz);
                    countdownTicks = 9000;
                    broadcast(world, "☄ Celestial anomaly breached orbit. Impact at [" + tx + ", " + ty + ", " + tz + "] in 7.5 minutes.", Formatting.LIGHT_PURPLE);
                }
            }

            if (pendingPos != null && countdownTicks > 0) {
                countdownTicks--;
                if (countdownTicks == 0) {
                    spawnImpact(world, pendingPos);
                    pendingPos = null;
                }
            }
        });
    }

    private static void spawnImpact(ServerWorld world, BlockPos center) {
        world.createExplosion(null, center.getX(), center.getY(), center.getZ(), 6.0F, World.ExplosionSourceType.BLOCK);
        for (int x = -3; x <= 3; x++) {
            for (int y = -2; y <= 1; y++) {
                for (int z = -3; z <= 3; z++) {
                    if (x * x + y * y + z * z <= 6.5D) {
                        world.setBlockState(center.add(x, y, z), RANDOM.nextBoolean() ? Blocks.OBSIDIAN.getDefaultState() : Blocks.CRYING_OBSIDIAN.getDefaultState());
                    }
                }
            }
        }
        world.setBlockState(center, CosmicStonesMod.METEOR_CORE.getDefaultState());
        MeteorCoreBlock.setStoneForLocation(center, selectStone());
        world.playSound(null, center, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 5.0F, 0.7F);
    }

    private static Item selectStone() {
        int r = RANDOM.nextInt(100);
        if (r < 6) return CosmicStonesMod.POWER_STONE;
        if (r < 16) return CosmicStonesMod.SOUL_STONE;
        if (r < 34) return CosmicStonesMod.TIME_STONE;
        if (r < 54) return CosmicStonesMod.SPACE_STONE;
        if (r < 76) return CosmicStonesMod.REALITY_STONE;
        return CosmicStonesMod.MIND_STONE;
    }

    private static void broadcast(ServerWorld world, String msg, Formatting color) {
        for (ServerPlayerEntity p : world.getPlayers()) p.sendMessage(Text.literal(msg).formatted(color, Formatting.BOLD), false);
    }
}
