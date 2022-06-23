package jard.torony.mixin.context;

import jard.torony.AccessGameContext;
import jard.torony.GameContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.checkerframework.checker.units.qual.A;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/***
 *  ItemTooltipContextMixin
 *  TODO: Write a description for this file.
 *
 *  Created by jard at 03:46 on June 22, 2022.
 ***/
@Mixin (ItemStack.class)
public abstract class ItemTooltipContextMixin implements AccessGameContext {
    @Inject (method = "getTranslationKey", at = @At ("HEAD"))
    public void hookTranslationKey (CallbackInfoReturnable <String> info) {
        PlayerEntity player = MinecraftClient.getInstance ().player;

        if (player != null)
            setCurrentContext (GameContext.fromItemPlayer ((ItemStack) (Object) this, player.getUuid ()));
    }

    @Inject (method = "getTooltip", at = @At ("HEAD"))
    public void hookTooltip (@Nullable PlayerEntity player, TooltipContext context, CallbackInfoReturnable <List <Text>> info) {
        if (player != null)
            setCurrentContext (GameContext.fromItemPlayer ((ItemStack) (Object) this, player.getUuid ()));
    }
}
