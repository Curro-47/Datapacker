package ck47.datapacker.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandSource;
import net.minecraft.command.DataCommandStorage;
import net.minecraft.command.argument.CommandFunctionArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.nbt.*;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.function.CommandFunctionManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class RaycastCommand implements Command<ServerCommandSource> {
    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        /// Setup variables
        Entity entity = context.getSource().getEntity();
        assert entity != null;

        // Get the command nodes as a list, ex. ["raycast", 12, "collider", "none", "x"]
        List<String> path = context.getNodes()
                .stream()
                .map(node -> node.getNode().getName())
                .toList();

        if (path.get(2).matches("block")) blockRaycast(entity, context, path);
        else if (path.get(2).matches("entity")) entityRaycast(entity, context, path);
        return 1;
    }

    private void blockRaycast(Entity entity, CommandContext<ServerCommandSource> context, List<String> path) throws CommandSyntaxException {
        double maxReachDistance = DoubleArgumentType.getDouble(context, "distance");

        World world = entity.getEntityWorld();
        Vec3d lookVec = entity.getRotationVec(1.0F); // Player's looking direction vector
        Vec3d rayStart = entity.getCameraPosVec(1.0F); // Player's eye position
        Vec3d rayEnd = rayStart.add(lookVec.multiply(maxReachDistance));

        RaycastContext.ShapeType shapeType = null;
        if (path.get(3).equals("collider")) shapeType = RaycastContext.ShapeType.COLLIDER;
        else if (path.get(3).equals("visual")) shapeType = RaycastContext.ShapeType.VISUAL;
        else if (path.get(3).equals("outline")) shapeType = RaycastContext.ShapeType.OUTLINE;

        RaycastContext.FluidHandling fluidHandling = null;
        if (path.get(4).equals("source")) fluidHandling = RaycastContext.FluidHandling.SOURCE_ONLY;
        else if (path.get(4).equals("all")) fluidHandling = RaycastContext.FluidHandling.ANY;
        else if (path.get(4).equals("none")) fluidHandling = RaycastContext.FluidHandling.NONE;

        BlockHitResult hitResult = world.raycast(new RaycastContext(rayStart, rayEnd, shapeType, fluidHandling, entity));
        BlockPos blockPos = hitResult.getBlockPos();
        double hitDistance = hitResult.getType() == BlockHitResult.Type.BLOCK
                ? rayStart.distanceTo(hitResult.getPos())
                : maxReachDistance;

        if (path.get(5).equals("function")) blockRaycastFunction(context, hitResult, lookVec);
        else if (path.get(5).equals("store")) blockRaycastStore(context, blockPos, hitResult, hitDistance);

        if (path.size() > 8 && path.get(8).equals("function")) blockRaycastFunction(context, hitResult, lookVec);
        else if (path.size() > 7 && path.get(7).equals("store")) blockRaycastStore(context, blockPos, hitResult, hitDistance);
    }

    private void blockRaycastFunction(CommandContext<ServerCommandSource> context, BlockHitResult hitResult, Vec3d lookVec) throws CommandSyntaxException {
        CommandFunctionManager manager = context.getSource().getServer().getCommandFunctionManager();
        Collection<CommandFunction<ServerCommandSource>> functionCollection = CommandFunctionArgumentType.getFunctions(context, "function");

        for (CommandFunction<ServerCommandSource> function : functionCollection) {
            manager.execute(function,
                    context.getSource()
                    .withPosition(hitResult.getPos())
                    .withLookingAt(hitResult.getPos().add(lookVec)));
        }
    }

    private void blockRaycastStore(CommandContext<ServerCommandSource> context, BlockPos blockPos, BlockHitResult hitResult, double hitDistance) throws CommandSyntaxException {
        NbtCompound data = new NbtCompound();
        data.put("x", NbtInt.of(blockPos.getX()));
        data.put("y", NbtInt.of(blockPos.getY()));
        data.put("z", NbtInt.of(blockPos.getZ()));
        data.put("distance", NbtDouble.of(hitDistance));
        data.put("hit", NbtInt.of(hitResult.getType() == BlockHitResult.Type.BLOCK ? 1 : 0));
        data.put("name", NbtString.of(Registries.BLOCK.getId(
                context.getSource().getWorld().getBlockState(hitResult.getBlockPos()).getBlock()
        ).toString()));

        editStoragePath(context, data);
    }

    private void entityRaycast(Entity entity, CommandContext<ServerCommandSource> context, List<String> path) throws CommandSyntaxException {
        double maxReachDistance = DoubleArgumentType.getDouble(context, "distance");
        Vec3d lookVec = entity.getRotationVec(1.0F); // Player's looking direction vector
        Vec3d rayStart = entity.getCameraPosVec(1.0F); // Player's eye position
        Vec3d rayEnd = rayStart.add(lookVec.multiply(maxReachDistance));

        Box rayBox = entity.getBoundingBox().stretch(lookVec.multiply(maxReachDistance)).expand(1.0);
        Collection<? extends Entity> targetEntities = EntityArgumentType.getEntities(context, "target entities");
        Predicate<Entity> entityPredicate = targetEntities::contains;

        EntityHitResult hitResult = ProjectileUtil.raycast(entity, rayStart, rayEnd, rayBox, entityPredicate, maxReachDistance*maxReachDistance);
        Entity hitEntity = hitResult != null ? hitResult.getEntity() : null;
        Vec3d hitPos = hitResult != null ? hitResult.getPos() : rayEnd;

        if (path.get(4).equals("function")) raycastEntityFunction(context, hitResult, lookVec);
        else if (path.get(4).equals("store")) raycastEntityStore(context, hitPos, hitPos.distanceTo(rayStart), hitEntity);

        if (path.size() > 7 && path.get(7).equals("function")) raycastEntityFunction(context, hitResult, lookVec);
        else if (path.size() > 6 && path.get(6).equals("store")) raycastEntityStore(context, hitPos, hitPos.distanceTo(rayStart), hitEntity);
    }

    private void raycastEntityFunction(CommandContext<ServerCommandSource> context, EntityHitResult hitResult, Vec3d lookVec) throws CommandSyntaxException {
        if (hitResult == null) return;

        CommandFunctionManager manager = context.getSource().getServer().getCommandFunctionManager();
        Collection<CommandFunction<ServerCommandSource>> functionCollection = CommandFunctionArgumentType.getFunctions(context, "function");

        for (CommandFunction<ServerCommandSource> function : functionCollection) {
            manager.execute(function,
                    hitResult.getEntity().getCommandSource(context.getSource().getWorld())
                            .withPosition(hitResult.getPos())
                            .withLookingAt(hitResult.getPos().add(lookVec)));
        }
    }

    private void raycastEntityStore(CommandContext<ServerCommandSource> context, Vec3d hitPos, double hitDistance, Entity hitEntity) throws CommandSyntaxException {
        NbtCompound data = new NbtCompound();
        data.put("x", NbtDouble.of(hitPos.x));
        data.put("y", NbtDouble.of(hitPos.y));
        data.put("z", NbtDouble.of(hitPos.z));
        data.put("distance", NbtDouble.of(hitDistance));
        data.put("name", NbtString.of(hitEntity!=null ? hitEntity.getName().getString() : ""));
        data.put("type", NbtString.of(hitEntity!=null ? Registries.ENTITY_TYPE.getId(hitEntity.getType()).toString() : ""));
        data.put("hit", NbtInt.of(hitEntity!=null ? 1 : 0));

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
            /// RAYCAST COMMAND
            LiteralCommandNode<ServerCommandSource> raycastNode = CommandManager
                    .literal("raycast")
                    .build();

            ArgumentCommandNode<ServerCommandSource, ?> raycastDistanceNode = CommandManager
                    .argument("distance", DoubleArgumentType.doubleArg())
                    .build();

            // Raycast Type (Block or entity)
            LiteralCommandNode<ServerCommandSource> raycastTypeBlock = CommandManager
                    .literal("block")
                    .build();

            LiteralCommandNode<ServerCommandSource> raycastTypeEntity = CommandManager
                    .literal("entity")
                    .build();

            /// BLOCK RAYCAST ARGUMENTS
            // Shape collider
            LiteralCommandNode<ServerCommandSource> raycastShapeCollider = CommandManager
                    .literal("collider")
                    .build();

            LiteralCommandNode<ServerCommandSource> raycastShapeOutline = CommandManager
                    .literal("outline")
                    .build();

            LiteralCommandNode<ServerCommandSource> raycastShapeVisual = CommandManager
                    .literal("visual")
                    .build();

            //Fluid handling
            LiteralCommandNode<ServerCommandSource> raycastFluidNone = CommandManager
                    .literal("none")
                    .build();

            LiteralCommandNode<ServerCommandSource> raycastFluidSource = CommandManager
                    .literal("source")
                    .build();

            LiteralCommandNode<ServerCommandSource> raycastFluidAll = CommandManager
                    .literal("all")
                    .build();

            /// ENTITY RAYCAST ARGUMENTS
            ArgumentCommandNode<ServerCommandSource, ?> raycastTargetEntities = CommandManager
                    .argument("target entities", EntityArgumentType.entities())
                    .build();

            /// OUTPUT STORE
            LiteralCommandNode<ServerCommandSource> raycastOutputStore = CommandManager
                    .literal("store")
                    .build();

            ArgumentCommandNode<ServerCommandSource, ?> raycastStoreStorage = CommandManager
                    .argument("storage", IdentifierArgumentType.identifier())
                    .build();

            ArgumentCommandNode<ServerCommandSource, ?> raycastStorePath = CommandManager
                    .argument("path", NbtPathArgumentType.nbtPath())
                    .executes(this)
                    .build();

            /// OUTPUT FUNCTION
            LiteralCommandNode<ServerCommandSource> raycastOutputFunction = CommandManager
                    .literal("function")
                    .build();

            ArgumentCommandNode<ServerCommandSource, ?> raycastFunction = CommandManager
                    .argument("function", CommandFunctionArgumentType.commandFunction())
                    .suggests((context, builder) ->
                            CommandSource.suggestIdentifiers(
                                    context.getSource().getServer()
                                            .getCommandFunctionManager()
                                            .getAllFunctions(),
                                    builder))
                    .executes(this)
                    .build();

            /// OUTPUT STORE 2 (After OUTPUT FUNCTION)
            LiteralCommandNode<ServerCommandSource> raycastOutputStore2 = CommandManager
                    .literal("store")
                    .build();

            ArgumentCommandNode<ServerCommandSource, ?> raycastStoreStorage2 = CommandManager
                    .argument("storage", IdentifierArgumentType.identifier())
                    .build();

            ArgumentCommandNode<ServerCommandSource, ?> raycastStorePath2 = CommandManager
                    .argument("path", NbtPathArgumentType.nbtPath())
                    .executes(this)
                    .build();

            /// OUTPUT FUNCTION
            LiteralCommandNode<ServerCommandSource> raycastOutputFunction2 = CommandManager
                    .literal("function")
                    .build();

            ArgumentCommandNode<ServerCommandSource, ?> raycastFunction2 = CommandManager
                    .argument("function", CommandFunctionArgumentType.commandFunction())
                    .suggests((context, builder) ->
                            CommandSource.suggestIdentifiers(
                                    context.getSource().getServer()
                                            .getCommandFunctionManager()
                                            .getAllFunctions(),
                                    builder))
                    .executes(this)
                    .build();

            raycastOutputFunction2.addChild(raycastFunction2);



            /// BUILD TREE
            raycastOutputStore2.addChild(raycastStoreStorage2);
            raycastStoreStorage2.addChild(raycastStorePath2);


            //Main nodes
            dispatcher.getRoot().addChild(raycastNode);
            raycastNode.addChild(raycastDistanceNode);

            //Raycast type
            raycastDistanceNode.addChild(raycastTypeBlock);
            raycastDistanceNode.addChild(raycastTypeEntity);

            /// Block
            //Shape collider
            raycastTypeBlock.addChild(raycastShapeCollider);
            raycastTypeBlock.addChild(raycastShapeOutline);
            raycastTypeBlock.addChild(raycastShapeVisual);

            //Fluid handling
            raycastShapeCollider.addChild(raycastFluidNone);
            raycastShapeCollider.addChild(raycastFluidSource);
            raycastShapeCollider.addChild(raycastFluidAll);

            raycastShapeOutline.addChild(raycastFluidNone);
            raycastShapeOutline.addChild(raycastFluidSource);
            raycastShapeOutline.addChild(raycastFluidAll);

            raycastShapeVisual.addChild(raycastFluidNone);
            raycastShapeVisual.addChild(raycastFluidSource);
            raycastShapeVisual.addChild(raycastFluidAll);

            /// Entity
            raycastTypeEntity.addChild(raycastTargetEntities);

            /// Both entity and block
            //Output type
            raycastFluidNone.addChild(raycastOutputStore);
            raycastFluidSource.addChild(raycastOutputStore);
            raycastFluidAll.addChild(raycastOutputStore);
            raycastTargetEntities.addChild(raycastOutputStore);

            raycastFluidNone.addChild(raycastOutputFunction);
            raycastFluidSource.addChild(raycastOutputFunction);
            raycastFluidAll.addChild(raycastOutputFunction);
            raycastTargetEntities.addChild(raycastOutputFunction);

            //Store
            raycastOutputStore.addChild(raycastStoreStorage);
            raycastStoreStorage.addChild(raycastStorePath);

            //Function
            raycastOutputFunction.addChild(raycastFunction);

            // 2nd Round (Store after Function or vice versa)
            raycastStorePath.addChild(raycastOutputFunction2);
            raycastFunction.addChild(raycastOutputStore2);
        });
    }
}