package dev.ftb.mods.ftbunearthed.crafting;

import com.google.common.base.MoreObjects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftbunearthed.util.MiscUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

public record ItemWithChance(ItemStack item, double chance) implements WeightedEntry {
	public static final Codec<ItemWithChance> CODEC = RecordCodecBuilder.create(builder -> builder.group(
			ItemStack.CODEC.fieldOf("item").forGetter(ItemWithChance::item),
			Codec.DOUBLE.validate(MiscUtil::validateChanceRange).fieldOf("chance").forGetter(ItemWithChance::chance)
	).apply(builder, ItemWithChance::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ItemWithChance> STREAM_CODEC = StreamCodec.composite(
			ItemStack.STREAM_CODEC, ItemWithChance::item,
			ByteBufCodecs.DOUBLE, ItemWithChance::chance,
			ItemWithChance::new
	);

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("item", item)
				.add("chance", chance)
				.toString();
	}

	@Override
	public Weight getWeight() {
		return Weight.of((int) (chance * 1000.0));
	}

	public void tryProduce(RandomSource rand, Consumer<ItemStack> consumer) {
		int count = (int) chance();
		double chance = chance() - count;
		if (rand.nextDouble() < chance) {
			count++;
		}
		if (count > 0) {
			int actual = item.getCount() * count;
			while (actual > 0) {
				int toAdd = Math.min(actual, item.getMaxStackSize());
				consumer.accept(item.copyWithCount(toAdd));
				actual -= toAdd;
			}
		}
	}

//	public ItemStack produce(RandomSource rand) {
//		ItemStack res = item.copy();
//		if (bonusPct == 0) {
//			return res;
//		} else if (bonusPct >= 100) {
//			res.grow(bonusPct / 100);
//		}
//		if (rand.nextInt(100) < bonusPct % 100) {
//			res.grow(1);
//		}
//		return res;
//	}
}
