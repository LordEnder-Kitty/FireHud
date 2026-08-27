package net.enderkitty.mixin;

import net.enderkitty.FireHud;
import net.enderkitty.config.FireHudConfig;
import net.enderkitty.config.FireHudConfigScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(OptionsScreen.class)
public abstract class OptionsScreenMixin extends Screen {
    @Shadow @Final private HeaderAndFooterLayout layout;
    @Shadow protected abstract void repositionElements();
    
    @Unique private static final WidgetSprites TEXTURES = new WidgetSprites(
            Identifier.fromNamespaceAndPath(FireHud.MOD_ID, "widget/config_button"), Identifier.fromNamespaceAndPath(FireHud.MOD_ID, "widget/config_button_highlighted"));
    
    public OptionsScreenMixin(Component title) { super(title); }
    
    @Inject(method = "init", at = @At(value = "RETURN"))
    private void init(CallbackInfo ci) {
        FireHudConfig config = FireHud.getConfig();
        
        if (FireHud.isClothConfigLoaded() && config.configButtonInSettings) {
            Button button = new ImageButton(0, 0, 20, 20, TEXTURES, press -> {
                Minecraft.getInstance().gui.setScreen(FireHudConfigScreen.create(this));
            });
            button.setTooltip(Tooltip.create(Component.translatable("tooltip.firehud.button.config")));
            
            GridLayout gridLayout = new GridLayout();
            gridLayout.defaultCellSetting().paddingHorizontal(4).paddingBottom(4).alignHorizontallyCenter();
            GridLayout.RowHelper adder = gridLayout.createRowHelper(2);
            
            adder.addChild(button);
            adder.addChild(SpacerElement.width(config.configButtonX), 1);
            adder.addChild(SpacerElement.height(config.configButtonY), 2);
            
            // Only the grid: vanilla's own init already registered every header and footer widget, and
            // Screen#addRenderableWidget does not deduplicate
            this.layout.addToContents(gridLayout);
            gridLayout.visitWidgets(this::addRenderableWidget);
            this.repositionElements();
        }
    }
}
