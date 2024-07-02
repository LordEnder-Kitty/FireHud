package net.enderkitty;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public interface EnchantTags {
    TagKey<Enchantment> FROST_WALKER = EnchantTags.of("prevents_fire_hearts");
            
    private static TagKey<Enchantment> of(String id) {
        return TagKey.of(RegistryKeys.ENCHANTMENT, Identifier.of(FireHud.MOD_ID, id));
    }
}
