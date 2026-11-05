package com.cosmicstones;

import com.cosmicstones.block.MeteorCoreBlock;
import com.cosmicstones.event.MeteorSpawnHandler;
import com.cosmicstones.event.PassiveHandler;
import com.cosmicstones.item.CosmicSiphonItem;
import com.cosmicstones.item.RawStoneItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Function;

public class CosmicStonesMod implements ModInitializer {
    public static final String MOD_ID = "cosmicstones";

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public static final Block METEOR_CORE = registerBlock("meteor_core",
            MeteorCoreBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.CRYING_OBSIDIAN)
                    .strength(20.0F, 1200.0F)
                    .lightLevel(state -> 14)
                    .requiresCorrectToolForDrops());

    public static final Item METEOR_CORE_ITEM = registerItem("meteor_core",
            props -> new BlockItem(METEOR_CORE, props),
            new Item.Properties().useBlockDescriptionPrefix().fireResistant());

    public static final Item POWER_STONE = registerStone("power_stone", "power");
    public static final Item SPACE_STONE = registerStone("space_stone", "space");
    public static final Item TIME_STONE = registerStone("time_stone", "time");
    public static final Item REALITY_STONE = registerStone("reality_stone", "reality");
    public static final Item SOUL_STONE = registerStone("soul_stone", "soul");
    public static final Item MIND_STONE = registerStone("mind_stone", "mind");

    public static final Item COSMIC_SIPHON = registerItem("cosmic_siphon",
            CosmicSiphonItem::new,
            new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.RARE));

    public static final ResourceKey<CreativeModeTab> ITEM_GROUP = ResourceKey.create(Registries.CREATIVE_MODE_TAB, id("general"));

    private static Block registerBlock(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties props) {
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, id(name));
        Block block = factory.apply(props.setId(key));
        return Registry.register(BuiltInRegistries.BLOCK, key, block);
    }

    private static Item registerItem(String name, Function<Item.Properties, Item> factory, Item.Properties props) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id(name));
        Item item = factory.apply(props.setId(key));
        return Registry.register(BuiltInRegistries.ITEM, key, item);
    }

    private static Item registerStone(String name, String type) {
        return registerItem(name,
                props -> new RawStoneItem(type, props),
                new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC));
    }

    @Override
    public void onInitialize() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ITEM_GROUP, FabricItemGroup.builder()
                .icon(() -> new ItemStack(COSMIC_SIPHON))
                .title(Component.translatable("itemGroup.cosmicstones"))
                .displayItems((ctx, entries) -> {
                    entries.accept(METEOR_CORE_ITEM);
                    entries.accept(COSMIC_SIPHON);
                    entries.accept(POWER_STONE);
                    entries.accept(SPACE_STONE);
                    entries.accept(TIME_STONE);
                    entries.accept(REALITY_STONE);
                    entries.accept(SOUL_STONE);
                    entries.accept(MIND_STONE);
                }).build());

        PassiveHandler.init();
        MeteorSpawnHandler.init();
    }
}
