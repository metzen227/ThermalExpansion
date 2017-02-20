package cofh.thermalexpansion.util.crafting;

import cofh.core.util.oredict.OreDictionaryArbiter;
import cofh.lib.inventory.ComparableItemStack;
import cofh.lib.util.helpers.ItemHelper;
import cofh.thermalfoundation.init.TFFluids;
import cofh.thermalfoundation.item.ItemMaterial;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import java.util.*;
import java.util.Map.Entry;

public class TransposerManager {

	private static Map<List<Integer>, RecipeTransposer> recipeMapFill = new THashMap<List<Integer>, RecipeTransposer>();
	private static Map<ComparableItemStackTransposer, RecipeTransposer> recipeMapExtract = new THashMap<ComparableItemStackTransposer, RecipeTransposer>();
	private static Set<ComparableItemStackTransposer> validationSet = new THashSet<ComparableItemStackTransposer>();

	public static final int DEFAULT_ENERGY = 800;

	public static RecipeTransposer getFillRecipe(ItemStack input, FluidStack fluid) {

		return input == null || fluid == null || fluid.getFluid() == null ? null : recipeMapFill.get(Arrays.asList(new ComparableItemStackTransposer(input).hashCode(), fluid.getFluid().hashCode()));
	}

	public static RecipeTransposer getExtractRecipe(ItemStack input) {

		return input == null ? null : recipeMapExtract.get(new ComparableItemStackTransposer(input));
	}

	public static boolean fillRecipeExists(ItemStack input, FluidStack fluid) {

		return getFillRecipe(input, fluid) != null;
	}

	public static boolean extractRecipeExists(ItemStack input, FluidStack fluid) {

		return getExtractRecipe(input) != null;
	}

	public static RecipeTransposer[] getFillRecipeList() {

		return recipeMapFill.values().toArray(new RecipeTransposer[recipeMapFill.values().size()]);
	}

	public static RecipeTransposer[] getExtractRecipeList() {

		return recipeMapExtract.values().toArray(new RecipeTransposer[recipeMapExtract.values().size()]);
	}

	public static boolean isItemValid(ItemStack input) {

		return input != null && validationSet.contains(new ComparableItemStackTransposer(input));
	}

	public static void addDefaultRecipes() {

		addFillRecipe(8000, new ItemStack(Blocks.COBBLESTONE), new ItemStack(Blocks.MOSSY_COBBLESTONE), new FluidStack(FluidRegistry.WATER, 250), false);
		addFillRecipe(8000, new ItemStack(Blocks.STONEBRICK), new ItemStack(Blocks.STONEBRICK, 1, 1), new FluidStack(FluidRegistry.WATER, 250), false);
		addFillRecipe(8000, new ItemStack(Blocks.SANDSTONE), new ItemStack(Blocks.END_STONE), new FluidStack(TFFluids.fluidEnder, 250), false);
		addFillRecipe(8000, new ItemStack(Blocks.ICE), new ItemStack(Blocks.PACKED_ICE), new FluidStack(TFFluids.fluidCryotheum, 250), false);
		addFillRecipe(4000, new ItemStack(Items.BRICK), new ItemStack(Items.NETHERBRICK), new FluidStack(FluidRegistry.LAVA, 250), false);
		addFillRecipe(4000, new ItemStack(Items.GLOWSTONE_DUST), new ItemStack(Items.BLAZE_POWDER), new FluidStack(TFFluids.fluidRedstone, 200), false);
		addFillRecipe(4000, new ItemStack(Items.SNOWBALL), ItemHelper.cloneStack(ItemMaterial.dustBlizz, 1), new FluidStack(TFFluids.fluidRedstone, 200), false);
		addFillRecipe(4000, new ItemStack(Blocks.SAND), ItemHelper.cloneStack(ItemMaterial.dustBlitz), new FluidStack(TFFluids.fluidRedstone, 200), false);
		addFillRecipe(4000, ItemHelper.cloneStack(ItemMaterial.dustObsidian, 1), ItemHelper.cloneStack(ItemMaterial.dustBasalz, 1), new FluidStack(TFFluids.fluidRedstone, 200), false);
	}

	public static void loadRecipes() {

		addFillRecipe(2000, ItemHelper.getOre("oreCinnabar"), ItemHelper.cloneStack(ItemMaterial.crystalCinnabar, 1), new FluidStack(TFFluids.fluidCryotheum, 200), false);
	}

