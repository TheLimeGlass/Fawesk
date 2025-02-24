Fawesk allows you to utilize FAWE with Skript.
Most servers use FAWE, so why not allow your scripts to set millions of blocks at a time too?

Set and get blocks
```vs
[:parallel] (worldedit|fawe) blocks (within|from) %location% (to|and) %location%
[:parallel] (worldedit|fawe) blocks (within|from) %location% (to|and) %location% (where|that match) \\[<.+>\\]
[:parallel] (worldedit|fawe) block[s] [at] %locations% (where|that match) \\[<.+>\\]
[:parallel] (worldedit|fawe) block[s] [at] %locations%

# Examples
parallel set fawe blocks from {test1} to {test2} where [block input is not air] to air # Use parallel for unordered large updates
set fawe blocks from {test1} to {test2} to wheat[age=5]
loop fawe blocks within {test1} to {test2} where [block input is a diamond block or a grass block] to air
```

Saving and pasting schematics.
_Currently only supporting (.sponge) V3 schematics as it's the best schematic save currently, will probably support enum schematic types through the pattern in the future_
```vs
paste schematic %string% at %location% [air:exclud[ing|e] air] [entities:exclud[ing|e] entities]
(save|create) schematic %string% from [pos1] %location% [to] [pos2] %location% [with origin %-location%]
```


https://github.com/user-attachments/assets/f92c66fa-feff-4cd9-8eb0-37aca41b4da4

