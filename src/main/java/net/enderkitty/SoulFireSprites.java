package net.enderkitty;

import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.sprite.SpriteId;

/** Vanilla keeps its own fire sprites as shared constants but has no soul equivalent, so we hold them here. */
public final class SoulFireSprites {
    public static final SpriteId FIRE_0 = Sheets.BLOCKS_MAPPER.defaultNamespaceApply("soul_fire_0");
    public static final SpriteId FIRE_1 = Sheets.BLOCKS_MAPPER.defaultNamespaceApply("soul_fire_1");

    private SoulFireSprites() {}
}
