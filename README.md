# Quiet Air

A tiny server-side Fabric mod that hides this console message:

```
Player <name> standing on air - force-sending blocks below
```

Vanilla logs it whenever a player appears to be standing on a block the server thinks is air (boats, carpets, slab edges, lag). The message is harmless, but on a busy server it floods the console.

Quiet Air only removes the log line. The server still resends the blocks, so gameplay doesn't change.

## Compatibility

- Minecraft 26.3, Fabric Loader 0.19.5+, Java 25
- Server-side only.

## Building

```
./gradlew build
```

The mod jar is `build/libs/quiet-air-<version>.jar` (not the `-sources` jar).

## License

MIT. See [LICENSE](LICENSE).
