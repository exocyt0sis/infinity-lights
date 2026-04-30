package com.infinitylights;

import net.neoforged.neoforge.common.ModConfigSpec;

public class InfinityLightsConfig {
    public static final ModConfigSpec SPEC;
        public static final ModConfigSpec.BooleanValue CONSOLE_MESSAGES;
    public static final ModConfigSpec.DoubleValue GLOWSTONE_TORCH_DEPLETION_RISK;
    public static final ModConfigSpec.BooleanValue GLOWSTONE_TORCH_RECYCLABLE;
    public static final ModConfigSpec.DoubleValue GLOWSTONE_LANTERN_DEPLETION_RISK;
        public static final ModConfigSpec.DoubleValue GLOWSTONE_JACKOLANTERN_DEPLETION_RISK;
        public static final ModConfigSpec.BooleanValue LANTERN_RECYCLABLE;
    public static final ModConfigSpec.DoubleValue TORCH_BURNOUT_TIME;
    public static final ModConfigSpec.BooleanValue TORCH_SMOLDERED_BY_RAIN;
        public static final ModConfigSpec.DoubleValue CANDLE_BURNOUT_TIME;
        public static final ModConfigSpec.BooleanValue CANDLE_SMOLDERED_BY_RAIN;
    public static final ModConfigSpec.DoubleValue LANTERN_BURNOUT_TIME;
        public static final ModConfigSpec.DoubleValue JACKOLANTERN_BURNOUT_TIME;
    public static final ModConfigSpec.DoubleValue CAMPFIRE_BURNOUT_TIME;
    public static final ModConfigSpec.BooleanValue CAMPFIRE_SMOLDERED_BY_RAIN;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        CONSOLE_MESSAGES = builder
                .comment("General settings.")
                .comment("If true, logs startup and depletion summaries to console (depletion logs only when at least one light depletes).")
                .define("ConsoleMessages", false);

        GLOWSTONE_TORCH_DEPLETION_RISK = builder
                .comment("Glowstone torch settings.", "Daily depletion probability. 0.0 = 0%, 1.0 = 100%.")
                .comment("Probability per Minecraft day that one active glowstone torch depletes.")
                .defineInRange("GlowstoneTorchDepletionRisk", 0.01D, 0.0D, 1.0D);

        GLOWSTONE_TORCH_RECYCLABLE = builder
                .comment("If true, a depleted glowstone torch can be crafted into one stick.")
                .define("GlowstoneTorchRecyclable", true);

        GLOWSTONE_LANTERN_DEPLETION_RISK = builder
                .comment("Glowstone lantern settings.", "Daily depletion probability. 0.0 = 0%, 1.0 = 100%.")
                .comment("Probability per Minecraft day that one active glowstone lantern depletes.")
                .defineInRange("GlowstoneLanternDepletionRisk", 0.01D, 0.0D, 1.0D);

        GLOWSTONE_JACKOLANTERN_DEPLETION_RISK = builder
                .comment("Glowstone Jack o'Lantern settings.", "Daily depletion probability. 0.0 = 0%, 1.0 = 100%.")
                .comment("Probability per Minecraft day that one active glowstone Jack o'Lantern depletes.")
                .defineInRange("GlowstoneJackoLanternDepletionRisk", 0.01D, 0.0D, 1.0D);

        TORCH_BURNOUT_TIME = builder
                .comment("Vanilla torch settings.", "Burnout timing is measured in Minecraft days. 1.0 = one day, 0.5 = half a day, 0.0 disables automatic burnout.")
                .comment("How many Minecraft days a vanilla torch burns before becoming a depleted torch.")
                .defineInRange("TorchBurnoutTime", 4.0D, 0.0D, 3650.0D);

        TORCH_SMOLDERED_BY_RAIN = builder
                .comment("If true, rain reduces vanilla torch light output by 75% and emits subdued gray smolder particles.")
                .define("TorchSmolderedByRain", true);

        CANDLE_BURNOUT_TIME = builder
                .comment("Vanilla candle settings.", "Burnout timing is measured in Minecraft days. 1.0 = one day, 0.5 = half a day, 0.0 disables automatic burnout.")
                .comment("How many Minecraft days a vanilla candle burns before going out.")
                .defineInRange("CandleBurnoutTime", 1.0D, 0.0D, 3650.0D);

        CANDLE_SMOLDERED_BY_RAIN = builder
                .comment("If true, rain reduces vanilla candle light output by 75% and emits subdued gray smolder particles.")
                .define("CandleSmolderedByRain", true);

        LANTERN_BURNOUT_TIME = builder
                .comment("Vanilla lantern settings.", "Burnout timing is measured in Minecraft days. 1.0 = one day, 0.5 = half a day, 0.0 disables automatic burnout.")
                .comment("How many Minecraft days a vanilla lantern burns before becoming a depleted lantern.")
                .defineInRange("LanternBurnoutTime", 8.0D, 0.0D, 3650.0D);

        JACKOLANTERN_BURNOUT_TIME = builder
                .comment("Vanilla Jack o'Lantern settings.", "Burnout timing is measured in Minecraft days. 1.0 = one day, 0.5 = half a day, 0.0 disables automatic burnout.")
                .comment("How many Minecraft days a vanilla Jack o'Lantern burns before becoming a carved pumpkin.")
                .defineInRange("JackoLanternBurnoutTime", 4.0D, 0.0D, 3650.0D);

        LANTERN_RECYCLABLE = builder
                .comment("If true, a depleted lantern can be crafted into eight iron nuggets.")
                .define("LanternRecyclable", true);

        CAMPFIRE_BURNOUT_TIME = builder
                .comment("Vanilla campfire settings.", "Burnout timing is measured in Minecraft days. 1.0 = one day, 0.5 = half a day, 0.0 disables automatic burnout.")
                .comment("How many Minecraft days a vanilla campfire burns before burning out and disappearing.")
                .defineInRange("CampfireBurnoutTime", 1.0D, 0.0D, 3650.0D);

        CAMPFIRE_SMOLDERED_BY_RAIN = builder
                .comment("If true, rain reduces vanilla campfire light output by 75% and emits subdued gray smolder particles.")
                .define("CampfireSmolderedByRain", true);

        SPEC = builder.build();
    }

    private InfinityLightsConfig() {
    }

    public static boolean isEnabled(String key) {
        return switch (key) {
                        case "GlowstoneTorchRecyclable", "GlowstoneTOrchRecyclable", "TorchRecyclable" -> GLOWSTONE_TORCH_RECYCLABLE.get();
                        case "GlowstoneLanternRecyclable", "LanternRecyclable" -> LANTERN_RECYCLABLE.get();
            case "ConsoleMessages" -> CONSOLE_MESSAGES.get();
            default -> false;
        };
    }
}
