package gregtech.api.items;

import gregtech.api.GregTech_API;
import gregtech.api.interfaces.IItemBehaviour;
import gregtech.api.util.GT_LanguageManager;
import gregtech.api.util.GT_Log;
import gregtech.api.util.GT_ModHandler;
import gregtech.api.util.GT_Utility;
import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import ic2.api.item.IElectricItemManager;
import ic2.api.item.ISpecialElectricItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

public abstract class GT_MetaBase_Item extends GT_Generic_Item implements ISpecialElectricItem, IElectricItemManager {
	/* ---------- CONSTRUCTOR AND MEMBER VARIABLES ---------- */
	private final HashMap<Short, ArrayList<IItemBehaviour<GT_MetaBase_Item>>> mItemBehaviors = new HashMap<Short, ArrayList<IItemBehaviour<GT_MetaBase_Item>>>();
	
	/**
	 * Creates the Item using these Parameters.
	 * @param aUnlocalized The Unlocalized Name of this Item.
	 * @param aGeneratedPrefixList The OreDict Prefixes you want to have generated.
	 */
	public GT_MetaBase_Item(String aUnlocalized) {
		super(aUnlocalized, "Generated Item", null, false);
        setHasSubtypes(true);
        setMaxDamage(0);
	}
	
	/**
	 * Adds a special Item Behaviour to the Item.
	 * 
	 * Note: the boolean Behaviours sometimes won't be executed if another boolean Behaviour returned true before.
	 * 
	 * @param aMetaValue the Meta Value of the Item you want to add it to. [0 - 32765]
	 * @param aBehavior the Click Behavior you want to add.
	 * @return the Item itself for convenience in constructing.
	 */
	public final GT_MetaBase_Item addItemBehavior(int aMetaValue, IItemBehaviour<GT_MetaBase_Item> aBehavior) {
		if (aMetaValue < 0 || aMetaValue >= 32766 || aBehavior == null) return this;
		ArrayList<IItemBehaviour<GT_MetaBase_Item>> tList = mItemBehaviors.get((short)aMetaValue);
		if (tList == null) {
			tList = new ArrayList<IItemBehaviour<GT_MetaBase_Item>>(1);
			mItemBehaviors.put((short)aMetaValue, tList);
		}
		tList.add(aBehavior);
		return this;
	}
	
	public abstract Long[] getElectricStats(ItemStack aStack);
	
    @Override
    public boolean onLeftClickEntity(ItemStack aStack, EntityPlayer aPlayer, Entity aEntity) {
    	use(aStack, 0, aPlayer);
		ArrayList<IItemBehaviour<GT_MetaBase_Item>> tList = mItemBehaviors.get((short)getDamage(aStack));
		if (tList != null) for (IItemBehaviour<GT_MetaBase_Item> tBehavior : tList) try {
			if (tBehavior.onLeftClickEntity(this, aStack, aPlayer, aEntity)) {
				if (aStack.stackSize <= 0) aPlayer.destroyCurrentEquippedItem();
				return true;
			}
			if (aStack.stackSize <= 0) {
				aPlayer.destroyCurrentEquippedItem();
				return false;
			}
		} catch(Throwable e) {
			if (GregTech_API.DEBUG_MODE) e.printStackTrace(GT_Log.err);
		}
    	return false;
    }
    
	@Override
	public boolean onItemUse(ItemStack aStack, EntityPlayer aPlayer, World aWorld, int aX, int aY, int aZ, int aSide, float hitX, float hitY, float hitZ) {
		use(aStack, 0, aPlayer);
		ArrayList<IItemBehaviour<GT_MetaBase_Item>> tList = mItemBehaviors.get((short)getDamage(aStack));
		if (tList != null) for (IItemBehaviour<GT_MetaBase_Item> tBehavior : tList) try {
			if (tBehavior.onItemUse(this, aStack, aPlayer, aWorld, aX, aY, aZ, aSide, hitX, hitY, hitZ)) {
				if (aStack.stackSize <= 0) aPlayer.destroyCurrentEquippedItem();
				return true;
			}
			if (aStack.stackSize <= 0) {
				aPlayer.destroyCurrentEquippedItem();
				return false;
			}
		} catch(Throwable e) {
			if (GregTech_API.DEBUG_MODE) e.printStackTrace(GT_Log.err);
		}
    	return false;
	}
	
