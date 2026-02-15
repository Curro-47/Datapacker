# Animation System

The Animation System allows you to easily animate armor stands with json files<br>
It might seem kind of useless but this allows to create animatable players, npc, and so much more.

To see the command to execute you animation file check [Animation Command](../Commands/animation.md)

> [!WARNING]
> This documentation assumes you already know how datapacks work

---
### FOLDER STRUCTURE

All your animations must be under a file named `animation`<br>
inside of `DatapackName/data/foldername/animation/...`

You can call the folder and file however you like, and you will run it using that name and the folder name, as such
`/animation <armor stand> foldername:filename`

---
### FILE CONTENTS

The json structure is as follows

    {
        "duration": int
        "loop": booleant
        "loop_start": int
        "bones": {
            "<bone_name>": {
                "<keyframe>": [<x>, <y>, <z>],
                ...
            },
            ...
        }
    }

`duration`: in ticks, when the current frame > duration, the animation will end, remember there are 20 ticks/s<br>
`loop`: If the animation should loop or not, if false it will never repeat, if true it will repeat until overwritten<br>
`loop_start`: if loop is true, the next loops will start on this frame<br>

`<bone_name>`: what bone will be animated, the bones an armor stand has are: **head, body, left_arm, right_arm, left_leg, right_leg**

`<keyframe>`: on what frame this position will be on the selected bone

`<x, y, z>`: the angle in degrees the bone will rotate on that axis, for example [90, 45, 175] will rotate 90º on the x axis, 45º on y, and 175º on z

<br>
This is an example of an animation where the armor stand waves with both hands
<br><br>

`wave_anim.json` inside of `data/test_animations/animation/...`

    {
        "duration" : 20,
        "loop" : true,
        "loop_start" : 10,
        "bones" : {
            "left_arm" : {
                "0" : [0, 0, 0],
                "10" : [0, 0, -180],
                "15" : [0, 0, -130],
                "20" : [0, 0, -180]
            },
            "right_arm" : {
                "0" : [0, 0, 0],
                "10" : [0, 0, 180],
                "15" : [0, 0, 130],
                "20" : [0, 0, 180]
            }
        }
    }

I can now execute this animation with `/animation @e[type=armor_stand] test_animations:wave_anim`

As you can see, the duration is equal to the last keyframe<br>
Im animating multiple bones, each with multiple keyframes<br>
and the animation is looping, starting at the 10th frame

> [!WARNING]
> When looping<br>
> Remember that the animation snaps between the last frame<br>
> and the first frame of the loop<br>
>
> If you want a smooth loop make sure the last and first frames are equal