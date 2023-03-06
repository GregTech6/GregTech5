package gregtech.api.util;

import gregtech.api.GregTech_API;
import gregtech.api.damagesources.GT_DamageSources;
import gregtech.api.enchants.Enchantment_Radioactivity;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.events.BlockScanningEvent;
import gregtech.api.interfaces.IDebugableBlock;
import gregtech.api.interfaces.tileentity.*;
import gregtech.api.items.GT_EnergyArmor_Item;
import gregtech.api.net.GT_Packet_Sound;
import gregtech.api.objects.GT_ItemStack;
import gregtech.api.objects.OrePrefixMaterialData;
import gregtech.api.threads.GT_Runnable_Sound;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.RecipeInputItemStack;
import ic2.api.recipe.RecipeInputOreDict;
import ic2.api.recipe.RecipeOutput;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;

import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.play.server.S07PacketRespawn;
import net.minecraft.network.play.server.S1DPacketEntityEffect;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.FluidContainerRegistry.FluidContainerData;
import cpw.mods.fml.common.FMLCommonHandler;

/**
 * NEVER INCLUDE THIS FILE IN YOUR MOD!!!
 * 
 * Just a few Utility Functions I use.
 */
public class GT_Utility {
	public static volatile int VERSION = 503;
	
	public static final List<Character> sNumberedCharacters   = Arrays.asList('0', '1', '2', '3', '4', '5', '6', '7', '8', '9');
	public static final List<Character> sUpperCasedCharacters = Arrays.asList('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z');
	public static final List<Character> sLowerCasedCharacters = Arrays.asList('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z');
	
	public static Field getPublicField(Object aObject, String aField) {
		Field rField = null;
		try {
			rField = aObject.getClass().getDeclaredField(aField);
		} catch (Throwable e) {/*Do nothing*/}
		return rField;
	}
	
	public static Field getField(Object aObject, String aField) {
		Field rField = null;
		try {
			rField = aObject.getClass().getDeclaredField(aField);
			rField.setAccessible(true);
		} catch (Throwable e) {/*Do nothing*/}
		return rField;
	}
	
	public static Field getField(Class aObject, String aField) {
		Field rField = null;
		try {
			rField = aObject.getDeclaredField(aField);
			rField.setAccessible(true);
		} catch (Throwable e) {/*Do nothing*/}
		return rField;
	}
	
	public static Method getMethod(Class aObject, String aMethod, Class<?>... aParameterTypes) {
		Method rMethod = null;
		try {
			rMethod = aObject.getMethod(aMethod, aParameterTypes);
			rMethod.setAccessible(true);
		} catch (Throwable e) {/*Do nothing*/}
		return rMethod;
	}

	public static Method getMethod(Object aObject, String aMethod, Class<?>... aParameterTypes) {
		Method rMethod = null;
		try {
			rMethod = aObject.getClass().getMethod(aMethod, aParameterTypes);
			rMethod.setAccessible(true);
		} catch (Throwable e) {/*Do nothing*/}
		return rMethod;
	}

	public static Field getField(Object aObject, String aField, boolean aPrivate, boolean aLogErrors) {
		try {
			Field tField = (aObject instanceof Class)?((Class)aObject).getDeclaredField(aField):(aObject instanceof String)?Class.forName((String)aObject).getDeclaredField(aField):aObject.getClass().getDeclaredField(aField);
			if (aPrivate) tField.setAccessible(true);
			return tField;
		} catch (Throwable e) {
			if (aLogErrors) e.printStackTrace(GT_Log.err);
		}
		return null;
	}
	
	public static Object getFieldContent(Object aObject, String aField, boolean aPrivate, boolean aLogErrors) {
		try {
			Field tField = (aObject instanceof Class)?((Class)aObject).getDeclaredField(aField):(aObject instanceof String)?Class.forName((String)aObject).getDeclaredField(aField):aObject.getClass().getDeclaredField(aField);
			if (aPrivate) tField.setAccessible(true);
			return tField.get(aObject instanceof Class || aObject instanceof String ? null : aObject);
		} catch (Throwable e) {
			if (aLogErrors) e.printStackTrace(GT_Log.err);
		}
		return null;
	}
	
	public static Object callPublicMethod(Object aObject, String aMethod, Object... aParameters) {
		return callMethod(aObject, aMethod, false, false, true, aParameters);
	}

	public static Object callPrivateMethod(Object aObject, String aMethod, Object... aParameters) {
		return callMethod(aObject, aMethod, true, false, true, aParameters);
	}
	
	public static Object callMethod(Object aObject, String aMethod, boolean aPrivate, boolean aUseUpperCasedDataTypes, boolean aLogErrors, Object... aParameters) {
		try {
			Class<?>[] tParameterTypes = new Class<?>[aParameters.length];
			for (byte i = 0; i < aParameters.length; i++) {
				if (aParameters[i] instanceof Class) {
					tParameterTypes[i] = (Class)aParameters[i];
					aParameters[i] = null;
				} else {
					tParameterTypes[i] = aParameters[i].getClass();
				}
				if (!aUseUpperCasedDataTypes) {
					if (tParameterTypes[i] == Boolean.class	) tParameterTypes[i] = boolean.class;	else
					if (tParameterTypes[i] == Byte.class	) tParameterTypes[i] = byte.class;		else
					if (tParameterTypes[i] == Short.class	) tParameterTypes[i] = short.class;		else
					if (tParameterTypes[i] == Integer.class	) tParameterTypes[i] = int.class;		else
					if (tParameterTypes[i] == Long.class	) tParameterTypes[i] = long.class;		else
					if (tParameterTypes[i] == Float.class	) tParameterTypes[i] = float.class;		else
					if (tParameterTypes[i] == Double.class	) tParameterTypes[i] = double.class;
				}
			}
			
			Method tMethod = (aObject instanceof Class)?((Class)aObject).getMethod(aMethod, tParameterTypes):aObject.getClass().getMethod(aMethod, tParameterTypes);
			if (aPrivate) tMethod.setAccessible(true);
			return tMethod.invoke(aObject, aParameters);
		} catch (Throwable e) {
			if (aLogErrors) e.printStackTrace(GT_Log.err);
		}
		return null;
	}
	
	public static Object callConstructor(String aClass, int aConstructorIndex, Object aReplacementObject, boolean aLogErrors, Object... aParameters) {
		if (aConstructorIndex < 0) {
			try {
				for (Constructor tConstructor : Class.forName(aClass).getConstructors()) {
					try {
						return tConstructor.newInstance(aParameters);
					} catch (Throwable e) {/*Do nothing*/}
				}
			} catch (Throwable e) {
				if (aLogErrors) e.printStackTrace(GT_Log.err);
			}
		} else {
			try {
				return Class.forName(aClass).getConstructors()[aConstructorIndex].newInstance(aParameters);
			} catch (Throwable e) {
				if (aLogErrors) e.printStackTrace(GT_Log.err);
			}
		}
		return aReplacementObject;
	}
	
	public static String capitalizeString(String aString) {
		if (aString != null && aString.length() > 0) return aString.substring(0, 1).toUpperCase() + aString.substring(1);
		return "";
	}
	
    public static boolean getPotion(EntityLivingBase aPlayer, int aPotionIndex) {
        try  {
        	Field tPotionHashmap = null;
        	
            Field[] var3 = EntityLiving.class.getDeclaredFields();
            int var4 = var3.length;
            
            for (int var5 = 0; var5 < var4; ++var5) {
                Field var6 = var3[var5];
                if (var6.getType() == HashMap.class) {
                    tPotionHashmap = var6;
                    tPotionHashmap.setAccessible(true);
                    break;
                }
            }
            
            if (tPotionHashmap != null) return ((HashMap)tPotionHashmap.get(aPlayer)).get(Integer.valueOf(aPotionIndex)) != null;
        } catch (Throwable e) {
        	if (GregTech_API.DEBUG_MODE) e.printStackTrace(GT_Log.err);
        }
    	return false;
    }
	
    public static String getClassName(Object aObject) {
    	if (aObject == null) return "null";
    	return aObject.getClass().getName().substring(aObject.getClass().getName().lastIndexOf(".")+1);
    }
    
    public static void removePotion(EntityLivingBase aPlayer, int aPotionIndex) {
        try  {
        	Field tPotionHashmap = null;
        	
            Field[] var3 = EntityLiving.class.getDeclaredFields();
            int var4 = var3.length;

            for (int var5 = 0; var5 < var4; ++var5) {
                Field var6 = var3[var5];
                if (var6.getType() == HashMap.class) {
                    tPotionHashmap = var6;
                    tPotionHashmap.setAccessible(true);
                    break;
                }
            }

            if (tPotionHashmap != null) ((HashMap)tPotionHashmap.get(aPlayer)).remove(Integer.valueOf(aPotionIndex));
        } catch (Throwable e) {
        	if (GregTech_API.DEBUG_MODE) e.printStackTrace(GT_Log.err);
        }
    }
	
	public static boolean getFullInvisibility(EntityPlayer aPlayer) {
		try {
			if (aPlayer.isInvisible()) {
				for (int i = 0; i < 4; i++) {
					if (aPlayer.inventory.armorInventory[i] != null) {
						if (aPlayer.inventory.armorInventory[i].getItem() instanceof GT_EnergyArmor_Item) {
							if ((((GT_EnergyArmor_Item)aPlayer.inventory.armorInventory[i].getItem()).mSpecials & 512) != 0) {
								if (GT_ModHandler.canUseElectricItem(aPlayer.inventory.armorInventory[i], 10000)) {
									return true;
								}
							}
						}
					}
				}
			}
		} catch(Throwable e) {if (GregTech_API.DEBUG_MODE) e.printStackTrace(GT_Log.err);}
		return false;
	}
	
	public static ItemStack suckOneItemStackAt(World aWorld, double aX, double aY, double aZ, double aL, double aH, double aW) {
		for (EntityItem tItem : (ArrayList<EntityItem>)aWorld.getEntitiesWithinAABB(EntityItem.class, AxisAlignedBB.getBoundingBox(aX, aY, aZ, aX+aL, aY+aH, aZ+aW))) {
			if (!tItem.isDead) {
				aWorld.removeEntity(tItem);
				tItem.setDead();
				return tItem.getEntityItem();
			}
		}
		return null;
	}

	public static byte getOppositeSide(int aSide) {
		return (byte)ForgeDirection.getOrientation(aSide).getOpposite().ordinal();
	}
	
	public static byte getTier(long l) {
		byte i = -1;
		while (++i < GregTech_API.VOLTAGES.length) if (l <= GregTech_API.VOLTAGES[i]) return i;
		return i;
	}
	
	public static void sendChatToPlayer(EntityPlayer aPlayer, String aChatMessage) {
		if (aPlayer != null && aPlayer instanceof EntityPlayerMP && aChatMessage != null) {
			aPlayer.addChatComponentMessage(new ChatComponentText(aChatMessage));
		}
	}
	
	public static boolean TE_CHECK = false, BC_CHECK = false, CHECK_ALL = true;
	
	public static void checkAvailabilities() {
		if (CHECK_ALL) {
			try {
				Class tClass = cofh.api.transport.IItemConduit.class;
				tClass.getCanonicalName();
				TE_CHECK = true;
			} catch(Throwable e) {/**/}
			try {
				Class tClass = buildcraft.api.transport.IPipeTile.class;
				tClass.getCanonicalName();
				BC_CHECK = true;
			} catch(Throwable e) {/**/}
			CHECK_ALL = false;
		}
	}
	
