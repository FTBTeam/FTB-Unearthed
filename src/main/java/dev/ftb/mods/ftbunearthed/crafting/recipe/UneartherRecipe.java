package dev.ftb.mods.ftbunearthed.crafting.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftbunearthed.crafting.ItemWithChance;
import dev.ftb.mods.ftbunearthed.registry.ModRecipes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.ArrayList;
import java.util.List;

public class UneartherRecipe extends BaseRecipe<UneartherRecipe> {
    private final Ingredient workerItem;
    private final Ingredient toolItem;
    private final int processingTime;
    private final List<ItemWithChance> outputs;

    public UneartherRecipe(Ingredient workerItem, Ingredient toolItem, int processingTime, List<ItemWithChance> outputs) {
        super(ModRecipes.UNEARTHER_SERIALIZER, ModRecipes.UNEARTHER_TYPE);

        this.workerItem = workerItem;
        this.toolItem = toolItem;
        this.processingTime = processingTime;
        this.outputs = outputs;
    }

    public Ingredient getWorkerItem() {
        return workerItem;
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

    public boolean test(ItemStack workerStack, ItemStack toolStack) {
        return workerItem.test(workerStack) && toolItem.test(toolStack);
    }

    public List<ItemStack> generateOutputs(RandomSource rand) {
        List<ItemStack> results = new ArrayList<>();
        outputs.forEach(o -> o.tryProduce(rand, results::add));
        return results;
    }

    public interface IFactory<T extends UneartherRecipe> {
        T create(Ingredient workerItem, Ingredient toolItem, int processingTime, List<ItemWithChance> outputs);
    }

    public static class Serializer<T extends UneartherRecipe> implements RecipeSerializer<T> {
        private final MapCodec<T> codec;
        private final StreamCodec<RegistryFriendlyByteBuf,T> streamCodec;

        public Serializer(IFactory<T> factory) {
            codec = RecordCodecBuilder.mapCodec(builder -> builder.group(
                    Ingredient.CODEC_NONEMPTY.fieldOf("worker_item").forGetter(UneartherRecipe::getWorkerItem),
                    Ingredient.CODEC_NONEMPTY.fieldOf("tool_item").forGetter(UneartherRecipe::getToolItem),
                    Codec.INT.optionalFieldOf("processing_time", 200).forGetter(UneartherRecipe::getProcessingTime),
                    ItemWithChance.CODEC.listOf().fieldOf("outputs").forGetter(UneartherRecipe::getOutputs)
            ).apply(builder, factory::create));

            streamCodec = StreamCodec.composite(
                    Ingredient.CONTENTS_STREAM_CODEC, UneartherRecipe::getWorkerItem,
                    Ingredient.CONTENTS_STREAM_CODEC, UneartherRecipe::getToolItem,
                    ByteBufCodecs.VAR_INT, UneartherRecipe::getProcessingTime,
                    ItemWithChance.STREAM_CODEC.apply(ByteBufCodecs.list()), UneartherRecipe::getOutputs,
                    factory::create
            );
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
