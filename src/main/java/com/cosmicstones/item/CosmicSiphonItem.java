package com.cosmicstones.item;

import com.cosmicstones.CosmicStonesMod;
import net.minecraft.block.AnvilBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public class CosmicSiphonItem extends Item {
    public CosmicSiphonItem(Settings settings) { super(settings); }

    public static String getSocketedStone(ItemStack stack) {
        if (!stack.hasNbt()) return "";
        NbtCompound tag = stack.getNbt();
        return tag != null ? tag.getString("SocketedStone") : "";
    }

    public static boolean isPassiveEnabled(ItemStack stack) {
        if (!stack.hasNbt()) return true;
        NbtCompound tag = stack.getNbt();
        return tag == null || !tag.contains("PassiveEnabled") || tag.getBoolean("PassiveEnabled");
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext ctx) {
        World world = ctx.getWorld();
        BlockPos pos = ctx.getBlockPos();
        PlayerEntity player = ctx.getPlayer();
        ItemStack stack = ctx.getStack();

        if (player != null && player.isSneaking() && world.getBlockState(pos).getBlock() instanceof AnvilBlock) {
            String stone = getSocketedStone(stack);
            if (!stone.isEmpty() && !world.isClient) {
                Item item = switch (stone) {
                    case "power" -> CosmicStonesMod.POWER_STONE;
                    case "space" -> CosmicStonesMod.SPACE_STONE;
                    case "time" -> CosmicStonesMod.TIME_STONE;
                    case "reality" -> CosmicStonesMod.REALITY_STONE;
                    case "soul" -> CosmicStonesMod.SOUL_STONE;
                    default -> CosmicStonesMod.MIND_STONE;
                };
                stack.getOrCreateNbt().remove("SocketedStone");
                player.getInventory().offerOrDrop(new ItemStack(item));
                world.playSound(null, pos, SoundEvents.BLOCK_ANVIL_USE, SoundCategory.PLAYERS, 1.0F, 1.0F);
                player.sendMessage(Text.literal("Extracted " + stone.toUpperCase() + " Stone.").formatted(Formatting.YELLOW), true);
                return ActionResult.SUCCESS;
            }
        }
        return super.useOnBlock(ctx);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (world.isClient) return TypedActionResult.success(stack);

        NbtCompound tag = stack.getOrCreateNbt();
        String stone = tag.getString("SocketedStone");

        ItemStack offhand = player.getOffHandStack();
        if (player.isSneaking() && offhand.getItem() instanceof RawStoneItem raw) {
            if (!stone.isEmpty()) {
                player.sendMessage(Text.literal("Siphon occupied! Use an Anvil while sneaking to extract.").formatted(Formatting.RED), true);
                return TypedActionResult.fail(stack);
            }
            tag.putString("SocketedStone", raw.getStoneType());
            tag.putBoolean("PassiveEnabled", true);
            offhand.decrement(1);
            world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 1.0F, 1.2F);
            return TypedActionResult.success(stack);
        }

        if (stone.isEmpty()) return TypedActionResult.pass(stack);

        int cd = executeMinor((ServerWorld) world, player, stone);
        if (cd > 0) player.getItemCooldownManager().set(this, cd);
        return TypedActionResult.success(stack);
    }

    private int executeMinor(ServerWorld world, PlayerEntity player, String stone) {
        switch (stone) {
            case "power" -> {
                int stacks = player.getScoreboardTags().stream().filter(t -> t.startsWith("overcharge_")).mapToInt(t -> Integer.parseInt(t.replace("overcharge_", ""))).findFirst().orElse(0);
                player.getScoreboardTags().removeIf(t -> t.startsWith("overcharge_"));
                float radius = stacks >= 8 ? 10.0F : (stacks >= 4 ? 7.0F : 4.0F);
                float damage = stacks >= 8 ? 16.0F : (stacks >= 4 ? 10.0F : 6.0F);

                for (LivingEntity target : world.getEntitiesByClass(LivingEntity.class, player.getBoundingBox().expand(radius), e -> e != player)) {
                    target.damage(world.getDamageSources().playerAttack(player), damage);
                    target.addVelocity(0, 0.55D + (stacks * 0.05D), 0);
                    target.velocityModified = true;
                }
                world.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, player.getX(), player.getY(), player.getZ(), 1, 0, 0, 0, 0);
                world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 1.5F, 0.7F);
                return 900;
            }
            case "space" -> {
                Vec3d start = player.getEyePos();
                Vec3d look = player.getRotationVector().normalize();
                Vec3d end = start.add(look.multiply(14.0D));
                BlockHitResult hit = world.raycast(new RaycastContext(start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player));
                BlockPos targetPos = hit.getBlockPos().offset(hit.getSide());
                player.teleport(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5);
                player.onLanding();
                world.playSound(null, targetPos, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
                return 800;
            }
            case "mind" -> {
                for (MobEntity mob : world.getEntitiesByClass(MobEntity.class, player.getBoundingBox().expand(16.0D), e -> true)) {
                    mob.setTarget(null);
                    if (mob instanceof CreeperEntity c) c.setFuseSpeed(-1);
                }
                world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_BEACON_DEACTIVATE, SoundCategory.PLAYERS, 1.2F, 1.5F);
                return 1200;
            }
        }
        return 900;
    }
}
