package saga.eternal_tinker.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import saga.eternal_tinker.Eternal_tinker;
import saga.eternal_tinker.item.ModItems;

/**
 * このModのクリエイティブタブを登録するクラス。
 */
public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = 
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Eternal_tinker.MODID);

    public static final RegistryObject<CreativeModeTab> ETERNAL_TINKER_TAB = CREATIVE_MODE_TABS.register("eternal_tinker_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.UNIVERSAL_TIC_ITEM.get()))
                    .title(Component.translatable("itemGroup." + Eternal_tinker.MODID + ".eternal_tinker_tab"))
                    .displayItems((parameters, output) -> {
                        // このModの全アイテムをクリエイティブタブに追加
                        output.accept(ModItems.UNIVERSAL_TIC_ITEM.get());
                    })
                    .build());

    /**
     * レジストリをイベントバスに登録する。
     */
    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}