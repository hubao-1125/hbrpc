package io.github.hubao.hbrpccore.cluster;

import io.github.hubao.hbrpccore.api.LoadBalancer;

import java.util.List;
import java.util.Objects;
import java.util.Random;

public class RandomLoadBalancer<T> implements LoadBalancer<T> {

    Random random = new Random();

    @Override
    public T choose(List<T> providers) {

        if (Objects.isNull(providers) || providers.size() == 0) {
            return null;
        }

        if (providers.size() == 1) {
            return providers.get(0);
        }

        return providers.get(random.nextInt(providers.size()));
    }
}
