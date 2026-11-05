package com.cosmicstones.item;

import com.cosmicstones.CosmicStonesMod;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class CosmicSiphonItem extends Item {
    public CosmicSiphonItem(Properties properties) {
        super(properties);
    }

    public static String getSocketedStone(ItemStack stack) {
        CustomData comp = stack.get(DataComponents.CUSTOM_DATA);
        if (comp == null) return "";
        return comp.copyTag().getStringOr("SocketedStone", "");
    }

    public static boolean isPassiveEnabled(ItemStack stack) {
        CustomData comp = stack.get(DataComponents.CUSTOM_DATA);
        if (comp == null) return true;
        CompoundTag tag = comp.copyTag();
        return !tag.contains("PassiveEnabled") || tag.getBooleanOr("PassiveEnabled", true);
    }

    private static void syncModelData(ItemStack stack, String stone) {
        float cmd = switch (stone) {
            case "power" -> 1.0F;
            case "space" -> 2.0F;
            case "time" -> 3.0F;
            case "reality" -> 4.0F;
            case "soul" -> 5.0F;
            case "mind" -> 6.0F;
            default -> 0.0F;
        };
        stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of(cmd), List.of(), List.of(), List.of()));
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        Player player = ctx.getPlayer();
        ItemStack stack = ctx.getItemInHand();

        if (player != null && player.isShiftKeyDown() && level.getBlockState(pos).is(BlockTags.ANVIL)) {
            String stone = getSocketedStone(stack);
            if (!stone.isEmpty() && !level.isClientSide()) {
                Item item = switch (stone) {
                    case "power" -> CosmicStonesMod.POWER_STONE;
                    case "space" -> CosmicStonesMod.SPACE_STONE;
                    case "time" -> CosmicStonesMod.TIME_STONE;
                    case "reality" -> CosmicStonesMod.REALITY_STONE;
                    case "soul" -> CosmicStonesMod.SOUL_STONE;
                    default -> CosmicStonesMod.MIND_STONE;
                };

                CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
                tag.remove("SocketedStone");
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                syncModelData(stack, "");

                ItemStack stoneStack = new ItemStack(item);
                if (!player.getInventory().add(stoneStack)) {
                    player.drop(stoneStack, false);
                }
                
                level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
                player.displayClientMessage(Component.literal("Extracted " + stone.toUpperCase() + " Stone.").withStyle(ChatFormatting.YELLOW), true);
                return InteractionResult.SUCCESS;
            }
        }
        return super.useOn(ctx);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        String stone = tag.getStringOr("SocketedStone", "");

        ItemStack offhand = player.getOffhandItem();
        if (player.isShiftKeyDown() && offhand.getItem() instanceof RawStoneItem raw) {
            if (!stone.isEmpty()) {
                player.displayClientMessage(Component.literal("Siphon occupied! Use an Anvil while sneaking to extract.").withStyle(ChatFormatting.RED), true);
                return InteractionResult.FAIL;
            }
            tag.putString("SocketedStone", raw.getStoneType());
            tag.putBoolean("PassiveEnabled", true);
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            syncModelData(stack, raw.getStoneType());

            offhand.shrink(1);
            level.playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.2F);
            return InteractionResult.SUCCESS;
        }

        if (stone.isEmpty()) return InteractionResult.PASS;

        int cd = executeMinor((ServerLevel) level, player, stone);
        if (cd > 0) player.getCooldowns().addCooldown(stack, cd);
        return InteractionResult.SUCCESS;
    }

    private int executeMinor(ServerLevel level, Player player, String stone) {
        switch (stone) {
            case "power" -> {
                int stacks = player.getTags().stream()
                        .filter(t -> t.startsWith("overcharge_"))
                        .mapToInt(t -> Integer.parseInt(t.replace("overcharge_", "")))
                        .findFirst().orElse(0);
                player.getTags().removeIf(t -> t.startsWith("overcharge_"));

                float radius = stacks >= 8 ? 10.0F : (stacks >= 4 ? 7.0F : 4.0F);
                float damage = stacks >= 8 ? 16.0F : (stacks >= 4 ? 10.0F : 6.0F);

                for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(radius), e -> e != player)) {
                    target.hurtServer(level, level.damageSources().playerAttack(player), damage);
                    target.push(0, 0.55D + (stacks * 0.05D), 0);
                    target.markHurt();
                }
                level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, player.getX(), player.getY(), player.getZ(), 1, 0, 0, 0, 0);
                level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.5F, 0.7F);
                return 900;
            }
            case "space" -> {
                Vec3 start = player.getEyePosition();
                Vec3 look = player.getLookAngle().normalize();
                Vec3 end = start.add(look.scale(14.0D));
                BlockHitResult hit = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
                BlockPos targetPos = hit.getBlockPos().relative(hit.getDirection());
                player.teleportTo(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5);
                player.resetFallDistance();
                level.playSound(null, targetPos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
                return 800;
            }
            case "mind" -> {
                for (Mob mob : level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(16.0D), e -> true)) {
                    mob.setTarget(null);
                    if (mob instanceof Creeper c) c.setSwellDir(-1);
                }
                level.playSound(null, player.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.2F, 1.5F);
                return 1200;
            }
        }
        return 900;
    }
}
