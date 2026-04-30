package com.infinitylights;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = InfinityLightsMod.MOD_ID, value = Dist.CLIENT)
public class ClientSetup {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(InfinityLightsMod.GLOWSTONE_TORCH.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(InfinityLightsMod.GLOWSTONE_WALL_TORCH.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(InfinityLightsMod.GLOWSTONE_LANTERN.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(InfinityLightsMod.DEPLETED_GLOWSTONE_TORCH.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(InfinityLightsMod.DEPLETED_GLOWSTONE_WALL_TORCH.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(InfinityLightsMod.BURNING_TORCH.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(InfinityLightsMod.BURNING_WALL_TORCH.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(InfinityLightsMod.BURNING_LANTERN.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(InfinityLightsMod.DEPLETED_TORCH.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(InfinityLightsMod.DEPLETED_WALL_TORCH.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(InfinityLightsMod.DEPLETED_LANTERN.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(InfinityLightsMod.BURNING_CAMPFIRE.get(), RenderType.cutout());
            InfinityLightsMod.BURNING_CANDLES.values().forEach(block -> ItemBlockRenderTypes.setRenderLayer(block.get(), RenderType.cutout()));
        });
    }
}
