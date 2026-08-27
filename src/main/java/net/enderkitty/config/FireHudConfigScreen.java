package net.enderkitty.config;

import me.shedaniel.autoconfig.AutoConfigClient;
import me.shedaniel.clothconfig2.api.AbstractConfigEntry;
import me.shedaniel.clothconfig2.gui.AbstractConfigScreen;
import me.shedaniel.clothconfig2.gui.entries.TooltipListEntry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class FireHudConfigScreen {
    // Cloth wraps tooltips to the full screen width, so a long one renders as a single line
    // covering the whole row it belongs to, value widget included
    private static final int MAX_TOOLTIP_WIDTH = 220;
    
    public static Screen create(Screen parent) {
        Screen screen = AutoConfigClient.getConfigScreen(FireHudConfig.class, parent).get();
        
        if (screen instanceof AbstractConfigScreen configScreen) {
            for (List<AbstractConfigEntry<?>> entries : configScreen.getCategorizedEntries().values()) {
                for (AbstractConfigEntry<?> entry : entries) {
                    if (entry instanceof TooltipListEntry<?> tooltipEntry) narrowTooltip(tooltipEntry, configScreen);
                }
            }
        }
        
        return screen;
    }
    
    private static void narrowTooltip(TooltipListEntry<?> entry, Screen screen) {
        Supplier<Optional<Component[]>> tooltip = entry.getTooltipSupplier();
        if (tooltip == null) return;
        
        entry.setTooltipSupplier(() -> tooltip.get().map(lines -> wrap(lines, Math.min(MAX_TOOLTIP_WIDTH, screen.width / 2))));
    }
    
    private static Component[] wrap(Component[] lines, int width) {
        Font font = Minecraft.getInstance().font;
        List<Component> wrapped = new ArrayList<>();
        
        for (Component line : lines) {
            if (font.width(line) <= width) {
                wrapped.add(line);
                continue;
            }
            for (FormattedText part : font.splitIgnoringLanguage(line, width)) {
                wrapped.add(toComponent(part));
            }
        }
        
        return wrapped.toArray(Component[]::new);
    }
    
    private static Component toComponent(FormattedText text) {
        MutableComponent component = Component.empty();
        text.visit((style, string) -> {
            component.append(Component.literal(string).setStyle(style));
            return Optional.empty();
        }, Style.EMPTY);
        return component;
    }
}
