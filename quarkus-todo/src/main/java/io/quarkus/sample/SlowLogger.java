package io.quarkus.sample;

import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.math3.distribution.BetaDistribution;
import org.apache.commons.math3.distribution.RealDistribution;

import io.quarkus.arc.Lock;

@Lock
@ApplicationScoped
class SlowLogger implements TodoLogger {

    private static final RealDistribution dist = new BetaDistribution(0.5, 0.5);

    @Override
    public synchronized void log(String s) {
        try {
            double sample = dist.sample();
            Thread.sleep(Math.round(250 * sample));
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(String.format("[LOG %s]: %s", Thread.currentThread().getName(), s));
    }

}
