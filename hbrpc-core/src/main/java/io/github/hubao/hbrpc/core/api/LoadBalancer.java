package io.github.hubao.hbrpc.core.api;

import java.util.List;
import java.util.Objects;

public interface LoadBalancer<T> {

    T choose(List<T> providers);

    LoadBalancer Default = providers -> Objects.isNull(providers) || providers.isEmpty() ? null : providers.get(0);
}
