package ck47.datapacker.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.joml.Vector3f;

public final class Vector3fCodec {

    public static final Codec<Vector3f> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.FLOAT.fieldOf("x").forGetter(Vector3f::x),
                    Codec.FLOAT.fieldOf("y").forGetter(Vector3f::y),
                    Codec.FLOAT.fieldOf("z").forGetter(Vector3f::z)
            ).apply(instance, Vector3f::new));

    private Vector3fCodec() {}
}