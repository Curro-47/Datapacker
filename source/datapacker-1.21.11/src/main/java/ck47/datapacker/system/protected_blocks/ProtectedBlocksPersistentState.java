package ck47.datapacker.system.protected_blocks;

import ck47.datapacker.util.BlockSelector;
import com.mojang.serialization.Codec;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Uuids;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

import java.util.*;

public class ProtectedBlocksPersistentState extends PersistentState {
    private final Map<UUID, Set<BlockSelector>> playerProtectedBlocksMap;

    public Map<UUID, Set<BlockSelector>> getKeybindMap() {
        return playerProtectedBlocksMap;
    }

    public ProtectedBlocksPersistentState(Map<UUID, Set<BlockSelector>> playerProtectedBlocksMap) {this.playerProtectedBlocksMap = new HashMap<>(playerProtectedBlocksMap);}
    public ProtectedBlocksPersistentState() {this.playerProtectedBlocksMap = new HashMap<>();}

    /* ---------------------------------------------- ACCESS ----------------------------------------*/

    public static ProtectedBlocksPersistentState get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE);
    }

    public static final Codec<ProtectedBlocksPersistentState> CODEC =
        Codec.unboundedMap(Uuids.CODEC, Codec.list(BlockSelector.CODEC).xmap(Set::copyOf, List::copyOf))
        .fieldOf("players")
        .xmap(ProtectedBlocksPersistentState::new, kbs -> kbs.playerProtectedBlocksMap)
        .codec();

    /* ---------------------------------------------- TYPE ----------------------------------------*/

    public static final PersistentStateType<ProtectedBlocksPersistentState> TYPE = new PersistentStateType<>(
            "datapacker_protectedblock",
            ProtectedBlocksPersistentState::new,
            CODEC,
            null
    );
}
