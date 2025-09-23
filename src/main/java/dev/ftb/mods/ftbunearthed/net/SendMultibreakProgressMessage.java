package dev.ftb.mods.ftbunearthed.net;

import dev.ftb.mods.ftbunearthed.FTBUnearthed;
import dev.ftb.mods.ftbunearthed.client.FTBUnearthedClient;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Collection;
import java.util.List;

public record SendMultibreakProgressMessage(BlockPos pos0, List<BlockPos> offsets, int progress) implements CustomPacketPayload {
    public static final StreamCodec<ByteBuf,BlockPos> BLOCKPOS_OFFSET_CODEC = StreamCodec.of(
            (buffer, value) -> {
                buffer.writeByte(value.getX());
                buffer.writeByte(value.getY());
                buffer.writeByte(value.getZ());
            },
            buffer -> new BlockPos(buffer.readByte(), buffer.readByte(), buffer.readByte())
    );
    public static final StreamCodec<FriendlyByteBuf, SendMultibreakProgressMessage> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SendMultibreakProgressMessage::pos0,
            BLOCKPOS_OFFSET_CODEC.apply(ByteBufCodecs.list()), SendMultibreakProgressMessage::offsets,
            ByteBufCodecs.VAR_INT, SendMultibreakProgressMessage::progress,
            SendMultibreakProgressMessage::new
    );
    public static final Type<SendMultibreakProgressMessage> TYPE = new Type<>(FTBUnearthed.id("send_multibreak_progress"));

    public static SendMultibreakProgressMessage create(BlockPos pos0, Collection<BlockPos> allPositions, int progress) {
        List<BlockPos> offsets = allPositions.stream().map(p -> p.subtract(pos0)).toList();
        return new SendMultibreakProgressMessage(pos0, offsets, progress);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private List<BlockPos> getPositions() {
        return offsets.stream().map(pos0::offset).toList();
    }

    public static void handle(SendMultibreakProgressMessage message, IPayloadContext ignoredContext) {
        FTBUnearthedClient.updateBreakPositions(message.getPositions(), message.progress);
    }
}
