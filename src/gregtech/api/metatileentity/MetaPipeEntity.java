package gregtech.api.metatileentity;

import gregtech.api.GregTech_API;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.objects.GT_ItemStack;
import gregtech.api.util.GT_Config;
import gregtech.api.util.GT_LanguageManager;
import gregtech.api.util.GT_Utility;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * NEVER INCLUDE THIS FILE IN YOUR MOD!!!
 * 
 * Extend this Class to add a new MetaPipe
 * Call the Constructor with the desired ID at the load-phase (not preload and also not postload!)
 * Implement the newMetaEntity-Method to return a new ready instance of your MetaTileEntity
 * 
 * Call the Constructor like the following example inside the Load Phase, to register it. 
 * "new GT_MetaTileEntity_E_Furnace(54, "GT_E_Furnace", "Automatic E-Furnace");"
 */
public abstract class MetaPipeEntity implements IMetaTileEntity {
	public static volatile int VERSION = 503;
	
	/**
	 * This variable tells, which directions the Block is connected to. It is a Bitmask.
	 */
	public byte mConnections = 0;

	/**
	 * For Pipe Rendering
	 */
	public abstract float getThickNess();
	
	/**
	 * For Pipe Rendering
	 */
	public abstract boolean renderInside(byte aSide);
	
	/**
	 * Only assigned for the MetaTileEntity in the List! Also only used to get the localized Name for the ItemStack and for getInvName.
	 */
	public String mName;
	
	public boolean doTickProfilingInThisTick = true;
	
	/**
	 * accessibility to this Field is no longer given, see below
	 */
	private IGregTechTileEntity mBaseMetaTileEntity;
	
	@Override
	public IGregTechTileEntity getBaseMetaTileEntity() {
		return mBaseMetaTileEntity;
	}
	
	@Override
	public ItemStack getStackForm(long aAmount) {
		return new ItemStack(GregTech_API.sBlockMachines, (int)aAmount, getBaseMetaTileEntity().getMetaTileID());
	}
	
	/**
	 * The Inventory of the MetaTileEntity. Amount of Slots can be larger than 256. HAYO!
	 */
	public final ItemStack[] mInventory;
	
	/**
	 * This registers your Machine at the List.
	 * Use only ID's larger than 2048, because i reserved these ones.
	 * See also the List in the API, as it has a Description containing all the reservations.
	 * @param aID the ID
	 * @example for Constructor overload.
	 * 
	 * 	public GT_MetaTileEntity_EBench(int aID, String mName, String mNameRegional) {
	 * 		super(aID, mName, mNameRegional);
	 * 	}
	 */
	public MetaPipeEntity(int aID, String aBasicName, String aRegionalName, int aInvSlotCount) {
		if (GregTech_API.sPostloadStarted || !GregTech_API.sPreloadStarted) throw new IllegalAccessError("This Constructor has to be called in the load Phase");
		if (GregTech_API.METATILEENTITIES[aID] == null) {
			GregTech_API.METATILEENTITIES[aID] = this;
		} else {
			throw new IllegalArgumentException("MetaMachine-Slot Nr. " + aID + " is already occupied!");
		}
		mName = aBasicName.replaceAll(" ", "_").toLowerCase();
		setBaseMetaTileEntity(new BaseMetaPipeEntity());
		getBaseMetaTileEntity().setMetaTileID((short)aID);
		GT_LanguageManager.addStringLocalization("gt.blockmachines." + mName + ".name", aRegionalName);
		mInventory = new ItemStack[aInvSlotCount];
		
		if (GregTech_API.gregtech.isClientSide()) {
			ItemStack tStack = new ItemStack(GregTech_API.sBlockMachines, 1, aID);
			tStack.getItem().addInformation(tStack, null, new ArrayList<String>(), true);
		}
	}
	
