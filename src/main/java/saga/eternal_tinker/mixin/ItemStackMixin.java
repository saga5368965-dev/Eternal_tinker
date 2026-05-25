package saga.eternal_tinker.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import saga.eternal_tinker.Eternal_tinker;
import saga.eternal_tinker.item.UniversalModifiableItem;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Unique
    private static final String ORIGINAL_ITEM_TAG = Eternal_tinker.MODID + ".original_item";

    @Shadow
    public abstract CompoundTag getOrCreateTag();

    @Shadow
    public abstract boolean hasTag();

    @Inject(method = "getHoverName", at = @At("HEAD"), cancellable = true)
    private void onGetHoverName(CallbackInfoReturnable<net.minecraft.network.chat.Component> cir) {
        ItemStack stack = (ItemStack)(Object)this;
        if (stack.getItem() instanceof UniversalModifiableItem) {
            ItemStack original = UniversalModifiableItem.getOriginalItem(stack);
            if (!original.isEmpty()) {
                cir.setReturnValue(original.getHoverName());
            }
        }
    }

    /**
     * 元のアイテムのモデル情報を取得
     */
    @Unique
    public ItemStack getOriginalForRender() {
        ItemStack stack = (ItemStack)(Object)this;
        if (stack.getItem() instanceof UniversalModifiableItem) {
            return UniversalModifiableItem.getOriginalItem(stack);
        }
        return stack;
    }
}
