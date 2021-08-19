package com.github.sushmita.zookeeper.leader_election;


import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.Collections;
import java.util.List;


public class LeaderElection implements Watcher {

    private ZooKeeper zooKeeper;
    private String ZOOKEEPER_ADDRESS = "localhost:2181";
    private int SESSION_TIMEOUT_IN_SEC = 3000;
    private String NAMESPACE_PREFIX = "/election";
    private String currentZnodeName;

    public LeaderElection() throws IOException, InterruptedException {
        zooKeeper = new ZooKeeper(ZOOKEEPER_ADDRESS, SESSION_TIMEOUT_IN_SEC, this);
    }

    public void run() throws InterruptedException {
        synchronized (zooKeeper){
            zooKeeper.wait();
        }
    }

    public void voluenteerForLeadership() throws KeeperException, InterruptedException {
        String path = NAMESPACE_PREFIX + "/c_";
        String znodeFullPath = zooKeeper.create(path, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        System.out.println("Znode created " + znodeFullPath);
        currentZnodeName = znodeFullPath.replace(NAMESPACE_PREFIX+"/", "");
    }

    public void runLeaderReElection() throws KeeperException, InterruptedException {
        Stat predecessorExists = null;
        while(predecessorExists==null) {
            List<String> children = zooKeeper.getChildren(NAMESPACE_PREFIX, this);
            Collections.sort(children);
            String smallestChild = children.get(0);
            if (smallestChild.equals(currentZnodeName)) {
                System.out.println("I am the leader");
                break;
            } else {
                System.out.println("I am not the leader, " + smallestChild + " is the leader ");
                // find the predecessor node to watch
                int i = Collections.binarySearch(children, currentZnodeName) - 1;
                String predecessor = children.get(i);
                // watch the predecessor node
                predecessorExists = zooKeeper.exists(NAMESPACE_PREFIX + "/" + predecessor, this);
                System.out.println("Watching to node: "+predecessor);
            }
        }
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
            case NodeDeleted:
                try {
                    runLeaderReElection();
                } catch (KeeperException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        }
    }
}
