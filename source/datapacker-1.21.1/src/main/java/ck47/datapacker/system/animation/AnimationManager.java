package ck47.datapacker.system.animation;

import ck47.datapacker.util.AnimationBone;
import ck47.datapacker.util.AnimationData;
import ck47.datapacker.util.AnimationReference;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.EulerAngle;
import org.joml.Vector3f;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AnimationManager {
    public static final Map<ArmorStandEntity, AnimationReference> animationDataMap = new HashMap<>();

    public static void play(Collection<ArmorStandEntity> entityCollection, Identifier animPath) throws CommandSyntaxException {
        if (AnimationLoader.get(animPath) == null) throw new CommandSyntaxException(
                new SimpleCommandExceptionType(Text.literal("Animation " + animPath.toString() + " was not found")),
                Text.literal("Animation " + animPath.toString() + " was not found")
        );

        for (ArmorStandEntity entity : entityCollection) {
            animationDataMap.put(entity, new AnimationReference(animPath, 0));
        }
    }

    public static void tick() {
        for (Map.Entry<ArmorStandEntity, AnimationReference> entry : animationDataMap.entrySet()) {
            ArmorStandEntity entity = entry.getKey();
            AnimationReference reference = entry.getValue();
            AnimationData animation = AnimationLoader.get(reference.id);

            moveBone("head", animation, reference, entity);
            moveBone("body", animation, reference, entity);
            moveBone("right_arm", animation, reference, entity);
            moveBone("left_arm", animation, reference, entity);
            moveBone("right_leg", animation, reference, entity);
            moveBone("left_leg", animation, reference, entity);

            reference.tick += 1;
            if (reference.tick > animation.duration() && !animation.loop()) animationDataMap.remove(entity);
            else if (reference.tick > animation.duration()) reference.tick = animation.loop_start();
        }
    }

    private static void moveBone(String boneName, AnimationData animation, AnimationReference reference, ArmorStandEntity entity) {
        AnimationBone bone = animation.bones().get(boneName);
        if (bone == null) return;

        int lastFrame = bone.closestPastKey(reference.tick);
        int nextFrame = bone.closestFutureKey(reference.tick);
        if (lastFrame < 0 || nextFrame < 0) return;

        float lerpIndex = (float)(reference.tick - lastFrame) / (float)(nextFrame - lastFrame); // 0..1 on where the current tick is
        if (lastFrame == nextFrame) lerpIndex = 0;

        Vector3f lastPos = bone.keyframeMap().get(lastFrame);
        Vector3f nextPos = bone.keyframeMap().get(nextFrame);

        Vector3f currentPos = new Vector3f(
                lastPos.x + lerpIndex*(nextPos.x - lastPos.x),
                lastPos.y + lerpIndex*(nextPos.y - lastPos.y),
                lastPos.z + lerpIndex*(nextPos.z - lastPos.z)
        );

        if (boneName.equals("head")) entity.setHeadRotation(new EulerAngle(currentPos.x, currentPos.y, currentPos.z));
        if (boneName.equals("body")) entity.setBodyRotation(new EulerAngle(currentPos.x, currentPos.y, currentPos.z));
        if (boneName.equals("right_arm")) entity.setRightArmRotation(new EulerAngle(currentPos.x, currentPos.y, currentPos.z));
        if (boneName.equals("left_arm")) entity.setLeftArmRotation(new EulerAngle(currentPos.x, currentPos.y, currentPos.z));
        if (boneName.equals("right_leg")) entity.setRightLegRotation(new EulerAngle(currentPos.x, currentPos.y, currentPos.z));
        if (boneName.equals("left_leg")) entity.setLeftLegRotation(new EulerAngle(currentPos.x, currentPos.y, currentPos.z));

    }
}