	@Override
	public boolean onItemUseFirst(ItemStack aStack, EntityPlayer aPlayer, World aWorld, int aX, int aY, int aZ, int aSide, float hitX, float hitY, float hitZ) {
		use(aStack, 0, aPlayer);
		ArrayList<IItemBehaviour<GT_MetaBase_Item>> tList = mItemBehaviors.get((short)getDamage(aStack));
		if (tList != null) for (IItemBehaviour<GT_MetaBase_Item> tBehavior : tList) try {
			if (tBehavior.onItemUseFirst(this, aStack, aPlayer, aWorld, aX, aY, aZ, aSide, hitX, hitY, hitZ)) {
				if (aStack.stackSize <= 0) aPlayer.destroyCurrentEquippedItem();
				return true;
			}
			if (aStack.stackSize <= 0) {
				aPlayer.destroyCurrentEquippedItem();
				return false;
			}
		} catch(Throwable e) {
			if (GregTech_API.DEBUG_MODE) e.printStackTrace(GT_Log.err);
		}
		return false;
	}
	
    @Override
	public ItemStack onItemRightClick(ItemStack aStack, World aWorld, EntityPlayer aPlayer) {
    	use(aStack, 0, aPlayer);
    	ArrayList<IItemBehaviour<GT_MetaBase_Item>> tList = mItemBehaviors.get((short)getDamage(aStack));
		if (tList != null) for (IItemBehaviour<GT_MetaBase_Item> tBehavior : tList) try {
			aStack = tBehavior.onItemRightClick(this, aStack, aWorld, aPlayer);
		} catch(Throwable e) {
			if (GregTech_API.DEBUG_MODE) e.printStackTrace(GT_Log.err);
		}
		return aStack;
    }
    
	@Override
    public final void addInformation(ItemStack aStack, EntityPlayer aPlayer, List aList, boolean aF3_H) {
		String tKey = getUnlocalizedName(aStack) + ".tooltip", tString = GT_LanguageManager.getTranslation(tKey);
		if (GT_Utility.isStringValid(tString) && !tKey.equals(tString)) aList.add(tString);
		
		Long[] tStats = getElectricStats(aStack);
		if (tStats != null) {
			if (tStats[3] > 0) {
				aList.add(EnumChatFormatting.AQUA + "Contains " + tStats[3] + " EU   Tier: " + (tStats[2]>0?tStats[2]:1) + EnumChatFormatting.GRAY);
			} else {
				long tCharge = getRealCharge(aStack);
				if (tStats[3] == -2 && tCharge <= 0) {
					aList.add(EnumChatFormatting.AQUA + "Empty. You should recycle it properly." + EnumChatFormatting.GRAY);
				} else {
					aList.add(EnumChatFormatting.AQUA + "" + tCharge + " / " + Math.abs(tStats[0]) + " EU - Voltage: " + GregTech_API.VOLTAGES[(int)(tStats[2]>0?tStats[2]<GregTech_API.VOLTAGES.length?tStats[2]:GregTech_API.VOLTAGES.length-1:1)] + EnumChatFormatting.GRAY);
				}
			}
		}
		
		ArrayList<IItemBehaviour<GT_MetaBase_Item>> tList = mItemBehaviors.get((short)getDamage(aStack));
		if (tList != null) for (IItemBehaviour<GT_MetaBase_Item> tBehavior : tList) aList = tBehavior.getAdditionalToolTips(aList, aStack);
		
		addAdditionalToolTips(aList, aStack);
	}
	
	@Override
	public void onUpdate(ItemStack aStack, World aWorld, Entity aPlayer, int aTimer, boolean aIsInHand) {
		ArrayList<IItemBehaviour<GT_MetaBase_Item>> tList = mItemBehaviors.get((short)getDamage(aStack));
		if (tList != null) for (IItemBehaviour<GT_MetaBase_Item> tBehavior : tList) tBehavior.onUpdate(aStack, aWorld, aPlayer, aTimer, aIsInHand);
	}
	
