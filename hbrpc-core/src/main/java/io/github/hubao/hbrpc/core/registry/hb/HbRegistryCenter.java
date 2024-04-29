package io.github.hubao.hbrpc.core.registry.hb;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import io.github.hubao.hbrpc.core.api.RegistryCenter;
import io.github.hubao.hbrpc.core.consumer.HttpInvoker;
import io.github.hubao.hbrpc.core.meta.InstanceMeta;
import io.github.hubao.hbrpc.core.meta.ServiceMeta;
import io.github.hubao.hbrpc.core.registry.ChangedListener;
import io.github.hubao.hbrpc.core.registry.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/*
 * Desc:
 *
 * @author hubao
 * @see 2024/4/29 20:53
 */
@Slf4j
public class HbRegistryCenter implements RegistryCenter {

    @Value("${hbregistry.servers}")
    private String servers;

    Map<String, Long> VERSIONS = new HashMap<>();
    MultiValueMap<InstanceMeta, ServiceMeta> RENEWS = new LinkedMultiValueMap<>();
    ScheduledExecutorService consumerExecutor = null;
    ScheduledExecutorService providerExecutor = null;

    @Override
    public void start() {
        log.info(" ===> HbRegistry start with server: {}", servers);
        consumerExecutor = Executors.newScheduledThreadPool(1);
        providerExecutor = Executors.newScheduledThreadPool(1);
        providerExecutor.scheduleAtFixedRate(() -> {
            RENEWS.keySet().forEach(instance -> {
                StringBuffer sb = new StringBuffer();
                for (ServiceMeta service : RENEWS.get(instance)) {
                    sb.append(service.toPath() + ",");
                }
                String services = sb.toString();
                if (services.endsWith(",")) {
                    services = services.substring(0, services.length() - 1);
                }
                Long timestamp = HttpInvoker.httpPost(JSON.toJSONString(instance), servers + "/renews?services=" + services, Long.class);
                log.info(" ===> HbRegistry renew instance {} for services {} at {}", instance, services, timestamp);
            });
        }, 5_000, 5_000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        log.info(" ===> HbRegistry stop with server: {}", servers);
        gracefulShutdown(consumerExecutor);
        gracefulShutdown(providerExecutor);
    }

    private static void gracefulShutdown(ScheduledExecutorService scheduledExecutorService) {
        scheduledExecutorService.shutdown();
        try {
            scheduledExecutorService.awaitTermination(1_000, TimeUnit.MILLISECONDS);
            if (!scheduledExecutorService.isTerminated()) {
                scheduledExecutorService.shutdownNow();
            }
        } catch (InterruptedException e) {

        }
    }

    @Override
    public void register(ServiceMeta service, InstanceMeta instance) {
        log.info(" ===> HbRegistry register instance {} from {}", instance, service);
        HttpInvoker.httpPost(JSON.toJSONString(instance), servers + "/reg?service=" + service.toPath(), InstanceMeta.class);
        log.info(" ===> HbRegistry registered  {}", instance);
        RENEWS.add(instance, service);
    }

    @Override
    public void unregister(ServiceMeta service, InstanceMeta instance) {
        log.info(" ===> HbRegistry unregister instance {} from {}", instance, service);
        HttpInvoker.httpPost(JSON.toJSONString(instance), servers + "/unreg?service=" + service.toPath(), InstanceMeta.class);
        log.info(" ===> HbRegistry unregistered  {}", instance);
        RENEWS.remove(instance, service);
    }

    @Override
    public List<InstanceMeta> fetchAll(ServiceMeta service) {
        log.info(" ===> HbRegistry find all instance for {}", service);
        List<InstanceMeta> instances = HttpInvoker.httpGet(servers + "/findAll?service=" + service.toPath(), new TypeReference<List<InstanceMeta>>() {
        });
        log.info(" ===> HbRegistry findAll:  {}", instances);
        return instances;
    }


    @Override
    public void subscribe(ServiceMeta service, ChangedListener listener) {
        consumerExecutor.scheduleWithFixedDelay(() -> {
            Long version = VERSIONS.getOrDefault(service.toPath(), -1L);
            Long newVersion = HttpInvoker.httpGet(servers + "/version?service=" + service.toPath(), Long.class);
            log.info(" ====> HbRegistry version= {}, newVersion = {}", version, newVersion);
            if (newVersion > version) {
                List<InstanceMeta> instances = fetchAll(service);
                listener.fire(new Event(instances));
                VERSIONS.put(service.toPath(), newVersion);
            }
        }, 1_000, 5_000, TimeUnit.MILLISECONDS);
    }
}
