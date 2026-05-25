package saga.eternal_tinker.recipe;

import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import saga.eternal_tinker.item.ModItems;
import saga.eternal_tinker.item.UniversalModifiableItem;

import javax.annotation.Nullable;

/**
 * 任意のツール・武器を作業台でUniversalModifiableItemへコンバートするレシピ
 */
public class SmithingTinkerRecipe implements CraftingRecipe {

    private final ResourceLocation id;
    private final Ingredient base;
    private final Ingredient addition;

    public SmithingTinkerRecipe(ResourceLocation id, Ingredient base, Ingredient addition) {
        this.id = id;
        this.base = base;
        this.addition = addition;
    }

    @Override
    public boolean matches(CraftingContainer inv, @NotNull Level level) {
        ItemStack baseStack = ItemStack.EMPTY;
        ItemStack additionStack = ItemStack.EMPTY;
        boolean foundBase = false;
        boolean foundAddition = false;

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) {
                if (!foundBase && this.base.test(stack) && !(stack.getItem() instanceof UniversalModifiableItem)) {
                    baseStack = stack;
                    foundBase = true;
                } else if (!foundAddition && this.addition.test(stack)) {
                    additionStack = stack;
                    foundAddition = true;
                } else {
                    return false;
                }
            }
        }

        return foundBase && foundAddition;
    }

    @Override
    public @NotNull ItemStack assemble(CraftingContainer inv, RegistryAccess registryAccess) {
        ItemStack originalStack = ItemStack.EMPTY;

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && this.base.test(stack) && !(stack.getItem() instanceof UniversalModifiableItem)) {
                originalStack = stack.copy();
                originalStack.setCount(1);
                break;
            }
        }

        return UniversalModifiableItem.convertAnyToolToTiC(originalStack, ModItems.UNIVERSAL_TIC_ITEM.get());
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public @NotNull ItemStack getResultItem(RegistryAccess registryAccess) {
        return new ItemStack(ModItems.UNIVERSAL_TIC_ITEM.get());
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    @Override
    public CraftingBookCategory category() {
        return CraftingBookCategory.MISC;
    }

    public static class Serializer implements RecipeSerializer<SmithingTinkerRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public @NotNull SmithingTinkerRecipe fromJson(@NotNull ResourceLocation id, @NotNull JsonObject json) {
            Ingredient base = Ingredient.fromJson(json.get("base"));
            Ingredient addition = Ingredient.fromJson(json.get("addition"));
            return new SmithingTinkerRecipe(id, base, addition);
        }

        @Nullable
        @Override
        public SmithingTinkerRecipe fromNetwork(@NotNull ResourceLocation id, FriendlyByteBuf buffer) {
            Ingredient base = Ingredient.fromNetwork(buffer);
            Ingredient addition = Ingredient.fromNetwork(buffer);
            return new SmithingTinkerRecipe(id, base, addition);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, SmithingTinkerRecipe recipe) {
            recipe.base.toNetwork(buffer);
            recipe.addition.toNetwork(buffer);
        }
    }

    public static class Type implements RecipeType<SmithingTinkerRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "smithing_tinker";
        private Type() {}
    }
}