	@Override
	public final boolean canProvideEnergy(ItemStack aStack) {
		Long[] tStats = getElectricStats(aStack);
		if (tStats == null) return false;
		return tStats[3] > 0 || (aStack.stackSize == 1 && (tStats[3] == -2 || tStats[3] == -3));
	}
	
	/* 1.7.10 Code
	@Override
	public final double getMaxCharge(ItemStack aStack) {
		Long[] tStats = getElectricStats(aStack);
		if (tStats == null) return 0;
		return Math.abs(tStats[0]);
	}
	
	@Override
	public final double getTransferLimit(ItemStack aStack) {
		Long[] tStats = getElectricStats(aStack);
		if (tStats == null) return 0;
		return Math.max(tStats[1], tStats[3]);
	}
	
	@Override
	public final double charge(ItemStack aStack, double aCharge, int aTier, boolean aIgnoreTransferLimit, boolean aSimulate) {
		Long[] tStats = getElectricStats(aStack);
		if (tStats == null || tStats[2] > aTier || !(tStats[3] == -1 || tStats[3] == -3 || (tStats[3] < 0 && aCharge == Integer.MAX_VALUE)) || aStack.stackSize != 1) return 0;
		long tChargeBefore = getRealCharge(aStack), tNewCharge = aCharge==Integer.MAX_VALUE?Long.MAX_VALUE:Math.min(Math.abs(tStats[0]), tChargeBefore + (aIgnoreTransferLimit?(long)aCharge:Math.min(tStats[1], (long)aCharge)));
		if (!aSimulate) setCharge(aStack, tNewCharge);
		return tNewCharge-tChargeBefore;
	}
	
	@Override
	public final double discharge(ItemStack aStack, double aCharge, int aTier, boolean aIgnoreTransferLimit, boolean aBatteryAlike, boolean aSimulate) {
		Long[] tStats = getElectricStats(aStack);
		if (tStats == null || tStats[2] > aTier) return 0;
		if (aBatteryAlike && !canProvideEnergy(aStack)) return 0;
		if (tStats[3] > 0) {
			if (aCharge < tStats[3] || aStack.stackSize < 1) return 0;
			if (!aSimulate) aStack.stackSize--;
			return tStats[3];
		}
		long tChargeBefore = getRealCharge(aStack), tNewCharge = Math.max(0, tChargeBefore - (aIgnoreTransferLimit?(long)aCharge:Math.min(tStats[1], (long)aCharge)));
		if (!aSimulate) setCharge(aStack, tNewCharge);
		return tChargeBefore-tNewCharge;
	}
	
	@Override
	public final double getCharge(ItemStack aStack) {
		return getRealCharge(aStack);
	}
	
	@Override
	public final boolean canUse(ItemStack aStack, double aAmount) {
		return getRealCharge(aStack) >= aAmount;
	}
	
	@Override
	public final boolean use(ItemStack aStack, double aAmount, EntityLivingBase aPlayer) {
		chargeFromArmor(aStack, aPlayer);
		if (aPlayer instanceof EntityPlayer && ((EntityPlayer)aPlayer).capabilities.isCreativeMode) return true;
		double tTransfer = discharge(aStack, aAmount, Integer.MAX_VALUE, true, false, true);
		if (tTransfer == aAmount) {
			discharge(aStack, aAmount, Integer.MAX_VALUE, true, false, false);
			chargeFromArmor(aStack, aPlayer);
			return true;
		}
		discharge(aStack, aAmount, Integer.MAX_VALUE, true, false, false);
		chargeFromArmor(aStack, aPlayer);
		return false;
	}
	
	@Override
	public final void chargeFromArmor(ItemStack aStack, EntityLivingBase aPlayer) {
		if (aPlayer == null || aPlayer.worldObj.isRemote) return;
		for (int i = 1; i < 5; i++) {
			ItemStack tArmor = aPlayer.getEquipmentInSlot(i);
			if (GT_ModHandler.isElectricItem(tArmor)) {
				IElectricItem tArmorItem = (IElectricItem)tArmor.getItem();
				if (tArmorItem.canProvideEnergy(tArmor) && tArmorItem.getTier(tArmor) >= getTier(aStack)) {
					double tCharge = ElectricItem.manager.discharge(tArmor, charge(aStack, Integer.MAX_VALUE-1, Integer.MAX_VALUE, true, true), Integer.MAX_VALUE, true, true, false);
					if (tCharge > 0) {
						charge(aStack, tCharge, Integer.MAX_VALUE, true, false);
						if (aPlayer instanceof EntityPlayer) {
							Container tContainer = ((EntityPlayer)aPlayer).openContainer;
							if (tContainer != null) tContainer.detectAndSendChanges();
						}
					}
				}
			}
		}
	}
	*/
	
