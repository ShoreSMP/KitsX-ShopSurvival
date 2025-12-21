KitsX Shop Survival (custom kit fork)

This fork of KitsX centers on *custom kit editing sessions*. Players use `/customkit` to open a GUI that stores their temporary kit inventory while they flow between the kit room, premade kits, and the editor without losing progress.

## Highlights

- `/customkit` is the only exposed command, guiding players toward a shared GUI workflow instead of command spam.
- Editing locks movement, container access, block placement, entity interactions, and armor-stand manipulation until the player either imports the kit or cancels with `/kitcancel`.
- Only filter/back-button slots are blocked in the editor; kit slots stay editable so players can rearrange items freely while building a kit.
- Premade kits, the kit room, and the editor all update one session, ensuring your kit snapshot persists between menus.

## Skript Support

```sk
on kit load:
  send "You loaded %kit%"
```

```sk
on kit save:
  send "You saved %kit%"
```

```sk
on kitroom open:
  send "You opened the kitroom"
```

## Developer API

### Maven

```xml
        <repository>
            <id>xyris-repo</id>
            <url>https://xyris.fun/repo/</url>
        </repository>

        <dependency>
            <groupId>dev.darkxx</groupId>
            <artifactId>KitsX</artifactId>
            <version>1.0.0</version>
            <scope>provided</scope>
        </dependency>
```

### Gradle

```groovy
repositories {
    maven("https://xyris.fun/repo/")
}

dependencies {
    compileOnly("dev.darkxx:KitsX:1.0.0")
}
```

---

First, initialize the KitsApiProvider in the `onEnable`

```java
        KitsApiProvider.init(this);
```

Next, get current instance of the KitsX api with the following:

```java
        KitsAPI kits = KitsApiProvider.get().getKitsAPI();
```

Here are some usage examples:

```java
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        
        // Load the kit for the player
        KitsAPI kits = KitsApiProvider.get().getKitsAPI();
        kits.load(p, "Kit 1");
    }

    @EventHandler
    public void onKitLoad(KitLoadEvent event) {
        Player player = e.getPlayer();
        String kitName = event.getKitName();
        
        // Send the player a message if the kit name equals to "Kit 1"
        if (kitName.equals("Kit 1")) {
            player.sendMessage("Hello, " + player.getName() + "! You loaded " + kitName + "!");
        }
    }
```