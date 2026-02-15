package ck47.datapacker.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class KeybindData {
    public final String key;
    public final List<Identifier> functionIdList;
    public final KeybindType type;

    public KeybindData(String key, List<Identifier> functionIdList, KeybindType type) {
        this.key = key;
        this.functionIdList = functionIdList;
        this.type = type;
    }

    public String key() {return key;}
    public List<Identifier> functionIdList() {return functionIdList;}
    public KeybindType type() {return type;}

    public static final PacketCodec<ByteBuf, KeybindData> PACKET_CODEC =
        PacketCodec.tuple(
            PacketCodecs.STRING,
            KeybindData::key,

            PacketCodecs.collection(ArrayList::new, Identifier.PACKET_CODEC),
            KeybindData::functionIdList,

            KeybindType.PACKET_CODEC,
            KeybindData::type,

            KeybindData::new
        );

    public static final Codec<KeybindData> CODEC =
        RecordCodecBuilder.create(
            instance -> instance.group(
                Codec.STRING.fieldOf("key").forGetter((KeybindData kbd) -> kbd.key),
                Identifier.CODEC.listOf().fieldOf("functionIdList").forGetter((KeybindData kbd) -> kbd.functionIdList),
                KeybindType.CODEC.fieldOf("type").forGetter((KeybindData kbd) -> kbd.type)
            ).apply(instance, KeybindData::new)
        );
}