	public static boolean isConnectableNonInventoryPipe(Object aTileEntity, int aSide) {
		if (aTileEntity == null) return false;
		checkAvailabilities();
		if (TE_CHECK) if (aTileEntity instanceof cofh.api.transport.IItemConduit) return true;
		if (BC_CHECK) if (aTileEntity instanceof buildcraft.api.transport.IPipeTile) return ((buildcraft.api.transport.IPipeTile)aTileEntity).isPipeConnected(ForgeDirection.getOrientation(aSide));
		return false;
	}
	
	/**
	 * Moves Stack from Inv-Slot to Inv-Slot, without checking if its even allowed.
	 * @return the Amount of moved Items
	 */
	public static byte moveStackIntoPipe(IInventory aTileEntity1, Object aTileEntity2, int[] aGrabSlots, int aGrabFrom, int aPutTo, List<ItemStack> aFilter, boolean aInvertFilter, byte aMaxTargetStackSize, byte aMinTargetStackSize, byte aMaxMoveAtOnce, byte aMinMoveAtOnce) {
		if (aTileEntity1 == null || aMaxTargetStackSize <= 0 || aMinTargetStackSize <= 0 || aMinTargetStackSize > aMaxTargetStackSize || aMaxMoveAtOnce <= 0 || aMinMoveAtOnce > aMaxMoveAtOnce) return 0;
		if (aTileEntity2 != null) {
			checkAvailabilities();
			if (TE_CHECK && aTileEntity2 instanceof cofh.api.transport.IItemConduit) {
				for (int i = 0; i < aGrabSlots.length; i++) {
					if (listContainsItem(aFilter, aTileEntity1.getStackInSlot(aGrabSlots[i]), true, aInvertFilter)) {
						if (isAllowedToTakeFromSlot(aTileEntity1, aGrabSlots[i], (byte)aGrabFrom, aTileEntity1.getStackInSlot(aGrabSlots[i]))) {
							if (Math.max(aMinMoveAtOnce, aMinTargetStackSize) <= aTileEntity1.getStackInSlot(aGrabSlots[i]).stackSize) {
								ItemStack tStack = copyAmount(Math.min(aTileEntity1.getStackInSlot(aGrabSlots[i]).stackSize, Math.min(aMaxMoveAtOnce, aMaxTargetStackSize)), aTileEntity1.getStackInSlot(aGrabSlots[i]));
								ItemStack rStack = ((cofh.api.transport.IItemConduit)aTileEntity2).insertItem(ForgeDirection.getOrientation(aPutTo), copy(tStack), false/*true*/);
								byte tMovedItemCount = (byte)(tStack.stackSize - (rStack == null ? 0 : rStack.stackSize));
								if (tMovedItemCount >= 1/*Math.max(aMinMoveAtOnce, aMinTargetStackSize)*/) {
									//((cofh.api.transport.IItemConduit)aTileEntity2).insertItem(ForgeDirection.getOrientation(aPutTo), copyAmount(tMovedItemCount, tStack), false);
									aTileEntity1.decrStackSize(aGrabSlots[i], tMovedItemCount);
									aTileEntity1.markDirty();
									return tMovedItemCount;
								}
							}
						}
					}
				}
				return 0;
			}
			if (BC_CHECK && aTileEntity2 instanceof buildcraft.api.transport.IPipeTile) {
				for (int i = 0; i < aGrabSlots.length; i++) {
					if (listContainsItem(aFilter, aTileEntity1.getStackInSlot(aGrabSlots[i]), true, aInvertFilter)) {
						if (isAllowedToTakeFromSlot(aTileEntity1, aGrabSlots[i], (byte)aGrabFrom, aTileEntity1.getStackInSlot(aGrabSlots[i]))) {
							if (Math.max(aMinMoveAtOnce, aMinTargetStackSize) <= aTileEntity1.getStackInSlot(aGrabSlots[i]).stackSize) {
								ItemStack tStack = copyAmount(Math.min(aTileEntity1.getStackInSlot(aGrabSlots[i]).stackSize, Math.min(aMaxMoveAtOnce, aMaxTargetStackSize)), aTileEntity1.getStackInSlot(aGrabSlots[i]));
								byte tMovedItemCount = (byte)((buildcraft.api.transport.IPipeTile)aTileEntity2).injectItem(copy(tStack), false, ForgeDirection.getOrientation(aPutTo));
								if (tMovedItemCount >= Math.max(aMinMoveAtOnce, aMinTargetStackSize)) {
									tMovedItemCount = (byte)(((buildcraft.api.transport.IPipeTile)aTileEntity2).injectItem(copyAmount(tMovedItemCount, tStack), true, ForgeDirection.getOrientation(aPutTo)));
									aTileEntity1.decrStackSize(aGrabSlots[i], tMovedItemCount);
									aTileEntity1.markDirty();
									return tMovedItemCount;
								}
							}
						}
					}
				}
				return 0;
			}
		}
		
		ForgeDirection tDirection = ForgeDirection.getOrientation(aGrabFrom);
		if (aTileEntity1 instanceof TileEntity && tDirection != ForgeDirection.UNKNOWN && tDirection.getOpposite() == ForgeDirection.getOrientation(aPutTo)) {
			int tX = ((TileEntity)aTileEntity1).xCoord + tDirection.offsetX, tY = ((TileEntity)aTileEntity1).yCoord + tDirection.offsetY, tZ = ((TileEntity)aTileEntity1).zCoord + tDirection.offsetZ;
			if (!hasBlockHitBox(((TileEntity)aTileEntity1).getWorldObj(), tX, tY, tZ)) {
				for (int i = 0; i < aGrabSlots.length; i++) {
					if (listContainsItem(aFilter, aTileEntity1.getStackInSlot(aGrabSlots[i]), true, aInvertFilter)) {
						if (isAllowedToTakeFromSlot(aTileEntity1, aGrabSlots[i], (byte)aGrabFrom, aTileEntity1.getStackInSlot(aGrabSlots[i]))) {
							if (Math.max(aMinMoveAtOnce, aMinTargetStackSize) <= aTileEntity1.getStackInSlot(aGrabSlots[i]).stackSize) {
								ItemStack tStack = copyAmount(Math.min(aTileEntity1.getStackInSlot(aGrabSlots[i]).stackSize, Math.min(aMaxMoveAtOnce, aMaxTargetStackSize)), aTileEntity1.getStackInSlot(aGrabSlots[i]));
								EntityItem tEntity = new EntityItem(((TileEntity)aTileEntity1).getWorldObj(), tX+0.5, tY+0.5, tZ+0.5, tStack);
								tEntity.motionX = tEntity.motionY = tEntity.motionZ = 0;
								((TileEntity)aTileEntity1).getWorldObj().spawnEntityInWorld(tEntity);
								aTileEntity1.decrStackSize(aGrabSlots[i], tStack.stackSize);
								aTileEntity1.markDirty();
								return (byte)tStack.stackSize;
							}
						}
					}
				}
			}
		}
		return 0;
	}
	
	/**
	 * Moves Stack from Inv-Slot to Inv-Slot, without checking if its even allowed. (useful for internal Inventory Operations)
	 * @return the Amount of moved Items
	 */
	public static byte moveStackFromSlotAToSlotB(IInventory aTileEntity1, IInventory aTileEntity2, int aGrabFrom, int aPutTo, byte aMaxTargetStackSize, byte aMinTargetStackSize, byte aMaxMoveAtOnce, byte aMinMoveAtOnce) {
		if (aTileEntity1 == null || aTileEntity2 == null || aMaxTargetStackSize <= 0 || aMinTargetStackSize <= 0 || aMinTargetStackSize > aMaxTargetStackSize || aMaxMoveAtOnce <= 0 || aMinMoveAtOnce > aMaxMoveAtOnce) return 0;
		
		ItemStack tStack1 = aTileEntity1.getStackInSlot(aGrabFrom), tStack2 = aTileEntity2.getStackInSlot(aPutTo), tStack3 = null;
		if (tStack1 != null) {
			if (tStack2 != null && !areStacksEqual(tStack1, tStack2)) return 0;
			tStack3 = copy(tStack1);
			aMaxTargetStackSize = (byte)Math.min(aMaxTargetStackSize, Math.min(tStack3.getMaxStackSize(), Math.min(tStack2==null?Integer.MAX_VALUE:tStack2.getMaxStackSize(), aTileEntity2.getInventoryStackLimit())));
			tStack3.stackSize = Math.min(tStack3.stackSize, aMaxTargetStackSize - (tStack2 == null?0:tStack2.stackSize));
			if (tStack3.stackSize > aMaxMoveAtOnce) tStack3.stackSize = aMaxMoveAtOnce;
			if (tStack3.stackSize + (tStack2==null?0:tStack2.stackSize) >= Math.min(tStack3.getMaxStackSize(), aMinTargetStackSize) && tStack3.stackSize >= aMinMoveAtOnce) {
				tStack3 = aTileEntity1.decrStackSize(aGrabFrom, tStack3.stackSize);
				aTileEntity1.markDirty();
				if (tStack3 != null) {
					if (tStack2 == null) {
						aTileEntity2.setInventorySlotContents(aPutTo, copy(tStack3));
						aTileEntity2.markDirty();
					} else {
						tStack2.stackSize += tStack3.stackSize;
						aTileEntity2.markDirty();
					}
					return (byte)tStack3.stackSize;
				}
			}
		}
		return 0;
	}
	
	public static boolean isAllowedToTakeFromSlot(IInventory aTileEntity, int aSlot, byte aSide, ItemStack aStack) {
		if (ForgeDirection.getOrientation(aSide) == ForgeDirection.UNKNOWN) {
			return isAllowedToTakeFromSlot(aTileEntity, aSlot, (byte)0, aStack)
				|| isAllowedToTakeFromSlot(aTileEntity, aSlot, (byte)1, aStack)
				|| isAllowedToTakeFromSlot(aTileEntity, aSlot, (byte)2, aStack)
				|| isAllowedToTakeFromSlot(aTileEntity, aSlot, (byte)3, aStack)
				|| isAllowedToTakeFromSlot(aTileEntity, aSlot, (byte)4, aStack)
				|| isAllowedToTakeFromSlot(aTileEntity, aSlot, (byte)5, aStack);
		}
		if (aTileEntity instanceof ISidedInventory) return ((ISidedInventory)aTileEntity).canExtractItem(aSlot, aStack, aSide);
		return true;
	}
	
	public static boolean isAllowedToPutIntoSlot(IInventory aTileEntity, int aSlot, byte aSide, ItemStack aStack) {
		if (ForgeDirection.getOrientation(aSide) == ForgeDirection.UNKNOWN) {
			return isAllowedToPutIntoSlot(aTileEntity, aSlot, (byte)0, aStack)
				|| isAllowedToPutIntoSlot(aTileEntity, aSlot, (byte)1, aStack)
				|| isAllowedToPutIntoSlot(aTileEntity, aSlot, (byte)2, aStack)
				|| isAllowedToPutIntoSlot(aTileEntity, aSlot, (byte)3, aStack)
				|| isAllowedToPutIntoSlot(aTileEntity, aSlot, (byte)4, aStack)
				|| isAllowedToPutIntoSlot(aTileEntity, aSlot, (byte)5, aStack);
		}
		if (aTileEntity instanceof ISidedInventory && !((ISidedInventory)aTileEntity).canInsertItem(aSlot, aStack, aSide)) return false;
		return aTileEntity.isItemValidForSlot(aSlot, aStack);
	}
	
