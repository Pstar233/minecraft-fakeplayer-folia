package io.github.hello09x.fakeplayer.v1_21_11.spi;

import io.github.hello09x.fakeplayer.api.spi.NMSNetwork;
import io.github.hello09x.fakeplayer.api.spi.NMSServerGamePacketListener;
import io.github.hello09x.fakeplayer.v1_21_11.network.FakeConnection;
import io.github.hello09x.fakeplayer.v1_21_11.network.FakeServerGamePacketListenerImpl;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.util.ProblemReporter;
import org.bukkit.Server;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.util.Optional;

public class NMSNetworkImpl implements NMSNetwork {

    @NotNull
    private final FakeConnection connection;

    private NMSServerGamePacketListener serverGamePacketListener;

    public NMSNetworkImpl(
            @NotNull InetAddress address
    ) {
        this.connection = new FakeConnection(address);
    }

    @NotNull
    @Override
    public NMSServerGamePacketListener placeNewPlayer(
            @NotNull Server server,
            @NotNull Player player
    ) {
        var handle = ((CraftPlayer) player).getHandle();
        var mcServer = ((CraftServer) server);
        var cookie = CommonListenerCookie.createInitial(((CraftPlayer) player).getProfile(), false);

        mcServer.getHandle().placeNewPlayer(
                this.connection,
                handle,
                cookie
        );


        var listener = new FakeServerGamePacketListenerImpl(
                mcServer.getServer(),
                this.connection,
                handle,
                cookie
        );
        this.serverGamePacketListener = listener;
        handle.connection = listener;

        return listener;
    }

    @NotNull
    @Override
    public NMSServerGamePacketListener getServerGamePacketListener() throws IllegalStateException {
        if (this.serverGamePacketListener == null) {
            throw new IllegalStateException("not initialized");
        }
        return this.serverGamePacketListener;
    }
}