	@Override
	public void setBaseMetaTileEntity(IGregTechTileEntity aBaseMetaTileEntity) {
		if (mBaseMetaTileEntity != null && aBaseMetaTileEntity == null) {
			mBaseMetaTileEntity.getMetaTileEntity().inValidate();
			mBaseMetaTileEntity.setMetaTileEntity(null);
		}
		mBaseMetaTileEntity = aBaseMetaTileEntity;
		if (mBaseMetaTileEntity != null) {
			mBaseMetaTileEntity.setMetaTileEntity(this);
		}
	}
	
	/**
	 * This is the normal Constructor.
	 */
	public MetaPipeEntity(String aName, int aInvSlotCount) {
		mInventory = new ItemStack[aInvSlotCount];
		mName = aName;
	}
	
	@Override
	public void onServerStart() {/*Do nothing*/}
	@Override
	public void onWorldSave(File aSaveDirectory) {/*Do nothing*/}
	@Override
	public void onWorldLoad(File aSaveDirectory) {/*Do nothing*/}
	@Override
	public void onConfigLoad(GT_Config aConfig) {/*Do nothing*/}
	@Override
	public void setItemNBT(NBTTagCompound aNBT) {/*Do nothing*/}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister aBlockIconRegister) {/*Do nothing*/}
	
	@Override
	public boolean allowCoverOnSide(byte aSide, GT_ItemStack aCoverID) {return true;}
	@Override
	public void onScrewdriverRightClick(byte aSide, EntityPlayer aPlayer, float aX, float aY, float aZ) {/*Do nothing*/}
	@Override
	public boolean onWrenchRightClick(byte aSide, byte aWrenchingSide, EntityPlayer aPlayer, float aX, float aY, float aZ) {return false;}
	@Override
	public void onExplosion() {/*Do nothing*/}
	@Override
	public void onFirstTick(IGregTechTileEntity aBaseMetaTileEntity) {/*Do nothing*/}
	@Override
	public void onPreTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {/*Do nothing*/}
	@Override
	public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {/*Do nothing*/}
	@Override
	public void inValidate() {/*Do nothing*/}
	@Override
	public void onRemoval() {/*Do nothing*/}
	@Override
	public void initDefaultModes(NBTTagCompound aNBT) {/*Do nothing*/}
	
	/**
	 * When a GUI is opened
	 */
	public void onOpenGUI() {/*Do nothing*/}
	
	/**
	 * When a GUI is closed
	 */
	public void onCloseGUI() {/*Do nothing*/}
	
	/**
	 * a Player rightclicks the Machine
	 * Sneaky rightclicks are not getting passed to this!
	 */
	@Override
	public boolean onRightclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer, byte aSide, float aX, float aY, float aZ) {return false;}
	@Override
	public void onLeftclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer) {/*Do nothing*/}
	@Override
	public void onValueUpdate(byte aValue) {/*Do nothing*/}
	@Override
	public byte getUpdateData() {return 0;}
	
    @Override
	public void doSound(byte aIndex, double aX, double aY, double aZ) {/*Do nothing*/}
    @Override
	public void startSoundLoop(byte aIndex, double aX, double aY, double aZ) {/*Do nothing*/}
    @Override
	public void stopSoundLoop(byte aValue, double aX, double aY, double aZ) {/*Do nothing*/}
	
    @Override
	public final void sendSound(byte aIndex) {if (!getBaseMetaTileEntity().hasMufflerUpgrade()) getBaseMetaTileEntity().sendBlockEvent((byte)4, aIndex);}
    @Override
	public final void sendLoopStart(byte aIndex) {if (!getBaseMetaTileEntity().hasMufflerUpgrade()) getBaseMetaTileEntity().sendBlockEvent((byte)5, aIndex);}
    @Override
	public final void sendLoopEnd(byte aIndex) {if (!getBaseMetaTileEntity().hasMufflerUpgrade()) getBaseMetaTileEntity().sendBlockEvent((byte)6, aIndex);}
    
    @Override
	public boolean isFacingValid(byte aFacing) {return false;}
    @Override
	public boolean isAccessAllowed(EntityPlayer aPlayer) {return true;}
    @Override
	public boolean isValidSlot(int aIndex) {return true;}
    @Override
	public boolean setStackToZeroInsteadOfNull(int aIndex) {return false;}
    
	@Override
	public ArrayList<String> getSpecialDebugInfo(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer, int aLogLevel, ArrayList<String> aList) {
		return aList;
	}
	
    @Override
	public boolean isLiquidInput(byte aSide) {
    	return true;
    }
    
    @Override
	public boolean isLiquidOutput(byte aSide) {
    	return true;
    }
    
	/**
	 * gets the contained Liquid
	 */
	@Override
	public FluidStack getFluid() {return null;}
	
	/**
	 * tries to fill this Tank
	 */
	@Override
	public int fill(FluidStack resource, boolean doFill) {return 0;}
	
	/**
	 * tries to empty this Tank
	 */
	@Override
	public FluidStack drain(int maxDrain, boolean doDrain) {return null;}
	
	/**
	 * Tank pressure
	 */
	public int getTankPressure() {return 0;}
	
	/**
	 * Liquid Capacity
	 */
	@Override
	public int getCapacity() {return 0;}
	
	/**
	 * Progress this machine has already made
	 */
	public int getProgresstime() {return 0;}
	
	/**
	 * Progress this Machine has to do to produce something
	 */
	public int maxProgresstime() {return 0;}
	
	/**
	 * Increases the Progress, returns the overflown Progress.
	 */
	public int increaseProgress(int aProgress) {return 0;}
	
	@Override
	public void onMachineBlockUpdate() {/*Do nothing*/}
	@Override
	public void receiveClientEvent(byte aEventID, byte aValue) {/*Do nothing*/}
	@Override
	public boolean isSimpleMachine() {return false;}
	
	@Override
	public byte getComparatorValue(byte aSide) {
		return 0;
	}
	
	@Override
	public boolean acceptsRotationalEnergy(byte aSide) {
		return false;
	}
	
	@Override
	public boolean injectRotationalEnergy(byte aSide, long aSpeed, long aEnergy) {
		return false;
	}
	
	@Override
	public String getSpecialVoltageToolTip() {return null;}
	
	@Override
	public boolean isGivingInformation() {return false;}
	@Override
	public String[] getInfoData() {return new String[]{};}
	
	public boolean isDigitalChest() {return false;}
	public ItemStack[] getStoredItemData() {return null;}
	public void setItemCount(int aCount) {/*Do nothing*/}
	public int getMaxItemCount() {return 0;}
	
	@Override
	public int getSizeInventory() {return mInventory.length;}
	@Override
	public ItemStack getStackInSlot(int aIndex) {if (aIndex >= 0 && aIndex < mInventory.length) return mInventory[aIndex]; return null;}
	@Override
	public void setInventorySlotContents(int aIndex, ItemStack aStack) {if (aIndex >= 0 && aIndex < mInventory.length) mInventory[aIndex] = aStack;}
	@Override
	public String getInventoryName() {if (GregTech_API.METATILEENTITIES[getBaseMetaTileEntity().getMetaTileID()] != null) return GregTech_API.METATILEENTITIES[getBaseMetaTileEntity().getMetaTileID()].getMetaName(); return "";}
	@Override
	public int getInventoryStackLimit() {return 64;}
	@Override
	public boolean isItemValidForSlot(int aIndex, ItemStack aStack) {return getBaseMetaTileEntity().isValidSlot(aIndex);}
	
	@Override
	public ItemStack decrStackSize(int aIndex, int aAmount) {
		ItemStack tStack = getStackInSlot(aIndex), rStack = GT_Utility.copy(tStack);
		if (tStack != null) {
			if (tStack.stackSize <= aAmount) {
				if (setStackToZeroInsteadOfNull(aIndex)) tStack.stackSize = 0; else setInventorySlotContents(aIndex, null);
			} else {
				rStack = tStack.splitStack(aAmount);
				if (tStack.stackSize == 0 && !setStackToZeroInsteadOfNull(aIndex)) setInventorySlotContents(aIndex, null);
			}
		}
		return rStack;
	}
	
	@Override
	public int[] getAccessibleSlotsFromSide(int aSide) {
		ArrayList<Integer> tList = new ArrayList<Integer>();
		IGregTechTileEntity tTileEntity = getBaseMetaTileEntity();
		boolean tSkip = tTileEntity.getCoverBehaviorAtSide((byte)aSide).letsItemsIn((byte)aSide, tTileEntity.getCoverIDAtSide((byte)aSide), tTileEntity.getCoverDataAtSide((byte)aSide), -2, tTileEntity) || tTileEntity.getCoverBehaviorAtSide((byte)aSide).letsItemsOut((byte)aSide, tTileEntity.getCoverIDAtSide((byte)aSide), tTileEntity.getCoverDataAtSide((byte)aSide), -2, tTileEntity);
		for (int i = 0; i < getSizeInventory(); i++) if (isValidSlot(i) && (tSkip || tTileEntity.getCoverBehaviorAtSide((byte)aSide).letsItemsOut((byte)aSide, tTileEntity.getCoverIDAtSide((byte)aSide), tTileEntity.getCoverDataAtSide((byte)aSide), i, tTileEntity) || tTileEntity.getCoverBehaviorAtSide((byte)aSide).letsItemsIn((byte)aSide, tTileEntity.getCoverIDAtSide((byte)aSide), tTileEntity.getCoverDataAtSide((byte)aSide), i, tTileEntity))) tList.add(i);
		int[] rArray = new int[tList.size()];
		for (int i = 0; i < rArray.length; i++) rArray[i] = tList.get(i);
		return rArray;
	}
	
	@Override
	public boolean canInsertItem(int aIndex, ItemStack aStack, int aSide) {
		return isValidSlot(aIndex) && aStack != null && aIndex < mInventory.length && (mInventory[aIndex] == null || GT_Utility.areStacksEqual(aStack, mInventory[aIndex])) && allowPutStack(getBaseMetaTileEntity(), aIndex, (byte)aSide, aStack);
	}
	
	@Override
	public boolean canExtractItem(int aIndex, ItemStack aStack, int aSide) {
		return isValidSlot(aIndex) && aStack != null && aIndex < mInventory.length && allowPullStack(getBaseMetaTileEntity(), aIndex, (byte)aSide, aStack);
	}
	
	@Override
	public boolean canFill(ForgeDirection aSide, Fluid aFluid) {
		return fill(aSide, new FluidStack(aFluid, 1), false) == 1;
	}
	
	@Override
	public boolean canDrain(ForgeDirection aSide, Fluid aFluid) {
		return drain(aSide, new FluidStack(aFluid, 1), false) != null;
	}
	
	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection aSide) {
		if (getCapacity() <= 0 && !getBaseMetaTileEntity().hasSteamEngineUpgrade()) return new FluidTankInfo[] {};
		return new FluidTankInfo[] {getInfo()};
	}
	
    public int fill_default(ForgeDirection aSide, FluidStack aFluid, boolean doFill) {
        return fill(aFluid, doFill);
    }
    
	@Override
    public int fill(ForgeDirection aSide, FluidStack aFluid, boolean doFill) {
        return fill_default(aSide, aFluid, doFill);
    }
    
	@Override
	public FluidStack drain(ForgeDirection aSide, FluidStack aFluid, boolean doDrain) {
		if (getFluid() != null && aFluid != null && getFluid().isFluidEqual(aFluid)) return drain(aFluid.amount, doDrain);
		return null;
	}
	
	@Override
	public FluidStack drain(ForgeDirection aSide, int maxDrain, boolean doDrain) {
		return drain(maxDrain, doDrain);
	}
	
	@Override
	public int getFluidAmount() {
		return 0;
	}
	
	@Override
	public FluidTankInfo getInfo() {
		return new FluidTankInfo(this);
	}

	@Override
	public String getMetaName() {
		return mName;
	}
	
	@Override
	public ItemStack getStackInSlotOnClosing(int i) {
		return null;
	}
	
	@Override
	public boolean doTickProfilingMessageDuringThisTick() {
		return doTickProfilingInThisTick;
	}
	
	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		return false;
	}
	
	@Override
	public boolean connectsToItemPipe(byte aSide) {
		return false;
	}
	
	@Override
	public void openInventory() {
		//
	}
	
	@Override
	public void closeInventory() {
		//
	}
	
	@Override
    public Object getServerGUI(int aID, InventoryPlayer aPlayerInventory, IGregTechTileEntity aBaseMetaTileEntity) {
    	return null;
    }
	
	@Override
    public Object getClientGUI(int aID, InventoryPlayer aPlayerInventory, IGregTechTileEntity aBaseMetaTileEntity) {
    	return null;
    }
    
	@Override
	public float getExplosionResistance(byte aSide) {
		return 10.0F;
	}
	
	@Override
	public ItemStack[] getRealInventory() {
		return mInventory;
	}

	@Override
	public boolean hasCustomInventoryName() {
		return false;
	}
	
	@Override
	public void markDirty() {
		//
	}
	
	@Override
	public void onColorChangeServer(byte aColor) {
		//
	}
	
	@Override
	public void onColorChangeClient(byte aColor) {
		//
	}
	
	public long injectEnergyUnits(byte aSide, long aVoltage, long aAmperage) {
		return 0;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean renderInInventory(Block aBlock, int aMeta, RenderBlocks aRenderer) {
		return false;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean renderInWorld(IBlockAccess aWorld, int aX, int aY, int aZ, Block aBlock, RenderBlocks aRenderer) {
		return false;
	}
	
	@Override
    public void doExplosion(long aExplosionPower) {
    	float tStrength = aExplosionPower<GregTech_API.VOLTAGE_ULTRALOW?1.0F:aExplosionPower<GregTech_API.VOLTAGE_LOW?2.0F:aExplosionPower<GregTech_API.VOLTAGE_MEDIUM?3.0F:aExplosionPower<GregTech_API.VOLTAGE_HIGH?4.0F:aExplosionPower<GregTech_API.VOLTAGE_EXTREME?5.0F:aExplosionPower<GregTech_API.VOLTAGE_EXTREME*2?6.0F:aExplosionPower<GregTech_API.VOLTAGE_INSANE?7.0F:aExplosionPower<GregTech_API.VOLTAGE_LUDICROUS?8.0F:aExplosionPower<GregTech_API.VOLTAGE_ULTIMATE?9.0F:10.0F;
    	int tX=getBaseMetaTileEntity().getXCoord(), tY=getBaseMetaTileEntity().getYCoord(), tZ=getBaseMetaTileEntity().getZCoord();
    	World tWorld = getBaseMetaTileEntity().getWorld();
    	tWorld.setBlock(tX, tY, tZ, Blocks.air);
    	if (GregTech_API.sMachineExplosions) tWorld.createExplosion(null, tX+0.5, tY+0.5, tZ+0.5, tStrength, true);
	}
	
	@Override
	public int getLightOpacity() {
		return 0;
	}
	
	@Override
	public void addCollisionBoxesToList(World aWorld, int aX, int aY, int aZ, AxisAlignedBB inputAABB, List<AxisAlignedBB> outputAABB, Entity collider) {
        AxisAlignedBB axisalignedbb1 = getCollisionBoundingBoxFromPool(aWorld, aX, aY, aZ);
        if (axisalignedbb1 != null && inputAABB.intersectsWith(axisalignedbb1)) outputAABB.add(axisalignedbb1);
	}
	
	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World aWorld, int aX, int aY, int aZ) {
		return AxisAlignedBB.getBoundingBox(aX, aY, aZ, aX+1, aY+1, aZ+1);
	}
	
	@Override
	public void onEntityCollidedWithBlock(World aWorld, int aX, int aY, int aZ, Entity collider) {
		//
	}
	
	@Override
    public void onCreated(ItemStack aStack, World aWorld, EntityPlayer aPlayer) {
		//
	}
}