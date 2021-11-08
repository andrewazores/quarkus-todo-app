package io.quarkus.sample;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.math3.distribution.BetaDistribution;
import org.apache.commons.math3.distribution.RealDistribution;

import io.quarkus.arc.Lock;

@Lock
@ApplicationScoped
class SlowLogger implements TodoLogger {

    private static final int POOL_SIZE = 2;
    private static final RealDistribution dist = new BetaDistribution(0.5, 0.5);
    private final BlockingQueue<String> q = new LinkedBlockingQueue<>(64);
    private final ExecutorService service = Executors.newWorkStealingPool(POOL_SIZE);
    private final ReentrantLock qlock = new ReentrantLock(true);

    SlowLogger() {
        for (int i = 0; i < POOL_SIZE; i++) {
            this.service.submit(() -> {
                while (true) {
                    qlock.lock();
                    try {
                        System.out.println(String.format("[LOG %s]: %s", Thread.currentThread().getName(), q.take()));
                        Thread.sleep(Math.round(250 * dist.sample()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        qlock.unlock();
                    }
                }
            });
        }
    }

    @Override
    public void log(String s) {
        try {
            q.put(s);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
