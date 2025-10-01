package dev.ftb.mods.ftbunearthed.crafting.recipe;

import dev.ftb.mods.ftbunearthed.item.WorkerToken;
import dev.ftb.mods.ftbunearthed.registry.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class TokenResetRecipe extends CustomRecipe {
    public TokenResetRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        int nItems = 0;
        for (int i = 0; i < input.size(); i++) {
            if (!(input.getItem(i).getItem() instanceof WorkerToken) || ++nItems > 1) {
                return false;
            }
        }
        return nItems == 1;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack found = ItemStack.EMPTY;

        for (int i = 0; i < input.size(); i++) {
            if (input.getItem(i).getItem() instanceof WorkerToken) {
                found = input.getItem(i);
                break;
            }
        }
        if (!found.isEmpty()) {
            var oldData = WorkerToken.getWorkerData(found);
            return WorkerToken.createWithData(new WorkerToken.WorkerData(VillagerProfession.NONE, oldData.type().orElse(VillagerType.PLAINS)));
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.TOKEN_RESET.get();
    }
}
