package net.enderkitty.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.enderkitty.FireHud;
import net.minecraft.client.gui.screens.Screen;

public class ModMenuImpl implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return (Screen parent) -> FireHud.isClothConfigLoaded() ? FireHudConfigScreen.create(parent) : null;
    }
}
