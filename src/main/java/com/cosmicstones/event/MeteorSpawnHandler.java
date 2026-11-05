package com.cosmicstones.event;

import com.cosmicstones.CosmicStonesMod;
import com.cosmicstones.block.MeteorCoreBlock;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.Random;

public class MeteorSpawnHandler {
    private static final Random RANDOM = new Random();
    private static BlockPos pendingPos = null;
    private static int countdownTicks = 0;
    private static boolean autoEnabled = false;

    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(Commands.literal("cosmicmeteor")
                    .requires(s -> s.hasPermission(2))
                    .then(Commands.literal("auto")
                            .then(Commands.literal("start").executes(ctx -> {
                                autoEnabled = true;
                                ctx.getSource().sendSuccess(() -> Component.literal("✔ Meteor auto-spawn ENABLED.").withStyle(ChatFormatting.GREEN), true);
                                return 1;
                            }))
                            .then(Commands.literal("stop").executes(ctx -> {
                                autoEnabled = false;
                                ctx.getSource().sendSuccess(() -> Component.literal("✖ Meteor auto-spawn DISABLED.").withStyle(ChatFormatting.RED), true);
                                return 1;
                            }))
                    )
            );
        });

        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (world.dimension() != Level.OVERWORLD) return;
            if (pendingPos == null && autoEnabled && world.isNight() && world.getDayTime() % 24000 == 14000) {
                if (!world.players().isEmpty() && RANDOM.nextFloat() < 0.20F) {
                    ServerPlayer anchor = world.players().get(RANDOM.nextInt(world.players().size()));
                    int tx = anchor.getBlockX() + (RANDOM.nextBoolean() ? 1 : -1) * (50 + RANDOM.nextInt(60));
                    int tz = anchor.getBlockZ() + (RANDOM.nextBoolean() ? 1 : -1) * (50 + RANDOM.nextInt(60));
                    int ty = world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, tx, tz);

                    pendingPos = new BlockPos(tx, ty, tz);
                    countdownTicks = 9000;
                    broadcast(world, "☄ Celestial anomaly breached orbit. Impact at [" + tx + ", " + ty + ", " + tz + "] in 7.5 minutes.", ChatFormatting.LIGHT_PURPLE);
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

    private static void spawnImpact(ServerLevel world, BlockPos center) {
        world.explode(null, center.getX(), center.getY(), center.getZ(), 6.0F, Level.ExplosionInteraction.BLOCK);
        for (int x = -3; x <= 3; x++) {
            for (int y = -2; y <= 1; y++) {
                for (int z = -3; z <= 3; z++) {
                    if (x * x + y * y + z * z <= 6.5D) {
                        world.setBlockAndUpdate(center.offset(x, y, z), RANDOM.nextBoolean() ? Blocks.OBSIDIAN.defaultBlockState() : Blocks.CRYING_OBSIDIAN.defaultBlockState());
                    }
                }
            }
        }
        world.setBlockAndUpdate(center, CosmicStonesMod.METEOR_CORE.defaultBlockState());
        MeteorCoreBlock.setStoneForLocation(center, selectStone());
        world.playSound(null, center, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 5.0F, 0.7F);
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

    private static void broadcast(ServerLevel world, String msg, ChatFormatting color) {
        Component text = Component.literal(msg).withStyle(color, ChatFormatting.BOLD);
        for (ServerPlayer p : world.players()) p.displayClientMessage(text, false);
    }
}
