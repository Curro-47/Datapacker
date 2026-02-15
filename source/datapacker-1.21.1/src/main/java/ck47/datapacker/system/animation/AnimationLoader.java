package ck47.datapacker.system.animation;

import ck47.datapacker.util.AnimationData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.util.HashMap;
import java.util.Map;

public class AnimationLoader extends JsonDataLoader implements IdentifiableResourceReloadListener {

    private static final Map<Identifier, AnimationData> animations = new HashMap<>();

    public AnimationLoader() {
        super(new GsonBuilder().create(), "animation");
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
        animations.clear();

        for (Map.Entry<Identifier, JsonElement> entry : prepared.entrySet()) {
            animations.put(
                    entry.getKey(),
                    AnimationData.fromJson(entry.getKey(), entry.getValue().getAsJsonObject())
            );
        }

        System.out.println("Successfully loaded " + animations.size() + " Animations");
    }

    @Override
    public Identifier getFabricId() {
        return Identifier.of("datapacker", "animation");
    }

    public static AnimationData get(Identifier animId) {
        return animations.get(animId);
    }
}
