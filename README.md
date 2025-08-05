# FTB-Unearthed

## Overview

FTB Unearthed is a resource generation mod for NeoForge which adds a multiblock structure, the Unearther, to allow villager workers to produce resources from raw materials, using a brush tool (although in theory any item can be used as the tool).

Only test recipes are provided by default, which only appear if running the mod in a development environment, or if `include_dev_recipes` is set to true in `config/ftbunearthed-startup.snbt`.

## The Unearther

The Unearther is a 3x3x3 multiblock which is auto-created when the item is placed in world. Breaking any block of the Unearther drops itself as a single item again (also dropping any items in the machine).

### Automation

The Unearther is a sided block for hopper/pipe purposes:

* Insert food items at the top. Food makes the worker operate more quickly; better foods last longer and provide a bigger speed boost.
* Insert input blocks on any side
* Extract outputs from the bottom

## Recipes

An example recipe:

```json5
{
  // remove this condition for "production" recipes
  "neoforge:conditions": [
    {
      "type": "ftbunearthed:dev_environment"
    }
  ],
  "type": "ftbunearthed:unearther",
  "input_block": "minecraft:sand",
  "output_items": [
    {
      "chance": 0.1,
      "item": {
        "count": 2,
        "id": "minecraft:diamond"
      }
    },
    {
      "chance": 0.3,
      "item": {
        "count": 1,
        "id": "minecraft:amethyst_shard"
      }
    },
    {
      "chance": 0.5,
      "item": {
        "count": 1,
        "id": "minecraft:emerald"
      }
    },
    {
      "chance": 1.5,
      "item": {
        "count": 1,
        "id": "minecraft:lapis_lazuli"
      }
    }
  ],
  "processing_time": 40,
  "tool_item": {
    "tag": "c:tools/brush"
  },
  "worker": {
    "profession": "minecraft:mason"
  }
}
```

### Recipe Fields
* `type` must always be `ftbunearthed:unearther`
* `input_block` is a blockstate string or block tag (use a `#` prefix for block tags).
  * This is a block, not an item, since these resources are "brushed" in block form, both by the Unearther machine and manually by a player.
* `processing_time` is the time in ticks for each work cycle. Optional; defaults to 200 ticks
* `worker` represents the villager data needed for this recipe, which is matched against a **Worker Token** item (see next section). This has three fields:
  * `profession` is a villager profession, from the villager profession registry (any vanilla or modded profession is accepted here). This field is mandatory.
  * `type` is a villager type from the villager type registry, e.g. `plains`, `desert`, `taiga`... Optional; is a "don't care" value if omitted
  * `level` is a villager trade level in the range 1..5. Optional; defaults to 1 if omitted. 
    * Any worker token used in the machine must have a `level` of at least this level for the recipe to be usable.
* `tool_item` is the tool item (typically a vanilla **Brush**) which must be inserted into the machine and used by the worker
* `damage_chance` is the chance (range 0.0..1.0) that the inserted tool item will take a point of durability damage each time a work cycle is done. 
  * Undamageable items are permitted in recipes, and will never be damaged, regardless of the value of this field
  * If the item accepts the Unbreaking enchantment, this will further reduce the chance of the item taking durability damage
  * This mod also adds an unbreakable **Reinforced Brush** item (which has no recipe by default; define a recipe in your modpack if you want to allow it to be used)
* `output_items` - a list of records, each of which has an `item` itemstack, and a `chance` to be dropped.
  * A chance of 1.0 indicates the output is always produced
  * A chance of >1.0 indicates that extra output may be produced (e.g. 1.5 means produce one item, and a 50% chance of a second item)

### KubeJS support

The mod registers a KubeJS schema to allow for easy recipe creation via KubeJS.  Example:

```javascript
ServerEvents.recipes(event => {
  const ftbunearthed = event.recipes.ftbunearthed;
  
  // red sand -> 1 lapis, using an armorer worker token (any village type & level), and any brush as a tool
  ftbunearthed.unearther([ { "item": "lapis_lazuli", "chance": 1.0 } ], "minecraft:red_sand", { "profession": "armorer" }, "#c:tools/brush")
})
```

## Worker Tokens
Any inserted worker token item also has villager data in the same form as the `worker` field above, encoded in the `ftbunearthed:worker_data` item data component.

Worker Tokens may be obtained with the `/give` command, e.g. 

```
# Get a level 1 Mason token in the (default) Plains biome
/give @s ftbunearthed:worker_token[ftbunearthed:worker_data={profession:"mason"}]

# Get a level 5 Fletcher token in the Desert biome
/give @s ftbunearthed:worker_token[ftbunearthed:worker_data={type:"desert",profession:"fletcher",level:5}]
```

Note that the `level` and `type` fields are optional and default to 1 and Plains, respectively.

When placed into the Unearther machine, a villager worker appears, based on the token's data.

## Requirements

- [FTB Library](https://www.curseforge.com/minecraft/mc-mods/ftb-library-forge) is a required dependency

## Support

- For **Modpack** issues, please go here: https://go.ftb.team/support-modpack
- For **Mod** issues, please go here: https://go.ftb.team/support-mod-issues
- Just got a question? Check out our Discord: https://go.ftb.team/discord

## Licence

All Rights Reserved to Feed The Beast Ltd. Source code is `visible source`, please see our [LICENSE.md](/LICENSE.md) for more information. Any Pull Requests made to this mod must have the CLA (Contributor Licence Agreement) signed and agreed to before the request will be considered.

## Keep up to date

[![](https://cdn.feed-the-beast.com/assets/socials/icons/social-discord.webp)](https://go.ftb.team/discord) [![](https://cdn.feed-the-beast.com/assets/socials/icons/social-github.webp)](https://go.ftb.team/github) [![](https://cdn.feed-the-beast.com/assets/socials/icons/social-twitter-x.webp)](https://go.ftb.team/twitter) [![](https://cdn.feed-the-beast.com/assets/socials/icons/social-youtube.webp)](https://go.ftb.team/youtube) [![](https://cdn.feed-the-beast.com/assets/socials/icons/social-twitch.webp)](https://go.ftb.team/twitch) [![](https://cdn.feed-the-beast.com/assets/socials/icons/social-instagram.webp)](https://go.ftb.team/instagram) [![](https://cdn.feed-the-beast.com/assets/socials/icons/social-facebook.webp)](https://go.ftb.team/facebook) [![](https://cdn.feed-the-beast.com/assets/socials/icons/social-tiktok.webp)](https://go.ftb.team/tiktok)
