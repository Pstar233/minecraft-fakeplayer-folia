package io.github.hello09x.fakeplayer.v1_21_8.network;

import io.github.hello09x.fakeplayer.api.spi.NMSServerGamePacketListener;
import io.github.hello09x.fakeplayer.core.Main;
import io.github.hello09x.fakeplayer.core.manager.FakeplayerManager;
import io.github.hello09x.fakeplayer.core.network.FakeChannel;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.internal.ThreadLocalRandom;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class FakeConnection extends Connection {

    private final static Logger log = Main.getInstance().getLogger();
    private final FakeplayerManager manager = Main.getInjector().getInstance(FakeplayerManager.class);

    public FakeConnection(@NotNull InetAddress address) {
        super(PacketFlow.SERVERBOUND);
        this.channel = new FakeChannel(null, address);
        this.address = this.channel.remoteAddress();
        Connection.configureSerialization(this.channel.pipeline(), PacketFlow.SERVERBOUND, false, null);
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public void send(Packet<?> packet, @Nullable ChannelFutureListener channelfuturelistener) {
        //System.out.println("||收到包1||" + packet.getClass().getName());
    }


    /**
     * 我回复了心跳包，在1.21.8上必须回复，不然假人会出现连接超时
     *
     * @param packet
     * @param future
     * @param flush
     */
    @Override
    public void send(Packet<?> packet, @Nullable ChannelFutureListener future, boolean flush) {
        //System.out.println("||收到包2||" + packet.getClass().getName());
        int delay = ThreadLocalRandom.current().nextInt(40, 100);
        Bukkit.getAsyncScheduler().runDelayed(Main.getInstance(), task -> {
            if (packet instanceof net.minecraft.network.protocol.common.ClientboundKeepAlivePacket keepAlive) {
                long id = keepAlive.getId();
                if (this.getPacketListener() instanceof net.minecraft.server.network.ServerGamePacketListenerImpl listener) {
                    try {
                        listener.handleKeepAlive(
                                new net.minecraft.network.protocol.common.ServerboundKeepAlivePacket(id)
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        }, delay, TimeUnit.MILLISECONDS);
        //super.send(packet, future, flush);
    }


    @Override
    public void send(Packet<?> packet) {
        //System.out.println("||收到包3||" + packet.getClass().getName());
    }

}