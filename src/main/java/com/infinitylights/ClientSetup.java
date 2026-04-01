package com.infinitylights;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = InfinityLightsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(InfinityLightsMod.STATIC_TORCH.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(InfinityLightsMod.STATIC_WALL_TORCH.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(InfinityLightsMod.DEPLETED_TORCH.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(InfinityLightsMod.DEPLETED_WALL_TORCH.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(InfinityLightsMod.DEPLETED_LANTERN.get(), RenderType.cutout());
        });
    }
}
