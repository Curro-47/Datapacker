package ck47.datapacker.system.keybind;

import ck47.datapacker.util.KeybindData;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

import java.util.HashMap;
import java.util.Map;

public class KeybindPersistentState extends PersistentState {
    private final Map<Identifier, KeybindData> keybindMap;

    public KeybindPersistentState(Map<Identifier, KeybindData> keybindMap) {this.keybindMap = new HashMap<>(keybindMap);}
    public KeybindPersistentState() {this.keybindMap = new HashMap<>();}

    public Map<Identifier, KeybindData> getKeybindMap() {
        return keybindMap;
    }

    public void add(Identifier name, KeybindData data) {
        markDirty();
        keybindMap.put(name, data);
    }

    public void remove(Identifier name) {
        markDirty();
        keybindMap.remove(name);
    }

    /* ---------------------------------------------- ACCESS ----------------------------------------*/

    public static KeybindPersistentState get(MinecraftServer server) {
        return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE);
    }

    public static final Codec<KeybindPersistentState> CODEC =
        Codec.unboundedMap(Identifier.CODEC, KeybindData.CODEC)
        .fieldOf("keybinds")
        .xmap(KeybindPersistentState::new, kbs -> kbs.keybindMap)
        .codec();

    /* ---------------------------------------------- TYPE ----------------------------------------*/

    public static final PersistentStateType<KeybindPersistentState> TYPE = new PersistentStateType<>(
        "datapacker_keybind",
        KeybindPersistentState::new,
        CODEC,
        null
    );
}
