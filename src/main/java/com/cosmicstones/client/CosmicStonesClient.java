package com.cosmicstones.client;

import com.cosmicstones.CosmicStonesMod;
import com.cosmicstones.item.CosmicSiphonItem;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.util.Identifier;

public class CosmicStonesClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModelPredicateProviderRegistry.register(
                CosmicStonesMod.COSMIC_SIPHON,
                new Identifier(CosmicStonesMod.MOD_ID, "socketed_stone"),
                (stack, world, entity, seed) -> switch (CosmicSiphonItem.getSocketedStone(stack)) {
                    case "power" -> 0.1F;
                    case "space" -> 0.2F;
                    case "time" -> 0.3F;
                    case "reality" -> 0.4F;
                    case "soul" -> 0.5F;
                    case "mind" -> 0.6F;
                    default -> 0.0F;
                }
        );
    }
}
