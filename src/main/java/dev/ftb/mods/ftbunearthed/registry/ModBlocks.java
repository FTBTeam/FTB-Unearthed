package dev.ftb.mods.ftbunearthed.registry;

import dev.ftb.mods.ftbunearthed.FTBUnearthed;
import dev.ftb.mods.ftbunearthed.block.UneartherCoreBlock;
import dev.ftb.mods.ftbunearthed.block.UneartherFrameBlock;
import dev.ftb.mods.ftbunearthed.block.UneartherFrameBlock.Part;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(FTBUnearthed.MODID);

    public static Block.Properties defaultProps() {
        return Block.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(3f, 10f)
                .sound(SoundType.METAL);
    }

    public static final DeferredBlock<UneartherCoreBlock> CORE
            = BLOCKS.register("core", () -> new UneartherCoreBlock(defaultProps()));
    public static final DeferredBlock<UneartherFrameBlock> L_EDGE
            = BLOCKS.register("l_edge", () -> new UneartherFrameBlock(defaultProps(), Part.LOWER_EDGE));
    public static final DeferredBlock<UneartherFrameBlock> M_EDGE
            = BLOCKS.register("m_edge", () -> new UneartherFrameBlock(defaultProps(), Part.MID_EDGE));
    public static final DeferredBlock<UneartherFrameBlock> U_EDGE
            = BLOCKS.register("u_edge", () -> new UneartherFrameBlock(defaultProps(), Part.UPPER_EDGE));
    public static final DeferredBlock<UneartherFrameBlock> L_CORNER
            = BLOCKS.register("l_corner", () -> new UneartherFrameBlock(defaultProps(), Part.LOWER_CORNER));
    public static final DeferredBlock<UneartherFrameBlock> U_CORNER
            = BLOCKS.register("u_corner", () -> new UneartherFrameBlock(defaultProps(), Part.UPPER_CORNER));
    public static final DeferredBlock<UneartherFrameBlock> M_FACE
            = BLOCKS.register("m_face", () -> new UneartherFrameBlock(defaultProps(), Part.MID_FACE));
    public static final DeferredBlock<UneartherFrameBlock> U_FACE
            = BLOCKS.register("u_face", () -> new UneartherFrameBlock(defaultProps(), Part.UPPER_FACE));

}
