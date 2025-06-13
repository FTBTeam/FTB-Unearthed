/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.ftb.mods.ftbunearthed.integration.jei;

import dev.ftb.mods.ftbunearthed.FTBUnearthed;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public abstract class BaseUnearthedCategory<T> implements IRecipeCategory<T> {
    private final RecipeType<T> type;
    private final Component localizedName;
    protected final IDrawable background;
    private final IDrawable icon;

    protected BaseUnearthedCategory(RecipeType<T> type, Component localizedName, IDrawable background, IDrawable icon) {
        this.type = type;
        this.localizedName = localizedName;
        this.background = background;
        this.icon = icon;
    }

    @Override
    public Component getTitle() {
        return localizedName;
    }

    @Override
    public int getWidth() {
        return background.getWidth();
    }

    @Override
    public int getHeight() {
        return background.getHeight();
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public RecipeType<T> getRecipeType() {
        return type;
    }

    static IGuiHelper guiHelper() {
        return UnearthedJeiPlugin.jeiHelpers.getGuiHelper();
    }

    static ResourceLocation bgTexture(String name) {
        return FTBUnearthed.id("textures/gui/jei/" + name);
    }
}
