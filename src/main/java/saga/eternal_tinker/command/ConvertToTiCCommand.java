package saga.eternal_tinker.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import saga.eternal_tinker.item.ModItems;
import saga.eternal_tinker.item.UniversalModifiableItem;

public class ConvertToTiCCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("ticonverthand")
                .requires(source -> source.hasPermission(2))
                .executes(context -> convertHeldItem(context))
        );
    }

    private static int convertHeldItem(CommandContext<CommandSourceStack> context) {
        try {
            Player player = context.getSource().getPlayerOrException();
            ItemStack heldItem = player.getMainHandItem();

            if (heldItem.isEmpty()) {
                context.getSource().sendFailure(Component.literal("手持ちのアイテムがありません！"));
                return 0;
            }

            String itemName = heldItem.getHoverName().getString();

            if (heldItem.getItem() instanceof UniversalModifiableItem) {
                context.getSource().sendFailure(Component.literal("このアイテムは既にTiC強化されています！"));
                return 0;
            }

            // デバッグ出力
            context.getSource().sendSuccess(() -> Component.literal("変換中: " + itemName), false);

            ItemStack result = UniversalModifiableItem.convertAnyToolToTiC(heldItem, ModItems.UNIVERSAL_TIC_ITEM.get());

            if (result.isEmpty()) {
                context.getSource().sendFailure(Component.literal("変換に失敗しました！"));
                return 0;
            }

            // 手持ちのアイテムを減らす
            heldItem.shrink(1);

            // 結果をプレイヤーに追加
            if (!player.addItem(result)) {
                player.drop(result, false);
            }

            context.getSource().sendSuccess(() -> Component.literal("§a" + itemName + " §7をTiC強化アイテムに変換しました！"), true);
            return 1;

        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("エラー: " + e.getMessage()));
            e.printStackTrace();
            return 0;
        }
    }
}