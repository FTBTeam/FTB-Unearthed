package dev.ftb.mods.ftbunearthed.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.ftb.mods.ftbunearthed.util.ManualBrushing;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BrushItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BrushItem.class)
public class BrushItemMixin {
    @Inject(method = "onUseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;"), cancellable = true)
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration, CallbackInfo ci, @Local BlockHitResult blockHitResult) {
        if (ManualBrushing.tryManualBrushing(level, livingEntity, blockHitResult)) {
            ci.cancel();
        }
    }
}
