package com.github.sushmita.zookeeper.service_registry_and_discovery;


import com.github.sushmita.zookeeper.service_registry_and_discovery.callback_handler.OnCallBack;
import com.github.sushmita.zookeeper.service_registry_and_discovery.callback_handler.OnCallBackImpl;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.net.UnknownHostException;

public class Application implements Watcher {

    private String ZOOKEEPER_ADDRESS = "localhost:2181";
    private int SESSION_TIMEOUT_IN_SEC = 3000;
    private ZooKeeper zooKeeper;
    private LeaderElection leaderElection;

    public Application(int port) throws IOException, KeeperException, InterruptedException {
        zooKeeper = new ZooKeeper(ZOOKEEPER_ADDRESS, SESSION_TIMEOUT_IN_SEC, this);
        OnCallBack onCallBack = new OnCallBackImpl(zooKeeper, port);
        leaderElection = new LeaderElection(zooKeeper,onCallBack);
    }

    public void run() throws InterruptedException {
        synchronized (zooKeeper){
            zooKeeper.wait();
        }
    }

    public void runLeaderReelection() throws InterruptedException, UnknownHostException, KeeperException {
        leaderElection.runLeaderReElection();
    }

    public void close() throws InterruptedException {
        zooKeeper.close();
        System.out.println("Closed Zookeeper connection!");
    }

    public void process(WatchedEvent watchedEvent) {
        switch (watchedEvent.getType()){
            case None:
                if(watchedEvent.getState().equals(Event.KeeperState.SyncConnected)){
                    System.out.println("successfully connected!");
                }else{
                    synchronized (zooKeeper){
                        zooKeeper.notifyAll();
                    }
                    System.out.println("successfully disconnected!");
                }
                break;
        }
    }
}