	@Override
	public final int getMaxCharge(ItemStack aStack) {
		Long[] tStats = getElectricStats(aStack);
		if (tStats == null) return 0;
		return (int)Math.abs(tStats[0]);
	}
	
	@Override
	public final int getTransferLimit(ItemStack aStack) {
		Long[] tStats = getElectricStats(aStack);
		if (tStats == null) return 0;
		return (int)Math.max(tStats[1], tStats[3]);
	}
	
	@Override
	public final int charge(ItemStack aStack, int aCharge, int aTier, boolean aIgnoreTransferLimit, boolean aSimulate) {
		Long[] tStats = getElectricStats(aStack);
		if (tStats == null || tStats[2] > aTier || !(tStats[3] == -1 || tStats[3] == -3 || (tStats[3] < 0 && aCharge == Integer.MAX_VALUE)) || aStack.stackSize != 1) return 0;
		long tChargeBefore = getRealCharge(aStack), tNewCharge = aCharge==Integer.MAX_VALUE?Long.MAX_VALUE:Math.min(Math.abs(tStats[0]), tChargeBefore + (aIgnoreTransferLimit?aCharge:Math.min(tStats[1], aCharge)));
		if (!aSimulate) setCharge(aStack, tNewCharge);
		return (int)(tNewCharge-tChargeBefore);
	}
	
	@Override
	public final int discharge(ItemStack aStack, int aCharge, int aTier, boolean aIgnoreTransferLimit, boolean aSimulate) {
		Long[] tStats = getElectricStats(aStack);
		if (tStats == null || tStats[2] > aTier) return 0;
		if (tStats[3] > 0) {
			if (aCharge < tStats[3] || aStack.stackSize < 1) return 0;
			if (!aSimulate) aStack.stackSize--;
			return (int)(long)tStats[3];
		}
		long tChargeBefore = getRealCharge(aStack), tNewCharge = Math.max(0, tChargeBefore - (aIgnoreTransferLimit?aCharge:Math.min(tStats[1], aCharge)));
		if (!aSimulate) setCharge(aStack, tNewCharge);
		return (int)(tChargeBefore-tNewCharge);
	}
	
	@Override
	public final int getCharge(ItemStack aStack) {
		return (int)Math.min(Integer.MAX_VALUE, getRealCharge(aStack));
	}
	
	@Override
	public final boolean canUse(ItemStack aStack, int aAmount) {
		return getRealCharge(aStack) >= aAmount;
	}
	
	@Override
	public final boolean use(ItemStack aStack, int aAmount, EntityLivingBase aPlayer) {
		chargeFromArmor(aStack, aPlayer);
		if (aPlayer instanceof EntityPlayer && ((EntityPlayer)aPlayer).capabilities.isCreativeMode) return true;
		int tTransfer = discharge(aStack, aAmount, Integer.MAX_VALUE, true, true);
		if (tTransfer == aAmount) {
			discharge(aStack, aAmount, Integer.MAX_VALUE, true, false);
			chargeFromArmor(aStack, aPlayer);
			return true;
		}
		discharge(aStack, aAmount, Integer.MAX_VALUE, true, false);
		chargeFromArmor(aStack, aPlayer);
		return false;
	}
	
