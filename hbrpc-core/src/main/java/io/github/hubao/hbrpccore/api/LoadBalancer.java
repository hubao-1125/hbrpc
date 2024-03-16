package io.github.hubao.hbrpccore.api;

import java.util.List;
import java.util.Objects;

public interface LoadBalancer<T> {

    T choose(List<T> providers);

    LoadBalancer Default = providers -> Objects.isNull(providers) || providers.size() == 0 ? null : providers.get(0);
}
