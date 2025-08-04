package dev.ftb.mods.ftbunearthed.network;

import dev.ftb.mods.ftbunearthed.FTBUnearthed;
import dev.ftb.mods.ftbunearthed.block.UneartherCoreBlockEntity;
import dev.ftb.mods.ftbunearthed.crafting.recipe.UneartherRecipe;
import dev.ftb.mods.ftbunearthed.registry.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.apache.commons.lang3.Validate;

public record UneartherStatusMessage(BlockPos pos, BlockState state, int processingTime) implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, UneartherStatusMessage> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, UneartherStatusMessage::pos,
            ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY), UneartherStatusMessage::state,
            ByteBufCodecs.VAR_INT, UneartherStatusMessage::processingTime,
            UneartherStatusMessage::new
    );
    public static final Type<UneartherStatusMessage> TYPE = new Type<>(FTBUnearthed.id("unearther_status"));

    public static UneartherStatusMessage create(UneartherCoreBlockEntity core) {
        int processingTime = core.getCurrentRecipe().map(UneartherRecipe::getProcessingTime).orElse(0);
        return new UneartherStatusMessage(core.getBlockPos(), getBlock(core.getInputStack()), processingTime);
    }

    private static BlockState getBlock(ItemStack stack) {
        return stack.getItem() instanceof BlockItem bi ? bi.getBlock().defaultBlockState() : Blocks.AIR.defaultBlockState();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(UneartherStatusMessage message, IPayloadContext context) {
        context.player().level().getBlockEntity(message.pos, ModBlockEntityTypes.UNEARTHER_CORE.get())
                .ifPresent(u -> u.syncStatusFromServer(ClientStatus.of(message)));
    }

    public record ClientStatus(Block block, int processingTime) {
        public static final ClientStatus IDLE = new ClientStatus(Blocks.AIR, 0);

        public static ClientStatus of(UneartherStatusMessage message) {
            return new ClientStatus(message.state.getBlock(), message.processingTime);
        }

        public boolean active() {
            return processingTime > 0;
        }
    }
}