	@Override
	public final void chargeFromArmor(ItemStack aStack, EntityLivingBase aPlayer) {
		if (aPlayer == null || aPlayer.worldObj.isRemote) return;
		for (int i = 1; i < 5; i++) {
			ItemStack tArmor = aPlayer.getEquipmentInSlot(i);
			if (GT_ModHandler.isElectricItem(tArmor)) {
				IElectricItem tArmorItem = (IElectricItem)tArmor.getItem();
				if (tArmorItem.canProvideEnergy(tArmor) && tArmorItem.getTier(tArmor) >= getTier(aStack)) {
					int tCharge = ElectricItem.manager.discharge(tArmor, charge(aStack, Integer.MAX_VALUE-1, Integer.MAX_VALUE, true, true), Integer.MAX_VALUE, true, false);
					if (tCharge > 0) {
						charge(aStack, tCharge, Integer.MAX_VALUE, true, false);
						if (aPlayer instanceof EntityPlayer) {
							Container tContainer = ((EntityPlayer)aPlayer).openContainer;
							if (tContainer != null) tContainer.detectAndSendChanges();
						}
					}
				}
			}
		}
	}
	
	public final long getRealCharge(ItemStack aStack) {
		Long[] tStats = getElectricStats(aStack);
		if (tStats == null) return 0;
		if (tStats[3] > 0) return (int)(long)tStats[3];
		NBTTagCompound tNBT = aStack.getTagCompound();
		return tNBT==null?0:tNBT.getLong("GT.ItemCharge");
	}
	
	public final boolean setCharge(ItemStack aStack, long aCharge) {
		Long[] tStats = getElectricStats(aStack);
		if (tStats == null || tStats[3] > 0) return false;
		NBTTagCompound tNBT = aStack.getTagCompound();
		if (tNBT == null) tNBT = new NBTTagCompound();
		tNBT.removeTag("GT.ItemCharge");
		aCharge = Math.min(tStats[0]<0?Math.abs(tStats[0] / 2):aCharge, Math.abs(tStats[0]));
		if (aCharge > 0) {
			aStack.setItemDamage(getChargedMetaData(aStack));
			tNBT.setLong("GT.ItemCharge", aCharge);
		} else {
			aStack.setItemDamage(getEmptyMetaData(aStack));
		}
		if (tNBT.hasNoTags()) aStack.setTagCompound(null); else aStack.setTagCompound(tNBT);
		isItemStackUsable(aStack);
		return true;
	}
	
	public short getChargedMetaData(ItemStack aStack) {return (short)aStack.getItemDamage();}
	public short getEmptyMetaData(ItemStack aStack) {return (short)aStack.getItemDamage();}
	
	public boolean isItemStackUsable(ItemStack aStack) {
		return true;
	}
	
	@Override
	public int getItemStackLimit(ItemStack aStack) {
		Long[] tStats = getElectricStats(aStack);
		if (tStats != null && (tStats[3] == -1 || tStats[3] == -3) && getRealCharge(aStack) > 0) return 1;
		return 64;
	}
	
	@Override public final Item getChargedItem(ItemStack itemStack) {return this;}
	@Override public final Item getEmptyItem(ItemStack itemStack) {return this;}
    @Override public final int getTier(ItemStack aStack) {Long[] tStats = getElectricStats(aStack); return (int)(tStats==null?Integer.MAX_VALUE:tStats[2]);}
	@Override public final String getToolTip(ItemStack aStack) {return null;} // This has its own ToolTip Handler, no need to let the IC2 Handler screw us up at this Point
	@Override public final IElectricItemManager getManager(ItemStack aStack) {return this;} // We are our own Manager
	@Override public final boolean getShareTag() {return true;} // just to be sure.
	@Override public int getItemEnchantability() {return 0;}
	@Override public boolean isBookEnchantable(ItemStack aStack, ItemStack aBook) {return false;}
	@Override public boolean getIsRepairable(ItemStack aStack, ItemStack aMaterial) {return false;}
}