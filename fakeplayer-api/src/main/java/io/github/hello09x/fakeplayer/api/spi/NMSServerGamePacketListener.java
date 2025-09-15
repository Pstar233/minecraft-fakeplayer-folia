package io.github.hello09x.fakeplayer.api.spi;

import io.netty.channel.ChannelFutureListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundKeepAlivePacket;
import org.bukkit.plugin.messaging.StandardMessenger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface NMSServerGamePacketListener {


    String BUNGEE_CORD_CHANNEL = "BungeeCord";

    String BUNGEE_CORD_CORRECTED_CHANNEL = StandardMessenger.validateAndCorrectChannel(BUNGEE_CORD_CHANNEL);

}
