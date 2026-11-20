# Changelog

## [2.0.0] - 2026-11-20

Rewritten from scratch for Minecraft 1.21.1.

- Now a multiloader mod: NeoForge, Fabric and Forge are supported
- New build modes: Circle (2 axes) and Sphere (3 axes). Both are hollow, centred on the block you
  start on, and pass through the block you end on. A circle lies flat when you drag along the ground
  and stands up when you drag upwards.
- The build mode indicator hides while the chat is open.
- New configs: `linear-server` sets everyone's limits (max blocks 128, max distance 20, reach 5 in
  survival and 16 in creative, midair placement in creative only) and which build modes they have.
  `linear-client` positions or hides the build mode indicator.
- The block blacklist and whitelist are now the item tags `#linear:blacklist` (chests and trapped
  chests by default) and `#linear:whitelist`. Turn on `useWhitelist` to use the whitelist.
- CraftTweaker support was replaced by the `/linear` command (op level 2), which overrides build modes
  and limits per player. `/linear mode enable` shows the player an unlock toast, so datapack functions
  can hand out build modes as progression. `/linear info` shows a player's current settings.
- New API for other mods in `tschipp.linear.api`, the same on every loader: `BlockSupply` provides
  extra blocks for a line, and `HeldItemHandler` lets items that aren't blocks draw lines. It replaces
  the old Forge events and `LinearHooks.registerDraggable`. The common jar is on maven.blamejared.com
  as `tschipp.linear:linear-common-1.21.1`.
