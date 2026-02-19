package ck47.datapacker.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.command.permission.Permission;
import net.minecraft.command.permission.PermissionLevel;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.Collection;

public class VelocityCommand implements Command<ServerCommandSource> {

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<? extends Entity> entityCollection = EntityArgumentType.getEntities(context, "target");
        Vec3d vector = new Vec3d(
                (DoubleArgumentType.getDouble(context, "x")),
                DoubleArgumentType.getDouble(context, "y"),
                DoubleArgumentType.getDouble(context, "z")
        );

        for (Entity entity : entityCollection) {
            if (entity instanceof ServerPlayerEntity player) {
                player.velocityDirty = true;
                player.knockedBack = true;
                player.setVelocity(vector);
            }
            else entity.setVelocity(vector);
        }

        return 0;
    }

    public void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            /// Velocity command
            LiteralCommandNode<ServerCommandSource> velocityNode = CommandManager
                    .literal("velocity")
                    .requires(source -> source.getPermissions().hasPermission(new Permission.Level(PermissionLevel.GAMEMASTERS)))
                    .build();

            // Entity
            ArgumentCommandNode<ServerCommandSource, ?> velocityEntity = CommandManager
                    .argument("target", EntityArgumentType.entities())
                    .build();

            // Velocity
            ArgumentCommandNode<ServerCommandSource, ?> velocityVector = CommandManager
                    .argument("x", DoubleArgumentType.doubleArg())
                    .then(CommandManager.argument("y", DoubleArgumentType.doubleArg())
                    .then(CommandManager.argument("z", DoubleArgumentType.doubleArg())
                    .executes(this)))
                    .build();

            dispatcher.getRoot().addChild(velocityNode);
            velocityNode.addChild(velocityEntity);
            velocityEntity.addChild(velocityVector);
        });
    }
}
