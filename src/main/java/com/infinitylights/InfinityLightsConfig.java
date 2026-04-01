package com.infinitylights;

import net.minecraftforge.common.ForgeConfigSpec;

public class InfinityLightsConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.DoubleValue TORCH_DEPLETION_RISK;
    public static final ForgeConfigSpec.DoubleValue LANTERN_DEPLETION_RISK;
    public static final ForgeConfigSpec.BooleanValue TORCH_RECYCLABLE;
    public static final ForgeConfigSpec.BooleanValue LANTERN_RECYCLABLE;
    public static final ForgeConfigSpec.BooleanValue CONSOLE_MESSAGES;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("Daily depletion probabilities. 0.0 = 0%, 1.0 = 100%.");

        TORCH_DEPLETION_RISK = builder
                .comment("Probability per Minecraft day that one active torch depletes.")
                .defineInRange("TorchDepletionRisk", 0.0D, 0.0D, 1.0D);

        LANTERN_DEPLETION_RISK = builder
                .comment("Probability per Minecraft day that one active lantern depletes.")
                .defineInRange("LanternDepletionRisk", 0.0D, 0.0D, 1.0D);

        TORCH_RECYCLABLE = builder
                .comment("If true, a Depleted Glowstone Torch can be crafted into one stick.")
                .define("TorchRecyclable", false);

        LANTERN_RECYCLABLE = builder
                .comment("If true, a Depleted Glowstone Lantern can be crafted into eight iron nuggets.")
                .define("LanternRecyclable", false);

        CONSOLE_MESSAGES = builder
                .comment("If true, logs startup and depletion summaries to console (depletion logs only when at least one light depletes).")
                .define("ConsoleMessages", false);

        SPEC = builder.build();
    }

    private InfinityLightsConfig() {
    }

    public static boolean isEnabled(String key) {
        return switch (key) {
            case "TorchRecyclable" -> TORCH_RECYCLABLE.get();
            case "LanternRecyclable" -> LANTERN_RECYCLABLE.get();
            case "ConsoleMessages" -> CONSOLE_MESSAGES.get();
            default -> false;
        };
    }
}
