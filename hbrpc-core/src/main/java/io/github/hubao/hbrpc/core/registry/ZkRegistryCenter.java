package io.github.hubao.hbrpc.core.registry;

import io.github.hubao.hbrpc.core.api.RegistryCenter;
import io.github.hubao.hbrpc.core.meta.InstanceMeta;
import io.github.hubao.hbrpc.core.meta.ServiceMeta;
import lombok.SneakyThrows;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.stream.Collectors;

public class ZkRegistryCenter implements RegistryCenter {

    private CuratorFramework client = null;

    @Value("${hbrpc.zkServer}")
    private String zkServer;

    @Value("${hbrpc.zkRootPath}")
    private String zkRootPath;

    @Override
    public void start() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        client = CuratorFrameworkFactory.builder()
                .connectString(zkServer).namespace(zkRootPath)
                .retryPolicy(retryPolicy)
                .build();
        System.out.println(" ===> zk Client starting to server [" + zkServer + "]");
        System.out.println(" ===> zk Client starting to root path [" + zkRootPath + "]");
        client.start();
    }

    @Override
    public void stop() {
        client.close();
        System.out.println(" ===> zk Client closed.");
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
            System.out.println(" ===> register to zk: " + instancePath);
            client.create().withMode(CreateMode.EPHEMERAL).forPath(instancePath, "provider".getBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
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
            // 删除
            String instancePath = servicePath + "/" + instance.toPath();
            System.out.println(" ===> unregister from zk: " + instancePath);
            client.delete().quietly().forPath(instancePath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<InstanceMeta> fetchAll(ServiceMeta service) {
        String servicePath = "/" + service.toPath();
        try {
            // 获取所有子节点
            List<String> nodes = client.getChildren().forPath(servicePath);
            System.out.println(" ===> fetchAll from zk: " + nodes);
            nodes.forEach(System.out::println);
            return nodes.stream().map(i -> {
                String[] str = i.split("_");
                return InstanceMeta.http(str[0], Integer.parseInt(str[1]));
            }).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SneakyThrows
    @Override
    public void subscribe(ServiceMeta service, ChangedListener listener) {
        final TreeCache cache = TreeCache.newBuilder(client, "/" + service.toPath())
                .setCacheData(true).setMaxDepth(2)
                .build();

        cache.getListenable().addListener(
                (client, event) -> {
                    // 任何变化都会触发
                    System.out.println(" zk subscribe event:" + event);
                    List<InstanceMeta> nodes = fetchAll(service);
                    listener.fire(new Event(nodes));
                }
        );
        cache.start();
    }
}
