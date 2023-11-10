package com.globalpbx.mailserver.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

public class FixedVirtualThreadExecutorService implements ExecutorService {
    private final ExecutorService VIRTUAL_THREAD_POOL_EXECUTOR_SERVICE = Executors.newVirtualThreadPerTaskExecutor();

    private Semaphore semaphore;

    private int poolSize;

    public FixedVirtualThreadExecutorService(int poolSize) {
        this.poolSize = poolSize;
        this.semaphore = new Semaphore(this.poolSize);
    }

    @Override
    public void shutdown() {
        VIRTUAL_THREAD_POOL_EXECUTOR_SERVICE.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return VIRTUAL_THREAD_POOL_EXECUTOR_SERVICE.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return VIRTUAL_THREAD_POOL_EXECUTOR_SERVICE.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return VIRTUAL_THREAD_POOL_EXECUTOR_SERVICE.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, java.util.concurrent.TimeUnit unit) throws InterruptedException {
        return VIRTUAL_THREAD_POOL_EXECUTOR_SERVICE.awaitTermination(timeout, unit);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        try {
            semaphore.acquire();
            return VIRTUAL_THREAD_POOL_EXECUTOR_SERVICE.submit(task);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        } finally {
            semaphore.release();
        }
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        try {
            semaphore.acquire();
            return VIRTUAL_THREAD_POOL_EXECUTOR_SERVICE.submit(task, result);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        } finally {
            semaphore.release();
        }
    }

    @Override
    public Future<?> submit(Runnable task) {
        try {
            semaphore.acquire();
            return VIRTUAL_THREAD_POOL_EXECUTOR_SERVICE.submit(task);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        } finally {
            semaphore.release();
        }
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        try {
            semaphore.acquire(tasks.size());
            return VIRTUAL_THREAD_POOL_EXECUTOR_SERVICE.invokeAll(tasks);
        } finally {
            semaphore.release(tasks.size());
        }
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, java.util.concurrent.TimeUnit unit) throws InterruptedException {
        try {
            semaphore.acquire(tasks.size());
            return VIRTUAL_THREAD_POOL_EXECUTOR_SERVICE.invokeAll(tasks, timeout, unit);
        } finally {
            semaphore.release(tasks.size());
        }
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        try {
            semaphore.acquire(tasks.size());
            return VIRTUAL_THREAD_POOL_EXECUTOR_SERVICE.invokeAny(tasks);
        } finally {
            semaphore.release(tasks.size());
        }
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, java.util.concurrent.TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        try {
            semaphore.acquire(tasks.size());
            return VIRTUAL_THREAD_POOL_EXECUTOR_SERVICE.invokeAny(tasks, timeout, unit);
        } finally {
            semaphore.release(tasks.size());
        }
    }

    @Override
    public void execute(Runnable command) {
        try {
            semaphore.acquire();
            VIRTUAL_THREAD_POOL_EXECUTOR_SERVICE.execute(command);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        } finally {
            semaphore.release();
        }
    }

}
