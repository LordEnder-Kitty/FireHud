package net.enderkitty.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.autoconfig.AutoConfig;
import net.enderkitty.FireHud;
import net.minecraft.client.gui.screen.Screen;

public class ModMenuImpl implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return (Screen parent) -> FireHud.isClothConfigLoaded() ? AutoConfig.getConfigScreen(FireHudConfig.class, parent).get() : null;
    }
}
