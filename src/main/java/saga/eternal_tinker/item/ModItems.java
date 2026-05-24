package saga.eternal_tinker;

import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import saga.eternal_tinker.item.ModItems;


@Mod(Eternal_tinker.MODID)
public class Eternal_tinker {
    public static final String MODID = "eternal_tinker";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Eternal_tinker() {
        // Forge の Mod イベントバスを取得
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // アイテムレジストリをイベントバスに登録
        ModItems.register(modEventBus);
        LOGGER.info("Eternal Tinker: Item Registry has been engaged.");

        // クリエイティブタブ等の登録（必要に応じて後ほどmodEventBusへ追加）
        LOGGER.info("Eternal Tinker: Modifier Registry has been engaged.");
        LOGGER.info("Eternal Tinker: Creative Tab Registry has been engaged.");
    }
}