package net.kairost.torohealth.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.kairost.torohealth.ToroHealth;

public record ModPresencePayload(int protocolVersion) implements CustomPacketPayload {
    public static final int PROTOCOL_VERSION = 1;
    public static final ModPresencePayload INSTANCE = new ModPresencePayload(PROTOCOL_VERSION);
    public static final Type<ModPresencePayload> TYPE = new Type<>(
        Identifier.fromNamespaceAndPath(ToroHealth.MODID, "presence")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, ModPresencePayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT,
        ModPresencePayload::protocolVersion,
        ModPresencePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
