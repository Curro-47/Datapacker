package ck47.datapacker.system.protected_blocks;

import ck47.datapacker.util.BlockSelector;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.Block;
import net.minecraft.registry.entry.RegistryEntry;
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
            Set<BlockSelector> protectedSet = ProtectedBlocksPersistentState.get(world.getServer().getOverworld()).getKeybindMap().get(uuid);

            RegistryEntry<Block> blockRegistry = world.getBlockState(pos).getRegistryEntry();
            return protectedSet == null || protectedSet.stream().noneMatch(pred -> pred.matches(blockRegistry));
        });
    }

    public static void Add(UUID uuid, BlockSelector selector, ServerWorld world) {
        ProtectedBlocksPersistentState persistentState = ProtectedBlocksPersistentState.get(world);
        persistentState.getKeybindMap().computeIfAbsent(uuid, id -> new HashSet<>()).add(selector);

        persistentState.markDirty();
    }

    public static void Remove(UUID uuid, BlockSelector selector, ServerWorld world) {
        ProtectedBlocksPersistentState persistentState = ProtectedBlocksPersistentState.get(world);
        Set<BlockSelector> set = persistentState.getKeybindMap().get(uuid);
        if (set != null) set.remove(selector);

        persistentState.markDirty();
    }

    public static void Clear(UUID uuid, ServerWorld world) {
        ProtectedBlocksPersistentState persistentState = ProtectedBlocksPersistentState.get(world);
        persistentState.getKeybindMap().remove(uuid);

        persistentState.markDirty();
    }

    public static Boolean IsProtected(UUID uuid, BlockSelector selector, ServerWorld world) {
        Set<BlockSelector> set = ProtectedBlocksPersistentState.get(world).getKeybindMap().get(uuid);
        return set != null && set.contains(selector);
    }

    public static Boolean IsProtected(UUID uuid, RegistryEntry<Block> block, ServerWorld world) {
        Set<BlockSelector> set = ProtectedBlocksPersistentState.get(world).getKeybindMap().get(uuid);
        return set != null && set.stream().anyMatch(pred -> pred.matches(block));
    }

    public static Set<BlockSelector> GetProtected(UUID uuid, ServerWorld world) {
        return ProtectedBlocksPersistentState.get(world).getKeybindMap().getOrDefault(uuid, Collections.emptySet());
    }

    public static Map<UUID, Set<BlockSelector>> GetAllProtected(ServerWorld world) {
        return ProtectedBlocksPersistentState.get(world).getKeybindMap();
    }
}
