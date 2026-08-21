package com.chasmet.recettemonsieurcuisine;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PortionMode {
    public static final String STANDARD = "standard";
    public static final String MEAT_180 = "meat180";
    public static final int MEAT_TOTAL_GRAMS = 180;
    private static final double FALLBACK_ORIGINAL_MEAT_GRAMS = 700.0;
    private static final Pattern LEADING_AMOUNT = Pattern.compile("^\\s*((?:\\d+\\s+)?\\d+\\s*/\\s*\\d+|\\d+(?:[.,]\\d+)?)\\s*(.*)$");
    private static final Pattern WEIGHT = Pattern.compile("(?i)(\\d+(?:[.,]\\d+)?|\\d+\\s*/\\s*\\d+)\\s*(kg|g|grammes?|kilogrammes?)\\b");
    private static final Pattern MULTIPLIED_WEIGHT = Pattern.compile("(?i)(\\d+(?:[.,]\\d+)?)\\s*[x×]\\s*(\\d+(?:[.,]\\d+)?)\\s*(kg|g)\\b");

    private PortionMode() {}

    public static boolean isMeatIngredient(String name) {
        String n = normalize(name);
        if (n.contains("bouillon") || n.contains("fond de") || n.contains("sauce") || n.contains("arome") || n.contains("epice")) return false;
        String[] keys = {
                "boeuf","poulet","dinde","veau","porc","agneau","mouton","canard","viande","steak",
                "jambon","saucisse","merguez","lapin","pintade","chorizo","lardon","bacon","coq",
                "chicken","beef","turkey","pork","lamb","sausage"
        };
        for (String k : keys) if (n.contains(normalize(k))) return true;
        return false;
    }

    public static boolean isMeatRecipe(Recipe recipe) {
        if (recipe == null) return false;
        for (Recipe.Ingredient ingredient : recipe.ingredients) if (isMeatIngredient(ingredient.name)) return true;
        return inferMeatName(recipe.title) != null;
    }

    public static List<Recipe.Ingredient> ingredientsForMode(Recipe recipe, String mode) {
        if (recipe == null || !MEAT_180.equals(mode) || !isMeatRecipe(recipe)) {
            return recipe == null ? new ArrayList<>() : recipe.ingredients;
        }

        List<Recipe.Ingredient> meat = new ArrayList<>();
        for (Recipe.Ingredient ingredient : recipe.ingredients) {
            if (isMeatIngredient(ingredient.name)) meat.add(ingredient);
        }

        List<Recipe.Ingredient> result = new ArrayList<>();
        if (meat.isEmpty()) {
            String inferred = inferMeatName(recipe.title);
            if (inferred != null) result.add(new Recipe.Ingredient(MEAT_TOTAL_GRAMS + " g", inferred));
            double factor = MEAT_TOTAL_GRAMS / FALLBACK_ORIGINAL_MEAT_GRAMS;
            for (Recipe.Ingredient ingredient : recipe.ingredients) {
                result.add(new Recipe.Ingredient(scaleAmount(ingredient.amount, factor), ingredient.name));
            }
            return result;
        }

        boolean allMeatMeasured = true;
        double measuredTotal = 0.0;
        for (Recipe.Ingredient ingredient : meat) {
            double grams = parseWeightGrams(ingredient.amount);
            if (grams <= 0) allMeatMeasured = false;
            else measuredTotal += grams;
        }
        double factor = allMeatMeasured && measuredTotal > 0
                ? MEAT_TOTAL_GRAMS / measuredTotal
                : MEAT_TOTAL_GRAMS / FALLBACK_ORIGINAL_MEAT_GRAMS;

        List<Integer> meatAllocations = allocateMeat(meat, allMeatMeasured, measuredTotal);
        int meatIndex = 0;
        for (Recipe.Ingredient ingredient : recipe.ingredients) {
            if (isMeatIngredient(ingredient.name)) {
                result.add(new Recipe.Ingredient(meatAllocations.get(meatIndex++) + " g", ingredient.name));
            } else {
                result.add(new Recipe.Ingredient(scaleAmount(ingredient.amount, factor), ingredient.name));
            }
        }
        return result;
    }

    public static String displayAmount(Recipe recipe, Recipe.Ingredient ingredient, String mode) {
        if (!MEAT_180.equals(mode) || recipe == null || ingredient == null) {
            return ingredient == null || ingredient.amount == null ? "" : ingredient.amount;
        }
        List<Recipe.Ingredient> adapted = ingredientsForMode(recipe, mode);
        int originalIndex = recipe.ingredients.indexOf(ingredient);
        if (originalIndex < 0) return ingredient.amount == null ? "" : ingredient.amount;
        int offset = adapted.size() == recipe.ingredients.size() + 1 ? 1 : 0;
        int adaptedIndex = originalIndex + offset;
        return adaptedIndex >= 0 && adaptedIndex < adapted.size() ? adapted.get(adaptedIndex).amount : ingredient.amount;
    }

    public static String displayIngredient(Recipe recipe, Recipe.Ingredient ingredient, String mode) {
        String amount = displayAmount(recipe, ingredient, mode);
        return amount == null || amount.trim().isEmpty() ? ingredient.name : amount + " • " + ingredient.name;
    }

    public static String adaptText(Recipe recipe, String text, String mode) {
        if (text == null || text.isEmpty() || recipe == null || !MEAT_180.equals(mode)) return text == null ? "" : text;
        String adaptedText = text;
        List<Recipe.Ingredient> adapted = ingredientsForMode(recipe, mode);
        int offset = adapted.size() == recipe.ingredients.size() + 1 ? 1 : 0;
        for (int i = 0; i < recipe.ingredients.size(); i++) {
            String originalAmount = recipe.ingredients.get(i).amount;
            int adaptedIndex = i + offset;
            if (originalAmount == null || originalAmount.trim().isEmpty() || adaptedIndex >= adapted.size()) continue;
            String newAmount = adapted.get(adaptedIndex).amount;
            if (newAmount != null && !newAmount.equals(originalAmount)) adaptedText = adaptedText.replace(originalAmount, newAmount);
        }
        return adaptedText;
    }

    private static List<Integer> allocateMeat(List<Recipe.Ingredient> meat, boolean measured, double measuredTotal) {
        List<Integer> allocations = new ArrayList<>();
        double shareTotal = 0.0;
        List<Double> shares = new ArrayList<>();
        for (Recipe.Ingredient ingredient : meat) {
            double share = measured ? parseWeightGrams(ingredient.amount) : parseLeadingNumber(ingredient.amount);
            if (share <= 0) share = 1.0;
            shares.add(share);
            shareTotal += share;
        }
        if (measured && measuredTotal > 0) shareTotal = measuredTotal;

        int remaining = MEAT_TOTAL_GRAMS;
        for (int i = 0; i < meat.size(); i++) {
            int grams;
            if (i == meat.size() - 1) grams = remaining;
            else {
                grams = (int) Math.round(MEAT_TOTAL_GRAMS * shares.get(i) / shareTotal);
                grams = Math.max(1, Math.min(remaining, grams));
            }
            allocations.add(grams);
            remaining -= grams;
        }
        return allocations;
    }

    private static double parseWeightGrams(String amount) {
        if (amount == null) return -1;
        String cleaned = amount.replace('\u00A0', ' ');
        Matcher multiplied = MULTIPLIED_WEIGHT.matcher(cleaned);
        if (multiplied.find()) {
            double count = parseDecimal(multiplied.group(1));
            double each = parseDecimal(multiplied.group(2));
            double grams = count * each;
            if (multiplied.group(3).toLowerCase(Locale.ROOT).startsWith("kg")) grams *= 1000.0;
            return grams;
        }
        Matcher matcher = WEIGHT.matcher(cleaned);
        if (!matcher.find()) return -1;
        double value = parseNumberToken(matcher.group(1));
        if (value <= 0) return -1;
        String unit = matcher.group(2).toLowerCase(Locale.ROOT);
        return (unit.startsWith("kg") || unit.startsWith("kilogram")) ? value * 1000.0 : value;
    }

    private static double parseLeadingNumber(String amount) {
        if (amount == null) return -1;
        Matcher matcher = LEADING_AMOUNT.matcher(amount.replace('\u00A0', ' '));
        return matcher.matches() ? parseNumberToken(matcher.group(1)) : -1;
    }

    private static String scaleAmount(String amount, double factor) {
        if (amount == null || amount.trim().isEmpty() || factor <= 0) return amount == null ? "" : amount;
        String cleaned = amount.replace('\u00A0', ' ');
        Matcher matcher = LEADING_AMOUNT.matcher(cleaned);
        if (!matcher.matches()) return amount;
        double value = parseNumberToken(matcher.group(1));
        if (value <= 0) return amount;
        String suffix = matcher.group(2) == null ? "" : matcher.group(2).trim();
        double scaled = value * factor;
        return formatScaledValue(scaled, suffix);
    }

    private static String formatScaledValue(double value, String suffix) {
        String normalizedSuffix = normalize(suffix);
        if (normalizedSuffix.matches("^(g|gramme|grammes)(\\b.*)?$")) {
            return formatPhysical(value, "g");
        }
        if (normalizedSuffix.matches("^(kg|kilogramme|kilogrammes)(\\b.*)?$")) {
            return formatPhysical(value * 1000.0, "g");
        }
        if (normalizedSuffix.matches("^(ml|millilitre|millilitres)(\\b.*)?$")) {
            return formatPhysical(value, "ml");
        }
        if (normalizedSuffix.matches("^(cl|centilitre|centilitres)(\\b.*)?$")) {
            return formatPhysical(value * 10.0, "ml");
        }
        if (normalizedSuffix.matches("^(l|litre|litres)(\\b.*)?$")) {
            return formatPhysical(value * 1000.0, "ml");
        }
        String number = formatKitchenFraction(value);
        return suffix.isEmpty() ? number : number + " " + suffix;
    }

    private static String formatPhysical(double value, String unit) {
        if (value <= 0) return "0 " + unit;
        if (value < 1.0) return formatDecimal(Math.max(0.1, Math.round(value * 10.0) / 10.0)) + " " + unit;
        return Math.max(1, (int) Math.round(value)) + " " + unit;
    }

    private static String formatKitchenFraction(double value) {
        if (value <= 0) return "0";
        if (value >= 8) return String.valueOf((int) Math.round(value));
        int eighths = Math.max(1, (int) Math.round(value * 8.0));
        int whole = eighths / 8;
        int remainder = eighths % 8;
        if (remainder == 0) return String.valueOf(whole);
        int gcd = gcd(remainder, 8);
        int numerator = remainder / gcd;
        int denominator = 8 / gcd;
        if (whole == 0) return numerator + "/" + denominator;
        return whole + " " + numerator + "/" + denominator;
    }

    private static double parseNumberToken(String token) {
        if (token == null) return -1;
        String t = token.trim().replace(',', '.').replaceAll("\\s+", " ");
        try {
            if (t.contains(" ") && t.contains("/")) {
                String[] parts = t.split(" ", 2);
                return Double.parseDouble(parts[0]) + parseFraction(parts[1]);
            }
            if (t.contains("/")) return parseFraction(t);
            return Double.parseDouble(t);
        } catch (Exception ignored) {
            return -1;
        }
    }

    private static double parseFraction(String token) {
        String[] parts = token.replace(" ", "").split("/");
        if (parts.length != 2) return -1;
        double numerator = Double.parseDouble(parts[0]);
        double denominator = Double.parseDouble(parts[1]);
        return denominator == 0 ? -1 : numerator / denominator;
    }

    private static double parseDecimal(String token) {
        try { return Double.parseDouble(token.replace(',', '.')); }
        catch (Exception ignored) { return -1; }
    }

    private static String formatDecimal(double value) {
        if (Math.abs(value - Math.rint(value)) < 0.0001) return String.valueOf((int) Math.rint(value));
        return String.format(Locale.FRANCE, "%.1f", value).replaceAll(",0$", "");
    }

    private static int gcd(int a, int b) {
        while (b != 0) { int t = a % b; a = b; b = t; }
        return Math.max(1, a);
    }

    private static String inferMeatName(String title) {
        String n = normalize(title);
        if (containsAny(n, "poulet", "chicken", "guinar", "coq")) return "poulet";
        if (containsAny(n, "boeuf", "beef", "steak")) return "bœuf";
        if (containsAny(n, "dinde", "turkey")) return "dinde";
        if (containsAny(n, "veau")) return "veau";
        if (containsAny(n, "porc", "pork", "filet mignon", "petit sale")) return "porc";
        if (containsAny(n, "agneau", "mouton", "lamb")) return "agneau";
        if (containsAny(n, "canard")) return "canard";
        if (containsAny(n, "jambon")) return "jambon";
        if (containsAny(n, "saucisse", "sausage", "currywurst")) return "saucisses";
        if (containsAny(n, "merguez")) return "merguez";
        if (containsAny(n, "chorizo")) return "chorizo";
        if (containsAny(n, "lardon", "bacon")) return "lardons";
        if (containsAny(n, "lapin")) return "lapin";
        if (containsAny(n, "pintade")) return "pintade";
        if (containsAny(n, "viande", "meat", "yapp")) return "viande";
        return null;
    }

    private static boolean containsAny(String text, String... values) {
        for (String value : values) if (text.contains(normalize(value))) return true;
        return false;
    }

    private static String normalize(String text) {
        if (text == null) return "";
        return Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }
}
