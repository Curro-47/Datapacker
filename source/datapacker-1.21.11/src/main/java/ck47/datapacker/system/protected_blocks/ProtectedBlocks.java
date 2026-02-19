package ck47.datapacker.system.protected_blocks;

import ck47.datapacker.util.BlockSelector;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.Block;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.*;

public class ProtectedBlocks {
    public static void Register() {
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (world.isClient()) return true;
            // Only handle server players (ignore fake entities)
            if (!(player instanceof ServerPlayerEntity serverPlayer)) return true;

            UUID uuid = serverPlayer.getUuid();
            Set<BlockSelector> protectedSet = ProtectedBlocksPersistentState.get(world.getServer()).getKeybindMap().get(uuid);

            RegistryEntry<Block> blockRegistry = world.getBlockState(pos).getRegistryEntry();
            return protectedSet == null || protectedSet.stream().noneMatch(pred -> pred.matches(blockRegistry));
        });
    }

    public static void Add(UUID uuid, BlockSelector selector, MinecraftServer server) {
        ProtectedBlocksPersistentState persistentState = ProtectedBlocksPersistentState.get(server);
        persistentState.markDirty();

        persistentState.getKeybindMap().computeIfAbsent(uuid, id -> new HashSet<>()).add(selector);
    }

    public static void Remove(UUID uuid, BlockSelector selector, MinecraftServer server) {
        ProtectedBlocksPersistentState persistentState = ProtectedBlocksPersistentState.get(server);
        persistentState.markDirty();

        Set<BlockSelector> set = persistentState.getKeybindMap().get(uuid);
        if (set != null) set.remove(selector);
    }

    public static void Clear(UUID uuid, MinecraftServer server) {
        ProtectedBlocksPersistentState persistentState = ProtectedBlocksPersistentState.get(server);
        persistentState.markDirty();

        persistentState.getKeybindMap().remove(uuid);
    }

    public static Boolean IsProtected(UUID uuid, BlockSelector selector, MinecraftServer server) {
        Set<BlockSelector> set = ProtectedBlocksPersistentState.get(server).getKeybindMap().get(uuid);
        return set != null && set.contains(selector);
    }

    public static Boolean IsProtected(UUID uuid, RegistryEntry<Block> block, MinecraftServer server) {
        Set<BlockSelector> set = ProtectedBlocksPersistentState.get(server).getKeybindMap().get(uuid);
        return set != null && set.stream().anyMatch(pred -> pred.matches(block));
    }

    public static Set<BlockSelector> GetProtected(UUID uuid, MinecraftServer server) {
        return ProtectedBlocksPersistentState.get(server).getKeybindMap().getOrDefault(uuid, Collections.emptySet());
    }

    public static Map<UUID, Set<BlockSelector>> GetAllProtected(MinecraftServer server) {
        return ProtectedBlocksPersistentState.get(server).getKeybindMap();
    }
}
