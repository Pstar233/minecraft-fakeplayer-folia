package io.github.hello09x.fakeplayer.v1_21_6.spi;

import io.github.hello09x.fakeplayer.api.spi.NMSNetwork;
import io.github.hello09x.fakeplayer.api.spi.NMSServerGamePacketListener;
import io.github.hello09x.fakeplayer.v1_21_6.network.FakeConnection;
import io.github.hello09x.fakeplayer.v1_21_6.network.FakeServerGamePacketListenerImpl;
import net.minecraft.network.Connection;
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
        var cookie = CommonListenerCookie.createInitial(((CraftPlayer) player).getProfile(), false);

        Object scopedCollector = null;
        try {
            var problemReporter = ((CraftServer) server).getServer().getClass()
                    .getMethod("problemReporter")
                    .invoke(((CraftServer) server).getServer());

            scopedCollector = problemReporter.getClass()
                    .getMethod("createScopedCollector", String.class)
                    .invoke(problemReporter, "fakeplayer-" + player.getName());
        } catch (Exception e) {
            // fallback: 构造一个空 collector，避免NPE
            scopedCollector = new ProblemReporter.ScopedCollector(null);
        }

        ((CraftServer) server).getHandle().placeNewPlayer(
                this.connection,
                handle,
                cookie,
                (ProblemReporter.ScopedCollector) scopedCollector,
                Optional.empty(),
                player.getName(),
                player.getLocation()
        );

        var mcServer = ((CraftServer) server).getServer();

        var listener = new FakeServerGamePacketListenerImpl(
                mcServer,
                this.connection,
                handle,
                cookie
        );
        this.serverGamePacketListener = listener;
        handle.connection = listener;

        //把 connection 注册进服务器的 active connections
        mcServer.getConnection().getConnections().add(this.connection);
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