	/**
	 * Moves Stack from Inv-Side to Inv-Side.
	 * @return the Amount of moved Items
	 */
	public static byte moveOneItemStack(Object aTileEntity1, Object aTileEntity2, byte aGrabFrom, byte aPutTo, List<ItemStack> aFilter, boolean aInvertFilter, byte aMaxTargetStackSize, byte aMinTargetStackSize, byte aMaxMoveAtOnce, byte aMinMoveAtOnce) {
		if (aTileEntity1 != null && aTileEntity1 instanceof IInventory) return moveOneItemStack((IInventory)aTileEntity1, aTileEntity2, aGrabFrom, aPutTo, aFilter, aInvertFilter, aMaxTargetStackSize, aMinTargetStackSize, aMaxMoveAtOnce, aMinMoveAtOnce, true);
		return 0;
	}
	
	/**
	 * This is only because I needed an additional Parameter for the Double Chest Check.
	 */
	private static byte moveOneItemStack(IInventory aTileEntity1, Object aTileEntity2, byte aGrabFrom, byte aPutTo, List<ItemStack> aFilter, boolean aInvertFilter, byte aMaxTargetStackSize, byte aMinTargetStackSize, byte aMaxMoveAtOnce, byte aMinMoveAtOnce, boolean aDoCheckChests) {
		if (aTileEntity1 == null || aMaxTargetStackSize <= 0 || aMinTargetStackSize <= 0 || aMaxMoveAtOnce <= 0 || aMinTargetStackSize > aMaxTargetStackSize || aMinMoveAtOnce > aMaxMoveAtOnce) return 0;
		
		int[] tGrabSlots = null;
		if (aTileEntity1 instanceof ISidedInventory) tGrabSlots = ((ISidedInventory)aTileEntity1).getAccessibleSlotsFromSide(aGrabFrom);
		if (tGrabSlots == null) {
			tGrabSlots = new int[aTileEntity1.getSizeInventory()];
			for (int i = 0; i < tGrabSlots.length; i++) tGrabSlots[i] = i;
		}
		
		if (aTileEntity2 != null && aTileEntity2 instanceof IInventory) {
			int[] tPutSlots = null;
			if (aTileEntity2 instanceof ISidedInventory) tPutSlots = ((ISidedInventory)aTileEntity2).getAccessibleSlotsFromSide(aPutTo);
			
			if (tPutSlots == null) {
				tPutSlots = new int[((IInventory)aTileEntity2).getSizeInventory()];
				for (int i = 0; i < tPutSlots.length; i++) tPutSlots[i] = i;
			}
			
			for (int i = 0; i < tGrabSlots.length; i++) {
				for (int j = 0; j < tPutSlots.length; j++) {
					if (listContainsItem(aFilter, aTileEntity1.getStackInSlot(tGrabSlots[i]), true, aInvertFilter)) {
						if (isAllowedToTakeFromSlot(aTileEntity1, tGrabSlots[i], aGrabFrom, aTileEntity1.getStackInSlot(tGrabSlots[i]))) {
							if (isAllowedToPutIntoSlot((IInventory)aTileEntity2, tPutSlots[j], aPutTo, aTileEntity1.getStackInSlot(tGrabSlots[i]))) {
								byte tMovedItemCount = moveStackFromSlotAToSlotB(aTileEntity1, (IInventory)aTileEntity2, tGrabSlots[i], tPutSlots[j], aMaxTargetStackSize, aMinTargetStackSize, aMaxMoveAtOnce, aMinMoveAtOnce);
								if (tMovedItemCount > 0) return tMovedItemCount;
							}
						}
					}
				}
			}
			
			if (aDoCheckChests && aTileEntity1 instanceof TileEntityChest) {
				TileEntityChest tTileEntity1 = (TileEntityChest)aTileEntity1;
				if (tTileEntity1.adjacentChestChecked) {
					byte tAmount = 0;
					if (tTileEntity1.adjacentChestXNeg != null) {
						tAmount = moveOneItemStack(tTileEntity1.adjacentChestXNeg, aTileEntity2, aGrabFrom, aPutTo, aFilter, aInvertFilter, aMaxTargetStackSize, aMinTargetStackSize, aMaxMoveAtOnce, aMinMoveAtOnce, false);
					} else if (tTileEntity1.adjacentChestZNeg != null) {
						tAmount = moveOneItemStack(tTileEntity1.adjacentChestZNeg, aTileEntity2, aGrabFrom, aPutTo, aFilter, aInvertFilter, aMaxTargetStackSize, aMinTargetStackSize, aMaxMoveAtOnce, aMinMoveAtOnce, false);
					} else if (tTileEntity1.adjacentChestXPos != null) {
						tAmount = moveOneItemStack(tTileEntity1.adjacentChestXPos, aTileEntity2, aGrabFrom, aPutTo, aFilter, aInvertFilter, aMaxTargetStackSize, aMinTargetStackSize, aMaxMoveAtOnce, aMinMoveAtOnce, false);
					} else if (tTileEntity1.adjacentChestZPos != null) {
						tAmount = moveOneItemStack(tTileEntity1.adjacentChestZPos, aTileEntity2, aGrabFrom, aPutTo, aFilter, aInvertFilter, aMaxTargetStackSize, aMinTargetStackSize, aMaxMoveAtOnce, aMinMoveAtOnce, false);
					}
					if (tAmount != 0) return tAmount;
				}
			}
			if (aDoCheckChests && aTileEntity2 instanceof TileEntityChest) {
				TileEntityChest tTileEntity2 = (TileEntityChest)aTileEntity2;
				if (tTileEntity2.adjacentChestChecked) {
					byte tAmount = 0;
					if (tTileEntity2.adjacentChestXNeg != null) {
						tAmount = moveOneItemStack(aTileEntity1, tTileEntity2.adjacentChestXNeg, aGrabFrom, aPutTo, aFilter, aInvertFilter, aMaxTargetStackSize, aMinTargetStackSize, aMaxMoveAtOnce, aMinMoveAtOnce, false);
					} else if (tTileEntity2.adjacentChestZNeg != null) {
						tAmount = moveOneItemStack(aTileEntity1, tTileEntity2.adjacentChestZNeg, aGrabFrom, aPutTo, aFilter, aInvertFilter, aMaxTargetStackSize, aMinTargetStackSize, aMaxMoveAtOnce, aMinMoveAtOnce, false);
					} else if (tTileEntity2.adjacentChestXPos != null) {
						tAmount = moveOneItemStack(aTileEntity1, tTileEntity2.adjacentChestXPos, aGrabFrom, aPutTo, aFilter, aInvertFilter, aMaxTargetStackSize, aMinTargetStackSize, aMaxMoveAtOnce, aMinMoveAtOnce, false);
					} else if (tTileEntity2.adjacentChestZPos != null) {
						tAmount = moveOneItemStack(aTileEntity1, tTileEntity2.adjacentChestZPos, aGrabFrom, aPutTo, aFilter, aInvertFilter, aMaxTargetStackSize, aMinTargetStackSize, aMaxMoveAtOnce, aMinMoveAtOnce, false);
					}
					if (tAmount != 0) return tAmount;
				}
			}
		}
		
		return moveStackIntoPipe(aTileEntity1, aTileEntity2, tGrabSlots, aGrabFrom, aPutTo, aFilter, aInvertFilter, aMaxTargetStackSize, aMinTargetStackSize, aMaxMoveAtOnce, aMinMoveAtOnce);
	}
	
	/**
	 * Moves Stack from Inv-Side to Inv-Slot.
	 * @return the Amount of moved Items
	 */
	public static byte moveOneItemStackIntoSlot(Object aTileEntity1, Object aTileEntity2, byte aGrabFrom, int aPutTo, List<ItemStack> aFilter, boolean aInvertFilter, byte aMaxTargetStackSize, byte aMinTargetStackSize, byte aMaxMoveAtOnce, byte aMinMoveAtOnce) {
		if (aTileEntity1 == null || !(aTileEntity1 instanceof IInventory) || aPutTo < 0 || aMaxTargetStackSize <= 0 || aMinTargetStackSize <= 0 || aMaxMoveAtOnce <= 0 || aMinTargetStackSize > aMaxTargetStackSize || aMinMoveAtOnce > aMaxMoveAtOnce) return 0;
		
		int[] tGrabSlots = null;
		if (aTileEntity1 instanceof ISidedInventory) tGrabSlots = ((ISidedInventory)aTileEntity1).getAccessibleSlotsFromSide(aGrabFrom);
		if (tGrabSlots == null) {
			tGrabSlots = new int[((IInventory)aTileEntity1).getSizeInventory()];
			for (int i = 0; i < tGrabSlots.length; i++) tGrabSlots[i] = i;
		}
		
		if (aTileEntity2 != null && aTileEntity2 instanceof IInventory) {
			for (int i = 0; i < tGrabSlots.length; i++) {
				if (listContainsItem(aFilter, ((IInventory)aTileEntity1).getStackInSlot(tGrabSlots[i]), true, aInvertFilter)) {
					if (isAllowedToTakeFromSlot((IInventory)aTileEntity1, tGrabSlots[i], aGrabFrom, ((IInventory)aTileEntity1).getStackInSlot(tGrabSlots[i]))) {
						if (isAllowedToPutIntoSlot((IInventory)aTileEntity2, aPutTo, (byte)6, ((IInventory)aTileEntity1).getStackInSlot(tGrabSlots[i]))) {
							byte tMovedItemCount = moveStackFromSlotAToSlotB((IInventory)aTileEntity1, (IInventory)aTileEntity2, tGrabSlots[i], aPutTo, aMaxTargetStackSize, aMinTargetStackSize, aMaxMoveAtOnce, aMinMoveAtOnce);
							if (tMovedItemCount > 0) return tMovedItemCount;
						}
					}
				}
			}
		}
		
		moveStackIntoPipe(((IInventory)aTileEntity1), aTileEntity2, tGrabSlots, aGrabFrom, aPutTo, aFilter, aInvertFilter, aMaxTargetStackSize, aMinTargetStackSize, aMaxMoveAtOnce, aMinMoveAtOnce);
		return 0;
	}
	
	/**
	 * Moves Stack from Inv-Slot to Inv-Slot.
	 * @return the Amount of moved Items
	 */
	public static byte moveFromSlotToSlot(IInventory aTileEntity1, IInventory aTileEntity2, int aGrabFrom, int aPutTo, List<ItemStack> aFilter, boolean aInvertFilter, byte aMaxTargetStackSize, byte aMinTargetStackSize, byte aMaxMoveAtOnce, byte aMinMoveAtOnce) {
		if (aTileEntity1 == null || aTileEntity2 == null || aGrabFrom < 0 || aPutTo < 0 || aMaxTargetStackSize <= 0 || aMinTargetStackSize <= 0 || aMaxMoveAtOnce <= 0 || aMinTargetStackSize > aMaxTargetStackSize || aMinMoveAtOnce > aMaxMoveAtOnce) return 0;
		if (listContainsItem(aFilter, aTileEntity1.getStackInSlot(aGrabFrom), true, aInvertFilter)) {
			if (isAllowedToTakeFromSlot(aTileEntity1, aGrabFrom, (byte)6, aTileEntity1.getStackInSlot(aGrabFrom))) {
				if (isAllowedToPutIntoSlot(aTileEntity2, aPutTo, (byte)6, aTileEntity1.getStackInSlot(aGrabFrom))) {
					byte tMovedItemCount = moveStackFromSlotAToSlotB(aTileEntity1, aTileEntity2, aGrabFrom, aPutTo, aMaxTargetStackSize, aMinTargetStackSize, aMaxMoveAtOnce, aMinMoveAtOnce);
					if (tMovedItemCount > 0) return tMovedItemCount;
				}
			}
		}
		return 0;
	}
	
