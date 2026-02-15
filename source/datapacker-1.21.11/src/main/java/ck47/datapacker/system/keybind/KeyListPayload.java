package ck47.datapacker.system.keybind;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.Set;

public record KeyListPayload(Set<String> keySet) implements CustomPayload {
    private static final Identifier id = Identifier.of("datapacker", "key_list_trigger");
    public static final Id<KeyListPayload> ID = new Id<>(id);
    public static final PacketCodec<RegistryByteBuf, KeyListPayload> CODEC = PacketCodec.tuple(PacketCodecs.collection(HashSet::new, PacketCodecs.STRING), KeyListPayload::keySet, KeyListPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
