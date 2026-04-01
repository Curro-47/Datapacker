package ck47.datapacker.system.event;

import ck47.datapacker.util.EventData;
import ck47.datapacker.util.EventType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.command.CommandSource;
import net.minecraft.command.DataCommandStorage;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.command.permission.LeveledPermissionPredicate;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.function.CommandFunctionManager;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.HashSet;

public class EventManager {
    private static final Collection<EventData> events = new HashSet<>();

    public static void Register() {
        ServerMessageEvents.CHAT_MESSAGE.register((message, entity, params) -> {
            NbtCompound data = new NbtCompound();
            data.put("message", NbtString.of(message.getSignedContent()));

            fire(EventType.CHAT, data, entity.getCommandSource(), entity.getCommandSource().getServer());
        });
    }

    public static void add(EventData event) {
        events.add(event);
    }

    private static void fire(EventType type, NbtCompound data, ServerCommandSource source, MinecraftServer server) {
        CommandFunctionManager manager = server.getCommandFunctionManager();

        for (EventData event : events) {
            if (event.type() != type) continue;

            editStoragePath(event, data, server);

            for (CommandFunction<ServerCommandSource> funct : event.function()) {
                manager.execute(funct,
                    source
                        .withSilent()
                        .withPermissions(LeveledPermissionPredicate.GAMEMASTERS));
            }
        }
    }

    private static void editStoragePath(EventData event, NbtCompound data, MinecraftServer server) {
        try {
            DataCommandStorage storage = server.getDataCommandStorage();
            NbtCompound root = storage.get(event.storage());
            if (root == null) root = new NbtCompound();

            event.path().put(root, data);
            storage.set(event.storage(), root);
        }
        catch (Exception ignored) {}
    }
}
