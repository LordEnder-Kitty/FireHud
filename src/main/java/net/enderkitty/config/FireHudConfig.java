package net.enderkitty.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "firehud")
public class FireHudConfig implements ConfigData {
    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.Gui.Tooltip
    public boolean renderVanillaHud = true;
    @ConfigEntry.Gui.Tooltip
    public float fireOpacity = 0.9f;
    @ConfigEntry.Gui.Tooltip
    public float firePos = 0.7f;
    @ConfigEntry.Gui.Tooltip
    public boolean sideFire = false;
    @ConfigEntry.Gui.Tooltip
    public boolean renderThirdPersonFire = true;
    @ConfigEntry.Gui.Tooltip
    public float fireVolume = 1.0f;
    @ConfigEntry.Gui.Tooltip
    public boolean applyFireVolRand = true;
    @ConfigEntry.Gui.Tooltip
    public float firePitch = 0.5f;
    @ConfigEntry.Gui.Tooltip
    public boolean applyFirePitchRand = true;
    
    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public VignetteOptions fireVignette = VignetteOptions.OFF;
    @ConfigEntry.Gui.Tooltip
    public float vignetteOpacity = 1.0f;
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(max = 4)
    public int vignetteScale = 0;
    
    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.Gui.Tooltip
    public boolean renderFireHearts = false;
    
    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.Gui.Tooltip
    public boolean fireScreenTint = false;
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.ColorPicker(allowAlpha = true)
    public int fireStartColor = 0;
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.ColorPicker(allowAlpha = true)
    public int fireEndColor = 1727987712;
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.ColorPicker(allowAlpha = true)
    public int soulFireStartColor = 0;
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.ColorPicker(allowAlpha = true)
    public int soulFireEndColor = 1711276287;
    
    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.Gui.Tooltip
    public boolean renderSoulFire = true;
    
    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.Gui.Tooltip
    public boolean renderFireInLava = true;
    @ConfigEntry.Gui.Tooltip
    public boolean renderThirdPersonFireInLava = true;
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public LavaFogOptions renderLavaFog = LavaFogOptions.VANILLA;
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(max = 100)
    public int lightFogDist = 50;
    
    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.Gui.Tooltip
    public boolean thermometer = false;
    @ConfigEntry.Gui.Tooltip
    public boolean onlyShowWhenOnFire = true;
    @ConfigEntry.Gui.Tooltip
    public boolean showFireTicks = false;
    @ConfigEntry.Gui.Tooltip
    public boolean onLeftSide = false;
    
    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.Gui.Tooltip
    public boolean renderWithFireResistance = true;
    @ConfigEntry.Gui.Tooltip
    public boolean displayFireResTimer = false;
    @ConfigEntry.Gui.Tooltip
    public boolean fireResTimerAsTicks = false;
    @ConfigEntry.Gui.Tooltip
    public int renderWithTimeLeft = 0;
    
    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.Gui.Tooltip
    public boolean configButtonInSettings = true;
    @ConfigEntry.Gui.Tooltip
    public int configButtonX = 328;
    @ConfigEntry.Gui.Tooltip
    public int configButtonY = 44;
    
    
    public enum VignetteOptions { OFF, FULL, UPPER, LOWER }
    public enum LavaFogOptions { VANILLA, LIGHT_FOG, NO_FOG }
}
