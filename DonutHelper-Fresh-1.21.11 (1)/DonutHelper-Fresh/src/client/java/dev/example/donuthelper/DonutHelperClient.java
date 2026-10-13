package dev.example.donuthelper;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public final class DonutHelperClient implements ClientModInitializer {
    public static final String MOD_ID = "donuthelper";

    @Override
    public void onInitializeClient() {
        Tracker.load();
        registerCommands();
        HudElementRegistry.addLast(
            Identifier.fromNamespaceAndPath(MOD_ID, "money_hud"),
            (graphics, deltaTracker) -> {
                Minecraft client = Minecraft.getInstance();
                if (client.player == null || client.options.hideGui) return;

                int x = 8;
                int y = 8;
                graphics.fill(x, y, x + 236, y + 65, 0xA9000000);
                graphics.text(client.font, "DonutHelper", x + 7, y + 6, 0xFFFFD166, true);
                graphics.text(client.font, "Balance: " + Tracker.money(Tracker.balance), x + 7, y + 19, 0xFFFFFFFF, true);
                graphics.text(client.font, "Session: +" + Tracker.money(Tracker.sessionEarned) +
                    "  |  Est/hr: " + Tracker.money(Tracker.hourlyRate()), x + 7, y + 32, 0xFF8FF0A4, true);
                double progress = Tracker.goal <= 0 ? 0 : Math.min(1.0, Tracker.balance / Tracker.goal);
                graphics.text(client.font, "Goal " + Tracker.money(Tracker.goal) + ": " +
                    String.format(java.util.Locale.US, "%.1f%%", progress * 100), x + 7, y + 45, 0xFFB8D8FF, true);
            }
        );
    }

    private static void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            dispatcher.register(literal("moneyhud")
                .then(literal("balance")
                    .then(argument("amount", DoubleArgumentType.doubleArg(0))
                        .executes(ctx -> {
                            double next = DoubleArgumentType.getDouble(ctx, "amount");
                            if (Tracker.hasBalanceBaseline && next > Tracker.balance) {
                                Tracker.sessionEarned += next - Tracker.balance;
                            }
                            Tracker.balance = next;
                            Tracker.hasBalanceBaseline = true;
                            Tracker.save();
                            ctx.getSource().sendFeedback(Component.literal(
                                "Balance recorded: " + Tracker.money(next) +
                                ". Session earnings: " + Tracker.money(Tracker.sessionEarned)));
                            return 1;
                        })))
                .then(literal("goal")
                    .then(argument("amount", DoubleArgumentType.doubleArg(1))
                        .executes(ctx -> {
                            Tracker.goal = DoubleArgumentType.getDouble(ctx, "amount");
                            Tracker.save();
                            ctx.getSource().sendFeedback(Component.literal("Goal set to " + Tracker.money(Tracker.goal)));
                            return 1;
                        })))
                .then(literal("price")
                    .then(argument("item", StringArgumentType.word())
                        .then(argument("buy", DoubleArgumentType.doubleArg(0))
                            .then(argument("sell", DoubleArgumentType.doubleArg(0))
                                .executes(ctx -> {
                                    String item = StringArgumentType.getString(ctx, "item").toLowerCase(java.util.Locale.ROOT);
                                    double buy = DoubleArgumentType.getDouble(ctx, "buy");
                                    double sell = DoubleArgumentType.getDouble(ctx, "sell");
                                    Tracker.prices.put(item, new Tracker.Price(buy, sell));
                                    Tracker.save();
                                    double margin = sell - buy;
                                    double pct = buy <= 0 ? 0 : margin / buy * 100.0;
                                    ctx.getSource().sendFeedback(Component.literal(
                                        item + ": buy " + Tracker.money(buy) + ", sell " + Tracker.money(sell) +
                                        ", profit/item " + Tracker.money(margin) +
                                        String.format(java.util.Locale.US, " (%.1f%% margin)", pct)));
                                    return 1;
                                })))))
                .then(literal("prices").executes(ctx -> {
                    if (Tracker.prices.isEmpty()) {
                        ctx.getSource().sendFeedback(Component.literal("No prices saved. Use /moneyhud price <item> <buy> <sell>."));
                    } else {
                        ctx.getSource().sendFeedback(Component.literal("Saved prices (profit per item):"));
                        Tracker.prices.forEach((item, p) -> ctx.getSource().sendFeedback(Component.literal(
                            item + ": buy " + Tracker.money(p.buy) + " / sell " + Tracker.money(p.sell) +
                            " / profit " + Tracker.money(p.sell - p.buy))));
                    }
                    return 1;
                }))
                .then(literal("sale")
                    .then(argument("item", StringArgumentType.word())
                        .then(argument("quantity", IntegerArgumentType.integer(1))
                            .executes(ctx -> {
                                String item = StringArgumentType.getString(ctx, "item").toLowerCase(java.util.Locale.ROOT);
                                int quantity = IntegerArgumentType.getInteger(ctx, "quantity");
                                Tracker.Price p = Tracker.prices.get(item);
                                if (p == null) {
                                    ctx.getSource().sendFeedback(Component.literal("No price for '" + item + "'. Add it with /moneyhud price " + item + " <buy> <sell>."));
                                    return 0;
                                }
                                double revenue = p.sell * quantity;
                                double profit = (p.sell - p.buy) * quantity;
                                Tracker.estimatedSales += revenue;
                                Tracker.estimatedProfit += profit;
                                Tracker.save();
                                ctx.getSource().sendFeedback(Component.literal(
                                    "Estimated sale: revenue " + Tracker.money(revenue) + ", profit " + Tracker.money(profit) +
                                    ". Session sales " + Tracker.money(Tracker.estimatedSales) +
                                    ", estimated profit " + Tracker.money(Tracker.estimatedProfit) + "."));
                                return 1;
                            }))))
                .then(literal("reset").executes(ctx -> {
                    Tracker.sessionEarned = 0;
                    Tracker.sessionStartedAt = System.currentTimeMillis();
                    Tracker.hasBalanceBaseline = false;
                    Tracker.save();
                    ctx.getSource().sendFeedback(Component.literal("Session earnings reset. Record your balance with /moneyhud balance <amount>."));
                    return 1;
                }))
            )
        );
    }
}
