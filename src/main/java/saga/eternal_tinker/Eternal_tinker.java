package saga.eternal_tinker;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import saga.eternal_tinker.command.ConvertToTiCCommand;
import saga.eternal_tinker.item.ModItems;
import saga.eternal_tinker.recipe.ModRecipeSerializers;
import saga.eternal_tinker.registry.ModCreativeTabs;
import saga.eternal_tinker.registry.ModifierRegistry;

@Mod(Eternal_tinker.MODID)
public class Eternal_tinker {
    public static final String MODID = "eternal_tinker";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Eternal_tinker() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.register(modEventBus);
        ModRecipeSerializers.register(modEventBus);
        ModifierRegistry.register(modEventBus);
        ModCreativeTabs.register(modEventBus);

        // コマンドを登録
        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);

        LOGGER.info("Eternal Tinker: All systems operational.");
    }

    private void registerCommands(RegisterCommandsEvent event) {
        ConvertToTiCCommand.register(event.getDispatcher());
        LOGGER.info("Eternal Tinker: /ticonvert and /ticonverthand commands registered!");
    }
}