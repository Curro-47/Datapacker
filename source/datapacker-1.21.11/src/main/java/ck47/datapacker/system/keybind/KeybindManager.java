package ck47.datapacker.system.keybind;

import ck47.datapacker.util.KeybindData;
import ck47.datapacker.util.KeybindType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.permission.LeveledPermissionPredicate;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.function.CommandFunctionManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.*;

public class KeybindManager {

    public static final Set<String> keySet = new HashSet<>();

    private static final Set<String> lastTickKeybindSet = new HashSet<>();

    public static void Register() {
        // Register keybind trigger recieving
        ServerPlayNetworking.registerGlobalReceiver(KeybindPayload.ID, (payload, context) -> {
            ProcessKeybindTrigger(context.server(), payload.keybindTranslationKey(), context.player());
        });

        // Register refresh key set when player enters
        ServerPlayNetworking.registerGlobalReceiver(KeyListPayload.ID, (payload, context) -> {
            refreshKeySet(payload);
        });

        ServerPlayConnectionEvents.JOIN.register(((handler, sender, server) -> {
            SendClientKeybindData(server, handler.getPlayer());
        }));
    }

    private static void ProcessKeybindTrigger(MinecraftServer server, Identifier kbName, ServerPlayerEntity player) {
        // Get KeybindState and Map
        KeybindPersistentState keybindState = KeybindPersistentState.get(server);
        Map<Identifier, KeybindData> keybindMap = keybindState.getKeybindMap();

        // Get data
        List<Identifier> functionIdList = keybindMap.get(kbName).functionIdList;

        CommandFunctionManager manager = server.getCommandFunctionManager();

        for (Identifier id : functionIdList) {
            manager.getFunction(id).ifPresent(
                funct -> manager.execute(funct,
                        player.getCommandSource()
                                .withSilent()
                                .withPermissions(LeveledPermissionPredicate.GAMEMASTERS))
            );
        }
    }

    public static void Add(MinecraftServer server, Identifier name, String code, List<Identifier> function, KeybindType type) {
        // Keybind creation
        KeybindPersistentState keybindState = KeybindPersistentState.get(server);

        keybindState.add( name, new KeybindData(code, function, type) );

        SendClientKeybindData(server);
    }

    public static void Remove(MinecraftServer server, Identifier name) {
        Identifier kb = GetKeybind(server, name);
        KeybindPersistentState keybindState = KeybindPersistentState.get(server);

        if (kb != null) keybindState.remove(kb);

        SendClientKeybindData(server);
    }

    private static void SendClientKeybindData(MinecraftServer server) {
        KeybindPersistentState keybindState = KeybindPersistentState.get(server);

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (!ServerPlayNetworking.canSend(player, KeybindSyncPayload.ID)) continue;
            ServerPlayNetworking.send(player, new KeybindSyncPayload(keybindState.getKeybindMap()));
        }
    }

    private static void SendClientKeybindData(MinecraftServer server, ServerPlayerEntity unloadedPlayer) {
        KeybindPersistentState keybindState = KeybindPersistentState.get(server);

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (!ServerPlayNetworking.canSend(player, KeybindSyncPayload.ID)) continue;
            ServerPlayNetworking.send(player, new KeybindSyncPayload(keybindState.getKeybindMap()));
        }
        if (!ServerPlayNetworking.canSend(unloadedPlayer, KeybindSyncPayload.ID)) return;
        ServerPlayNetworking.send(unloadedPlayer, new KeybindSyncPayload(keybindState.getKeybindMap()));
    }

    public static Identifier GetKeybind(MinecraftServer server, Identifier name) {
        for (Identifier kb : KeybindPersistentState.get(server).getKeybindMap().keySet()) {
            if (kb.equals(name)) return kb;
        }
        return null;
    }

    public static Map<Identifier, KeybindData> GetKeybindMap(MinecraftServer server) {
        return KeybindPersistentState.get(server).getKeybindMap();
    }

    private static void refreshKeySet(KeyListPayload payload) {
        keySet.clear();
        keySet.addAll(payload.keySet());
    }
}
