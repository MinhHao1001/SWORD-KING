package com.example.kingsword;

import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class KingSwordMod implements ModInitializer {
    public static final String MOD_ID = "kingsword";

    public static final Item KING_SWORD = new KingSwordItem(
            Tiers.NETHERITE, 
            new Item.Properties()
                    .attributes(SwordItem.createAttributes(Tiers.NETHERITE, 10, -2.4F))
                    .durability(2500)
    );

    @Override
    public void onInitialize() {
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, "king_sword"), KING_SWORD);
    }

    public static class KingSwordItem extends SwordItem {
        public KingSwordItem(Tiers tier, Properties properties) {
            super(tier, properties);
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            ItemStack itemStack = player.getItemInHand(hand);

            if (player.getHealth() > 5.0F) {
                player.hurt(level.damageSources().magic(), 5.0F);

                if (!level.isClientSide()) {
                    AABB searchArea = player.getBoundingBox().inflate(7.0D, 3.0D, 7.0D);
                    List<Entity> targets = level.getEntities(player, searchArea, 
                            entity -> entity instanceof LivingEntity && entity != player);

                    for (Entity target : targets) {
                        double dx = target.getX() - player.getX();
                        double dz = target.getZ() - player.getZ();
                        target.setDeltaMovement(dx * 0.3D, 0.85D, dz * 0.3D);
                        target.hasImpulse = true;

                        ((LivingEntity) target).hurt(level.damageSources().playerAttack(player), 12.0F);
                    }

                    level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLOSION.value(), SoundSource.PLAYERS, 1.0F, 0.5F);
                }

                player.getCooldowns().addCooldown(this, 60);
                return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
            } else if (!level.isClientSide()) {
                player.sendSystemMessage(Component.literal("§cBạn không đủ máu (cần hơn 5 máu) để dùng kỹ năng!"));
            }

            return InteractionResultHolder.pass(itemStack);
        }
    }
}
