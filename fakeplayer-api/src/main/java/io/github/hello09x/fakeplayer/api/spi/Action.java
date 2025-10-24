package io.github.hello09x.fakeplayer.api.spi;

import java.util.concurrent.CompletableFuture;

public interface Action {

    /**
     * 活跃 tick 时执行
     *
     * @return 是否有做出行动
     */
    default CompletableFuture<Boolean> CompletableFutureTick() {
        return null;
    }

    boolean tick();

    /**
     * 非活跃 tick 时执行
     */
    void inactiveTick();

    /**
     * 结束动作时执行
     */
    void stop();


}
