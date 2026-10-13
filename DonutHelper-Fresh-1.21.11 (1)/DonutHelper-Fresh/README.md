# DonutHelper — Fabric 1.21.11

A client-side DonutSMP helper for Minecraft Java Edition 1.21.11. It provides:
- A HUD with your manually tracked balance, session earnings/hour, and goal progress.
- A profit calculator using item buy/sell prices you enter.
- A small local price list and manual sale tracker.

It does **not** generate money, automate gameplay, interact with server internals, or bypass server rules. Balance and prices are entered by the player, so hourly figures are estimates based on your entries.

## Requirements
- Windows 10/11
- Java JDK 21
- Gradle 9.x installed and available on PATH
- Minecraft Java 1.21.11 with Fabric Loader and Fabric API for 1.21.11

## Build the JAR
1. Install JDK 21 from https://adoptium.net/ and verify `java -version` and `javac -version`.
2. Install Gradle 9.x from https://gradle.org/install/ and verify `gradle --version`.
3. Extract this folder somewhere simple, e.g. `C:\MinecraftMods\DonutHelper`.
4. Double-click `build.bat`.
5. If the build succeeds, the mod file will be `build\libs\donuthelper-1.0.0.jar`.
6. Copy that JAR into `%APPDATA%\.minecraft\mods`.
7. Launch the Fabric profile for Minecraft 1.21.11. Fabric API must also be in the mods folder.

The first build needs internet access so Gradle can download Minecraft/Fabric dependencies.

## Commands
Enter these in Minecraft chat:

- `/moneyhud balance <amount>` — enter your current in-game balance. The first entry sets the baseline; later entries track positive balance changes as session earnings.
- `/moneyhud goal <amount>` — set a balance goal (default $50,000,000).
- `/moneyhud price <item> <buy-price> <sell-price>` — save a market price, e.g. `/moneyhud price diamond 1200 1600`.
- `/moneyhud prices` — list saved prices and profit per item.
- `/moneyhud sale <item> <quantity>` — estimate revenue/profit using the saved sell and buy prices.
- `/moneyhud reset` — reset session earnings and the balance baseline.

Prices and tracking data are saved locally in `.minecraft/config/donuthelper.properties`.

## Notes
- This project targets Minecraft 1.21.11 and Java 21 using Fabric's official Mojang mappings.
- The project source is provided; a precompiled JAR is not included because this environment cannot download the required Fabric/Gradle build dependencies. Run `build.bat` on your Windows PC to produce the actual mod JAR.
