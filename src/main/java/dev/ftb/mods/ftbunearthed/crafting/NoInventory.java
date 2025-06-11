package dev.ftb.mods.ftbunearthed.crafting;

import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;

public class NoInventory extends RecipeWrapper {
	public static final NoInventory INSTANCE = new NoInventory();

	private NoInventory() {
		super(new ItemStackHandler(0));
	}
}
