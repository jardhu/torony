package jard.torony.mixin.context;

import jard.torony.AccessGameContext;
import jard.torony.GameContext;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/***
 *  RenderArmorContextMixin
 *  Sets the appropriate current GameContext when rendering armor features.
 *
 *  Created by jard at 22:24 on May 29, 2022.
 ***/
@Mixin(ArmorFeatureRenderer.class)
public abstract class RenderArmorContextMixin extends FeatureRenderer <LivingEntity, BipedEntityModel <LivingEntity>> implements AccessGameContext {
    public RenderArmorContextMixin (FeatureRendererContext <LivingEntity, BipedEntityModel <LivingEntity>> context) {
        super (context);
    }

    @Inject (method = "renderArmor", at = @At ("HEAD"))
    public void hookGameContext (MatrixStack matrices, VertexConsumerProvider vertexConsumers, LivingEntity entity, EquipmentSlot armorSlot, int light, BipedEntityModel <LivingEntity> model, CallbackInfo info) {
        ItemStack item = entity.getEquippedStack (armorSlot);
        setCurrentContext (GameContext.fromItemPlayer (item, entity.getUuid ()));
    }
}
