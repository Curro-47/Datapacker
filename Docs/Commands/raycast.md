# Raycast Command

> [!IMPORTANT]
> `raycast <distance> "ray" "out"`
>
> **"ray"**<br>
> `(block) (collider|outline|visual) (all|source|none)`<br>
> `(entity) <target entities>`
>
> **"out"**<br>
> `(store) <storage> <path> [ (function) \<function> ]`<br>
> `(function) \<function> [ (store) \<storage> \<path> ]`

The raycast command, when used, creates a raycast from the executing entities pov (crosshair), which travels in a straight line forwards,<br>
detecting either a block or entity as specified<br>

> [!NOTE]
> Currently the raycast command executes the raycast<br>
> from the casting entity into the direction its looking to
>
> If you want to make raycast anywhere in the world i suggest you use **Marker** entities, and execute the command as them
>
> Using `positioned` and `rotated` **WILL NOT WORK**<br>
> very sorry about that

---
### \<DISTANCE>
The maximum distance in blocks which the raycast travels before "giving up" and either not storing any values or not executing the function

---
### (BLOCK)
This sets the raycast to collide with only blocks<br>
it will keep moving until reaching its max distance or colliding with something, ignoring entities

> ---
> ### (COLLIDER | OUTLINE | VISUAL)
> `COLLIDER` : Only detects blocks with collisions (no grass, flowers, rails, etc)<br>
> `OUTLINE` : Detects all blocks on their outline<br>
> `VISUAL` : Detects the visual part of the block (I dont understand this one as well, but usually used for rendering and stuff)
>
> ---
> ### (ALL | SOURCE | NONE)
> How it interacts with fluids<br>
> `ALL` : Detects all fluids<br>
> `SOURCE` : Only detects fluid source blocks<br>
> `NONE` : Ignore fluids, treat them as air<br>
> 
> ---

---
### (ENTITY)
This sets the raycast to collide with only entities<br>

> [!WARNING]
> This raycast mode ignores blocks, able to detect entities through walls<br>
> This might lead to unexpected results<br>
>
> If you want it to not go though blocks you can combine it with the block raycast<br>
> either check that the distance to a block is higher than to an entity<br>
> or plug the output distance from the block raycast into the distance argument on the entity raycast

> ---
>
> ### \<TARGET ENTITIES>
> This argument specifies which entities can be detected with this raycast,<br>
> ignoring all entities which do not qualify and passing through them
>
> ---

---

### (STORE)
Stores the resulting data inside of an nbt storage

> ---
>
> ### \<STORAGE>
> The storage identifier where the information will be stored<br>
> Similar to the `/data` command
>
> ---
> ### \<PATH>
> The nbt path where the information will be stored<br>
> Similar to the `/data` command
>
> ---

The following data will be stored:

**BLOCK RAYCAST**
- `x` : block x position (int)
- `y` : block y position (int)
- `z` : block z position (int)
- `hit` : 1 if the raycast was successful, 0 if it was not
- `distance` : distance between the start and finish of the raycast (double), if the raycast failed then the distance will be the maximum distance
- `name` : name of the block

<br />

**ENTITY RAYCAST**
- `x` : hit x position (double)
- `y` : hit y position (double)
- `z` : hit z position (double)
- `hit` : 1 if the raycast was successful, 0 if it was not
- `distance` : distance between the start and finish of the raycast (double), if the raycast failed then the distance will be the maximum distance
- `name` : name of the hit entity
- `type` : type of the hit entity

---

### (FUNCTION)
Executes a function at the target position and entity (if applicable)

> ---
>
> ### \<FUNCTION>
> *Not to be confused with (FUNCTION)*<br>
> Is the function id to execute if the raycast succeds
>
> ---

<br>

**ENTITY RAYCAST** : Executes the function at the hit position (not the entities position) looking at the raycast direction and as the hit entity<br>
**BLOCK RAYCAST** : Executes the function at the hit position (not blocks position) as the entity who executed the raycast command