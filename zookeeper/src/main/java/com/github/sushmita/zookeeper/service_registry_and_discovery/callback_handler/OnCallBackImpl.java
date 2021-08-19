package com.github.sushmita.zookeeper.service_registry_and_discovery.callback_handler;

import com.github.sushmita.zookeeper.service_registry_and_discovery.ServiceRegistry;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class OnCallBackImpl implements OnCallBack {

    private ServiceRegistry serviceRegistry;
    private int port;

    public OnCallBackImpl(ZooKeeper zooKeeper, int port) throws KeeperException, InterruptedException {
        serviceRegistry = new ServiceRegistry(zooKeeper);
        this.port = port;
    }
    public void registerToCluster() throws KeeperException, InterruptedException, UnknownHostException {
        InetAddress IP=InetAddress.getLocalHost();
        serviceRegistry.registerToCluster(IP.getHostAddress() + ":" + port);
    }

    public void registerForRegistryUpdates() throws KeeperException, InterruptedException {
        serviceRegistry.unRegisterFromCluster();
        serviceRegistry.registerForUpdates();
    }
}
