package io.github.hubao.hbrpc.core.registry.zk;

import io.github.hubao.hbrpc.core.api.RegistryCenter;
import io.github.hubao.hbrpc.core.meta.InstanceMeta;
import io.github.hubao.hbrpc.core.meta.ServiceMeta;
import io.github.hubao.hbrpc.core.registry.ChangedListener;
import io.github.hubao.hbrpc.core.registry.Event;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class ZkRegistryCenter implements RegistryCenter {

    @Value("${kkrpc.zkServer}")
    String servers;

    @Value("${kkrpc.zkRoot}")
    String root;

    private CuratorFramework client = null;
    private List<TreeCache> caches = new ArrayList<>();

    @Override
    public void start() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        client = CuratorFrameworkFactory.builder()
                .connectString(servers)
                .namespace(root) // dubbo的group就是这个玩意儿
                .retryPolicy(retryPolicy)
                .build();
        log.info(" ===> zk client starting to server[" + servers + "/" + root + "].");
        client.start();
    }

    @Override
    public void stop() {
        log.info(" ===> zk tree cache closed.");
        caches.forEach(TreeCache::close);
        log.info(" ===> zk client stopped.");
        client.close();
    }

    @Override
    public void register(ServiceMeta service, InstanceMeta instance) {
        String servicePath = "/" + service.toPath();
        try {
            // 创建服务的持久化节点
            if (client.checkExists().forPath(servicePath) == null) {
                client.create().withMode(CreateMode.PERSISTENT).forPath(servicePath, "service".getBytes());
            }
            // 创建实例的临时性节点
            String instancePath = servicePath + "/" + instance.toPath();
            log.info(" ===> register to zk: " + instancePath);
            client.create().withMode(CreateMode.EPHEMERAL).forPath(instancePath, "provider".getBytes());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void unregister(ServiceMeta service, InstanceMeta instance) {
        String servicePath = "/" + service.toPath();
        try {
            // 判断服务是否存在
            if (client.checkExists().forPath(servicePath) == null) {
                return;
            }
            // 删除实例节点
            String instancePath = servicePath + "/" + instance.toPath();
            log.info(" ===> unregister from zk: " + instancePath);
            client.delete().quietly().forPath(instancePath);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<InstanceMeta> fetchAll(ServiceMeta service) {
        String servicePath = "/" + service.toPath();
        try {
            // 获取所有子节点
            List<String> nodes = client.getChildren().forPath(servicePath);
            log.info(" ===> fetchAll from zk: " + servicePath);
            nodes.forEach(System.out::println);
            return mapInstances(nodes);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static List<InstanceMeta> mapInstances(List<String> nodes) {
        return nodes.stream().map(x -> {
            String[] strs = x.split("_");
            return InstanceMeta.http(strs[0], Integer.valueOf(strs[1]));
        }).collect(Collectors.toList());
    }

    @SneakyThrows
    @Override
    public void subscribe(ServiceMeta service, ChangedListener listener) {
        final TreeCache cache = TreeCache.newBuilder(client, "/"+service.toPath())
                .setCacheData(true).setMaxDepth(2).build();
        cache.getListenable().addListener(
                (curator, event) -> {
                    // 有任何节点变动这里会执行
                    log.info("zk subscribe event: " + event);
                    List<InstanceMeta> nodes = fetchAll(service);
                    listener.fire(new Event(nodes));
                }
        );
        cache.start();
        caches.add(cache);
    }
}
