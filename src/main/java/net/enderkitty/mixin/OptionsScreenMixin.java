package net.enderkitty.mixin;

import me.shedaniel.autoconfig.AutoConfig;
import net.enderkitty.FireHud;
import net.enderkitty.config.FireHudConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
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
    @Shadow @Final private ThreePartsLayoutWidget layout;
    @Shadow protected abstract void refreshWidgetPositions();
    
    @Unique private static final ButtonTextures TEXTURES = new ButtonTextures(
            Identifier.of(FireHud.MOD_ID, "widget/config_button"), Identifier.of(FireHud.MOD_ID, "widget/config_button_highlighted"));
    
    public OptionsScreenMixin(Text title) { super(title); }
    
    @Inject(method = "init", at = @At(value = "RETURN"))
    private void init(CallbackInfo ci) {
        FireHudConfig config = FireHud.getConfig();
        
        if (FireHud.isClothConfigLoaded() && config.configButtonInSettings) {
            ButtonWidget button = new TexturedButtonWidget(0, 0, 20, 20, TEXTURES, press -> {
                MinecraftClient.getInstance().setScreen(AutoConfig.getConfigScreen(FireHudConfig.class, this).get());
            });
            button.setTooltip(Tooltip.of(Text.translatable("tooltip.firehud.button.config")));
            
            GridWidget gridWidget = new GridWidget();
            gridWidget.getMainPositioner().marginX(4).marginBottom(4).alignHorizontalCenter();
            GridWidget.Adder adder = gridWidget.createAdder(2);
            
            adder.add(button);
            adder.add(EmptyWidget.ofWidth(config.configButtonX), 1);
            adder.add(EmptyWidget.ofHeight(config.configButtonY), 2);
            
            this.layout.addBody(gridWidget);
            this.layout.forEachChild(this::addDrawableChild);
            this.refreshWidgetPositions();
        }
    }
}
