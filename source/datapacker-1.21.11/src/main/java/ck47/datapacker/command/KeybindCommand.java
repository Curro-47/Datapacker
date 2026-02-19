package ck47.datapacker.command;

import ck47.datapacker.system.keybind.KeybindManager;
import ck47.datapacker.system.keybind.KeybindPersistentState;
import ck47.datapacker.util.KeybindData;
import ck47.datapacker.util.KeybindType;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.CommandFunctionArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.permission.*;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.stream.Collectors;

public class KeybindCommand implements Command<ServerCommandSource> {
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        // Get the command nodes as a list
        List<String> path = context.getNodes()
                .stream()
                .map(node -> node.getNode().getName())
                .toList();

        int success = 0;
        if (path.get(1).equals("add"))         success = AddKeybind(context, path);
        else if (path.get(1).equals("remove")) success = RemoveKeybind(context);
        else if (path.get(1).equals("list"))   success = ListKeybinds(context);

        return success;
    }

    private int AddKeybind(CommandContext<ServerCommandSource> context, List<String> path) throws CommandSyntaxException {
        /// Get Arguments
        Identifier name = IdentifierArgumentType.getIdentifier(context, "name");
        String key = StringArgumentType.getString(context, "key");
        KeybindType type = KeybindType.getFromString(path.get(5));
        List<Identifier> function = CommandFunctionArgumentType.getFunctions(context, "function")
                                        .stream()
                                        .map(CommandFunction::id).toList();

        // Key validation
        /*if (key.equals(InputUtil.UNKNOWN_KEY)) {
            context.getSource().sendError(Text.literal("Invalid key: " + keyString));
            return 0;
        }*/

        KeybindManager.Add(context.getSource().getServer(), name, key, function, type);
        return 1;
    }

    private int RemoveKeybind(CommandContext<ServerCommandSource> context) {
        /// Get Name
        Identifier name = IdentifierArgumentType.getIdentifier(context, "name");

        // Name validation
        if (KeybindManager.GetKeybind(context.getSource().getServer(), name) == null) {
            context.getSource().sendError(Text.literal("Name doesn't exist: " + name));
            return 0;
        }

        KeybindManager.Remove(context.getSource().getServer(), name);
        return 1;
    }

    private int ListKeybinds(CommandContext<ServerCommandSource> context) {
        KeybindPersistentState keybindState = KeybindPersistentState.get(context.getSource().getServer());
        Map<Identifier, KeybindData> keybindMap = keybindState.getKeybindMap();

        Set<Identifier> ids = KeybindManager.GetKeybindMap(context.getSource().getServer()).keySet();
        Set<String> idsAsString = ids.stream().map(id -> {
            KeybindData kbData = keybindMap.get(id);
            return "%s (%s, %s, %s)".formatted(id.toString(), kbData.key, kbData.functionIdList.toString(), kbData.type.name());
        }).collect(Collectors.toSet());

        context.getSource().sendFeedback(() -> Text.literal(String.join("\n", idsAsString)), false);

        return 1;
    }

    public void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            /* Create Nodes */
            /// Main node
            LiteralCommandNode<ServerCommandSource> keybindNode = CommandManager
                    .literal("keybind")
                    .requires(source -> source.getPermissions().hasPermission(new Permission.Level(PermissionLevel.GAMEMASTERS)))
                    .build();

            /// Add and Remove
            LiteralCommandNode<ServerCommandSource> keybindAdd = CommandManager
                    .literal("add")
                    .build();

            LiteralCommandNode<ServerCommandSource> keybindRemove = CommandManager
                    .literal("remove")
                    .build();

            LiteralCommandNode<ServerCommandSource> keybindList = CommandManager
                    .literal("list")
                    .executes(this)
                    .build();

            /// Name
            ArgumentCommandNode<ServerCommandSource, ?> keybindAddName = CommandManager
                    .argument("name", IdentifierArgumentType.identifier())
                    .build();

            ArgumentCommandNode<ServerCommandSource, ?> keybindRemoveName = CommandManager
                    .argument("name", IdentifierArgumentType.identifier())
                    .suggests((context, builder) -> {
                        final Set<String> keybindNameSet = new HashSet<>();
                        for (Map.Entry<Identifier, KeybindData> kb : KeybindManager.GetKeybindMap(context.getSource().getServer()).entrySet()) {
                            keybindNameSet.add(kb.getKey().getNamespace() + ":" + kb.getKey().getPath());
                        }

                        return net.minecraft.command.CommandSource.suggestMatching(keybindNameSet, builder);
                    })
                    .executes(this)
                    .build();

            /// Keybind
            ArgumentCommandNode<ServerCommandSource, ?> keybindKey = CommandManager
                    .argument("key", StringArgumentType.word())
                    .suggests((context, builder) -> CommandSource.suggestMatching(KeybindManager.keySet, builder))
                    .build();

            /// Function
            ArgumentCommandNode<ServerCommandSource, ?> keybindFunction = CommandManager
                    .argument("function", CommandFunctionArgumentType.commandFunction())
                    .suggests((context, builder) ->
                            CommandSource.suggestIdentifiers(
                                    context.getSource().getServer()
                                            .getCommandFunctionManager()
                                            .getAllFunctions(),
                                    builder))
                    .build();

            /// Types
            LiteralCommandNode<ServerCommandSource> keybindTypeON_TRUE = CommandManager
                    .literal("ON_TRUE")
                    .executes(this)
                    .build();
            LiteralCommandNode<ServerCommandSource> keybindTypeWHILE_TRUE = CommandManager
                    .literal("WHILE_TRUE")
                    .executes(this)
                    .build();
            LiteralCommandNode<ServerCommandSource> keybindTypeON_FALSE = CommandManager
                    .literal("ON_FALSE")
                    .executes(this)
                    .build();
            LiteralCommandNode<ServerCommandSource> keybindTypeWHILE_FALSE = CommandManager
                    .literal("WHILE_FALSE")
                    .executes(this)
                    .build();

            /* Build Node Tree */
            /// Main node
            dispatcher.getRoot().addChild(keybindNode);
            /// Add and Remove
            keybindNode.addChild(keybindAdd);
            keybindNode.addChild(keybindRemove);
            keybindNode.addChild(keybindList);
            /// Name
            keybindAdd.addChild(keybindAddName);
            keybindRemove.addChild(keybindRemoveName);
            /// Function and Default
            keybindAddName.addChild(keybindKey);
            keybindKey.addChild(keybindFunction);
            /// Types
            keybindFunction.addChild(keybindTypeON_TRUE);
            keybindFunction.addChild(keybindTypeWHILE_TRUE);
            keybindFunction.addChild(keybindTypeON_FALSE);
            keybindFunction.addChild(keybindTypeWHILE_FALSE);
        });
    }
}
