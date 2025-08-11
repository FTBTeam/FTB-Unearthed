package dev.ftb.mods.ftbunearthed.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftbunearthed.FTBUnearthedTags;
import dev.ftb.mods.ftbunearthed.registry.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class WorkerToken extends Item {
    public WorkerToken(Properties properties) {
        super(properties);
    }

    public static void addTooltip(ItemTooltipEvent event) {
        if (event.getItemStack().is(FTBUnearthedTags.Items.WORKER_TOKENS)) {
            getWorkerData(event.getItemStack()).ifPresent(data -> {
                if (!data.hideTooltip) {
                    List<Component> toolTip = event.getToolTip();
                    toolTip.add(tooltipLine("worker_profession", data.getProfessionName()));
                    toolTip.add(tooltipLine("worker_type", data.getVillagerTypeName()));
                    toolTip.add(tooltipLine("worker_level", String.valueOf(data.getVillagerLevel())));
                }
            });
        }
    }

    public static Component tooltipLine(String what, MutableComponent value) {
        return Component.translatable("ftbunearthed.tooltip." + what, value.withStyle(ChatFormatting.AQUA))
                .withStyle(ChatFormatting.YELLOW);
    }

    public static Component tooltipLine(String what, String value) {
        return tooltipLine(what, Component.literal(value));
//        return Component.translatable("ftbunearthed.tooltip." + what, Component.literal(value).withStyle(ChatFormatting.AQUA))
//                .withStyle(ChatFormatting.YELLOW);
    }

    @Override
    public Component getName(ItemStack stack) {
        Component defName = super.getName(stack);
        return getWorkerData(stack)
                .map(data -> defName.copy().append(" (").append(data.getProfessionName()).append(")"))
                .orElse((MutableComponent) defName);
    }

    public static Optional<WorkerData> getWorkerData(ItemStack stack) {
        return Optional.ofNullable(stack.get(ModDataComponents.WORKER_DATA));
    }

    // can't use VillagerData, sadly, because it doesn't override equals() and hashCode()
    public record WorkerData(VillagerProfession profession, Optional<VillagerType> type, Optional<Integer> level, boolean hideTooltip)
            implements Predicate<ItemStack>
    {
        public static final Codec<WorkerData> COMPONENT_CODEC = RecordCodecBuilder.create(builder -> builder.group(
                        BuiltInRegistries.VILLAGER_PROFESSION.byNameCodec()
                                .fieldOf("profession")
                                .forGetter(WorkerData::profession),
                        BuiltInRegistries.VILLAGER_TYPE.byNameCodec()
                                .optionalFieldOf("type")
                                .forGetter(WorkerData::type),
                        Codec.INT.optionalFieldOf("level").forGetter(WorkerData::level),
                        Codec.BOOL.optionalFieldOf("hide_tooltip", false).forGetter(WorkerData::hideTooltip)
                )
                .apply(builder, WorkerData::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, WorkerData> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.registry(Registries.VILLAGER_PROFESSION), WorkerData::profession,
                ByteBufCodecs.optional(ByteBufCodecs.registry(Registries.VILLAGER_TYPE)), WorkerData::type,
                ByteBufCodecs.optional(ByteBufCodecs.VAR_INT), WorkerData::level,
                ByteBufCodecs.BOOL, WorkerData::hideTooltip,
                WorkerData::new
        );

        public WorkerData(VillagerProfession profession) {
            this(profession, Optional.empty(), Optional.empty(), false);
        }

        public WorkerData(VillagerProfession profession, VillagerType type) {
            this(profession, Optional.of(type), Optional.empty(), false);
        }

        public WorkerData(VillagerProfession profession, int level) {
            this(profession, Optional.empty(), Optional.of(level), false);
        }

        public WorkerData hideTooltip(boolean hide) {
            return new WorkerData(profession, type, level, hide);
        }

        @Override
        public boolean test(ItemStack workerStack) {
            return WorkerToken.getWorkerData(workerStack).map(data -> {
                if (!data.profession.equals(this.profession)) return false;
                if (this.type.isPresent() && !data.type.equals(this.type)) return false;
                return data.getVillagerLevel() >= this.getVillagerLevel();
            }).orElse(false);
        }

        public int getVillagerLevel() {
            return level().orElse(1);
        }

        public VillagerData toVillagerData() {
            return new VillagerData(type.orElse(VillagerType.PLAINS), profession, getVillagerLevel());
        }

        public MutableComponent getProfessionName() {
            return Component.translatable("entity.minecraft.villager." + profession.toString());
        }

        public MutableComponent getVillagerTypeName() {
            return Component.translatable("ftbunearthed.villager_type." + type().orElse(VillagerType.PLAINS));
        }
    }
}
