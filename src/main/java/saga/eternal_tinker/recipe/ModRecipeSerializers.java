package saga.eternal_tinker.recipe;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import saga.eternal_tinker.Eternal_tinker;

/**
 * このModのカスタムレシピシリアライザを登録するクラス。
 */
public class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = 
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Eternal_tinker.MODID);

    // SmithingTinkerRecipeのシリアライザ
    public static final RegistryObject<RecipeSerializer<SmithingTinkerRecipe>> SMITHING_TINKER_RECIPE = 
            RECIPE_SERIALIZERS.register("smithing_tinker", () -> SmithingTinkerRecipe.Serializer.INSTANCE);

    /**
     * レジストリをイベントバスに登録する。
     */
    public static void register(IEventBus eventBus) {
        RECIPE_SERIALIZERS.register(eventBus);
    }
}