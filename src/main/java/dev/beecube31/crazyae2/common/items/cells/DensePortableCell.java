package dev.beecube31.crazyae2.common.items.cells;

import appeng.api.AEApi;
import appeng.api.config.FuzzyMode;
import appeng.api.implementations.guiobjects.IGuiItem;
import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.api.implementations.items.IItemGroup;
import appeng.api.implementations.items.IStorageCell;
import appeng.api.storage.ICellInventoryHandler;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AEPartLocation;
import appeng.core.localization.GuiText;
import appeng.core.sync.GuiBridge;
import appeng.items.contents.CellConfig;
import appeng.items.contents.CellUpgrades;
import appeng.items.contents.PortableCellViewer;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import appeng.util.Platform;
import dev.beecube31.crazyae2.common.sync.CrazyAETooltip;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;

import java.util.List;
import java.util.Set;

public class DensePortableCell extends AEBasePoweredItem implements IStorageCell<IAEItemStack>, IGuiItem, IItemGroup {
    protected final int capacity;
    protected final double idleDrain;
    protected final int bytesPerType;

    public DensePortableCell(double batteryCapacity, int bytes, int bytesPerType, double idleDrain) {
        super(batteryCapacity);
        this.capacity = bytes;
        this.bytesPerType = bytesPerType;
        this.idleDrain = idleDrain;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(final World w, final EntityPlayer player, final EnumHand hand) {
        if (player.isSneaking()) {
            ItemStack item = player.getHeldItem(hand);
            NBTTagCompound itemNBT = item.getTagCompound();
            if (itemNBT != null) {
                final boolean isEnabled = itemNBT.getBoolean("autoPickup");
                itemNBT.setBoolean("autoPickup", !isEnabled);

                player.sendStatusMessage(new TextComponentString(CrazyAETooltip.AUTO_PICKUP.getLocalWithSpaceAtEnd() + this.getAutoPickupState(!isEnabled)), true);
                return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
            }
        }

        Platform.openGUI(player, null, AEPartLocation.INTERNAL, GuiBridge.GUI_PORTABLE_CELL);
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean isFull3D() {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addCheckedInformation(final ItemStack stack, final World world, final List<String> lines, final ITooltipFlag advancedTooltips) {
        super.addCheckedInformation(stack, world, lines, advancedTooltips);

        final ICellInventoryHandler<IAEItemStack> cdi = AEApi.instance()
                .registries()
                .cell()
                .getCellInventory(stack, null,
                        AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class));

        NBTTagCompound nbt = stack.getTagCompound();
        boolean isAutoPickupEnabled = nbt != null && nbt.getBoolean("autoPickup");

        lines.add(CrazyAETooltip.AUTO_PICKUP.getLocalWithSpaceAtEnd() + this.getAutoPickupState(isAutoPickupEnabled));
        lines.add(CrazyAETooltip.AUTO_PICKUP_HOW_TO_ENABLE.getLocal());
        lines.add(CrazyAETooltip.AUTO_PICKUP_TIP.getLocal());

        AEApi.instance().client().addCellInformation(cdi, lines);
    }

    @Override
    public int getBytes(final ItemStack cellItem) {
        return this.capacity;
    }

    @Override
    public int getBytesPerType(final ItemStack cellItem) {
        return this.bytesPerType;
    }

    @Override
    public int getTotalTypes(final ItemStack cellItem) {
        return 63;
    }

    @Override
    public boolean isBlackListed(final ItemStack cellItem, final IAEItemStack requestedAddition) {
        return false;
    }

    @Override
    public boolean storableInStorageCell() {
        return false;
    }

    @Override
    public boolean isStorageCell(final ItemStack i) {
        return true;
    }

    @Override
    public double getIdleDrain() {
        return this.idleDrain;
    }

    @Override
    public IStorageChannel<IAEItemStack> getChannel() {
        return AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
    }

    @Override
    public String getUnlocalizedGroupName(final Set<ItemStack> others, final ItemStack is) {
        return GuiText.StorageCells.getUnlocalized();
    }

    @Override
    public boolean isEditable(final ItemStack is) {
        return true;
    }

    @Override
    public IItemHandler getUpgradesInventory(final ItemStack is) {
        return new CellUpgrades(is, 2);
    }

    @Override
    public IItemHandler getConfigInventory(final ItemStack is) {
        return new CellConfig(is);
    }

    @Override
    public FuzzyMode getFuzzyMode(final ItemStack is) {
        final String fz = Platform.openNbtData(is).getString("FuzzyMode");
        try {
            return FuzzyMode.valueOf(fz);
        } catch (final Throwable t) {
            return FuzzyMode.IGNORE_ALL;
        }
    }

    @Override
    public void setFuzzyMode(final ItemStack is, final FuzzyMode fzMode) {
        Platform.openNbtData(is).setString("FuzzyMode", fzMode.name());
    }

    @Override
    public IGuiItemObject getGuiObject(final ItemStack is, final World w, final BlockPos pos) {
        return new PortableCellViewer(is, pos.getX());
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }

    private String getAutoPickupState(boolean isEnabled) {
        return isEnabled ? CrazyAETooltip.ENABLED_LOWERCASE.getLocal() : CrazyAETooltip.DISABLED_LOWERCASE.getLocal();
    }
}
