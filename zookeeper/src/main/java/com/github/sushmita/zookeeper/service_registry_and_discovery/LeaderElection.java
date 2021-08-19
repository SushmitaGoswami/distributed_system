package com.github.sushmita.zookeeper.service_registry_and_discovery;


import com.github.sushmita.zookeeper.service_registry_and_discovery.callback_handler.OnCallBack;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;


public class LeaderElection implements Watcher {

    private ZooKeeper zooKeeper;
    private String NAMESPACE_PREFIX = "/election";
    private String currentZnodeName;
    private OnCallBack onCallBack;

    public LeaderElection(ZooKeeper zooKeeper, OnCallBack onCallBack) throws IOException, InterruptedException, KeeperException {
        this.zooKeeper = zooKeeper;
        this.onCallBack = onCallBack;
        voluenteerForLeadership();
    }


    public void voluenteerForLeadership() throws KeeperException, InterruptedException {
        String path = NAMESPACE_PREFIX + "/c_";
        String znodeFullPath = zooKeeper.create(path, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        System.out.println("Znode created " + znodeFullPath);
        currentZnodeName = znodeFullPath.replace(NAMESPACE_PREFIX+"/", "");
    }

    public void runLeaderReElection() throws KeeperException, InterruptedException, UnknownHostException {
        Stat predecessorExists = null;
        while(predecessorExists==null) {
            List<String> children = zooKeeper.getChildren(NAMESPACE_PREFIX, this);
            Collections.sort(children);
            String smallestChild = children.get(0);
            if (smallestChild.equals(currentZnodeName)) {
                System.out.println("I am the leader");
                onCallBack.registerForRegistryUpdates();
                break;
            } else {
                System.out.println("I am not the leader, " + smallestChild + " is the leader ");
                onCallBack.registerToCluster();
                // find the predecessor node to watch
                int i = Collections.binarySearch(children, currentZnodeName) - 1;
                String predecessor = children.get(i);
                // watch the predecessor node
                predecessorExists = zooKeeper.exists(NAMESPACE_PREFIX + "/" + predecessor, this);
                System.out.println("Watching to node: "+predecessor);
            }
        }
    }

    public void process(WatchedEvent watchedEvent) {
        switch (watchedEvent.getType()){
            case NodeDeleted:
                try {
                    runLeaderReElection();
                } catch (KeeperException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
        }
    }
}
