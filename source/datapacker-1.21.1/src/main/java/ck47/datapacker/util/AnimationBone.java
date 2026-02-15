package ck47.datapacker.util;

import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class AnimationBone {
    private final Map<Integer, Vector3f> keyframeMap;

    public AnimationBone(Map<Integer, Vector3f> keyframeMap) {
        this.keyframeMap = keyframeMap;
    }

    public Map<Integer, Vector3f> keyframeMap() {
        return keyframeMap;
    }

    public int closestPastKey(double num) {
        for (int i : keyframeMap.keySet().stream().sorted(Comparator.reverseOrder()).toList()) {
            if (num >= i) return i;
        }
        return -1;
    }

    public int closestFutureKey(double num) {
        for (int i : keyframeMap.keySet().stream().sorted().toList()) {
            if (num <= i) return i;
        }
        return -1;
    }
}
