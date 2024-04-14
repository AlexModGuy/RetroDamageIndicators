package com.github.alexmodguy.retrodamageindicators;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class Config {
    public static final ForgeConfigSpec SPEC;
    public static final Config INSTANCE;

    static {
        final Pair<Config, ForgeConfigSpec> clientPair = new ForgeConfigSpec.Builder().configure(Config::new);
        SPEC = clientPair.getRight();
        INSTANCE = clientPair.getLeft();
    }

    public final ForgeConfigSpec.BooleanValue damageParticlesEnabled;
    public final ForgeConfigSpec.DoubleValue damageParticleSize;
    public final ForgeConfigSpec.BooleanValue damageParticleOutline;
    public final ForgeConfigSpec.BooleanValue hudIndicatorEnabled;
    public final ForgeConfigSpec.DoubleValue maxDistance;
    public final ForgeConfigSpec.BooleanValue colorblindHealthBar;
    public final ForgeConfigSpec.BooleanValue healthDecimals;
    public final ForgeConfigSpec.BooleanValue healthSeperator;
    public final ForgeConfigSpec.IntValue hudLingerTime;
    public final ForgeConfigSpec.DoubleValue hudIndicatorSize;
    public final ForgeConfigSpec.DoubleValue hudIndicatorBackgroundOpacity;
    public final ForgeConfigSpec.BooleanValue hudIndicatorAlignLeft;
    public final ForgeConfigSpec.BooleanValue hudIndicatorAlignTop;
    public final ForgeConfigSpec.IntValue hudIndicatorPositionX;
    public final ForgeConfigSpec.IntValue hudIndicatorPositionY;
    public final ForgeConfigSpec.DoubleValue hudEntitySize;
    public final ForgeConfigSpec.BooleanValue hudNameTextOutline;
    public final ForgeConfigSpec.BooleanValue hudHealthTextOutline;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> oldRenderEntities;

    public Config(final ForgeConfigSpec.Builder builder) {
        builder.push("damage-particles");
        damageParticlesEnabled = builder.comment("Whether the pop-up particles when a mob is injured or healed are enabled.").translation("damage_particles_enabled").define("damage_particles_enabled", true);
        damageParticleSize = builder.comment("The relative size of damage particles.").translation("damage_particle_size").defineInRange("damage_particle_size", 1.0D, 0.1D, 10.0D);
        damageParticleOutline = builder.comment("Whether the numbers that appear as pop-up particles are outlined in a darker color.").translation("damage_particle_outline").define("damage_particle_outline", true);
        builder.pop();
        builder.push("hud-indicator");
        hudIndicatorEnabled = builder.comment("Whether the hud damage indicator is enabled.").translation("hud_indicator_enabled").define("hud_indicator_enabled", true);
        maxDistance = builder.comment("How far away (in blocks) entities can be to appear in the hud health indicator").translation("max_distance").defineInRange("max_distance", 100D, 3D, 10000D);
        colorblindHealthBar = builder.comment("Whether health appears with a more visible yellow/black scheme.").translation("colorblind_health_bar").define("colorblind_health_bar", false);
        healthDecimals = builder.comment("Whether health appears with a decimal point.").translation("health_decimals").define("health_decimals", true);
        healthSeperator = builder.comment("Whether health appears appears as a | (true) or / (false).").translation("health_separator").define("health_separator", true);
        hudLingerTime = builder.comment("How long after mousing over an entity the hud damage indicator remains on screen, in game ticks.").translation("hud_linger_time").defineInRange("hud_linger_time", 30, 0, 1200);
        hudIndicatorSize = builder.comment("The relative size of hud indicator.").translation("hud_indicator_size").defineInRange("hud_indicator_size", 0.75D, 0.0D, 10.0D);
        hudIndicatorBackgroundOpacity = builder.comment("How opaque the background of the hud indicator is.").translation("hud_indicator_background_opacity").defineInRange("hud_indicator_background_opacity", 0.75D, 0.0D, 10.0D);
        hudIndicatorAlignLeft = builder.comment("True if the hud indicator appears on the left side of the screen, false for right.").translation("hud_indicator_align_left").define("hud_indicator_align_left", true);
        hudIndicatorAlignTop = builder.comment("True if the hud indicator appears on the top of the screen, false for bottom.").translation("hud_indicator_align_top").define("hud_indicator_align_top", true);
        hudIndicatorPositionX = builder.comment("How many pixels from the left side of the screen the hud indicator is.").translation("hud_indicator_position_x").defineInRange("hud_indicator_position_x", 10, Integer.MIN_VALUE, Integer.MAX_VALUE);
        hudIndicatorPositionY = builder.comment("How many pixels from the top of the screen the hud indicator is.").translation("hud_indicator_position_y").defineInRange("hud_indicator_position_y", 10, Integer.MIN_VALUE, Integer.MAX_VALUE);
        hudEntitySize = builder.comment("The size in pixels a usual entity should render as in the hud indicator.").translation("hud_entity_size").defineInRange("hud_entity_size", 38.0D, 0.0D, 2000.0D);
        hudNameTextOutline = builder.comment("Whether the name of the entity in the hud indicator should be outlined.").translation("hud_name_text_outline").define("hud_name_text_outline", false);
        hudHealthTextOutline = builder.comment("Whether the health of the entity in the hud indicator should be outlined.").translation("hud_health_text_outline").define("hud_health_text_outline", false);
        oldRenderEntities = builder.comment("List of all entity_types to just render as a model instead of with entity context. add to this if an entity is rendering strangely.").defineList("hud_old_render_entities", List.of("alexsmobs:giant_squid"), o -> o instanceof String);
        builder.pop();

    }
}
