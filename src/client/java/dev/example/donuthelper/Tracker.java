package dev.example.donuthelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import net.fabricmc.loader.api.FabricLoader;

final class Tracker {
    static double balance = 0;
    static double goal = 50_000_000;
    static double sessionEarned = 0;
    static double estimatedSales = 0;
    static double estimatedProfit = 0;
    static boolean hasBalanceBaseline = false;
    static long sessionStartedAt = System.currentTimeMillis();
    static final Map<String, Price> prices = new LinkedHashMap<>();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("donuthelper.properties");

    record Price(double buy, double sell) {}

    static double hourlyRate() {
        double hours = Math.max(1.0 / 60.0, (System.currentTimeMillis() - sessionStartedAt) / 3_600_000.0);
        return sessionEarned / hours;
    }

    static String money(double amount) {
        return "$" + String.format(Locale.US, "%,.0f", amount);
    }

    static void load() {
        if (!Files.exists(FILE)) return;
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(FILE)) {
            p.load(in);
            balance = parse(p, "balance", 0);
            goal = parse(p, "goal", 50_000_000);
            sessionEarned = parse(p, "sessionEarned", 0);
            estimatedSales = parse(p, "estimatedSales", 0);
            estimatedProfit = parse(p, "estimatedProfit", 0);
            hasBalanceBaseline = Boolean.parseBoolean(p.getProperty("hasBalanceBaseline", "false"));
            sessionStartedAt = parseLong(p, "sessionStartedAt", System.currentTimeMillis());
            for (String key : p.stringPropertyNames()) {
                if (key.startsWith("price.")) {
                    String item = key.substring("price.".length());
                    String[] values = p.getProperty(key, "").split(",", -1);
                    if (values.length == 2) {
                        try { prices.put(item, new Price(Double.parseDouble(values[0]), Double.parseDouble(values[1]))); }
                        catch (NumberFormatException ignored) {}
                    }
                }
            }
        } catch (IOException ignored) {
        }
    }

    static void save() {
        Properties p = new Properties();
        p.setProperty("balance", Double.toString(balance));
        p.setProperty("goal", Double.toString(goal));
        p.setProperty("sessionEarned", Double.toString(sessionEarned));
        p.setProperty("estimatedSales", Double.toString(estimatedSales));
        p.setProperty("estimatedProfit", Double.toString(estimatedProfit));
        p.setProperty("hasBalanceBaseline", Boolean.toString(hasBalanceBaseline));
        p.setProperty("sessionStartedAt", Long.toString(sessionStartedAt));
        prices.forEach((item, price) -> p.setProperty("price." + item, price.buy + "," + price.sell));
        try {
            Files.createDirectories(FILE.getParent());
            try (OutputStream out = Files.newOutputStream(FILE)) {
                p.store(out, "DonutHelper local tracker data");
            }
        } catch (IOException ignored) {
        }
    }

    private static double parse(Properties p, String key, double fallback) {
        try { return Double.parseDouble(p.getProperty(key, Double.toString(fallback))); }
        catch (NumberFormatException ignored) { return fallback; }
    }

    private static long parseLong(Properties p, String key, long fallback) {
        try { return Long.parseLong(p.getProperty(key, Long.toString(fallback))); }
        catch (NumberFormatException ignored) { return fallback; }
    }

    private Tracker() {}
}
