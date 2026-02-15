package ck47.datapacker.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockKeys;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public sealed interface BlockSelector
        permits BlockSelector.Block, BlockSelector.Tag {

    boolean matches(RegistryEntry<net.minecraft.block.Block> entry);
    String getName();

    NbtString toNbt();
    static BlockSelector fromString(String selectorString) {
        if (selectorString.charAt(0) == '#') {
            Identifier selectorId = Identifier.of(selectorString.substring(1));
            TagKey<net.minecraft.block.Block> tagKey = TagKey.of(RegistryKeys.BLOCK, selectorId);
            return new BlockSelector.Tag(tagKey);
        }

        Identifier selectorId = Identifier.of(selectorString);
        RegistryKey<net.minecraft.block.Block> key = RegistryKey.of(RegistryKeys.BLOCK, selectorId);
        return new BlockSelector.Block(key);
    }

    record Block(RegistryKey<net.minecraft.block.Block> key) implements BlockSelector {
        @Override
        public boolean matches(RegistryEntry<net.minecraft.block.Block> entry) {
            return entry.matchesKey(key);
        }
        @Override
        public String getName() {
            return key().getValue().toString();
        }
        @Override
        public NbtString toNbt() {
            return NbtString.of(key.getValue().toString());
        }
    }

    record Tag(TagKey<net.minecraft.block.Block> tag) implements BlockSelector {
        @Override
        public boolean matches(RegistryEntry<net.minecraft.block.Block> entry) {
            return entry.isIn(tag);
        }
        @Override
        public String getName() {
            return "#"+tag().id().toString();
        }
        @Override
        public NbtString toNbt() {
            return NbtString.of("#"+tag.id().toString());
        }
    }
}