package saga.eternal_tinker.item;

import com.google.common.collect.Multimap;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import saga.eternal_tinker.Eternal_tinker;
import slimeknights.mantle.client.SafeClientAccess;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.modifiers.hook.behavior.AttributesModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.behavior.EnchantmentModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.display.DurabilityDisplayModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.SlotStackModifierHook;
import slimeknights.tconstruct.library.modifiers.modules.build.RarityModule;
import slimeknights.tconstruct.library.tools.IndestructibleItemEntity;
import slimeknights.tconstruct.library.tools.capability.ToolCapabilityProvider;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;
import slimeknights.tconstruct.library.tools.definition.module.display.ToolNameHook;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.helper.TooltipUtil;
import slimeknights.tconstruct.library.tools.item.IModifiableDisplay;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import java.util.*;
import java.util.function.Consumer;

public class UniversalModifiableItem extends Item implements IModifiableDisplay {

    public static final String ORIGINAL_ITEM_TAG = Eternal_tinker.MODID + ".original_item";
    public static final String ORIGINAL_STATS_TAG = Eternal_tinker.MODID + ".original_stats";
    public static final String ORIGINAL_ENCHANTMENTS_TAG = Eternal_tinker.MODID + ".original_enchantments";

    private final ToolDefinition toolDefinition;
    private final int maxStackSize;

    public enum ToolWeaponType {
        SWORD, PICKAXE, AXE, SHOVEL, HOE, BOW, CROSSBOW, TRIDENT, GUN, OTHER
    }

    public UniversalModifiableItem(Properties properties, ToolDefinition toolDefinition) {
        super(properties.stacksTo(1));
        this.toolDefinition = toolDefinition;
        this.maxStackSize = 1;
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return stack.isDamaged() ? 1 : maxStackSize;
    }

    public static ToolWeaponType getToolWeaponType(ItemStack stack) {
        Item item = stack.getItem();
        String className = item.getClass().getName();

        if (item instanceof SwordItem) return ToolWeaponType.SWORD;
        if (item instanceof PickaxeItem) return ToolWeaponType.PICKAXE;
        if (item instanceof AxeItem) return ToolWeaponType.AXE;
        if (item instanceof ShovelItem) return ToolWeaponType.SHOVEL;
        if (item instanceof HoeItem) return ToolWeaponType.HOE;
        if (item instanceof BowItem) return ToolWeaponType.BOW;
        if (item instanceof CrossbowItem) return ToolWeaponType.CROSSBOW;
        if (item instanceof TridentItem) return ToolWeaponType.TRIDENT;

        if (className.contains("Gun") || className.contains("gun")) return ToolWeaponType.GUN;
        return ToolWeaponType.OTHER;
    }

