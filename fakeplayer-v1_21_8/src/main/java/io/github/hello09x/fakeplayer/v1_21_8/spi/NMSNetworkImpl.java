package io.github.hello09x.fakeplayer.v1_21_8.spi;

import com.mojang.authlib.GameProfile;
import io.github.hello09x.fakeplayer.api.spi.NMSNetwork;
import io.github.hello09x.fakeplayer.api.spi.NMSServerGamePacketListener;
import io.github.hello09x.fakeplayer.core.Main;
import io.github.hello09x.fakeplayer.v1_21_8.network.FakeConnection;
import io.github.hello09x.fakeplayer.v1_21_8.network.FakeServerGamePacketListenerImpl;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.ChunkPos;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.util.Optional;
import java.util.function.Predicate;

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
                cookie,
                new ProblemReporter.ScopedCollector(null),
                Optional.empty(),
                player.getName(),
                player.getLocation()
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
