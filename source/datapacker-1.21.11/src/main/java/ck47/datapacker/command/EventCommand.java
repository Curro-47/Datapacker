package ck47.datapacker.command;

import ck47.datapacker.system.event.EventManager;
import ck47.datapacker.util.EventData;
import ck47.datapacker.util.EventType;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.CommandFunctionArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.command.permission.Permission;
import net.minecraft.command.permission.PermissionLevel;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import java.util.List;

public class EventCommand implements Command<ServerCommandSource> {
    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

        // Get the command nodes as a list, ex. ["raycast", 12, "collider", "none", "x"]
        List<String> path = context.getNodes()
                .stream()
                .map(node -> node.getNode().getName())
                .toList();

        EventManager.add(new EventData(
                EventType.fromString(path.get(1)),
                IdentifierArgumentType.getIdentifier(context, "storage"),
                NbtPathArgumentType.getNbtPath(context, "path"),
                CommandFunctionArgumentType.getFunctions(context, "function")));

        return 0;
    }

    public void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            LiteralCommandNode<ServerCommandSource> eventNode = CommandManager
                    .literal("event")
                    .requires(source -> source.getPermissions().hasPermission(new Permission.Level(PermissionLevel.GAMEMASTERS)))
                    .build();

            // Options
            LiteralCommandNode<ServerCommandSource> eventOptChat = CommandManager
                    .literal("chat")
                    .requires(source -> source.getPermissions().hasPermission(new Permission.Level(PermissionLevel.GAMEMASTERS)))
                    .build();

            // Storage
            ArgumentCommandNode<ServerCommandSource, ?> eventStorage = CommandManager
                    .argument("storage", IdentifierArgumentType.identifier())
                    .build();

            ArgumentCommandNode<ServerCommandSource, ?> eventPath = CommandManager
                    .argument("path", NbtPathArgumentType.nbtPath())
                    .build();

            // Function
            ArgumentCommandNode<ServerCommandSource, ?> eventFunction = CommandManager
                    .argument("function", CommandFunctionArgumentType.commandFunction())
                    .suggests((context, builder) ->
                            CommandSource.suggestIdentifiers(
                                    context.getSource().getServer()
                                            .getCommandFunctionManager()
                                            .getAllFunctions(),
                                    builder))
                    .executes(this)
                    .build();

            dispatcher.getRoot().addChild(eventNode);

            eventNode.addChild(eventOptChat);

            eventOptChat.addChild(eventStorage);

            eventStorage.addChild(eventPath);
            eventPath.addChild(eventFunction);
        });
    }
}
