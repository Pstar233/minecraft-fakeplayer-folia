package io.github.hello09x.fakeplayer.api.spi;

import java.util.concurrent.CompletableFuture;

public interface Action {


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
