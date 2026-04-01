package ck47.datapacker.util;

import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.util.Identifier;

import java.util.Collection;

public record EventData(
        EventType type,
        Identifier storage,
        NbtPathArgumentType.NbtPath path,
        Collection<CommandFunction<ServerCommandSource>> function) {}
