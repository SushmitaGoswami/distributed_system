package com.github.sushmita.zookeeper.service_registry_and_discovery.callback_handler;

import org.apache.zookeeper.KeeperException;

import java.net.UnknownHostException;

public interface OnCallBack {

    void registerToCluster() throws KeeperException, InterruptedException, UnknownHostException;
    void registerForRegistryUpdates() throws KeeperException, InterruptedException;
}
