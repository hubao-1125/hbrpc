package io.github.hubao.hbrpc.core.api;

import io.github.hubao.hbrpc.core.meta.InstanceMeta;
import io.github.hubao.hbrpc.core.meta.ServiceMeta;
import io.github.hubao.hbrpc.core.registry.ChangedListener;

import java.util.List;

public interface RegistryCenter {

    void start();

    void stop();


    // provider侧
    void register(ServiceMeta service, InstanceMeta instance);

    void unregister(ServiceMeta service, InstanceMeta instance);

    // consumer 侧
    List<InstanceMeta> fetchAll(ServiceMeta service);

    void subscribe(ServiceMeta service, ChangedListener listener);

    class StaticRegistryCenter implements RegistryCenter {
        private List<String> providers;

        public StaticRegistryCenter(List<String> providers) {
            this.providers = providers;
        }

        @Override
        public void start() {
        }

        @Override
        public void stop() {
        }

        @Override
        public void register(ServiceMeta service, InstanceMeta instance) {
        }

        @Override
        public void unregister(ServiceMeta service, InstanceMeta instance) {
        }

        @Override
        public List<InstanceMeta> fetchAll(ServiceMeta service) {

            return null;
        }

        @Override
        public void subscribe(ServiceMeta service, ChangedListener listener) {

        }
    }
}
