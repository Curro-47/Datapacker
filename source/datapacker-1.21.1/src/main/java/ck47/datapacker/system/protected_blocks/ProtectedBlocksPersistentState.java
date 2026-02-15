package ck47.datapacker.system.protected_blocks;

import ck47.datapacker.util.BlockSelector;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;

import java.util.*;

public class ProtectedBlocksPersistentState extends PersistentState {
    private final Map<UUID, Set<BlockSelector>> playerProtectedBlocksMap = new HashMap<>();

    public Map<UUID, Set<BlockSelector>> getKeybindMap() {
        return playerProtectedBlocksMap;
    }

    /* ---------------------------------------------- ACCESS ----------------------------------------*/

    public static ProtectedBlocksPersistentState get(ServerWorld world) {
        ProtectedBlocksPersistentState protectedBlocksState = world.getPersistentStateManager().getOrCreate(TYPE, "datapacker_protected_blocks");

        return protectedBlocksState;
    }

    /* ---------------------------------------------- TYPE ----------------------------------------*/

    public static final PersistentState.Type<ProtectedBlocksPersistentState> TYPE = new Type<>(
            ProtectedBlocksPersistentState::new,
            ProtectedBlocksPersistentState::fromNbt,
            null
    );

    /* ----------------------------------------------- LOADING ------------------------------------- */

    public static ProtectedBlocksPersistentState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup wrapper) {
        ProtectedBlocksPersistentState state = new ProtectedBlocksPersistentState();

        NbtCompound pbTag = nbt.getCompound("protected_blocks");
        for (String uuid : pbTag.getKeys()) {
            NbtList pbPlayerList = pbTag.getList(uuid, NbtElement.STRING_TYPE);
            Set<BlockSelector> blockSelectorSet = new HashSet<>();
            for (int i = 0; i < pbPlayerList.size(); i++) {
                String blockSelectorString = pbPlayerList.get(i).asString();
                blockSelectorSet.add(BlockSelector.fromString(blockSelectorString));
            }

            state.playerProtectedBlocksMap.put(UUID.fromString(uuid), blockSelectorSet);
        }

        System.out.println(pbTag);

        return state;
    }

    /* ----------------------------------------------- SAVING ------------------------------------- */

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound pbTag = new NbtCompound();

        for(Map.Entry<UUID, Set<BlockSelector>> pbEntry : playerProtectedBlocksMap.entrySet()) {
            Set<BlockSelector> blockSelectorSet = pbEntry.getValue();
            NbtList nbtList = new NbtList();

            for (BlockSelector bs : blockSelectorSet) {
                nbtList.add(bs.toNbt());
            }

            pbTag.put(pbEntry.getKey().toString(), nbtList);
        }
        nbt.put("protected_blocks", pbTag);

        System.out.println(nbt);

        return nbt;
    }
}
