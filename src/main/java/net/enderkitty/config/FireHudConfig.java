package net.enderkitty.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "firehud")
public class FireHudConfig implements ConfigData {
    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.Gui.Tooltip
    public boolean renderVanillaHud = true;
    public float fireOpacity = 0.9f;
    public float firePos = 0.7f;
    @ConfigEntry.Gui.Tooltip
    public boolean sideFire = false;
    public boolean renderThirdPersonFire = true;
    public float fireVolume = 1.0f;
    @ConfigEntry.Gui.Tooltip
    public boolean applyFireVolRand = true;
    public float firePitch = 0.3f;
    @ConfigEntry.Gui.Tooltip
    public boolean applyFirePitchRand = true;
    
    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public VignetteOptions fireVignette = VignetteOptions.OFF;
    public float vignetteOpacity = 1.0f;
    @ConfigEntry.BoundedDiscrete(min = 1, max = 4)
    public int vignetteScale = 3;
    
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
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public LavaFogOptions renderLavaFog = LavaFogOptions.VANILLA;
    
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
    public boolean configButtonInSettings = true;
    public int configButtonX = 328;
    public int configButtonY = 44;
    
    
    public enum VignetteOptions { OFF, FULL, UPPER, LOWER }
    public enum LavaFogOptions {VANILLA, LIGHT_FOG, NO_FOG }
}
