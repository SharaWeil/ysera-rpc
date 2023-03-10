package com.ysera.rpc.core.registry.zk;

import com.ysera.rpc.core.registry.Registry;
import com.ysera.rpc.core.registry.RegistryProperties;
import com.ysera.rpc.core.registry.RegistryProperties.ZookeeperProperties;
import com.ysera.rpc.exception.RegistryException;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Author Administrator
 * @Date 2023/1/17
 *                        /root
 *                         /
 *                    /ysera-rpc
 *                  /         \
 *        /service-name1      /service-name2
 *           /                     \
 *     [[ip:port],[ip:port]]    [[ip:port],[ip:port]]
 *
 **/
public class ZookeeperRegistry implements Registry {
    private static final Logger log = LoggerFactory.getLogger(ZookeeperRegistry.class);

    private final ZookeeperProperties properties;
    private final CuratorFramework client;
    private static final String DEFAULT_ZOOKEEPER_ADDRESS = "127.0.0.1:2181";

    public ZookeeperRegistry(RegistryProperties registryProperties) {
        properties = registryProperties.getZookeeper();

        final ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(
                (int) properties.getRetryPolicy().getBaseSleepTime().toMillis(),
                properties.getRetryPolicy().getMaxRetries(),
                (int) properties.getRetryPolicy().getMaxSleep().toMillis());

        CuratorFrameworkFactory.Builder builder =
                CuratorFrameworkFactory.builder()
                        .connectString(properties.getConnectString())
                        .retryPolicy(retryPolicy)
                        .namespace(properties.getNamespace())
                        .sessionTimeoutMs((int) properties.getSessionTimeout().toMillis())
                        .connectionTimeoutMs((int) properties.getConnectionTimeout().toMillis());
        client = builder.build();
    }

    @PostConstruct
    public void start() {
        client.start();
        log.info("start zk client !!!");
        try {
            if (!client.blockUntilConnected(30, TimeUnit.SECONDS)) {
                client.close();
                throw new RegistryException("zookeeper connect timeout: " + DEFAULT_ZOOKEEPER_ADDRESS);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void put(String key, String value, boolean deleteOnDisconnect) {
        put(key,value.getBytes(StandardCharsets.UTF_8),deleteOnDisconnect);
    }

    @Override
    public void put(String key, byte[] value, boolean deleteOnDisconnect) {
        final CreateMode mode = deleteOnDisconnect ? CreateMode.EPHEMERAL : CreateMode.PERSISTENT;
        try {
            client.create()
                    .orSetData()
                    .creatingParentsIfNeeded()
                    .withMode(mode)
                    .forPath(key, value);
        } catch (Exception e) {
            throw new RegistryException("Failed to put registry key: " + key, e);
        }
    }

    @Override
    public List<String> children(String key) {
        try {

            List<String> result = client.getChildren().forPath(key);
            result.sort(Comparator.reverseOrder());
            return result;
        } catch (Exception e) {
            throw new RegistryException("zookeeper get children error", e);
        }
    }

    @Override
    public Boolean exist(String serviceName) {
        try {
            return null != client.checkExists().forPath(serviceName);
        } catch (Exception e) {
            throw new RegistryException("check path error:", e);
        }
    }

    @Override
    public byte[] getInfo(String key) {
        try {
            return client.getData().forPath(key);
        } catch (Exception e) {
            throw new RegistryException("check path error:", e);
        }
    }
}
