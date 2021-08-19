package com.github.sushmita.zookeeper.client;

import com.github.sushmita.zookeeper.leader_election.LeaderElection;
import com.github.sushmita.zookeeper.service_registry_and_discovery.Application;
import com.github.sushmita.zookeeper.watchers.WatcherDemo;
import org.apache.zookeeper.KeeperException;

import java.io.IOException;
import java.net.UnknownHostException;

public class Main {
    public static void main(String[] args) {
        try {
//            LeaderElection leaderElection = new LeaderElection();
//            leaderElection.voluenteerForLeadership();
//            leaderElection.runLeaderReElection();
//            leaderElection.run();
//            leaderElection.close();

//            WatcherDemo watcherDemo = new WatcherDemo();
//            watcherDemo.watchTargetZnode();
//            watcherDemo.run();
//            watcherDemo.close();

             runServiceRegistryDemo(args);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }

    }

    private static void runServiceRegistryDemo(String args[]) throws InterruptedException, IOException, KeeperException {
            int port = args.length ==0? 8080:Integer.parseInt(args[0]);
            Application application = new Application(port);
            application.runLeaderReelection();
            application.run();
            application.close();
    }
}
