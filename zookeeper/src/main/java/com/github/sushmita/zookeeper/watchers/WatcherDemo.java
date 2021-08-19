package com.github.sushmita.zookeeper.watchers;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.List;

public class WatcherDemo implements Watcher{
    private ZooKeeper zooKeeper;
    private String ZOOKEEPER_ADDRESS = "localhost:2181";
    private int SESSION_TIMEOUT_IN_SEC = 3000;
    private String targetZnode = "/target_znode";

    public WatcherDemo() throws IOException, InterruptedException {
        zooKeeper = new ZooKeeper(ZOOKEEPER_ADDRESS, SESSION_TIMEOUT_IN_SEC, this);
    }

    public void run() throws InterruptedException {
        synchronized (zooKeeper){
            zooKeeper.wait();
        }
    }

    public void close() throws InterruptedException {
        zooKeeper.close();
        System.out.println("Closed Zookeeper connection!");
    }

    public void watchTargetZnode() throws KeeperException, InterruptedException {
        Stat exists = zooKeeper.exists(targetZnode, this);
        if(exists == null){
            return;
        }
        byte[] data = zooKeeper.getData(targetZnode, this, exists);
        System.out.println("Data :- " + data);
        List<String> children = zooKeeper.getChildren(targetZnode, this);
        System.out.println("Children :- " + children);
    }


    public void process(WatchedEvent watchedEvent) {
        switch (watchedEvent.getType()){
            case None:
                if(watchedEvent.getState().equals(Watcher.Event.KeeperState.SyncConnected)){
                    System.out.println("successfully connected!");
                }else{
                    synchronized (zooKeeper){
                        zooKeeper.notifyAll();
                    }
                    System.out.println("successfully disconnected!");
                }
                break;
            case NodeCreated:
                System.out.println("Node created: " + watchedEvent.getPath());
                break;
            case NodeDeleted:
                System.out.println("Node deleted: " + watchedEvent.getPath());
                break;
            case NodeDataChanged:
                System.out.println("Node data changed: " + watchedEvent.getPath());
                break;
            case NodeChildrenChanged:
                System.out.println("Node children changed: " + watchedEvent.getPath());
                break;
        }

        try {
            watchTargetZnode();
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
