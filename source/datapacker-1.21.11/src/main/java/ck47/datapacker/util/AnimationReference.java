package ck47.datapacker.util;

import net.minecraft.util.Identifier;

public class AnimationReference {
    public final Identifier id;
    public final float startRotation;
    public int tick;

    public AnimationReference(Identifier id, float startRotation, int tick) {
        this.id = id;
        this.startRotation = startRotation;
        this.tick = tick;
    }

    public void addTick() {
        tick ++;
    }
}
