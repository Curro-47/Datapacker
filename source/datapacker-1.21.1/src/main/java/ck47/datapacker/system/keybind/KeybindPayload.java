package ck47.datapacker.system.keybind;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record KeybindPayload(Identifier keybindTranslationKey) implements CustomPayload {
    private static final Identifier id = Identifier.of("datapacker", "keybind_trigger");
    public static final CustomPayload.Id<KeybindPayload> ID = new CustomPayload.Id<>(id);
    public static final PacketCodec<RegistryByteBuf, KeybindPayload> CODEC = PacketCodec.tuple(Identifier.PACKET_CODEC, KeybindPayload::keybindTranslationKey, KeybindPayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
