package io.github.hubao.hbrpc.core.registry.zk;

import com.alibaba.fastjson.JSON;
import io.github.hubao.hbrpc.core.api.RegistryCenter;
import io.github.hubao.hbrpc.core.api.RpcException;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class ZkRegistryCenter implements RegistryCenter {

    @Value("${hbrpc.zk.server:localhost:2181}")
    String servers;

    @Value("${hbrpc.zk.root:hbrpc}")
    String root;

    private CuratorFramework client = null;
    private List<TreeCache> caches = new ArrayList<>();

    private boolean running = false;

    @Override
    public synchronized void start() {
        if(running) {
            log.info(" ===> zk client has started to server[" + servers + "/" + root + "], ignored.");
            return;
        }
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
    public synchronized void stop() {
        if(!running) {
            log.info(" ===> zk client isn't running to server[" + servers + "/" + root + "], ignored.");
            return;
        }
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
                client.create().withMode(CreateMode.PERSISTENT).forPath(servicePath, service.toMetas().getBytes());
            }
            // 创建实例的临时性节点
            String instancePath = servicePath + "/" + instance.toPath();
            log.info(" ===> register to zk: " + instancePath);
            client.create().withMode(CreateMode.EPHEMERAL).forPath(instancePath, instance.toMetas().getBytes());
        } catch (Exception ex) {
            throw new RpcException(ex);
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
            throw new RpcException(ex);
        }
    }

    @Override
    public List<InstanceMeta> fetchAll(ServiceMeta service) {
        String servicePath = "/" + service.toPath();
        try {
            // 获取所有子节点
            List<String> nodes = client.getChildren().forPath(servicePath);
            log.info(" ===> fetchAll from zk: " + servicePath);
            return mapInstances(nodes, servicePath);
        } catch (Exception ex) {
            throw new RpcException(ex);
        }
    }

    private List<InstanceMeta> mapInstances(List<String> nodes, String servicePath) {
        return nodes.stream().map(x -> {
            String[] strs = x.split("_");
            InstanceMeta instance = InstanceMeta.http(strs[0], Integer.valueOf(strs[1]));
            System.out.println(" instance: " + instance.toUrl());
            String nodePath = servicePath + "/" + x;
            byte[] bytes;
            try {
                bytes = client.getData().forPath(nodePath);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            Map<String,Object> params = JSON.parseObject(new String(bytes));
            params.forEach((k,v) -> {
                System.out.println(k + " -> " +v);
                instance.getParameters().put(k,v==null?null:v.toString());
            });
            return instance;
        }).collect(Collectors.toList());
    }

    @SneakyThrows
    @Override
    public void subscribe(ServiceMeta service, ChangedListener listener) {
        final TreeCache cache = TreeCache.newBuilder(client, "/"+service.toPath())
                .setCacheData(true).setMaxDepth(2).build();
        cache.getListenable().addListener(
                (curator, event) -> {
                    synchronized (ZkRegistryCenter.class) {
                        if (running) {
                            // 有任何节点变动这里会执行
                            log.info("zk subscribe event: " + event);
                            List<InstanceMeta> nodes = fetchAll(service);
                            listener.fire(new Event(nodes));
                        }
                    }
                }
        );
        cache.start();
        caches.add(cache);
    }
}
