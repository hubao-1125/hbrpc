package io.github.hubao.hbrpccore.cluster;

import io.github.hubao.hbrpccore.api.LoadBalancer;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRibonLoadBalancer<T> implements LoadBalancer<T> {

    AtomicInteger index = new AtomicInteger(0);


    @Override
    public T choose(List<T> providers) {

        if (Objects.isNull(providers) || providers.size() == 0) {
            return null;
        }

        if (providers.size() == 1) {
            return providers.get(0);
        }

        return providers.get(index.getAndIncrement() % providers.size());
    }
}
