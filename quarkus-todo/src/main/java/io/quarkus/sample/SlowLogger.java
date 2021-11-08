package io.quarkus.sample;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.math3.distribution.BetaDistribution;
import org.apache.commons.math3.distribution.RealDistribution;

import io.quarkus.arc.Lock;

@Lock
@ApplicationScoped
class SlowLogger implements TodoLogger {

    private static final RealDistribution dist = new BetaDistribution(0.5, 0.5);
    private final BlockingQueue<String> q = new ArrayBlockingQueue<>(64);
    private final ExecutorService service = Executors.newWorkStealingPool(2);

    SlowLogger() {
        this.service.submit(() -> {
            while (true) {
                try {
                    System.out.println(String.format("[LOG %s]: %s", Thread.currentThread().getName(), q.take()));
                    double sample = dist.sample();
                    Thread.sleep(Math.round(1_000 * sample));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
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
