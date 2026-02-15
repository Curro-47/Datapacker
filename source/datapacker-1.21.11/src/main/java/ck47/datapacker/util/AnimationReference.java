package ck47.datapacker.util;

import net.minecraft.util.Identifier;

public class AnimationReference {
    public final Identifier id;
    public int tick;

    public AnimationReference(Identifier id, int tick) {
        this.id = id;
        this.tick = tick;
    }

    public void addTick() {
        tick ++;
    }
}
