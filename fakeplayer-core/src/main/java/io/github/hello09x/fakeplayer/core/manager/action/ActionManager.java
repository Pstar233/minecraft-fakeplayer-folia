package io.github.hello09x.fakeplayer.core.manager.action;

import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.hello09x.fakeplayer.api.spi.ActionSetting;
import io.github.hello09x.fakeplayer.api.spi.ActionTicker;
import io.github.hello09x.fakeplayer.api.spi.ActionType;
import io.github.hello09x.fakeplayer.api.spi.NMSBridge;
import io.github.hello09x.fakeplayer.core.Main;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Singleton
public class ActionManager {

    private final static Logger log = Main.getInstance().getLogger();

    private final Map<UUID, Map<ActionType, ActionTicker>> managers = new HashMap<>();

    private final NMSBridge bridge;


    @Inject
    public ActionManager(NMSBridge bridge) {
        this.bridge = bridge;
    }

    public boolean hasActiveAction(
            @NotNull Player player,
            @NotNull ActionType action
    ) {
        return Optional.ofNullable(this.managers.get(player.getUniqueId()))
                .map(manager -> manager.get(action))
                .filter(ac -> ac.getSetting().remains > 0)
                .isPresent();
    }

    public @NotNull @Unmodifiable Set<ActionType> getActiveActions(@NotNull Player player) {
        var manager = this.managers.get(player.getUniqueId());
        if (manager == null || managers.isEmpty()) {
            return Collections.emptySet();
        }

        return manager.entrySet()
                .stream()
                .filter(actions -> actions.getValue().getSetting().remains > 0)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    public void setAction(
            @NotNull Player player,
            @NotNull ActionType action,
            @NotNull ActionSetting setting
    ) {
        var managers = this.managers.computeIfAbsent(player.getUniqueId(), key -> new HashMap<>());
        managers.put(action, bridge.createAction(player, action, setting));
    }

    public void stop(@NotNull Player player) {
        var managers = this.managers.get(player.getUniqueId());
        if (managers == null || managers.isEmpty()) {
            return;
        }

        for (var entry : managers.entrySet()) {
            if (!entry.getValue().equals(ActionSetting.stop())) {
                entry.setValue(bridge.createAction(player, entry.getKey(), ActionSetting.stop()));
            }
        }
    }

    public void tick(Player player) {

        Map<ActionType, ActionTicker> actionTypeActionTickerMap = managers.get(player.getUniqueId());
        if (player == null || !player.isOnline()) return;

        if (!player.isValid()) {
            managers.remove(player.getUniqueId());
            actionTypeActionTickerMap.values().forEach(ActionTicker::stop);
            return;
        }

        if (actionTypeActionTickerMap == null || actionTypeActionTickerMap.isEmpty()) return;
        actionTypeActionTickerMap.values().removeIf(ticker -> {
            try {
                return ticker.tick();
            } catch (Throwable e) {
                log.warning(Throwables.getStackTraceAsString(e));
                return false;
            }
        });

        if (actionTypeActionTickerMap.isEmpty()) {
            managers.remove(player.getUniqueId());
        }

    }

}
