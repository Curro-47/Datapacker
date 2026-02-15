package ck47.datapacker.keybind;

import ck47.datapacker.system.keybind.KeyListPayload;
import ck47.datapacker.system.keybind.KeybindPayload;
import ck47.datapacker.system.keybind.KeybindSyncPayload;
import ck47.datapacker.util.KeybindData;
import ck47.datapacker.util.KeybindType;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public class KeybindManagerClient {
    public static  Map<Identifier, KeybindData> targetKeybindSet = new HashMap<>();
    private static final Set<Identifier> lastTickKeybindSet = new HashSet<>();

    public static void Register() {
        // Register keybind event
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world == null) return;
            long handle = client.getWindow().getHandle();

            Set<Identifier> lastTickKeybindSetClone = new HashSet<>(lastTickKeybindSet);
            lastTickKeybindSet.clear();

            // Loop through all keybinds
            for (Map.Entry<Identifier, KeybindData> kb : targetKeybindSet.entrySet()) {
                ProcessKeybind(kb, handle, lastTickKeybindSetClone);
            }
        });

        // Register targetKeybind recieving packet
        ClientPlayNetworking.registerGlobalReceiver(KeybindSyncPayload.ID, (payload, context) -> {
            context.client().execute(() ->
                editTargetKeybindSet(payload)
            );
        });

        ClientPlayConnectionEvents.JOIN.register(((handler, sender, client) -> sendKeySetToServer()));
    }

    private static void ProcessKeybind(Map.Entry<Identifier, KeybindData> kb, long handle, Set<Identifier> lastTickKeybindSetClone) {
        // Get data
        InputUtil.Key key = InputUtil.fromTranslationKey(kb.getValue().key);
        KeybindType type = kb.getValue().type;
        boolean pressedLastTick = lastTickKeybindSetClone.contains(kb.getKey());

        // Check if the input has been pressed
        boolean pressed = false;
        if (key.getCategory() == InputUtil.Type.KEYSYM) // Detect from keyboard
            pressed = GLFW.glfwGetKey(handle, key.getCode()) == GLFW.GLFW_PRESS;
        if (key.getCategory() == InputUtil.Type.MOUSE) // Detect from mouse
            pressed = GLFW.glfwGetMouseButton(handle, key.getCode()) == GLFW.GLFW_PRESS;

        // Check if the type matches the pressed states
        boolean success = false;
        if (type == KeybindType.ON_TRUE && pressed && !pressedLastTick)  success = true;
        if (type == KeybindType.WHILE_TRUE && pressed)                   success = true;
        if (type == KeybindType.ON_FALSE && !pressed && pressedLastTick) success = true;
        if (type == KeybindType.WHILE_FALSE && !pressed)         success = true;

        // Execute command
        if (success) System.out.println(ClientPlayNetworking.canSend(KeybindPayload.ID));
        if (success && ClientPlayNetworking.canSend(KeybindPayload.ID)) ClientPlayNetworking.send(new KeybindPayload(kb.getKey()));

        if (pressed) lastTickKeybindSet.add(kb.getKey());
    }

    private static void editTargetKeybindSet(KeybindSyncPayload payload) {
        System.out.println(payload.keybindMap().keySet());
        targetKeybindSet.clear();
        targetKeybindSet.putAll(payload.keybindMap());
    }

    private static void sendKeySetToServer() {
        Set<String> keySet = new HashSet<>();

        // Mouse codes
        for (int i = 0; i <= 7; i++) {
            keySet.add(InputUtil.Type.MOUSE.createFromCode(i).getTranslationKey());
        }

        // Keyboard codes
        for (int i = 32; i <= 348; i++) {
            keySet.add(InputUtil.Type.KEYSYM.createFromCode(i).getTranslationKey());
        }

        if (ClientPlayNetworking.canSend(KeyListPayload.ID)) ClientPlayNetworking.send(new KeyListPayload(keySet));
    }
}
