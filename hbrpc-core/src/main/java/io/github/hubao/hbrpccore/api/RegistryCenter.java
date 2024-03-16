package io.github.hubao.hbrpccore.api;

import java.util.List;

public interface RegistryCenter {

    void start();

    void stop();


    // provider侧
    void register(String service, String instance);

    void unregister(String service, String instance);

    // consumer 侧
    List<String> fetchAll(String service);

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
        public void register(String service, String instance) {
        }

        @Override
        public void unregister(String service, String instance) {
        }

        @Override
        public List<String> fetchAll(String service) {

            return providers;
        }
    }
}
