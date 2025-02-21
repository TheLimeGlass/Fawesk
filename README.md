Recode of the addon to use full asynchronous setting and new FAWE API

Set and get blocks
```vs
(worldedit|fawe) blocks (within|from) %location% to %location% (where|that match) \\[<.+>\\]
(worldedit|fawe) blocks (within|from) %location% to %location%

# Examples
set fawe blocks from {test1} to {test2} where [block input is not air] to air
set fawe blocks from {test1} to {test2} to wheat[age=5]
loop fawe blocks within {test1} to {test2} where [block input is a diamond block or a grass block] to air
```

Saving and pasting schematics.
_Currently only supporting (.sponge) V3 schematics as it's the best schematic save currently, will probably support enum schematic types through the pattern in the future_
```vs
paste schematic %string% at %location% [air:exclud[ing|e] air] [entities:exclud[ing|e] entities]
(save|create) schematic %string% from [pos1] %location% [to] [pos2] %location% [with origin %-location%]
```