	public static boolean listContainsItem(Collection<ItemStack> aList, ItemStack aStack, boolean aTrueIfListEmpty, boolean aInvertFilter) {
		if (aStack == null || aStack.stackSize < 1) return false;
		if (aList == null) return aTrueIfListEmpty;
		while (aList.contains(null)) aList.remove(null);
		if (aList.size() < 1) return aTrueIfListEmpty;
		Iterator<ItemStack> tIterator = aList.iterator();
		ItemStack tStack = null;
		while (tIterator.hasNext()) if ((tStack = tIterator.next())!= null && areStacksEqual(aStack, tStack)) return !aInvertFilter;
		return aInvertFilter;
	}
	
	public static boolean areStacksOrToolsEqual(ItemStack aStack1, ItemStack aStack2) {
		if (aStack1 != null && aStack2 != null && aStack1.getItem() == aStack2.getItem()) {
			if (aStack1.getItem().isDamageable()) return true;
			return ((aStack1.getTagCompound() == null) == (aStack2.getTagCompound() == null)) && (aStack1.getTagCompound() == null || aStack1.getTagCompound().equals(aStack2.getTagCompound())) && (Items.feather.getDamage(aStack1) == Items.feather.getDamage(aStack2) || Items.feather.getDamage(aStack1) == GregTech_API.ITEM_WILDCARD_DAMAGE || Items.feather.getDamage(aStack2) == GregTech_API.ITEM_WILDCARD_DAMAGE);
		}
		return false;
	}
	
	public static boolean areStacksEqual(ItemStack aStack1, ItemStack aStack2) {
		return areStacksEqual(aStack1, aStack2, false);
	}
	
	public static boolean areStacksEqual(ItemStack aStack1, ItemStack aStack2, boolean aIgnoreNBT) {
		return aStack1 != null && aStack2 != null && aStack1.getItem() == aStack2.getItem() && (aIgnoreNBT || ((aStack1.getTagCompound() == null) == (aStack2.getTagCompound() == null)) && (aStack1.getTagCompound() == null || aStack1.getTagCompound().equals(aStack2.getTagCompound()))) && (Items.feather.getDamage(aStack1) == Items.feather.getDamage(aStack2) || Items.feather.getDamage(aStack1) == GregTech_API.ITEM_WILDCARD_DAMAGE || Items.feather.getDamage(aStack2) == GregTech_API.ITEM_WILDCARD_DAMAGE);
	}
	
	public static boolean areUnificationsEqual(ItemStack aStack1, ItemStack aStack2) {
		return areUnificationsEqual(aStack1, aStack2, false);
	}
	
	public static boolean areUnificationsEqual(ItemStack aStack1, ItemStack aStack2, boolean aIgnoreNBT) {
		return areStacksEqual(GT_OreDictUnificator.get(aStack1), GT_OreDictUnificator.get(aStack2), aIgnoreNBT);
	}
	
	public static String getFluidName(Fluid aFluid, boolean aLocalized) {
		if (aFluid == null) return "";
		String rName = aLocalized?aFluid.getLocalizedName():aFluid.getUnlocalizedName();
		if (rName.contains(".")) return capitalizeString(rName.replaceAll("fluid.", "").replaceAll("tile.", ""));
		return rName;
	}
	
	public static String getFluidName(FluidStack aFluid, boolean aLocalized) {
		if (aFluid == null) return "";
		return getFluidName(aFluid.getFluid(), aLocalized);
	}
	
	/**
	 * Forge screwed the Fluid Registry up again, so I make my own, which is also much more efficient than the stupid Stuff over there.
	 */
	private static final List<FluidContainerData> sFluidContainerList = new ArrayList<FluidContainerData>();
	private static final Map<GT_ItemStack, FluidContainerData> sFilledContainerToData = new HashMap<GT_ItemStack, FluidContainerData>();
	private static final Map<GT_ItemStack, Map<Fluid, FluidContainerData>> sEmptyContainerToFluidToData = new HashMap<GT_ItemStack, Map<Fluid, FluidContainerData>>();
	
	static {
		GregTech_API.sItemStackMappings.add(sFilledContainerToData);
		GregTech_API.sItemStackMappings.add(sEmptyContainerToFluidToData);
	}
	
	public static void reInit() {
		sFilledContainerToData.clear();
		sEmptyContainerToFluidToData.clear();
		for (FluidContainerData tData : sFluidContainerList) {
			sFilledContainerToData.put(new GT_ItemStack(tData.filledContainer), tData);
			Map<Fluid, FluidContainerData> tFluidToContainer = sEmptyContainerToFluidToData.get(new GT_ItemStack(tData.emptyContainer));
			if (tFluidToContainer == null) sEmptyContainerToFluidToData.put(new GT_ItemStack(tData.emptyContainer), tFluidToContainer = new HashMap<Fluid, FluidContainerData>());
			tFluidToContainer.put(tData.fluid.getFluid(), tData);
		}
	}
	
	public static void addFluidContainerData(FluidContainerData aData) {
		sFluidContainerList.add(aData);
		sFilledContainerToData.put(new GT_ItemStack(aData.filledContainer), aData);
		Map<Fluid, FluidContainerData> tFluidToContainer = sEmptyContainerToFluidToData.get(new GT_ItemStack(aData.emptyContainer));
		if (tFluidToContainer == null) sEmptyContainerToFluidToData.put(new GT_ItemStack(aData.emptyContainer), tFluidToContainer = new HashMap<Fluid, FluidContainerData>());
		tFluidToContainer.put(aData.fluid.getFluid(), aData);
	}
	
    public static ItemStack fillFluidContainer(FluidStack aFluid, ItemStack aStack, boolean aRemoveFluidDirectly) {
		if (isStackInvalid(aStack) || aFluid == null) return null;
		if (GT_ModHandler.isWater(aFluid) && ItemList.Bottle_Empty.isStackEqual(aStack)) {
			if (aFluid.amount >= 250) {
				if (aRemoveFluidDirectly) aFluid.amount -= 250;
				return new ItemStack(Items.potionitem, 1, 0);
			}
			return null;
		}
    	if (aStack.getItem() instanceof IFluidContainerItem && ((IFluidContainerItem)aStack.getItem()).getFluid(aStack) == null && ((IFluidContainerItem)aStack.getItem()).getCapacity(aStack) <= aFluid.amount) {
			if (aRemoveFluidDirectly)
	    		aFluid.amount -= ((IFluidContainerItem)aStack.getItem()).fill(aStack = copyAmount(1, aStack), aFluid, true);
			else
	    		((IFluidContainerItem)aStack.getItem()).fill(aStack = copyAmount(1, aStack), aFluid, true);
			return aStack;
    	}
    	Map<Fluid, FluidContainerData> tFluidToContainer = sEmptyContainerToFluidToData.get(new GT_ItemStack(aStack));
    	if (tFluidToContainer == null) return null;
    	FluidContainerData tData = tFluidToContainer.get(aFluid.getFluid());
    	if (tData == null || tData.fluid.amount > aFluid.amount) return null;
    	if (aRemoveFluidDirectly) aFluid.amount -= tData.fluid.amount;
		return copyAmount(1, tData.filledContainer);
    }
    
    public static ItemStack getFluidDisplayStack(FluidStack aFluid, boolean aUseStackSize) {
    	if (aFluid == null || aFluid.getFluid() == null) return null;
    	ItemStack rStack = ItemList.Display_Fluid.getWithDamage(aUseStackSize ? aFluid.amount / 1000 : 1, aFluid.fluidID);
    	NBTTagCompound tNBT = new NBTTagCompound();
		tNBT.setLong("mFluidDisplayAmount", aFluid.amount);
		tNBT.setLong("mFluidDisplayHeat", aFluid.getFluid().getTemperature(aFluid));
		tNBT.setBoolean("mFluidState", aFluid.getFluid().isGaseous(aFluid));
		rStack.setTagCompound(tNBT);
    	return rStack;
    }
    
    public static boolean containsFluid(ItemStack aStack, FluidStack aFluid) {
		if (isStackInvalid(aStack) || aFluid == null) return false;
    	if (aStack.getItem() instanceof IFluidContainerItem) return aFluid.isFluidEqual(((IFluidContainerItem)aStack.getItem()).getFluid(aStack = copyAmount(1, aStack)));
    	FluidContainerData tData = sFilledContainerToData.get(new GT_ItemStack(aStack));
    	return tData==null?false:tData.fluid.isFluidEqual(aFluid);
    }
    
	public static FluidStack getFluidForFilledItem(ItemStack aStack) {
		if (isStackInvalid(aStack)) return null;
		if (aStack.getItem() instanceof IFluidContainerItem) return ((IFluidContainerItem)aStack.getItem()).drain(copyAmount(1, aStack), Integer.MAX_VALUE, true);
		FluidContainerData tData = sFilledContainerToData.get(new GT_ItemStack(aStack));
		return tData==null?null:tData.fluid.copy();
	}
	
	public static ItemStack getContainerForFilledItem(ItemStack aStack) {
		if (isStackInvalid(aStack)) return null;
		FluidContainerData tData = sFilledContainerToData.get(new GT_ItemStack(aStack));
		if (tData != null) return copyAmount(1, tData.emptyContainer);
		if (aStack.getItem() instanceof IFluidContainerItem) {
			((IFluidContainerItem)aStack.getItem()).drain(aStack = copyAmount(1, aStack), Integer.MAX_VALUE, true);
			return aStack;
		}
		return null;
	}
	
	public static ItemStack getContainerItem(ItemStack aStack) {
		if (isStackInvalid(aStack)) return null;
		if (aStack.getItem().hasContainerItem(aStack)) return aStack.getItem().getContainerItem(aStack);
		/** These are all special Cases, in which it is intended to have only GT Blocks outputting those Container Items */
		if (ItemList.Cell_Empty.isStackEqual(aStack, false, true)) return null;
		if (ItemList.IC2_Fuel_Can_Filled.isStackEqual(aStack, false, true)) return ItemList.IC2_Fuel_Can_Empty.get(1);
		if (ItemList.Fuel_Can_Plastic_Filled.isStackEqual(aStack, false, true)) return ItemList.Fuel_Can_Plastic_Empty.get(1);
		if (aStack.getItem() == Items.potionitem || aStack.getItem() == Items.experience_bottle) return ItemList.Bottle_Empty.get(1);
		
		if (aStack.getItem() instanceof IFluidContainerItem) {
			ItemStack tStack = copyAmount(1, aStack);
			((IFluidContainerItem)aStack.getItem()).drain(tStack, Integer.MAX_VALUE, true);
			if (!areStacksEqual(aStack, tStack)) return tStack;
			return null;
		}
		
		int tCapsuleCount = GT_ModHandler.getCapsuleCellContainerCount(aStack);
		if (tCapsuleCount > 0) return ItemList.Cell_Empty.get(tCapsuleCount);
		
		if (ItemList.IC2_ForgeHammer.isStackEqual(aStack) || ItemList.IC2_WireCutter.isStackEqual(aStack)) return copyMetaData(Items.feather.getDamage(aStack) + 1, aStack);
		return null;
	}
	
