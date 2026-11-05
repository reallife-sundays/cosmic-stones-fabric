package com.cosmicstones;

import com.cosmicstones.block.MeteorCoreBlock;
import com.cosmicstones.event.MeteorSpawnHandler;
import com.cosmicstones.event.PassiveHandler;
import com.cosmicstones.item.CosmicSiphonItem;
import com.cosmicstones.item.RawStoneItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public class CosmicStonesMod implements ModInitializer {
    public static final String MOD_ID = "cosmicstones";

    public static final Block METEOR_CORE = new MeteorCoreBlock(FabricBlockSettings.copyOf(Blocks.CRYING_OBSIDIAN).strength(20.0F, 1200.0F).luminance(14).requiresTool());
    public static final Item METEOR_CORE_ITEM = new BlockItem(METEOR_CORE, new Item.Settings().fireproof());

    public static final Item POWER_STONE = new RawStoneItem("power");
    public static final Item SPACE_STONE = new RawStoneItem("space");
    public static final Item TIME_STONE = new RawStoneItem("time");
    public static final Item REALITY_STONE = new RawStoneItem("reality");
    public static final Item SOUL_STONE = new RawStoneItem("soul");
    public static final Item MIND_STONE = new RawStoneItem("mind");

    public static final Item COSMIC_SIPHON = new CosmicSiphonItem(new Item.Settings().maxCount(1).fireproof().rarity(Rarity.RARE));
    public static final RegistryKey<ItemGroup> ITEM_GROUP = RegistryKey.of(RegistryKeys.ITEM_GROUP, new Identifier(MOD_ID, "general"));

    @Override
    public void onInitialize() {
        Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "meteor_core"), METEOR_CORE);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "meteor_core"), METEOR_CORE_ITEM);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "power_stone"), POWER_STONE);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "space_stone"), SPACE_STONE);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "time_stone"), TIME_STONE);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "reality_stone"), REALITY_STONE);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "soul_stone"), SOUL_STONE);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "mind_stone"), MIND_STONE);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "cosmic_siphon"), COSMIC_SIPHON);

        Registry.register(Registries.ITEM_GROUP, ITEM_GROUP, FabricItemGroup.builder()
                .icon(() -> new ItemStack(COSMIC_SIPHON))
                .displayName(Text.translatable("itemGroup.cosmicstones"))
                .entries((ctx, entries) -> {
                    entries.add(METEOR_CORE_ITEM);
                    entries.add(COSMIC_SIPHON);
                    entries.add(POWER_STONE);
                    entries.add(SPACE_STONE);
                    entries.add(TIME_STONE);
                    entries.add(REALITY_STONE);
                    entries.add(SOUL_STONE);
                    entries.add(MIND_STONE);
                }).build());

        PassiveHandler.init();
        MeteorSpawnHandler.init();
    }
}
