# Animation Command

> [!IMPORTANT]
> `/animation (add) <target> <animation path>`<br>
> `/animation (stop) <target>`<br>
> `/animation (get) <target> <storage> <path>`

Plays an animation on an armor stand from a .json file in a datapack

More info on the file and folder structure in [Animation](../Systems/Animation.md)

---
### (PLAY | STOP | GET)
`play`: starts an animation on the selected entities
`stop`: stops an entities current animation
`get`: gets the data of an entities current animation and stores it in storage

---
### \<TARGET>

The entity which you want to apply the animation to<br>
This MUST be an armor stand

---
### \<ANIMATION PATH>

The path on the datapack where the animation is stored

defined by: `foldername:filename`<br>
Where DatapackName/data/`foldername`/animation/`filename`.json

---
### \<STORAGE> \<PATH>
Used in the `get` branch<br>
Stores the animations data in said storage, at said path<br>
works like storing data with the `/data` command