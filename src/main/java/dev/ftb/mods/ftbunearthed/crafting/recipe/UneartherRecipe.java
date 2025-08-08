package dev.ftb.mods.ftbunearthed.crafting.recipe;

import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftbunearthed.crafting.ItemWithChance;
import dev.ftb.mods.ftbunearthed.item.WorkerToken.WorkerData;
import dev.ftb.mods.ftbunearthed.registry.ModRecipes;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class UneartherRecipe extends BaseRecipe<UneartherRecipe> implements Comparable<UneartherRecipe> {
    private final WorkerData workerData;
    private final Ingredient toolItem;
    private final String inputStateStr;
    private final BlockPredicateArgument.Result inputPredicate;
    private final int processingTime;
    private final List<ItemWithChance> outputs;
    private final float damageChance;

    public UneartherRecipe(String inputStateStr, WorkerData workerData, Ingredient toolItem, int processingTime, List<ItemWithChance> outputs, float damageChance) {
        super(ModRecipes.UNEARTHER_SERIALIZER, ModRecipes.UNEARTHER_TYPE);

        this.inputStateStr = inputStateStr;
        this.workerData = workerData;
        this.toolItem = toolItem;
        this.processingTime = processingTime;
        this.outputs = outputs;
        this.damageChance = damageChance;

        try {
            inputPredicate = BlockPredicateArgument.parse(BuiltInRegistries.BLOCK.asLookup(), new StringReader(inputStateStr));
        } catch (CommandSyntaxException e) {
            throw new JsonSyntaxException(e);
        }
    }

    public String getInputStateStr() {
        return inputStateStr;
    }

    private Set<Block> getInputBlocks() {
        if (inputPredicate instanceof BlockPredicateArgument.BlockPredicate b) {
            return Set.of(b.state.getBlock());
        } else if (inputPredicate instanceof BlockPredicateArgument.TagPredicate t) {
            return t.tag.stream().map(Holder::value).collect(Collectors.toSet());
        }
        return Set.of();
    }

    public List<Either<ItemStack, Fluid>> getInputsForDisplay() {
        Set<Block> blocks = getInputBlocks();
        List<Either<ItemStack, Fluid>> res = new ArrayList<>();

        for (Block b : blocks) {
            if (b instanceof LiquidBlock l) {
                if (l.fluid != Fluids.EMPTY) {
                    res.add(Either.right(l.fluid));
                }
            } else {
                ItemStack s = b.asItem().getDefaultInstance();
                if (!s.isEmpty()) {
                    res.add(Either.left(s));
                }
            }
        }
        return res;
    }

    public WorkerData getWorkerData() {
        return workerData;
    }

    public Ingredient getToolItem() {
        return toolItem;
    }

    public int getProcessingTime() {
        return processingTime;
    }

    public List<ItemWithChance> getOutputs() {
        return outputs;
    }

    public float getDamageChance() {
        return damageChance;
    }

    public boolean test(ItemStack inputStack, ItemStack workerStack, ItemStack toolStack) {
        // used by the unearther block entity
        return isValidInput(inputStack) && workerData.test(workerStack) && toolItem.test(toolStack);
    }

    public boolean testManual(ItemStack input, ItemStack mainHandItem) {
        // used when brushing manually (villager token doesn't matter here)
        return isValidInput(input) && toolItem.test(mainHandItem);
    }

    public List<ItemStack> generateOutputs(RandomSource rand) {
        List<ItemStack> results = new ArrayList<>();
        outputs.forEach(o -> o.tryProduce(rand, results::add));
        return results;
    }

    public boolean isValidInput(ItemStack stack) {
        return stack.getItem() instanceof BlockItem bi && getInputBlocks().contains(bi.getBlock());
    }

    @Override
    public int compareTo(@NotNull UneartherRecipe o) {
        return Integer.compare(workerData.getVillagerLevel(), o.workerData.getVillagerLevel());
    }

    public interface IFactory<T extends UneartherRecipe> {
        T create(String inputStateStr, WorkerData workerData, Ingredient toolItem, int processingTime, List<ItemWithChance> outputs, float damageChance);
    }

    public static class Serializer<T extends UneartherRecipe> implements RecipeSerializer<T> {
        private final MapCodec<T> codec;
        private final StreamCodec<RegistryFriendlyByteBuf,T> streamCodec;

        public Serializer(IFactory<T> factory) {
            codec = RecordCodecBuilder.mapCodec(builder -> builder.group(
                    Codec.STRING.fieldOf("input_block").forGetter(UneartherRecipe::getInputStateStr),
                    WorkerData.COMPONENT_CODEC.fieldOf("worker").forGetter(UneartherRecipe::getWorkerData),
                    Ingredient.CODEC_NONEMPTY.fieldOf("tool_item").forGetter(UneartherRecipe::getToolItem),
                    ExtraCodecs.POSITIVE_INT.optionalFieldOf("processing_time", 200).forGetter(UneartherRecipe::getProcessingTime),
                    ItemWithChance.CODEC.listOf().fieldOf("output_items").forGetter(UneartherRecipe::getOutputs),
                    Codec.FLOAT.validate(this::zeroToOne).optionalFieldOf("damage_chance", 0.1f).forGetter(UneartherRecipe::getDamageChance)
            ).apply(builder, factory::create));

            streamCodec = StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, UneartherRecipe::getInputStateStr,
                    WorkerData.STREAM_CODEC, UneartherRecipe::getWorkerData,
                    Ingredient.CONTENTS_STREAM_CODEC, UneartherRecipe::getToolItem,
                    ByteBufCodecs.VAR_INT, UneartherRecipe::getProcessingTime,
                    ItemWithChance.STREAM_CODEC.apply(ByteBufCodecs.list()), UneartherRecipe::getOutputs,
                    ByteBufCodecs.FLOAT, UneartherRecipe::getDamageChance,
                    factory::create
            );
        }

        private DataResult<Float> zeroToOne(float f) {
            return f >= 0f && f <= 1f ? DataResult.success(f) : DataResult.error(() -> "must be in range 0 to 1 (got " + f + ")");
        }

        @Override
        public MapCodec<T> codec() {
            return codec;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
            return streamCodec;
        }
    }
}
