package net.enderkitty;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.enchantment.Enchantment;

public interface EnchantTags {
    TagKey<Enchantment> PREVENTS_FIRE_HEARTS = TagKey.create(Registries.ENCHANTMENT, FireHud.id("prevents_fire_hearts"));
}
