# Villager Decorum

By Jaubert.

Villagers sit on cushions by their workstation, near their bed and around the bell.

- At work, a villager sits on a cushion placed in front of or beside its workstation (touching it, not diagonally), faces the workstation and keeps working from there.
- In free time, or when it has no job, it sits on cushions within 5 blocks of its bed.
- At gathering time, it sits on cushions within 10 blocks of the bell.
- Each time it stays seated for 30 to 60 seconds, then gets up and walks around before sitting again.
- Baby villagers don't sit.
- Cushions on the low step of stairs work as benches: near beds and the bell, villagers sit facing away from the back of the stairs.
- The cushions are the ones already in the game, crafted from 3 wool slabs.
- Works together with Villager Manners.
  https://github.com/jaubertt/Villager_Manners

## Settings

Each place has its own switch, all on by default. With Mod Menu installed, open Mods, select Villager Decorum and click the settings button.

| Switch | Villagers sit on cushions |
| --- | --- |
| Workstations | Touching their workstation, while they work |
| Beds | Within 5 blocks of their bed, in free time or when they have no job |
| Bells | Within 10 blocks of the bell, at gathering time |

Settings are saved in config/villager_decorum.json. On a dedicated server, edit that file while the server is stopped.

If you remove the mod, villagers that were sitting stay on their cushions until the cushion is broken.

The server needs the mod for villagers to sit. Players who also install it see them sitting; players without it see them standing on the cushion.

## Requirements

- Fabric API
  https://modrinth.com/mod/fabric-api
- Optional: Mod Menu, for the settings button
  https://modrinth.com/mod/modmenu

## Building

The jar is built by Gradle into build/libs:

```
./gradlew build
```

## Project layout

- src/main: everything that also runs on servers (sitting on cushions and the config).
- src/client: the sitting pose and the settings screen.
- libs/modmenu-api-stub.jar: two interfaces of Mod Menu's API, used only to compile. They are left out of every jar; the real ones come from Mod Menu when it is installed. Mod Menu is MIT licensed (libs/MODMENU-LICENSE.txt).

## Issues

Report bugs and ideas here:
https://github.com/jaubertt/Villager_Decorum/issues

## License

MIT (see LICENSE).
