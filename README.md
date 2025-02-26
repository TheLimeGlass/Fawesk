## Fawesk
Fawesk allows you to utilize FastAsyncWorldEdit within Skript.

Finally! No need to wait a tick for looping blocks, just straight raw block editing, any size, done in an instant.

### Requirements
- Supports Vanilla Spigot 1.17+
- FastAsyncWorldEdit
- Skript 2.10+
- Java 21+

### Set and get blocks
```vs
[:parallel] (worldedit|fawe) blocks (within|from) %location% (to|and) %location%
[:parallel] (worldedit|fawe) blocks (within|from) %location% (to|and) %location% (where|that match) \\[<.+>\\]
[:parallel] (worldedit|fawe) block[s] [at] %locations% (where|that match) \\[<.+>\\]
[:parallel] (worldedit|fawe) block[s] [at] %locations%

# Examples
set parallel fawe blocks from {test1} to {test2} where [block input is not air] to air
set fawe blocks from {test1} to {test2} to wheat[age=5]
set the fawe block at {test1} to a diamond block
set the fawe block below all of the players to a diamond block
set parallel fawe blocks {_blocks::*} [where block input is a diamond block] to air
loop fawe blocks within {test1} to {test2} where [block input is a diamond block or a grass block] to air
```
Note that the `where` filter will be ran asynchronously, so be careful with what you input check.
The `block input` information is thread safe.

Use the `parallel` keyword to run the block changes in parallel. Useful for massive edits.

The order of blocks will be unordered

### Saving and pasting schematics.
_The files are saved in the FastAsyncWorldEdit schematics folder._

_The file extension doesn't matter for the format, but if no format is used,
WorldEdit will automatically determine the format based off the file extension._
```
paste schematic %string% at %location%
    [using [clipboard] [format] %-clipboardformat/builtinclipboardformat%]
    [air:exclud[ing|e] air] [entities:exclud[ing|e] entities]

(save|create) schematic %string% from [pos1] %location% [to] [pos2] %location% 
    [using [clipboard] [format] %-clipboardformat/builtinclipboardformat%]
    [with origin %-location%] [entities:includ[ing|e] entities] [biome:[and] includ[ing|e] biome[s]]

# Examples
save schematic "example.sponge" from {pos1} to {pos2} using sponge v3
save schematic "example.schem" from {pos1} to {pos2} using fast v3
save schematic "example.schematic" from {pos1} to {pos2}

paste schematic "example.schematic" at location
paste schematic "example.sponge" at location using sponge v3
paste schematic "example.schem" at location using fast v3 excluding air excluding entities
```
Note: When using the include entities, Fawesk will load the chunks for you.
This is needed as WorldEdit doesn't account for the chunks not being loaded in 1.20+.

Built in clipboard formats:
```
brokeentity: Broken Entity
fast_v2: fast v2
fast_v3: fast v3
fast: fast old
mcedit_schematic: mcedit schematic, mc edit schematic, mc edit
minecraft_structure: minecraft structure, minecraft structure block
sponge_v1_schematic: sponge v1 schematic, sponge v1
sponge_v2_schematic: sponge v2 schematic, sponge v2
sponge_v3_schematic: sponge v3 schematic, sponge v3
sponge: sponge old
```
You can also register your own clipboard format and use the name of it.

https://github.com/user-attachments/assets/f92c66fa-feff-4cd9-8eb0-37aca41b4da4

