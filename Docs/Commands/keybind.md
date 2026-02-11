# Keybind Command

> **keybind** (add) \<name> \<key> \<function> (ON_TRUE|WHILE_TRUE|ON_FALSE|WHILE_FALSE)<br>
> **keybind** (remove) \<name><br>
> **keybind** (list)

The keybind command is used to add, remove and list custom keybinds from players.

> [!NOTE]
> The keybinds are the same for every player (at least for now)<br>
> To make player specific keybinds you can check for the source player inside of the connected function

### ( ADD | REMOVE | LIST )

As each name implies:<br>
(add) creates a new keybind<br>
(remove) eliminates an existing keybind<br>
(list) lists all current existing keybinds

### \< NAME >

Is the name you assign to the keybind<br>
It can be anything, as a recommendation use names relating to its keybind or function<br>

### < KEY >

Key assigned to this keybind<br>
When typing it in you can see the list, altough you can find the full list in [GLFW Input List](https://www.glfw.org/docs/3.3/group__input.html)<br>
Remember only Keyboard and Mouse keys are available

### < FUNCTION >

The function assigned to when the keybind fires<br>
It gets executed as the player that presses the keybind<br>
It will only execute if the condition specified is fulfilled this frame

### ( ON_TRUE | WHILE_TRUE | ON_FALSE | WHILE_FALSE )

This is the condition that must be fulfilled for the function to execute

**ON_TRUE** : The function executes *ONCE* when the key is *PRESSED*<br>
**WHILE_TRUE** : The function executes *EVERY FRAME* the key is *PRESSED*<br>
**ON_FALSE** : The function executes *ONCE* when the key is *RELEASED*<br>
**WHILE_FALSE** : The function executes *EVERY FRAME* the key is *RELEASED*