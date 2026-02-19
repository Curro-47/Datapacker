package ck47.datapacker;

import ck47.datapacker.command.*;
import ck47.datapacker.system.animation.AnimationLoader;
import ck47.datapacker.system.animation.AnimationManager;
import ck47.datapacker.system.keybind.KeyListPayload;
import ck47.datapacker.system.keybind.KeybindManager;
import ck47.datapacker.system.keybind.KeybindPayload;
import ck47.datapacker.system.keybind.KeybindSyncPayload;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ck47.datapacker.system.protected_blocks.ProtectedBlocks;

public class Datapacker implements ModInitializer {
	public static final String MOD_ID = "datapacker";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution

        PayloadTypeRegistry.playS2C().register(KeybindSyncPayload.ID, KeybindSyncPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(KeyListPayload.ID, KeyListPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(KeybindPayload.ID, KeybindPayload.CODEC);

        new RaycastCommand().register();
        new ProtectBlockCommand().register();
        new KeybindCommand().register();
        new AnimationCommand().register();
        new VelocityCommand().register();
        KeybindManager.Register();

        ProtectedBlocks.Register();

        ServerTickEvents.END_SERVER_TICK.register((server) -> {
            if (server.getOverworld() == null) return;

            AnimationManager.tick();
        });

        ResourceLoader.get(ResourceType.SERVER_DATA).registerReloader(AnimationLoader.getFabricId(), new AnimationLoader());

		LOGGER.info("Hello Fabric world!");
	}
}