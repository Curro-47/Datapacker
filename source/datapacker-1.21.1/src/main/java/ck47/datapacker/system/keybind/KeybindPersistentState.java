package ck47.datapacker.system.keybind;

import ck47.datapacker.util.KeybindData;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;

import java.util.HashMap;
import java.util.Map;

public class KeybindPersistentState extends PersistentState {
    private final Map<Identifier, KeybindData> keybindMap = new HashMap<>();

    public Map<Identifier, KeybindData> getKeybindMap() {
        return keybindMap;
    }

    public void add(Identifier name, KeybindData data) {
        keybindMap.put(name, data);
        markDirty();
    }

    public void remove(Identifier name) {
        keybindMap.remove(name);
        markDirty();
    }

    /* ---------------------------------------------- ACCESS ----------------------------------------*/

    public static KeybindPersistentState get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE, "datapacker_keybinds");
    }

    /* ---------------------------------------------- TYPE ----------------------------------------*/

    public static final PersistentState.Type<KeybindPersistentState> TYPE = new Type<>(
        KeybindPersistentState::new,
        KeybindPersistentState::fromNbt,
        null
    );

    /* ----------------------------------------------- LOADING ------------------------------------- */

    public static KeybindPersistentState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup wrapper) {
        KeybindPersistentState state = new KeybindPersistentState();

        NbtCompound kbTag = nbt.getCompound("keybinds");
        for (String key : kbTag.getKeys()) {
            state.keybindMap.put(Identifier.of(key), KeybindData.fromNbt(kbTag.getCompound(key)));
        }

        return state;
    }

    /* ----------------------------------------------- SAVING ------------------------------------- */

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound kbTag = new NbtCompound();
        for(Map.Entry<Identifier, KeybindData> kbEntry : keybindMap.entrySet()) {
            kbTag.put(kbEntry.getKey().toString(), kbEntry.getValue().toNbt());
        }
        nbt.put("keybinds", kbTag);

        return nbt;
    }
}