	public static synchronized boolean removeSimpleIC2MachineRecipe(ItemStack aInput, Map<IRecipeInput, RecipeOutput> aRecipeList, ItemStack aOutput) {
		if ((isStackInvalid(aInput) && isStackInvalid(aOutput)) || aRecipeList == null) return false;
		boolean rReturn = false;
		Iterator<Map.Entry<IRecipeInput, RecipeOutput>> tIterator = aRecipeList.entrySet().iterator();
		aOutput = GT_OreDictUnificator.get(aOutput);
		while (tIterator.hasNext()) {
			Map.Entry<IRecipeInput, RecipeOutput> tEntry = tIterator.next();
			if (aInput == null || tEntry.getKey().matches(aInput)) {
				List<ItemStack> tList = tEntry.getValue().items;
				if (tList != null) for (ItemStack tOutput : tList) if (aOutput == null || areStacksEqual(GT_OreDictUnificator.get(tOutput), aOutput)) {
					tIterator.remove();
					rReturn = true;
					break;
				}
			}
		}
		return rReturn;
	}
	
	public static boolean addSimpleIC2MachineRecipe(ItemStack aInput, Map<IRecipeInput, RecipeOutput> aRecipeList, NBTTagCompound aNBT, Object... aOutput) {
		if (isStackInvalid(aInput) || aOutput.length == 0 || aRecipeList == null) return false;
		OrePrefixMaterialData tOreName = GT_OreDictUnificator.getAssociation(aInput);
		if (tOreName != null) {
			aRecipeList.put(new RecipeInputOreDict(tOreName.toString(), aInput.stackSize), new RecipeOutput(aNBT, GT_OreDictUnificator.getStackArray(true, aOutput)));
		} else {
			aRecipeList.put(new RecipeInputItemStack(copy(aInput), aInput.stackSize), new RecipeOutput(aNBT, GT_OreDictUnificator.getStackArray(true, aOutput)));
		}
		return true;
	}
	
	private static int sBookCount = 0;
	
	public static ItemStack getWrittenBook(String aTitle, String aAuthor, String... aPages) {
		if (isStringInvalid(aTitle) || isStringInvalid(aAuthor) || aPages.length <= 0) return null;
		sBookCount++;
		ItemStack rStack = new ItemStack(Items.written_book, 1);
        NBTTagCompound tNBT = new NBTTagCompound();
        tNBT.setString("title", GT_LanguageManager.addStringLocalization("Book." + aTitle + ".Name", aTitle));
        tNBT.setString("author", aAuthor);
        NBTTagList tNBTList = new NBTTagList();
        for (byte i = 0; i < aPages.length; i++) {
        	aPages[i] = GT_LanguageManager.addStringLocalization("Book." + aTitle + ".Page" + ((i<10)?"0"+i:i), aPages[i]);
	        if (i < 48) {
	        	if (aPages[i].length() < 256)
	        		tNBTList.appendTag(new NBTTagString(aPages[i]));
	        	else
	        		GT_Log.err.println("WARNING: String for written Book too long! -> " + aPages[i]);
	        } else {
        		GT_Log.err.println("WARNING: Too much Pages for written Book! -> " + aTitle);
	        	break;
	        }
        }
		tNBTList.appendTag(new NBTTagString("Credits to " + aAuthor + " for writing this Book. This was Book Nr. " + sBookCount + " at its creation. Gotta get 'em all!"));
        tNBT.setTag("pages", tNBTList);
        rStack.setTagCompound(tNBT);
        GregTech_API.sBookList.put(aTitle, rStack);
		return copy(rStack);
	}
	
	public static Map<GT_PlayedSound, Integer> sPlayedSoundMap = new HashMap<GT_PlayedSound, Integer>();
	
	public static boolean doSoundAtClient(String aSoundName, int aTimeUntilNextSound, float aSoundStrength) {
		return doSoundAtClient(aSoundName, aTimeUntilNextSound, aSoundStrength, GregTech_API.gregtech.getThePlayer());
	}
	
	public static boolean doSoundAtClient(String aSoundName, int aTimeUntilNextSound, float aSoundStrength, Entity aEntity) {
		if (aEntity == null) return false;
		return doSoundAtClient(aSoundName, aTimeUntilNextSound, aSoundStrength, aEntity.posX, aEntity.posY, aEntity.posZ);
	}
	
	public static boolean doSoundAtClient(String aSoundName, int aTimeUntilNextSound, float aSoundStrength, double aX, double aY, double aZ) {
		return doSoundAtClient(aSoundName, aTimeUntilNextSound, aSoundStrength, 0.9F + new Random().nextFloat() * 0.2F, aX, aY, aZ);
	}
	
	public static boolean doSoundAtClient(String aSoundName, int aTimeUntilNextSound, float aSoundStrength, float aSoundModulation, double aX, double aY, double aZ) {
		if (GT_Utility.isStringInvalid(aSoundName) || !FMLCommonHandler.instance().getEffectiveSide().isClient() || GregTech_API.gregtech.getThePlayer() == null || !GregTech_API.gregtech.getThePlayer().worldObj.isRemote) return false;
		if (GregTech_API.sMultiThreadedSounds)
			new Thread(new GT_Runnable_Sound(GregTech_API.gregtech.getThePlayer().worldObj, (int)aX, (int)aY, (int)aZ, aTimeUntilNextSound, aSoundName, aSoundStrength, aSoundModulation), "Sound Effect").start();
		else
			new GT_Runnable_Sound(GregTech_API.gregtech.getThePlayer().worldObj, (int)aX, (int)aY, (int)aZ, aTimeUntilNextSound, aSoundName, aSoundStrength, aSoundModulation).run();
		return true;
	}
	
	public static boolean sendSoundToPlayers(World aWorld, String aSoundName, float aSoundStrength, float aSoundModulation, int aX, int aY, int aZ) {
		if (isStringInvalid(aSoundName) || aWorld == null || aWorld.isRemote) return false;
        GregTech_API.sNetworkHandler.sendPacketToAllPlayersInRange(aWorld, new GT_Packet_Sound(aSoundName, aSoundStrength, aSoundModulation, aX, (short)aY, aZ), aX, aZ);
		return true;
	}
	
	public static int stackToInt(ItemStack aStack) {
		if (isStackInvalid(aStack)) return 0;
		return Item.getIdFromItem(aStack.getItem()) | (Items.feather.getDamage(aStack)<<16);
	}
	
	public static int stackToWildcard(ItemStack aStack) {
		if (isStackInvalid(aStack)) return 0;
		return Item.getIdFromItem(aStack.getItem()) | (GregTech_API.ITEM_WILDCARD_DAMAGE<<16);
	}
	
	public static ItemStack intToStack(int aStack) {
		int tID = aStack&(~0>>>16), tMeta = aStack>>>16;
	    Item tItem = Item.getItemById(tID);
		if (tItem != null) return new ItemStack(tItem, 1, tMeta);
		return null;
	}
	
	public static Integer[] stacksToIntegerArray(ItemStack... aStacks) {
		Integer[] rArray = new Integer[aStacks.length];
		for (int i = 0; i < rArray.length; i++) {
			rArray[i] = stackToInt(aStacks[i]);
		}
		return rArray;
	}
	
	public static int[] stacksToIntArray(ItemStack... aStacks) {
		int[] rArray = new int[aStacks.length];
		for (int i = 0; i < rArray.length; i++) {
			rArray[i] = stackToInt(aStacks[i]);
		}
		return rArray;
	}
	
	public static boolean arrayContains(Object aObject, Object... aObjects) {
		return listContains(aObject, Arrays.asList(aObjects));
	}
	
	public static boolean listContains(Object aObject, Collection aObjects) {
		if (aObjects == null) return false;
		return aObjects.contains(aObject);
	}
	
	public static <T> boolean arrayContainsNonNull(T... aArray) {
		if (aArray != null) for (Object tObject : aArray) if (tObject != null) return true;
		return false;
	}
	
	public static <T> ArrayList<T> getArrayListWithoutNulls(T... aArray) {
		if (aArray == null) return new ArrayList<T>();
		ArrayList<T> rList = new ArrayList<T>(Arrays.asList(aArray));
		for (int i = 0; i < rList.size(); i++) if (rList.get(i) == null) rList.remove(i--);
		return rList;
	}
	
	public static <T> ArrayList<T> getArrayListWithoutTrailingNulls(T... aArray) {
		if (aArray == null) return new ArrayList<T>();
		ArrayList<T> rList = new ArrayList<T>(Arrays.asList(aArray));
		for (int i = rList.size() - 1; i >= 0 && rList.get(i) == null;) rList.remove(i--);
		return rList;
	}
	
	public static Block getBlock(Object aBlock) {
		return (Block)aBlock;
	}
	
	public static Block getBlockFromStack(Object aStack) {
		if (isStackInvalid(aStack)) return Blocks.air;
		return Block.getBlockFromItem(((ItemStack)aStack).getItem());
	}
	
	public static boolean isBlockValid(Object aBlock) {
		return aBlock != null &&  (aBlock instanceof Block);
	}
	
	public static boolean isBlockInvalid(Object aBlock) {
		return aBlock == null || !(aBlock instanceof Block);
	}
	
	public static boolean isStringValid(Object aString) {
		return aString != null && !aString.toString().isEmpty();
	}
	
	public static boolean isStringInvalid(Object aString) {
		return aString == null || aString.toString().isEmpty();
	}
	
	public static boolean isStackValid(Object aStack) {
		return aStack != null &&  (aStack instanceof ItemStack) && ((ItemStack)aStack).getItem() != null && ((ItemStack)aStack).stackSize >= 0;
	}
	
	public static boolean isStackInvalid(Object aStack) {
		return aStack == null || !(aStack instanceof ItemStack) || ((ItemStack)aStack).getItem() == null || ((ItemStack)aStack).stackSize <  0;
	}
	
	public static boolean isDebugItem(ItemStack aStack) {
		return ItemList.Armor_Cheat.isStackEqual(aStack, true, true) || areStacksEqual(GT_ModHandler.getIC2Item("debug", 1), aStack, true);
	}
	
	public static boolean isOpaqueBlock(World aWorld, int aX, int aY, int aZ) {
		return aWorld.getBlock(aX, aY, aZ).isOpaqueCube();
	}
	
	public static boolean isAirBlock(World aWorld, int aX, int aY, int aZ) {
		return aWorld.getBlock(aX, aY, aZ).isAir(aWorld, aX, aY, aZ);
	}
	
	public static boolean hasBlockHitBox(World aWorld, int aX, int aY, int aZ) {
		return aWorld.getBlock(aX, aY, aZ).getCollisionBoundingBoxFromPool(aWorld, aX, aY, aZ) != null;
	}
	
