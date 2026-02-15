package ck47.datapacker.system.keybind;

import ck47.datapacker.util.KeybindData;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public record KeybindSyncPayload(Map<Identifier, KeybindData> keybindMap) implements CustomPayload {
    private static final Identifier id = Identifier.of("datapacker", "keybind_sync_trigger");
    public static final CustomPayload.Id<KeybindSyncPayload> ID = new CustomPayload.Id<>(id);
    public static final PacketCodec<RegistryByteBuf, KeybindSyncPayload> CODEC = PacketCodec.tuple(PacketCodecs.map(HashMap::new, Identifier.PACKET_CODEC, KeybindData.PACKET_CODEC), KeybindSyncPayload::keybindMap, KeybindSyncPayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
