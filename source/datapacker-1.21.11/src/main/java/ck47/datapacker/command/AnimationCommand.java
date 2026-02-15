package ck47.datapacker.command;

import ck47.datapacker.system.animation.AnimationManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.stream.Collectors;

public class AnimationCommand implements Command<ServerCommandSource> {
    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<? extends Entity> entityRawCollection = EntityArgumentType.getEntities(context, "target");
        Collection<ArmorStandEntity> entityCollection = entityRawCollection.stream()
                .filter(e -> e instanceof ArmorStandEntity)
                .map(e -> (ArmorStandEntity)e)
                .collect(Collectors.toSet());
        Identifier animationPath = IdentifierArgumentType.getIdentifier(context, "animation path");

        AnimationManager.play(entityCollection, animationPath);

        return 1;
    }

    public void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            /// Animation command
            LiteralCommandNode<ServerCommandSource> animationNode = CommandManager
                    .literal("animation")
                    .build();

            ArgumentCommandNode<ServerCommandSource, ?> animationEntity = CommandManager
                    .argument("target", EntityArgumentType.entities())
                    .build();

            ArgumentCommandNode<ServerCommandSource, ?> animationPath = CommandManager
                    .argument("animation path", IdentifierArgumentType.identifier())
                    .executes(this)
                    .build();

            dispatcher.getRoot().addChild(animationNode);
            animationNode.addChild(animationEntity);
            animationEntity.addChild(animationPath);
        });
    }
}