	public static void refreshRecipes() {

		Map<List<Integer>, RecipeTransposer> tempFill = new THashMap<List<Integer>, RecipeTransposer>(recipeMapFill.size());
		Map<ComparableItemStackTransposer, RecipeTransposer> tempExtract = new THashMap<ComparableItemStackTransposer, RecipeTransposer>(recipeMapExtract.size());
		Set<ComparableItemStackTransposer> tempSet = new THashSet<ComparableItemStackTransposer>();
		RecipeTransposer tempRecipe;

		for (Entry<List<Integer>, RecipeTransposer> entry : recipeMapFill.entrySet()) {
			tempRecipe = entry.getValue();
			ComparableItemStackTransposer input = new ComparableItemStackTransposer(tempRecipe.input);
			FluidStack fluid = tempRecipe.fluid.copy();
			tempFill.put(Arrays.asList(input.hashCode(), fluid.getFluid().hashCode()), tempRecipe);
			tempSet.add(input);
		}
		for (Entry<ComparableItemStackTransposer, RecipeTransposer> entry : recipeMapExtract.entrySet()) {
			tempRecipe = entry.getValue();
			ComparableItemStackTransposer input = new ComparableItemStackTransposer(tempRecipe.input);
			tempExtract.put(input, tempRecipe);
			tempSet.add(input);
		}
		recipeMapFill.clear();
		recipeMapExtract.clear();

		recipeMapFill = tempFill;
		recipeMapExtract = tempExtract;

		validationSet.clear();
		validationSet = tempSet;
	}

	/* ADD RECIPES */
	public static boolean addFillRecipe(int energy, ItemStack input, ItemStack output, FluidStack fluid, boolean reversible) {

		if (input == null || output == null || fluid == null || fluid.getFluid() == null || fluid.amount <= 0 || energy <= 0) {
			return false;
		}
		if (fillRecipeExists(input, fluid)) {
			return false;
		}
		RecipeTransposer recipeFill = new RecipeTransposer(input, output, fluid, energy, 100);
		recipeMapFill.put(Arrays.asList(new ComparableItemStackTransposer(input).hashCode(), fluid.getFluid().hashCode()), recipeFill);
		validationSet.add(new ComparableItemStackTransposer(input));

		if (reversible) {
			addExtractRecipe(energy, output, input, fluid, 100, false);
		}
		return true;
	}

	public static boolean addExtractRecipe(int energy, ItemStack input, ItemStack output, FluidStack fluid, int chance, boolean reversible) {

		if (input == null || fluid == null || fluid.getFluid() == null || fluid.amount <= 0 || energy <= 0) {
			return false;
		}
		if (extractRecipeExists(input, fluid)) {
			return false;
		}
		if (output == null && reversible || output == null && chance != 0) {
			return false;
		}
		RecipeTransposer recipeExtraction = new RecipeTransposer(input, output, fluid, energy, chance);
		recipeMapExtract.put(new ComparableItemStackTransposer(input), recipeExtraction);
		validationSet.add(new ComparableItemStackTransposer(input));

		if (reversible) {
			addFillRecipe(energy, output, input, fluid, false);
		}
		return true;
	}

	/* REMOVE RECIPES */
	public static boolean removeFillRecipe(ItemStack input, FluidStack fluid) {

		return recipeMapFill.remove(Arrays.asList(new ComparableItemStackTransposer(input).hashCode(), fluid.getFluid().hashCode())) != null;
	}

	public static boolean removeExtractRecipe(ItemStack input) {

		return recipeMapExtract.remove(new ComparableItemStackTransposer(input)) != null;
	}

	/* RECIPE CLASS */
	public static class RecipeTransposer {

		final ItemStack input;
		final ItemStack output;
		final FluidStack fluid;
		final int energy;
		final int chance;

		RecipeTransposer(ItemStack input, ItemStack output, FluidStack fluid, int energy, int chance) {

			this.input = input;
			this.output = output;
			this.fluid = fluid;
			this.energy = energy;
			this.chance = chance;
		}

		public ItemStack getInput() {

			return input;
		}

		public ItemStack getOutput() {

			return output;
		}

		public FluidStack getFluid() {

			return fluid;
		}

		public int getEnergy() {

			return energy;
		}

		public int getChance() {

			return chance;
		}

	}

	/* MODE ENUM */
	public enum Mode {
		FILL, EXTRACT
	}

	/* ITEMSTACK CLASS */
	public static class ComparableItemStackTransposer extends ComparableItemStack {

		static final String ORE = "ore";
		static final String CROP = "crop";
		static final String DUST = "dust";
		static final String INGOT = "ingot";
		static final String NUGGET = "nugget";
		static final String GEM = "gem";

		static boolean safeOreType(String oreName) {

			return oreName.startsWith(ORE) || oreName.startsWith(CROP) || oreName.startsWith(DUST) || oreName.startsWith(INGOT) || oreName.startsWith(NUGGET) || oreName.startsWith(GEM);
		}

		static int getOreID(ItemStack stack) {

			ArrayList<Integer> ids = OreDictionaryArbiter.getAllOreIDs(stack);

			if (ids != null) {
				for (int i = 0, e = ids.size(); i < e; ) {
					int id = ids.get(i++);
					if (id != -1 && safeOreType(ItemHelper.oreProxy.getOreName(id))) {
						return id;
					}
				}
			}
			return -1;
		}

		ComparableItemStackTransposer(ItemStack stack) {

			super(stack);
			oreID = getOreID(stack);
		}

		@Override
		public ComparableItemStackTransposer set(ItemStack stack) {

			super.set(stack);
			oreID = getOreID(stack);

			return this;
		}
	}

}
