# Linear [![](http://cf.way2muchnoise.eu/linear.svg)](https://minecraft.curseforge.com/projects/linear) [![](http://cf.way2muchnoise.eu/versions/linear.svg)](https://minecraft.curseforge.com/projects/linear)

Place lines, walls, circles and spheres of blocks by dragging from one block to another. With
multi-placing on (`V`), sneak, hold a block, press right-click at the start and release it at the end.
`G` switches between lines along one, two or three axes, walls along two or three, a circle and a
sphere. Circles and spheres are hollow, centred on the start and pass through the end.

Linear 2 runs on NeoForge, Fabric and Forge for Minecraft 1.21.1.

## Configuration

`linear-server` sets everyone's limits and build modes; `/linear` (op level 2) overrides them per player:

```
/linear mode enable|disable <players> <mode|all>
/linear mode reset <players>
/linear limits <players> maxBlocks|maxDistance <value>
/linear limits <players> reach|midair survival|creative <value>
/linear limits reset <players>
/linear info <player>
```

`/linear mode enable` shows the player an unlock toast, so a datapack function can hand out build modes
as progress. Blocks in the `#linear:blacklist` item tag can never be placed in a line; with
`useWhitelist` on, only blocks in `#linear:whitelist` can.

## For mod authors

Linear's API is in the `tschipp.linear.api` package of the common jar and works the same on every loader:

- `BlockSupply` - more of a block than the player carries, for example from a bag. Asked after the
  player's own inventory.
- `HeldItemHandler` - an item that is not a block but hands out blocks, so it can draw lines too.

Register both through `LinearApi`, guarded by a check that Linear is installed.

```
repositories {
	maven {
		url "https://maven.blamejared.com/"
	}
}

dependencies {
	compileOnly "tschipp.linear:linear-common-1.21.1:VERSION"
}
```
