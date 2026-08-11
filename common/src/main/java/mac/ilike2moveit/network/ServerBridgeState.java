package mac.ilike2moveit.network;

import mac.ilike2moveit.ILike2MoveIt;

/** Client connection capability shared by EMF bridges. Reset before and after every connection. */
public final class ServerBridgeState {
    public static final int PROTOCOL = 1;
    private static volatile int negotiatedProtocol;

    private ServerBridgeState() {
    }

    public static void reset() {
        negotiatedProtocol = 0;
    }

    public static void accept(ServerBridgePayload payload) {
        acceptProtocol(payload.protocol());
        if (isPresent()) {
            ILike2MoveIt.LOGGER.info("[Server Bridge] protocol {} present; authoritative mob states enabled.",
                    payload.protocol());
        } else {
            ILike2MoveIt.LOGGER.warn("[Server Bridge] unsupported protocol {}; mob states remain disabled.",
                    payload.protocol());
        }
    }

    /** Pure protocol gate kept separate so the fail-closed contract can be tested without Minecraft. */
    public static void acceptProtocol(int protocol) {
        negotiatedProtocol = protocol == PROTOCOL ? protocol : 0;
    }

    public static boolean isPresent() {
        return negotiatedProtocol == PROTOCOL;
    }

    public static Float currentEmfPresent() {
        return isPresent() ? 1.0F : 0.0F;
    }
}
