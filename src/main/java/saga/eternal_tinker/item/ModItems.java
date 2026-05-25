package saga.eternal_tinker.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import saga.eternal_tinker.Eternal_tinker;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Eternal_tinker.MODID);

    // ToolDefinitionの作成
    private static final ToolDefinition UNIVERSAL_MODIFIABLE = ToolDefinition.create(
            new ResourceLocation(Eternal_tinker.MODID, "universal_modifiable"));

    // アイテムの登録 - これが最重要！
    public static final RegistryObject<UniversalModifiableItem> UNIVERSAL_TIC_ITEM =
            ITEMS.register("universal_tic_item",
                    () -> new UniversalModifiableItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), UNIVERSAL_MODIFIABLE));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}