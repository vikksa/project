package com.vikson.projects.help.threading;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.concurrent.CompletableFuture;

public class ContextAwareThread {

    private ContextAwareThread() {
    }

    public static CompletableFuture runAsync(Runnable runnable) {
        CompletableFuture future = new CompletableFuture();
        SecurityContext context = SecurityContextHolder.getContext();
        new Thread(() -> {
            SecurityContextHolder.setContext(context);
            runnable.run();
            future.complete(null);
        }).start();
        return future;
    }

}
