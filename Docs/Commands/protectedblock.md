# Protectedblock Command

> [!IMPORTANT] Syntax
> **protectedblock** (add | remove | check) \<target> \<block>
> **protectedblock** (list) \<target>

The protectblock command is used to disable players from breaking certain blocks, this list is stored per-player, so different players can have different block break "permissions"

---
### (ADD | REMOVE | CHECK | LIST)

**add** : Adds a block or block-tag to a player's protectedblock list<br>
**remove** : Removes a block or block-tag from a player's protectedblock list<br>
**check** : Checks if a player has a certain block or block tag protected, returns 1 if protected, 0 if not<br>
**list** : Lists all of a players protectedblocks, returns the length of that list

> [!NOTE] Note (CHECK)
> When using **check** with a block it also checks if that block is within a protected block-tag<br>
>
> For example. If a player has *"#minecraft:logs"* protected running 
>
>>*/protectedblock check \<player> minecraft:oak_log*
>
> will return 1 (meaning protected) even if *"minecraft:oak_log"* was not explicitly in the protectedblock list

---
### \<TARGET>

The player (or players) which you want to (add | remeove | check | list) a keybind from

---
### \<BLOCK>

The block or block-tag (list of blocks) you want to (add | remeove | check | list)