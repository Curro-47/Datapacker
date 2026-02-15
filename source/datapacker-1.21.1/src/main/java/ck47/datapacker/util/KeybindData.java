package ck47.datapacker.util;

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

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();

        nbt.putString("key", key);
        nbt.putString("type", type.name());

        NbtList functionList = new NbtList();
        for(Identifier funct : functionIdList) {
            functionList.add(NbtString.of(funct.toString()));
        }
        nbt.put("function", functionList);

        return nbt;
    }

    public static KeybindData fromNbt(NbtCompound nbt) {
        String key = nbt.getString("key");
        KeybindType type = KeybindType.valueOf(nbt.getString("type"));

        NbtList nbtList = nbt.getList("function", NbtList.STRING_TYPE);
        List<Identifier> ids = new HashSet<>(nbtList).stream()
                .map(id -> Identifier.of(id.asString()))
                .toList();

        return new KeybindData(key, ids, type);
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
}