	/**
	 * Converts a Number to a String
	 */
    public static String parseNumberToString(int aNumber) {
    	String tString = "";
    	boolean temp = true, negative = false;
    	
    	if (aNumber<0) {
    		aNumber *= -1;
    		negative = true;
    	}
    	
    	for (int i = 1000000000; i > 0; i /= 10) {
    		int tDigit = (aNumber/i)%10;
			if ( temp && tDigit != 0) temp = false;
			if (!temp) {
				tString += tDigit;
				if (i != 1) for (int j = i; j > 0; j /= 1000) if (j == 1) tString += ",";
			}
    	}
    	
    	if (tString.equals("")) tString = "0";
    	
    	return negative?"-"+tString:tString;
    }
    
    public static NBTTagCompound getNBTContainingBoolean(NBTTagCompound aNBT, Object aTag, boolean aValue) {
    	if (aNBT == null) aNBT = new NBTTagCompound();
    	aNBT.setBoolean(aTag.toString(), aValue);
    	return aNBT;
    }
    
    public static NBTTagCompound getNBTContainingByte(NBTTagCompound aNBT, Object aTag, byte aValue) {
    	if (aNBT == null) aNBT = new NBTTagCompound();
    	aNBT.setByte(aTag.toString(), aValue);
    	return aNBT;
    }
    
    public static NBTTagCompound getNBTContainingShort(NBTTagCompound aNBT, Object aTag, short aValue) {
    	if (aNBT == null) aNBT = new NBTTagCompound();
    	aNBT.setShort(aTag.toString(), aValue);
    	return aNBT;
    }
    
    public static NBTTagCompound getNBTContainingInteger(NBTTagCompound aNBT, Object aTag, int aValue) {
    	if (aNBT == null) aNBT = new NBTTagCompound();
    	aNBT.setInteger(aTag.toString(), aValue);
    	return aNBT;
    }
    
    public static NBTTagCompound getNBTContainingFloat(NBTTagCompound aNBT, Object aTag, float aValue) {
    	if (aNBT == null) aNBT = new NBTTagCompound();
    	aNBT.setFloat(aTag.toString(), aValue);
    	return aNBT;
    }
    
    public static NBTTagCompound getNBTContainingDouble(NBTTagCompound aNBT, Object aTag, double aValue) {
    	if (aNBT == null) aNBT = new NBTTagCompound();
    	aNBT.setDouble(aTag.toString(), aValue);
    	return aNBT;
    }
    
    public static NBTTagCompound getNBTContainingString(NBTTagCompound aNBT, Object aTag, Object aValue) {
    	if (aNBT == null) aNBT = new NBTTagCompound();
    	if (aValue == null) return aNBT;
    	aNBT.setString(aTag.toString(), aValue.toString());
    	return aNBT;
    }
    
    public static boolean isWearingFullFrostHazmat(EntityLivingBase aEntity) {
		for (byte i = 1; i < 5; i++) if (!isStackInList(aEntity.getEquipmentInSlot(i), GregTech_API.sFrostHazmatList)) return false;
		return true;
    }
    
    public static boolean isWearingFullHeatHazmat(EntityLivingBase aEntity) {
		for (byte i = 1; i < 5; i++) if (!isStackInList(aEntity.getEquipmentInSlot(i), GregTech_API.sHeatHazmatList)) return false;
		return true;
    }
    
    public static boolean isWearingFullBioHazmat(EntityLivingBase aEntity) {
		for (byte i = 1; i < 5; i++) if (!isStackInList(aEntity.getEquipmentInSlot(i), GregTech_API.sRadioHazmatList)) return false;
		return true;
    }
    
    public static boolean isWearingFullRadioHazmat(EntityLivingBase aEntity) {
		for (byte i = 1; i < 5; i++) if (!isStackInList(aEntity.getEquipmentInSlot(i), GregTech_API.sRadioHazmatList)) return false;
		return true;
    }
    
    public static boolean isWearingFullElectroHazmat(EntityLivingBase aEntity) {
		for (byte i = 1; i < 5; i++) if (!isStackInList(aEntity.getEquipmentInSlot(i), GregTech_API.sElectroHazmatList)) return false;
		return true;
    }
    
    public static boolean isWearingFullGasHazmat(EntityLivingBase aEntity) {
		for (byte i = 1; i < 5; i++) if (!isStackInList(aEntity.getEquipmentInSlot(i), GregTech_API.sHeatHazmatList)) return false;
		return true;
    }
    
    public static int getRadioactivityLevel(ItemStack aStack) {
    	for (Materials tMaterial : Materials.RADIOACTIVE_MATERIALS) if (tMaterial.contains(aStack)) {
    		if (tMaterial.mEnchantmentArmors instanceof Enchantment_Radioactivity) return tMaterial.mEnchantmentArmorsLevel;
        	if (tMaterial.mEnchantmentTools instanceof Enchantment_Radioactivity) return tMaterial.mEnchantmentToolsLevel;
    	}
    	return EnchantmentHelper.getEnchantmentLevel(Enchantment_Radioactivity.INSTANCE.effectId, aStack);
    }
    
    public static boolean isImmuneToBreathingGasses(EntityLivingBase aEntity) {
    	return isWearingFullGasHazmat(aEntity);
    }
    
    public static boolean applyHeatDamage(EntityLivingBase aEntity, float aDamage) {
    	if (aDamage > 0 && aEntity != null && aEntity.getActivePotionEffect(Potion.fireResistance) == null && !isWearingFullHeatHazmat(aEntity)) {
        	aEntity.attackEntityFrom(GT_DamageSources.getHeatDamage(), aDamage);
    		return true;
    	}
    	return false;
    }
    
    public static boolean applyFrostDamage(EntityLivingBase aEntity, float aDamage) {
    	if (aDamage > 0 && aEntity != null && !isWearingFullFrostHazmat(aEntity)) {
        	aEntity.attackEntityFrom(GT_DamageSources.getFrostDamage(), aDamage);
    		return true;
    	}
    	return false;
    }
    
    public static boolean applyElectricityDamage(EntityLivingBase aEntity, long aVoltage, long aAmperage) {
    	long aDamage = getTier(aVoltage) * aAmperage * 4;
    	if (aDamage > 0 && aEntity != null && !isWearingFullElectroHazmat(aEntity)) {
        	aEntity.attackEntityFrom(ic2.api.info.Info.DMG_ELECTRIC, aDamage);
    		return true;
    	}
    	return false;
    }
    