    private static float getAttackDamage(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof SwordItem sword) return sword.getDamage();
        if (item instanceof AxeItem && item instanceof DiggerItem digger) {
            switch (digger.getTier().getLevel()) {
                case 0: return 6.0f; case 1: return 7.0f; case 2: return 8.0f; case 3: return 9.0f; case 4: return 10.0f; default: return 7.0f;
            }
        }
        if (item instanceof PickaxeItem && item instanceof DiggerItem digger) return 1.0f + digger.getTier().getAttackDamageBonus();
        if (item instanceof ShovelItem && item instanceof DiggerItem digger) return 1.5f + digger.getTier().getAttackDamageBonus();
        if (item instanceof TridentItem) return 9.0f;
        if (item instanceof BowItem) return 2.0f;
        return 3.0f;
    }

    private static float getAttackSpeed(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof SwordItem) return -2.4f;
        if (item instanceof PickaxeItem) return -2.8f;
        if (item instanceof AxeItem) return -3.2f;
        if (item instanceof ShovelItem) return -3.0f;
        if (item instanceof HoeItem) return -2.0f;
        if (item instanceof TridentItem) return -2.9f;
        return -2.4f;
    }

    private static float getMiningSpeed(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof DiggerItem digger) return digger.getTier().getSpeed();
        if (item instanceof SwordItem) return 1.5f;
        return 4.0f;
    }

    private static int getDurability(ItemStack stack) {
        if (stack.isDamageableItem()) return stack.getMaxDamage();
        if (stack.getItem() instanceof BowItem) return 384;
        if (stack.getItem() instanceof CrossbowItem) return 326;
        return 250;
    }

    public static void saveOriginalStats(ItemStack tiCStack, ItemStack originalStack) {
        CompoundTag statsTag = new CompoundTag();
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(originalStack.getItem());
        statsTag.putString("original_item_id", itemId != null ? itemId.toString() : "minecraft:air");
        statsTag.putString("original_mod_id", itemId != null ? itemId.getNamespace() : "minecraft");

        if (originalStack.isDamageableItem()) {
            statsTag.putInt("original_damage", originalStack.getDamageValue());
            statsTag.putInt("original_max_damage", originalStack.getMaxDamage());
        }

        statsTag.putFloat("attack_damage", getAttackDamage(originalStack));
        statsTag.putFloat("attack_speed", getAttackSpeed(originalStack));
        statsTag.putFloat("mining_speed", getMiningSpeed(originalStack));
        statsTag.putInt("durability", getDurability(originalStack));

        if (originalStack.hasCustomHoverName()) {
            statsTag.putString("original_custom_name", Component.Serializer.toJson(originalStack.getHoverName()));
        }
        tiCStack.getOrCreateTag().put(ORIGINAL_STATS_TAG, statsTag);
    }

    public static void saveOriginalEnchantments(ItemStack tiCStack, ItemStack originalStack) {
        if (originalStack.isEnchanted()) {
            CompoundTag enchantsTag = new CompoundTag();
            Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(originalStack);
            for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                ResourceLocation enchId = ForgeRegistries.ENCHANTMENTS.getKey(entry.getKey());
                if (enchId != null) enchantsTag.putInt(enchId.toString(), entry.getValue());
            }
            tiCStack.getOrCreateTag().put(ORIGINAL_ENCHANTMENTS_TAG, enchantsTag);
        }
    }

    public static ItemStack convertAnyToolToTiC(ItemStack originalStack, Item universalModifiableItem) {
        if (originalStack.isEmpty()) return ItemStack.EMPTY;

        ToolWeaponType type = getToolWeaponType(originalStack);
        ResourceLocation defId;
        switch (type) {
            case SWORD: defId = new ResourceLocation(Eternal_tinker.MODID, "universal_sword"); break;
            case PICKAXE: defId = new ResourceLocation(Eternal_tinker.MODID, "universal_pickaxe"); break;
            case AXE: defId = new ResourceLocation(Eternal_tinker.MODID, "universal_axe"); break;
            case SHOVEL: defId = new ResourceLocation(Eternal_tinker.MODID, "universal_shovel"); break;
            case HOE: defId = new ResourceLocation(Eternal_tinker.MODID, "universal_hoe"); break;
            case BOW:
            case CROSSBOW: defId = new ResourceLocation(Eternal_tinker.MODID, "universal_bow"); break;
            case TRIDENT: defId = new ResourceLocation(Eternal_tinker.MODID, "universal_trident"); break;
            case GUN: defId = new ResourceLocation(Eternal_tinker.MODID, "universal_gun"); break;
            default: defId = new ResourceLocation(Eternal_tinker.MODID, "universal_modifiable");
        }

        ToolDefinition definition = ToolDefinition.create(defId);
        ItemStack tiCStack = new ItemStack(universalModifiableItem, 1);

        CompoundTag originalTag = new CompoundTag();
        originalStack.save(originalTag);
        tiCStack.getOrCreateTag().put(ORIGINAL_ITEM_TAG, originalTag);

        saveOriginalStats(tiCStack, originalStack);
        saveOriginalEnchantments(tiCStack, originalStack);

        ToolStack.ensureInitialized(tiCStack, definition);

        ToolStack tool = ToolStack.from(tiCStack);
        CompoundTag stats = tiCStack.getOrCreateTag().getCompound(ORIGINAL_STATS_TAG);
        if (stats.contains("original_damage")) {
            tool.setDamage(stats.getInt("original_damage"));
        }
        return tiCStack;
    }

    public static ItemStack getOriginalItem(ItemStack tiCStack) {
        CompoundTag tag = tiCStack.getTag();
        if (tag != null && tag.contains(ORIGINAL_ITEM_TAG, 10)) {
            return ItemStack.of(tag.getCompound(ORIGINAL_ITEM_TAG));
        }
        return ItemStack.EMPTY;
    }

    private void syncOriginalToTiC(ItemStack tiCStack, ItemStack originalStack) {
        CompoundTag newOriginalTag = new CompoundTag();
        originalStack.save(newOriginalTag);
        tiCStack.getOrCreateTag().put(ORIGINAL_ITEM_TAG, newOriginalTag);
        saveOriginalStats(tiCStack, originalStack);
        saveOriginalEnchantments(tiCStack, originalStack);
        if (originalStack.isDamageableItem()) {
            ToolStack.from(tiCStack).setDamage(originalStack.getDamageValue());
        }
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        ItemStack tiCStack = context.getItemInHand();
        ItemStack original = getOriginalItem(tiCStack);
        if (!original.isEmpty()) {
            InteractionHand hand = context.getHand();
            Player player = context.getPlayer();
            if (player != null) {
                ItemStack originalCopy = original.copy();
                originalCopy.setTag(tiCStack.getTagElement(ORIGINAL_ITEM_TAG));
                player.setItemInHand(hand, originalCopy);
                InteractionResult result = originalCopy.getItem().useOn(context);
                player.setItemInHand(hand, tiCStack);
                if (result.consumesAction()) syncOriginalToTiC(tiCStack, originalCopy);
                return result;
            }
            return original.getItem().useOn(context);
        }
        return super.useOn(context);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack tiCStack = player.getItemInHand(hand);
        ItemStack original = getOriginalItem(tiCStack);
        if (!original.isEmpty()) {
            ItemStack originalCopy = original.copy();
            originalCopy.setTag(tiCStack.getTagElement(ORIGINAL_ITEM_TAG));
            player.setItemInHand(hand, originalCopy);
            InteractionResultHolder<ItemStack> result = originalCopy.getItem().use(level, player, hand);
            player.setItemInHand(hand, tiCStack);
            if (result.getResult().consumesAction()) syncOriginalToTiC(tiCStack, result.getObject());
            return new InteractionResultHolder<>(result.getResult(), tiCStack);
        }
        return super.use(level, player, hand);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers = getAttributeModifiers(ToolStack.from(stack), slot);
        if (!modifiers.isEmpty()) return modifiers;
        ItemStack original = getOriginalItem(stack);
        if (!original.isEmpty()) return original.getAttributeModifiers(slot);
        return modifiers;
    }

    @Override
    public @NotNull Multimap<Attribute, AttributeModifier> getAttributeModifiers(IToolStackView tool, EquipmentSlot slot) {
        return AttributesModifierHook.getHeldAttributeModifiers(tool, slot);
    }

    @Override
    public ToolDefinition getToolDefinition() { return this.toolDefinition; }
    @Override
    public boolean isEnchantable(ItemStack stack) { return false; }
    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) { return false; }
    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) { return enchantment.isCurse(); }

    @Override
    public int getEnchantmentLevel(ItemStack stack, Enchantment enchantment) {
        int level = EnchantmentModifierHook.getEnchantmentLevel(stack, enchantment);
        if (level > 0) return level;
        ItemStack original = getOriginalItem(stack);
        if (!original.isEmpty()) return EnchantmentHelper.getItemEnchantmentLevel(enchantment, original);
        return level;
    }

    @Override
    public Map<Enchantment, Integer> getAllEnchantments(ItemStack stack) {
        Map<Enchantment, Integer> enchantments = EnchantmentModifierHook.getAllEnchantments(stack);
        if (!enchantments.isEmpty()) return enchantments;
        ItemStack original = getOriginalItem(stack);
        if (!original.isEmpty()) return EnchantmentHelper.getEnchantments(original);
        return enchantments;
    }

    @Override
    public ItemStack getRenderTool() { return ItemStack.EMPTY; }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ToolCapabilityProvider(stack);
    }

    @Override
    public void verifyTagAfterLoad(CompoundTag nbt) { ToolStack.verifyTag(this, nbt, getToolDefinition()); }
    @Override
    public void onCraftedBy(ItemStack stack, Level worldIn, Player playerIn) { ToolStack.ensureInitialized(stack, getToolDefinition()); }

    @Override
    public Rarity getRarity(ItemStack stack) {
        int rarity = ModifierUtil.getVolatileInt(stack, RarityModule.RARITY);
        if (rarity > 0) return Rarity.values()[Mth.clamp(rarity, 0, 3)];
        ItemStack original = getOriginalItem(stack);
        if (!original.isEmpty()) return original.getRarity();
        return Rarity.COMMON;
    }

    @Override
    public boolean isRepairable(ItemStack stack) { return false; }
    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) { return false; }
    @Override
    public boolean canBeDepleted() { return false; }

    @Override
    public int getMaxDamage(ItemStack stack) {
        ToolStack tool = ToolStack.from(stack);
        if (tool != null) return tool.getStats().getInt(ToolStats.DURABILITY);
        ItemStack original = getOriginalItem(stack);
        if (!original.isEmpty() && original.isDamageableItem()) return original.getMaxDamage();
        return ToolDamageUtil.getFakeMaxDamage(stack);
    }

    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T damager, Consumer<T> onBroken) {
        ToolDamageUtil.handleDamageItem(stack, amount, damager, onBroken);
        return 0;
    }

    @Override
    public int getBarColor(ItemStack pStack) { return DurabilityDisplayModifierHook.getDurabilityRGB(pStack); }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        ItemStack original = getOriginalItem(stack);
        if (!original.isEmpty()) {
            return original.getItem().onEntitySwing(original, entity);
        }
        return false;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        InventoryTickModifierHook.heldInventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
        ItemStack original = getOriginalItem(stack);
        if (!original.isEmpty()) {
            original.getItem().inventoryTick(original, worldIn, entityIn, itemSlot, isSelected);
            syncOriginalToTiC(stack, original);
        }
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack held, Slot slot, ClickAction action, Player player) {
        return SlotStackModifierHook.overrideStackedOnOther(held, slot, action, player);
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack slotStack, ItemStack held, Slot slot, ClickAction action, Player player, SlotAccess access) {
        return SlotStackModifierHook.overrideOtherStackedOnMe(slotStack, held, slot, action, player, access);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltip, @NotNull TooltipFlag flag) {
        ItemStack original = getOriginalItem(stack);
        if (!original.isEmpty()) {
            original.getItem().appendHoverText(original, level, tooltip, flag);
            tooltip.add(Component.literal(""));
            tooltip.add(Component.literal("§7┌─ §3Tinkers' Modified §7─§r"));
            CompoundTag stats = stack.getTag() != null ? stack.getTag().getCompound(ORIGINAL_STATS_TAG) : null;
            if (stats != null && stats.contains("durability")) {
                int durability = stats.getInt("durability");
                int damage = stats.getInt("original_damage");
                tooltip.add(Component.literal("§7├─ §ePreserved Durability: §f" + (durability - damage) + "/" + durability));
            }
            if (stack.getTag() != null && stack.getTag().contains(ORIGINAL_ENCHANTMENTS_TAG)) {
                int enchCount = stack.getTag().getCompound(ORIGINAL_ENCHANTMENTS_TAG).size();
                if (enchCount > 0) tooltip.add(Component.literal("§7├─ §ePreserved Enchantments: §f" + enchCount));
            }
            tooltip.add(Component.literal("§7└─ §3Use modifiers to enhance further§r"));
        }
        TooltipUtil.addInformation(this, stack, level, tooltip, SafeClientAccess.getTooltipKey(), flag);
    }

    @Override
    public int getDefaultTooltipHideFlags(@NotNull ItemStack stack) { return TooltipUtil.getModifierHideFlags(getToolDefinition()); }
    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) { return ToolNameHook.getName(toolDefinition, stack); }

    // ==================== クライアントサイド 動的レンダラー（他のMod対応） ====================

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return new DynamicItemRenderer();
            }
        });
    }

    /**
     * 動的レンダラー - 元のアイテムの見た目をそのまま表示（他のModアイテムにも対応）
     */
    @OnlyIn(Dist.CLIENT)
    private static class DynamicItemRenderer extends BlockEntityWithoutLevelRenderer {

        public DynamicItemRenderer() {
            super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                    Minecraft.getInstance().getEntityModels());
        }

        @Override
        public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack,
                                 MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
            ItemStack original = UniversalModifiableItem.getOriginalItem(stack);
            if (!original.isEmpty()) {
                // 他のModのアイテムも含めて元のアイテムをレンダリング
                Minecraft.getInstance().getItemRenderer().renderStatic(
                        original, context, combinedLight, combinedOverlay,
                        poseStack, buffer, Minecraft.getInstance().level, 0);
            } else {
                super.renderByItem(stack, context, poseStack, buffer, combinedLight, combinedOverlay);
            }
        }
    }
}