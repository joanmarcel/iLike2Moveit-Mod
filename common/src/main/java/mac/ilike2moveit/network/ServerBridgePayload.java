package mac.ilike2moveit.network;

import mac.ilike2moveit.ILike2MoveIt;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Versioned server capability. Its absence is an intentional, fail-closed state. */
public record ServerBridgePayload(int protocol) implements CustomPacketPayload {
    public static final int PROTOCOL = ServerBridgeState.PROTOCOL;
    public static final Type<ServerBridgePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ILike2MoveIt.MODID, "server_bridge"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerBridgePayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.VAR_INT, ServerBridgePayload::protocol,
                    ServerBridgePayload::new);
    public static final ServerBridgePayload CURRENT = new ServerBridgePayload(PROTOCOL);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