    public static boolean applyRadioactivity(EntityLivingBase aEntity, int aLevel) {
    	if (aLevel > 0 && aEntity != null && aEntity.getCreatureAttribute() != EnumCreatureAttribute.UNDEAD && aEntity.getCreatureAttribute() != EnumCreatureAttribute.ARTHROPOD && !isWearingFullRadioHazmat(aEntity)) {
        	aEntity.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id	, aLevel * 1000, Math.max(1, (5*aLevel) / 7)));
        	aEntity.addPotionEffect(new PotionEffect(Potion.digSlowdown.id	, aLevel * 1000, Math.max(1, (5*aLevel) / 7)));
        	aEntity.addPotionEffect(new PotionEffect(Potion.confusion.id	, aLevel * 1000, Math.max(1, (5*aLevel) / 7)));
        	aEntity.addPotionEffect(new PotionEffect(Potion.weakness.id		, aLevel * 1000, Math.max(1, (5*aLevel) / 7)));
        	aEntity.addPotionEffect(new PotionEffect(Potion.hunger.id		, aLevel * 1000, Math.max(1, (5*aLevel) / 7)));
        	aEntity.addPotionEffect(new PotionEffect(24 /* IC2 Radiation */	, aLevel * 1000, Math.max(1, (5*aLevel) / 7)));
        	return true;
    	}
    	return false;
    }
    
	public static ItemStack setStack(Object aSetStack, Object aToStack) {
		if (isStackInvalid(aSetStack) || isStackInvalid(aToStack)) return null;
		((ItemStack)aSetStack).func_150996_a(((ItemStack)aToStack).getItem());
		((ItemStack)aSetStack).stackSize = ((ItemStack)aToStack).stackSize;
		Items.feather.setDamage((ItemStack)aSetStack, Items.feather.getDamage((ItemStack)aToStack));
		((ItemStack)aSetStack).setTagCompound(((ItemStack)aToStack).getTagCompound());
		return (ItemStack)aSetStack;
	}
	
	public static ItemStack[] copyStackArray(Object... aStacks) {
		ItemStack[] rStacks = new ItemStack[aStacks.length];
		for (int i = 0; i < aStacks.length; i++) rStacks[i] = copy(aStacks[i]);
		return rStacks;
	}
	
	public static ItemStack copy(Object... aStacks) {
		for (Object tStack : aStacks) if (isStackValid(tStack)) return ((ItemStack)tStack).copy();
		return null;
	}
	
	public static ItemStack copyAmount(long aAmount, Object... aStacks) {
		ItemStack rStack = copy(aStacks);
		if (isStackInvalid(rStack)) return null;
		if (aAmount > 64) aAmount = 64; else if (aAmount == -1) aAmount = 111; else if (aAmount < 0) aAmount = 0;
		rStack.stackSize = (byte)aAmount;
		return rStack;
	}
	
	public static ItemStack copyMetaData(long aMetaData, Object... aStacks) {
		ItemStack rStack = copy(aStacks);
		if (isStackInvalid(rStack)) return null;
		Items.feather.setDamage(rStack, (short)aMetaData);
		return rStack;
	}
	
	public static ItemStack copyAmountAndMetaData(long aAmount, long aMetaData, Object... aStacks) {
		ItemStack rStack = copyAmount(aAmount, aStacks);
		if (isStackInvalid(rStack)) return null;
		Items.feather.setDamage(rStack, (short)aMetaData);
		return rStack;
	}
	
	/**
	 * returns a copy of an ItemStack with its Stacksize being multiplied by aMultiplier
	 */
	public static ItemStack mul(long aMultiplier, Object... aStacks) {
		ItemStack rStack = copy(aStacks);
		if (rStack == null) return null;
		rStack.stackSize *= aMultiplier;
		return rStack;
	}
	
	/**
	 * Loads an ItemStack properly.
	 */
	public static ItemStack loadItem(NBTTagCompound aNBT) {
		ItemStack rStack = ItemStack.loadItemStackFromNBT(aNBT);
		try {
			if (rStack != null && (rStack.getItem().getClass().getName().startsWith("ic2.core.migration"))) {
				rStack.getItem().onUpdate(rStack, GregTech_API.sDummyWorld, null, 0, false);
			}
		} catch(Throwable e) {
			e.printStackTrace(GT_Log.err);
		}
		return GT_OreDictUnificator.get(true, rStack);
	}
	
	public static <E> E selectItemInList(int aIndex, E aReplacement, List<E> aList) {
		if (aList == null || aList.isEmpty()) return aReplacement;
		if (aList.size() <= aIndex) return aList.get(aList.size() - 1);
		if (aIndex < 0) return aList.get(0);
		return aList.get(aIndex);
	}
	
	public static <E> E selectItemInList(int aIndex, E aReplacement, E... aList) {
		if (aList == null || aList.length == 0) return aReplacement;
		if (aList.length <= aIndex) return aList[aList.length - 1];
		if (aIndex < 0) return aList[0];
		return aList[aIndex];
	}
	
	public static boolean isStackInList(ItemStack aStack, Collection<GT_ItemStack> aList) {
		if (aStack == null) return false;
		return isStackInList(new GT_ItemStack(aStack), aList);
	}
	
	public static boolean isStackInList(GT_ItemStack aStack, Collection<GT_ItemStack> aList) {
		return aStack != null && (aList.contains(aStack) || aList.contains(new GT_ItemStack(aStack.mItem, aStack.mStackSize, GregTech_API.ITEM_WILDCARD_DAMAGE)));
	}
	
    /**
     * re-maps all Keys of a Map after the Keys were weakened.
     */
	public static <K, V> Map<K, V> reMap(Map<K, V> aMap) {
		Map<K, V> tMap = new HashMap<K, V>();
		tMap.putAll(aMap);
		aMap.clear();
		aMap.putAll(tMap);
		return aMap;
	}
	
	/**
	 * Why the fuck do neither Java nor Guava have a Function to do this?
	 */
    public static <K, V extends Comparable> LinkedHashMap<K,V> sortMapByValuesAcending(Map<K,V> aMap) {
        List<Map.Entry<K,V>> tEntrySet = new LinkedList<Map.Entry<K,V>>(aMap.entrySet());
        Collections.sort(tEntrySet, new Comparator<Map.Entry<K,V>>() {@Override public int compare(Entry<K, V> aValue1, Entry<K, V> aValue2) {return aValue1.getValue().compareTo(aValue2.getValue());}});
        LinkedHashMap<K,V> rMap = new LinkedHashMap<K,V>();
        for (Map.Entry<K,V> tEntry : tEntrySet) rMap.put(tEntry.getKey(), tEntry.getValue());
        return rMap;
    }
    
	/**
	 * Why the fuck do neither Java nor Guava have a Function to do this?
	 */
    public static <K, V extends Comparable> LinkedHashMap<K,V> sortMapByValuesDescending(Map<K,V> aMap) {
        List<Map.Entry<K,V>> tEntrySet = new LinkedList<Map.Entry<K,V>>(aMap.entrySet());
        Collections.sort(tEntrySet, new Comparator<Map.Entry<K,V>>() {@Override public int compare(Entry<K, V> aValue1, Entry<K, V> aValue2) {return -aValue1.getValue().compareTo(aValue2.getValue());}});
        LinkedHashMap<K,V> rMap = new LinkedHashMap<K,V>();
        for (Map.Entry<K,V> tEntry : tEntrySet) rMap.put(tEntry.getKey(), tEntry.getValue());
        return rMap;
    }
    
	/**
	 * This checks if the Dimension is really a Dimension and not another Planet or something.
	 * Used for my Teleporter.
	 */
	public static boolean isRealDimension(int aDimensionID) {
		try {
			if (DimensionManager.getProvider(aDimensionID).getClass().getName().contains("com.xcompwiz.mystcraft")) return true;
		} catch (Throwable e) {/*Do nothing*/}
		try {
			if (DimensionManager.getProvider(aDimensionID).getClass().getName().contains("TwilightForest")) return true;
		} catch (Throwable e) {/*Do nothing*/}
		return GregTech_API.sDimensionalList.contains(aDimensionID);
	}
	
	public static boolean moveEntityToDimensionAtCoords(Entity aEntity, int aDimension, double aX, double aY, double aZ) {
		WorldServer tTargetWorld = DimensionManager.getWorld(aDimension), tOriginalWorld = DimensionManager.getWorld(aEntity.worldObj.provider.dimensionId);
		if (tTargetWorld != null && tOriginalWorld != null && tTargetWorld != tOriginalWorld) {
			if (aEntity.ridingEntity != null) aEntity.mountEntity(null);
			if (aEntity.riddenByEntity != null) aEntity.riddenByEntity.mountEntity(null);
			
			if (aEntity instanceof EntityPlayerMP) {
				EntityPlayerMP aPlayer = (EntityPlayerMP)aEntity;
		        aPlayer.dimension = aDimension;
		        aPlayer.playerNetServerHandler.sendPacket(new S07PacketRespawn(aPlayer.dimension, aPlayer.worldObj.difficultySetting, aPlayer.worldObj.getWorldInfo().getTerrainType(), aPlayer.theItemInWorldManager.getGameType()));
		        tOriginalWorld.removePlayerEntityDangerously(aPlayer);
		        aPlayer.isDead = false;
		        aPlayer.setWorld(tTargetWorld);
		        MinecraftServer.getServer().getConfigurationManager().func_72375_a(aPlayer, tOriginalWorld);
		        aPlayer.playerNetServerHandler.setPlayerLocation(aX+0.5, aY+0.5, aZ+0.5, aPlayer.rotationYaw, aPlayer.rotationPitch);
		        aPlayer.theItemInWorldManager.setWorld(tTargetWorld);
		        MinecraftServer.getServer().getConfigurationManager().updateTimeAndWeatherForPlayer(aPlayer, tTargetWorld);
		        MinecraftServer.getServer().getConfigurationManager().syncPlayerInventory(aPlayer);
		        Iterator tIterator = aPlayer.getActivePotionEffects().iterator();
		        while (tIterator.hasNext()) {
		            PotionEffect potioneffect = (PotionEffect)tIterator.next();
		            aPlayer.playerNetServerHandler.sendPacket(new S1DPacketEntityEffect(aPlayer.getEntityId(), potioneffect));
		        }
		        aPlayer.playerNetServerHandler.setPlayerLocation(aX+0.5, aY+0.5, aZ+0.5, aPlayer.rotationYaw, aPlayer.rotationPitch);
		        FMLCommonHandler.instance().firePlayerChangedDimensionEvent(aPlayer, tOriginalWorld.provider.dimensionId, aDimension);
			} else {
				aEntity.setPosition(aX+0.5, aY+0.5, aZ+0.5);
				aEntity.worldObj.removeEntity(aEntity);
				aEntity.dimension = aDimension;
				aEntity.isDead = false;
	            Entity tNewEntity = EntityList.createEntityByName(EntityList.getEntityString(aEntity), tTargetWorld);
	            if (tNewEntity != null) {
	            	tNewEntity.copyDataFrom(aEntity, true);
		            aEntity.setDead();
	            	tNewEntity.isDead = false;
	            	boolean temp = tNewEntity.forceSpawn;
		            tNewEntity.forceSpawn = true;
	            	tTargetWorld.spawnEntityInWorld(tNewEntity);
		            tNewEntity.forceSpawn = temp;
	            	tNewEntity.isDead = false;
	            	aEntity = tNewEntity;
	            }
			}
			
			if (aEntity instanceof EntityLivingBase) {
				((EntityLivingBase)aEntity).setPositionAndUpdate(aX, aY, aZ);
			} else {
				aEntity.setPosition(aX, aY, aZ);
			}
			
            tOriginalWorld.resetUpdateEntityTick();
            tTargetWorld.resetUpdateEntityTick();
			return true;
		}
		return false;
	}
	
	public static int getCoordinateScan(ArrayList<String> aList, EntityPlayer aPlayer, World aWorld, int aScanLevel, int aX, int aY, int aZ, int aSide, float aClickX, float aClickY, float aClickZ) {
		if (aList == null) return 0;
		
		ArrayList<String> tList = new ArrayList<String>();
		int rEUAmount = 0;
		
		TileEntity tTileEntity = aWorld.getTileEntity(aX, aY, aZ);
	    
	    Block tBlock = aWorld.getBlock(aX, aY, aZ);
	    
	    tList.add("----- X: " + aX + " Y: " + aY + " Z: " + aZ + " -----");
	    try {
		    if (tTileEntity != null && tTileEntity instanceof IInventory)
		    	tList.add("Name: " + ((IInventory)tTileEntity).getInventoryName() + "  MetaData: " + aWorld.getBlockMetadata(aX, aY, aZ));
		    else
		    	tList.add("Name: " + tBlock.getUnlocalizedName() + "  MetaData: " + aWorld.getBlockMetadata(aX, aY, aZ));
		    
		    tList.add("Hardness: " + tBlock.getBlockHardness(aWorld, aX, aY, aZ) + "  Blast Resistance: " + tBlock.getExplosionResistance(aPlayer, aWorld, aX, aY, aZ, aPlayer.posX, aPlayer.posY, aPlayer.posZ));
		    if (tBlock.isBeaconBase(aWorld, aX, aY, aZ, aX, aY+1, aZ)) tList.add("Is valid Beacon Pyramid Material");
		} catch(Throwable e) {if (GregTech_API.DEBUG_MODE) e.printStackTrace(GT_Log.err);}
	    if (tTileEntity != null) {
			try {if (tTileEntity instanceof IFluidHandler) {
				rEUAmount+=500;
			    FluidTankInfo[] tTanks = ((IFluidHandler)tTileEntity).getTankInfo(ForgeDirection.getOrientation(aSide));
			    if (tTanks != null) for (byte i = 0; i < tTanks.length; i++) {
			    	tList.add("Tank " + i + ": " + (tTanks[i].fluid==null?0:tTanks[i].fluid.amount) + " / " + tTanks[i].capacity + " " + getFluidName(tTanks[i].fluid, true));
			    }
			}} catch(Throwable e) {if (GregTech_API.DEBUG_MODE) e.printStackTrace(GT_Log.err);}
			try {if (tTileEntity instanceof ic2.api.reactor.IReactorChamber) {
				rEUAmount+=500;
			    tTileEntity = (TileEntity)(((ic2.api.reactor.IReactorChamber)tTileEntity).getReactor());
			}} catch(Throwable e) {if (GregTech_API.DEBUG_MODE) e.printStackTrace(GT_Log.err);}
			try {if (tTileEntity instanceof ic2.api.reactor.IReactor) {
				rEUAmount+=500;
				tList.add("Heat: " + ((ic2.api.reactor.IReactor)tTileEntity).getHeat() + "/" + ((ic2.api.reactor.IReactor)tTileEntity).getMaxHeat()
						+ "  HEM: " + ((ic2.api.reactor.IReactor)tTileEntity).getHeatEffectModifier() + "  Base EU Output: "/* + ((ic2.api.reactor.IReactor)tTileEntity).getOutput()*/);
			}} catch(Throwable e) {if (GregTech_API.DEBUG_MODE) e.printStackTrace(GT_Log.err);}
			try {if (tTileEntity instanceof ic2.api.tile.IWrenchable) {
				rEUAmount+=100;
		        tList.add("Facing: " + ((ic2.api.tile.IWrenchable)tTileEntity).getFacing() + " / Chance: " + (((ic2.api.tile.IWrenchable)tTileEntity).getWrenchDropRate()*100) + "%");
			    tList.add(((ic2.api.tile.IWrenchable)tTileEntity).wrenchCanRemove(aPlayer)?"You can remove this with a Wrench":"You can NOT remove this with a Wrench");
			}} catch(Throwable e) {if (GregTech_API.DEBUG_MODE) e.printStackTrace(GT_Log.err);}
			try {if (tTileEntity instanceof ic2.api.energy.tile.IEnergyTile) {
				rEUAmount+=200;
			    //aList.add(((ic2.api.energy.tile.IEnergyTile)tTileEntity).isAddedToEnergyNet()?"Added to E-net":"Not added to E-net! Bug?");
			}} catch(Throwable e) {if (GregTech_API.DEBUG_MODE) e.printStackTrace(GT_Log.err);}
			try {if (tTileEntity instanceof ic2.api.energy.tile.IEnergySink) {
				rEUAmount+=400;
		        //aList.add("Demanded Energy: " + ((ic2.api.energy.tile.IEnergySink)tTileEntity).demandsEnergy());
		        //tList.add("Max Safe Input: " + getTier(((ic2.api.energy.tile.IEnergySink)tTileEntity).getSinkTier()));
		        //tList.add("Max Safe Input: " + ((ic2.api.energy.tile.IEnergySink)tTileEntity).getMaxSafeInput());
		    }} catch(Throwable e) {if (GregTech_API.DEBUG_MODE) e.printStackTrace(GT_Log.err);}
			try {if (tTileEntity instanceof ic2.api.energy.tile.IEnergySource) {
				rEUAmount+=400;
		        //aList.add("Max Energy Output: " + ((ic2.api.energy.tile.IEnergySource)tTileEntity).getMaxEnergyOutput());
		    }} catch(Throwable e) {if (GregTech_API.DEBUG_MODE) e.printStackTrace(GT_Log.err);}
			try {if (tTileEntity instanceof ic2.api.energy.tile.IEnergyConductor) {
				rEUAmount+=200;
		        tList.add("Conduction Loss: " + ((ic2.api.energy.tile.IEnergyConductor)tTileEntity).getConductionLoss());
		    }} catch(Throwable e) {if (GregTech_API.DEBUG_MODE) e.printStackTrace(GT_Log.err);}
			try {if (tTileEntity instanceof ic2.api.tile.IEnergyStorage) {
				rEUAmount+=200;
		        tList.add("Contained Energy: " + ((ic2.api.tile.IEnergyStorage)tTileEntity).getStored() + " of " + ((ic2.api.tile.IEnergyStorage)tTileEntity).getCapacity());
		        //aList.add(((ic2.api.tile.IEnergyStorage)tTileEntity).isTeleporterCompatible(ic2.api.Direction.YP)?"Teleporter Compatible":"Not Teleporter Compatible");
			}} catch(Throwable e) {if (GregTech_API.DEBUG_MODE) e.printStackTrace(GT_Log.err);}
			try {if (tTileEntity instanceof IUpgradableMachine) {
				rEUAmount+=500;
		    	if (((IUpgradableMachine)tTileEntity).hasMufflerUpgrade()) tList.add("Has Muffler Upgrade");
		    }} catch(Throwable e) {if (GregTech_API.DEBUG_MODE) e.printStackTrace(GT_Log.err);}
			try {if (tTileEntity instanceof IMachineProgress) {
				rEUAmount+=400;
		    	int tValue = 0;
		    	if (0 < (tValue = ((IMachineProgress)tTileEntity).getMaxProgress())) tList.add("Progress: " + tValue + " / " + ((IMachineProgress)tTileEntity).getProgress());
		    }} catch(Throwable e) {if (GregTech_API.DEBUG_MODE) e.printStackTrace(GT_Log.err);}
			try {if (tTileEntity instanceof ICoverable) {
				rEUAmount+=300;
		    	String tString = ((ICoverable)tTileEntity).getCoverBehaviorAtSide((byte)aSide).getDescription((byte)aSide, ((ICoverable)tTileEntity).getCoverIDAtSide((byte)aSide), ((ICoverable)tTileEntity).getCoverDataAtSide((byte)aSide), (ICoverable)tTileEntity);
		    	if (tString != null && !tString.equals("")) tList.add(tString);
		    }} catch(Throwable e) {if (GregTech_API.DEBUG_MODE) e.printStackTrace(GT_Log.err);}
			try {if (tTileEntity instanceof IBasicEnergyContainer && ((IBasicEnergyContainer)tTileEntity).getEUCapacity() > 0) {
				tList.add("Max IN: " + ((IBasicEnergyContainer)tTileEntity).getInputVoltage() + " EU");
			    tList.add("Max OUT: " + ((IBasicEnergyContainer)tTileEntity).getOutputVoltage() + " EU at " + ((IBasicEnergyContainer)tTileEntity).getOutputAmperage() + " Amperes");
				tList.add("Energy: " + ((IBasicEnergyContainer)tTileEntity).getStoredEU() + " / " + ((IBasicEnergyContainer)tTileEntity).getEUCapacity() + "EU");
			}} catch(Throwable e) {if (GregTech_API.DEBUG_MODE) e.printStackTrace(GT_Log.err);}
			try {if (tTileEntity instanceof IGregTechTileEntity) {
		    	tList.add("Owned by: " + ((IGregTechTileEntity)tTileEntity).getOwnerName());
		    }} catch(Throwable e) {if (GregTech_API.DEBUG_MODE) e.printStackTrace(GT_Log.err);}
			try {if (tTileEntity instanceof IGregTechDeviceInformation && ((IGregTechDeviceInformation)tTileEntity).isGivingInformation()) {
				tList.addAll(Arrays.asList(((IGregTechDeviceInformation)tTileEntity).getInfoData()));
			}} catch(Throwable e) {if (GregTech_API.DEBUG_MODE) e.printStackTrace(GT_Log.err);}
			try {if (tTileEntity instanceof ic2.api.crops.ICropTile) {
				if (((ic2.api.crops.ICropTile)tTileEntity).getScanLevel() < 4) {
					rEUAmount+=10000;
					((ic2.api.crops.ICropTile)tTileEntity).setScanLevel((byte)4);
				}
				if (((ic2.api.crops.ICropTile)tTileEntity).getID() >= 0 && ((ic2.api.crops.ICropTile)tTileEntity).getID() < ic2.api.crops.Crops.instance.getCropList().length && ic2.api.crops.Crops.instance.getCropList()[((ic2.api.crops.ICropTile)tTileEntity).getID()] != null) {
					rEUAmount+=1000;
					tList.add("Type -- Crop-Name: " + ic2.api.crops.Crops.instance.getCropList()[((ic2.api.crops.ICropTile)tTileEntity).getID()].name()
			        		+ "  Growth: " + ((ic2.api.crops.ICropTile)tTileEntity).getGrowth()
			        		+ "  Gain: " + ((ic2.api.crops.ICropTile)tTileEntity).getGain()
			        		+ "  Resistance: " + ((ic2.api.crops.ICropTile)tTileEntity).getResistance()
			        		);
			        tList.add("Plant -- Fertilizer: " + ((ic2.api.crops.ICropTile)tTileEntity).getNutrientStorage()
			        		+ "  Water: " + ((ic2.api.crops.ICropTile)tTileEntity).getHydrationStorage()
			        		+ "  Weed-Ex: " + ((ic2.api.crops.ICropTile)tTileEntity).getWeedExStorage()
			        		+ "  Scan-Level: " + ((ic2.api.crops.ICropTile)tTileEntity).getScanLevel()
			        		);
			        tList.add("Environment -- Nutrients: " + ((ic2.api.crops.ICropTile)tTileEntity).getNutrients()
			        		+ "  Humidity: " + ((ic2.api.crops.ICropTile)tTileEntity).getHumidity()
			        		+ "  Air-Quality: " + ((ic2.api.crops.ICropTile)tTileEntity).getAirQuality()
			        		);
			        String tString = "";
			        for (String tAttribute : ic2.api.crops.Crops.instance.getCropList()[((ic2.api.crops.ICropTile)tTileEntity).getID()].attributes()) {
			        	tString += ", " + tAttribute;
			        }
			        tList.add("Attributes:" + tString.replaceFirst(",", ""));
			        tList.add("Discovered by: " + ic2.api.crops.Crops.instance.getCropList()[((ic2.api.crops.ICropTile)tTileEntity).getID()].discoveredBy());
				}
			}} catch(Throwable e) {if (GregTech_API.DEBUG_MODE) e.printStackTrace(GT_Log.err);}
    	}
	    try {if (tBlock instanceof IDebugableBlock) {
	    	rEUAmount+=500;
	        ArrayList<String> temp = ((IDebugableBlock)tBlock).getDebugInfo(aPlayer, aX, aY, aZ, 3);
	        if (temp != null) tList.addAll(temp);
	    }} catch(Throwable e) {if (GregTech_API.DEBUG_MODE) e.printStackTrace(GT_Log.err);}
	    
	    BlockScanningEvent tEvent = new BlockScanningEvent(aWorld, aPlayer, aX, aY, aZ, (byte)aSide, aScanLevel, tBlock, tTileEntity, tList, aClickX, aClickY, aClickZ);
	    tEvent.mEUCost = rEUAmount;
	    MinecraftForge.EVENT_BUS.post(tEvent);
	    if (!tEvent.isCanceled()) aList.addAll(tList);
		return tEvent.mEUCost;
	}
	
	/**
	 * @return an Array containing the X and the Y Coordinate of the clicked Point, with the top left Corner as Origin, like on the Texture Sheet. return values should always be between [0.0F and 0.99F].
	 */
	public static float[] getClickedFacingCoords(byte aSide, float aX, float aY, float aZ) {
		switch (aSide) {
		case  0: return new float[] {Math.min(0.99F, Math.max(0,1-aX)), Math.min(0.99F, Math.max(0,  aZ))};
		case  1: return new float[] {Math.min(0.99F, Math.max(0,  aX)), Math.min(0.99F, Math.max(0,  aZ))};
		case  2: return new float[] {Math.min(0.99F, Math.max(0,1-aX)), Math.min(0.99F, Math.max(0,1-aY))};
		case  3: return new float[] {Math.min(0.99F, Math.max(0,  aX)), Math.min(0.99F, Math.max(0,1-aY))};
		case  4: return new float[] {Math.min(0.99F, Math.max(0,  aZ)), Math.min(0.99F, Math.max(0,1-aY))};
		case  5: return new float[] {Math.min(0.99F, Math.max(0,1-aZ)), Math.min(0.99F, Math.max(0,1-aY))};
		default: return new float[] {0.5F, 0.5F};
		}
	}
	
	/**
	 * This Function determines the direction a Block gets when being Wrenched.
	 * returns -1 if invalid. Even though that could never happen.
	 */
	public static byte determineWrenchingSide(byte aSide, float aX, float aY, float aZ) {
		byte tBack = getOppositeSide(aSide);
		switch (aSide) {
		case  0: case  1:
			if (aX < 0.25) {
				if (aZ < 0.25) return tBack;
				if (aZ > 0.75) return tBack;
				return 4;
			}
			if (aX > 0.75) {
				if (aZ < 0.25) return tBack;
				if (aZ > 0.75) return tBack;
				return 5;
			}
			if (aZ < 0.25) return 2;
			if (aZ > 0.75) return 3;
			return aSide;
		case  2: case  3:
			if (aX < 0.25) {
				if (aY < 0.25) return tBack;
				if (aY > 0.75) return tBack;
				return 4;
			}
			if (aX > 0.75) {
				if (aY < 0.25) return tBack;
				if (aY > 0.75) return tBack;
				return 5;
			}
			if (aY < 0.25) return 0;
			if (aY > 0.75) return 1;
			return aSide;
		case  4: case  5:
			if (aZ < 0.25) {
				if (aY < 0.25) return tBack;
				if (aY > 0.75) return tBack;
				return 2;
			}
			if (aZ > 0.75) {
				if (aY < 0.25) return tBack;
				if (aY > 0.75) return tBack;
				return 3;
			}
			if (aY < 0.25) return 0;
			if (aY > 0.75) return 1;
			return aSide;
		}
		return -1;
	}
}