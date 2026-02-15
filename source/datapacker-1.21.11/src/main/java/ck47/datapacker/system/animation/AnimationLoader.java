package ck47.datapacker.system.animation;

import ck47.datapacker.util.AnimationData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class AnimationLoader extends JsonDataLoader<JsonElement> {

    public static final Map<Identifier, AnimationData> animations = new HashMap<>();

    public AnimationLoader() {
        super(Codecs.JSON_ELEMENT, ResourceFinder.json("animation"));
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
        System.out.println("TEST RELOAD");
        animations.clear();

        for (Map.Entry<Identifier, JsonElement> entry : prepared.entrySet()) {
            animations.put(
                    entry.getKey(),
                    AnimationData.fromJson(entry.getKey(), entry.getValue().getAsJsonObject())
            );
        }

        System.out.println("Successfully loaded " + animations.size() + " Animations");
    }

    public static @NotNull Identifier getFabricId() {
        return Identifier.of("datapacker", "animation");
    }

    public static AnimationData get(Identifier animId) {
        return animations.get(animId);
    }
}
