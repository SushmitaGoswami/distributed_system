package com.github.sushmita.zookeeper.service_registry_and_discovery;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.ArrayList;
import java.util.List;

public class ServiceRegistry implements Watcher {

    private ZooKeeper zooKeeper;
    private String SERVICE_REGISTRY_NAMESPACE = "/service_registry";
    private List<String> allAddresses;
    private String currentZnode;

    public ServiceRegistry(ZooKeeper zooKeeper) throws KeeperException, InterruptedException {
        this.zooKeeper = zooKeeper;
        createServiceRegistry();
    }

    public synchronized void updateAddress() throws KeeperException, InterruptedException {
        allAddresses = new ArrayList<String>();
        List<String> children = zooKeeper.getChildren(SERVICE_REGISTRY_NAMESPACE, this);
        for(String child:children){
            Stat exists = zooKeeper.exists(SERVICE_REGISTRY_NAMESPACE + "/" + child, false);
            if(exists == null)
                continue;
            String znode_data = new String(zooKeeper.getData(SERVICE_REGISTRY_NAMESPACE + "/" + child, false, exists));
            allAddresses.add(znode_data);
        }
        printUpdatedAddress();
    }

    public List<String> getAllAddresses() throws KeeperException, InterruptedException {
        if(allAddresses == null){
            updateAddress();
        }
        return allAddresses;
    }

    public void registerToCluster(String metaData) throws KeeperException, InterruptedException {
        if(currentZnode!=null && zooKeeper.exists(currentZnode,false)!=null) {
            return;
        }
        currentZnode = zooKeeper.create(SERVICE_REGISTRY_NAMESPACE + "/n_", metaData.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
    }
    public void unRegisterFromCluster() throws KeeperException, InterruptedException {
        if(currentZnode!=null && zooKeeper.exists(currentZnode, false)!= null) {
            zooKeeper.delete(currentZnode, -1);
        }
    }

    private void createServiceRegistry() throws KeeperException, InterruptedException {
        if(zooKeeper.exists(SERVICE_REGISTRY_NAMESPACE, false) == null) {
            zooKeeper.create(SERVICE_REGISTRY_NAMESPACE, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            System.out.println("Service Registry Created!");
        }
    }

    public void process(WatchedEvent watchedEvent) {
        switch (watchedEvent.getType()){
            case NodeChildrenChanged:
                try {
                    System.out.println("Node children changed");
                    updateAddress();
                } catch (KeeperException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        }
    }

    public void printUpdatedAddress() throws KeeperException, InterruptedException {
        System.out.print("Updated children [");
        getAllAddresses().stream().forEach((str)->System.out.print(str + ", "));
        System.out.println("]");
    }
    public void registerForUpdates() {
        try {
            updateAddress();
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
