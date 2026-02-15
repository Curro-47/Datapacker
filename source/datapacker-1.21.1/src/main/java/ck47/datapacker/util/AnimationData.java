package ck47.datapacker.util;

import ck47.datapacker.Datapacker;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.util.Identifier;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public record AnimationData(
        Identifier animPath,
        int duration,
        boolean loop,
        int loop_start,
        Map<String, AnimationBone> bones) {

    public static AnimationData fromJson(Identifier animId, JsonObject json) {
        Logger logger = LoggerFactory.getLogger(Datapacker.MOD_ID);

        if (!json.has("duration")) {
            logger.error("animation_duration : not found");
            return null;
        }
        int animationDuration = json.get("duration").getAsInt();
        boolean animationLoop = json.get("loop").getAsBoolean();
        int animationLoopStart = json.get("loop_start").getAsInt();

        if (!json.has("bones")) {
            logger.error("bones : not found");
            return null;
        }
        JsonObject bonesJson = json.getAsJsonObject("bones");

        Map<String, AnimationBone> bones = new HashMap<>();
        for (var bone : bonesJson.entrySet()) {
            String name = bone.getKey();
            JsonObject keyframesJson = bone.getValue().getAsJsonObject();

            Map<Integer, Vector3f> keyframes = new HashMap<>();
            for (var keyframeJson : keyframesJson.entrySet()) {
                int frameTick = Integer.parseInt(keyframeJson.getKey());
                JsonArray framePos = keyframeJson.getValue().getAsJsonArray();
                float x = framePos.get(0).getAsFloat();
                float y = framePos.get(1).getAsFloat();
                float z = framePos.get(2).getAsFloat();

                keyframes.put(frameTick, new Vector3f(x, y, z));
            }

            bones.put(name, new AnimationBone(keyframes));
        }

        return new AnimationData(animId, animationDuration, animationLoop, animationLoopStart, bones);
    }
}
