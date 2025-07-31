package dev.ftb.mods.ftbunearthed.util;

import com.mojang.serialization.DataResult;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;

public class MiscUtil {
    public static final Direction[] HORIZONTALS = new Direction[] {
            Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST
    };

    public static DataResult<Double> validateChanceRange(double d) {
        return d > 0.0 ? DataResult.success(d) : DataResult.error(() -> "must be > 0.0");
    }
}
