package ck47.datapacker.util;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public enum KeybindType {
    ON_TRUE,
    WHILE_TRUE,
    ON_FALSE,
    WHILE_FALSE;

    public static KeybindType getFromString(String str) {
        switch (str) {
            case "ON_TRUE" -> {
                return KeybindType.ON_TRUE;
            }
            case "WHILE_TRUE" -> {
                return KeybindType.WHILE_TRUE;
            }
            case "ON_FALSE" -> {
                return KeybindType.ON_FALSE;
            }
            case "WHILE_FALSE" -> {
                return KeybindType.WHILE_FALSE;
            }
            default -> {
                return null;
            }
        }
    }

    public static final PacketCodec<ByteBuf, KeybindType> PACKET_CODEC =
            PacketCodecs.STRING.xmap(
                KeybindType::valueOf,
                KeybindType::name
            );
}
