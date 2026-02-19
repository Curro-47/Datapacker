package ck47.datapacker.command;

import ck47.datapacker.system.animation.AnimationLoader;
import ck47.datapacker.system.animation.AnimationManager;
import ck47.datapacker.util.AnimationData;
import ck47.datapacker.util.AnimationReference;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.DataCommandStorage;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.command.permission.Permission;
import net.minecraft.command.permission.PermissionLevel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AnimationCommand implements Command<ServerCommandSource> {
    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        List<String> path = context.getNodes()
                .stream()
                .map(node -> node.getNode().getName())
                .toList();

        if (path.get(1).equals("play")) play(context);
        if (path.get(1).equals("stop")) stop(context);
        if (path.get(1).equals("get")) get(context);

        return 1;
    }

    private void play(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<? extends Entity> entityRawCollection = EntityArgumentType.getEntities(context, "target");
        Collection<ArmorStandEntity> entityCollection = entityRawCollection.stream()
                .filter(e -> e instanceof ArmorStandEntity)
                .map(e -> (ArmorStandEntity)e)
                .collect(Collectors.toSet());
        Identifier animationPath = IdentifierArgumentType.getIdentifier(context, "animation path");

        AnimationManager.play(entityCollection, animationPath);
    }

    private void stop(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<? extends Entity> entityRawCollection = EntityArgumentType.getEntities(context, "target");
        Collection<ArmorStandEntity> entityCollection = entityRawCollection.stream()
                .filter(e -> e instanceof ArmorStandEntity)
                .map(e -> (ArmorStandEntity)e)
                .collect(Collectors.toSet());

        for (ArmorStandEntity entity : entityCollection) AnimationManager.stop(entity);
    }

    private void get(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if (!(EntityArgumentType.getEntity(context, "target") instanceof ArmorStandEntity entity)) return;

        NbtCompound data = new NbtCompound();
        try {
            AnimationReference reference = AnimationManager.animationDataMap.get(entity);
            AnimationData animation = AnimationLoader.get(reference.id);

            data.put("duration", NbtInt.of(animation.duration()));
            data.put("tick", NbtInt.of(reference.tick));
            data.put("id", NbtString.of(reference.id.toString()));
            data.put("loop", NbtInt.of(animation.loop() ? 1 : 0));
            data.put("loop_start", NbtInt.of(animation.loop_start()));
        }
        catch (Exception e) {
            data.put("duration", NbtInt.of(0));
            data.put("tick", NbtInt.of(0));
            data.put("id", NbtString.of(""));
            data.put("loop", NbtInt.of(0));
            data.put("loop_start", NbtInt.of(0));
        }

        editStoragePath(context, data);
    }

    private void editStoragePath(CommandContext<ServerCommandSource> context, NbtCompound data) throws CommandSyntaxException {
        Identifier storageId = IdentifierArgumentType.getIdentifier(context, "storage");
        NbtPathArgumentType.NbtPath path = NbtPathArgumentType.getNbtPath(context, "path");

        DataCommandStorage storage = context.getSource().getServer().getDataCommandStorage();
        NbtCompound root = storage.get(storageId);
        if (root == null) root = new NbtCompound();

        path.put(root, data);
        storage.set(storageId, root);
    }

    public void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            /// Animation command
            LiteralCommandNode<ServerCommandSource> animationNode = CommandManager
                    .literal("animation")
                    .requires(source -> source.getPermissions().hasPermission(new Permission.Level(PermissionLevel.GAMEMASTERS)))
                    .build();

            ArgumentCommandNode<ServerCommandSource, ?> animationPath = CommandManager
                    .argument("animation path", IdentifierArgumentType.identifier())
                    .executes(this)
                    .build();

            /// Action
            LiteralCommandNode<ServerCommandSource> animationPlay = CommandManager
                    .literal("play")
                    .then(CommandManager.argument("target", EntityArgumentType.entities())
                    .then(animationPath))
                    .build();

            LiteralCommandNode<ServerCommandSource> animationStop = CommandManager
                    .literal("stop")
                    .then(CommandManager.argument("target", EntityArgumentType.entities())
                    .executes(this))
                    .build();

            LiteralCommandNode<ServerCommandSource> animationGet = CommandManager
                    .literal("get")
                    .then(CommandManager.argument("target", EntityArgumentType.entity())
                    .then(CommandManager.argument("storage", IdentifierArgumentType.identifier())
                    .then(CommandManager.argument("path", NbtPathArgumentType.nbtPath())
                    .executes(this))))
                    .build();

            dispatcher.getRoot().addChild(animationNode);
            animationNode.addChild(animationPlay);
            animationNode.addChild(animationStop);
            animationNode.addChild(animationGet);
        });
    }
}
