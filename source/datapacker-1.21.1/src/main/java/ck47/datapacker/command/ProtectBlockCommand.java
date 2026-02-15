package ck47.datapacker.command;

import ck47.datapacker.util.BlockSelector;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.block.Block;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.RegistryEntryPredicateArgumentType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import ck47.datapacker.system.protected_blocks.ProtectedBlocks;

import java.util.*;
import java.util.stream.Collectors;

public class ProtectBlockCommand implements Command<ServerCommandSource> {
    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "target");
        assert players != null;

        // Get the command nodes as a list
        List<String> path = context.getNodes()
                .stream()
                .map(node -> node.getNode().getName())
                .toList();

        // Loop through players in the selector
        for (ServerPlayerEntity player: players) {
            UUID uuid = player.getUuid();

            if (path.contains("list")) return list(context, uuid);

            // For every other option get BlockSelector
            var entryPredicate = RegistryEntryPredicateArgumentType.getRegistryEntryPredicate(context, "block", RegistryKeys.BLOCK)
                    .getEntry();
            BlockSelector selector = null;
            if (entryPredicate.left().isPresent()) selector = new BlockSelector.Block(entryPredicate.left().get().registryKey());
            else if (entryPredicate.right().isPresent()) selector = new BlockSelector.Tag(entryPredicate.right().get().getTag());

            if (path.contains("add")) ProtectedBlocks.Add(uuid, selector, context.getSource().getWorld());
            else if (path.contains("remove")) ProtectedBlocks.Remove(uuid, selector, context.getSource().getWorld());
            else if (path.contains("check")) return check(context, uuid, selector);
        }

        return 0;
    }

    private int list(CommandContext<ServerCommandSource> context, UUID uuid) {
        Set<String> ids = ProtectedBlocks.GetProtected(uuid, context.getSource().getWorld()).stream()
                .map(BlockSelector::getName)
                .collect(Collectors.toSet());
        context.getSource().sendFeedback(() -> Text.literal(String.join(", ", ids)), false);
        return ids.size();
    }

    private int check(CommandContext<ServerCommandSource> context, UUID uuid, BlockSelector selector) {
        if (ProtectedBlocks.IsProtected(uuid, selector, context.getSource().getWorld())) return 1;

        // If selector is BLOCK and is detectable by any selector TAG of the player
        if (selector instanceof BlockSelector.Block blockSelector) {
            // BlockSelector.Block --> RegistryEntry<Block>
            RegistryWrapper.WrapperLookup wrapperLookup = context.getSource().getWorld().getRegistryManager();
            RegistryEntry<Block> entry = wrapperLookup.getWrapperOrThrow(RegistryKeys.BLOCK).getOrThrow(blockSelector.key());

            boolean isInATag = ProtectedBlocks.IsProtected(uuid, entry, context.getSource().getWorld());
            if (isInATag) return 1;
        }

        return 0;
    }

    public void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            /// PROTECT BLOCK COMMAND
            LiteralCommandNode<ServerCommandSource> protectBlockNode = CommandManager
                    .literal("protectblock")
                    .build();

            LiteralCommandNode<ServerCommandSource> protectBlockAddNode = CommandManager
                    .literal("add")
                    .build();

            LiteralCommandNode<ServerCommandSource> protectBlockRemoveNode = CommandManager
                    .literal("remove")
                    .build();

            LiteralCommandNode<ServerCommandSource> protectBlockListNode = CommandManager
                    .literal("list")
                    .build();

            LiteralCommandNode<ServerCommandSource> protectBlockCheckNode = CommandManager
                    .literal("check")
                    .build();

            ArgumentCommandNode<ServerCommandSource, ?> protectBlockPlayerNode = CommandManager
                    .argument("target", EntityArgumentType.players())
                    .build();

            ArgumentCommandNode<ServerCommandSource, ?> protectBlockListPlayerNode = CommandManager
                    .argument("target", EntityArgumentType.players())
                    .executes(new ProtectBlockCommand())
                    .build();

            ArgumentCommandNode<ServerCommandSource, ?> protectBlockPredicateNode = CommandManager
                    .argument("block", RegistryEntryPredicateArgumentType.registryEntryPredicate(registryAccess, RegistryKeys.BLOCK))
                    .executes(new ProtectBlockCommand())
                    .build();

            dispatcher.getRoot().addChild(protectBlockNode);

            protectBlockNode.addChild(protectBlockAddNode);
            protectBlockNode.addChild(protectBlockRemoveNode);
            protectBlockNode.addChild(protectBlockListNode);
            protectBlockNode.addChild(protectBlockCheckNode);

            protectBlockAddNode.addChild(protectBlockPlayerNode);
            protectBlockRemoveNode.addChild(protectBlockPlayerNode);
            protectBlockCheckNode.addChild(protectBlockPlayerNode);
            protectBlockListNode.addChild(protectBlockListPlayerNode);

            protectBlockPlayerNode.addChild(protectBlockPredicateNode);
        });
    }
}
