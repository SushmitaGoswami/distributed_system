# Distributed_system

## What is distributed system?
Distributed system is a system of set of different processes running on different computers communicating with each other via the network, it may share the same state or work together to achieve a common goal.

## Motivation behind distributed system design
There are several limitation of a centralized system. Followings are the some of those.
- **Vertical scaling** is limited by the performance and storage capacity of a single machine.
- **Single point of failure** occurs if there is no failover set up available.
- **Security breach and privacy issue** - DDOS attack and 
-  **High Latency** or poor user experience when some user from other continents wants to access the service.

**Quiz Time** - So, if more than one processes running on the same machine while sharing the same memory, cpu and all underlying resources provided by OS, will it be a distributed set up?

**Answer** - No.

All these issues can be solved using an distributed system architecture.

## Terminologies
- **Node** - Processes, connected via network running on different computers.
- **Cluster** - A set of processes running on different computer executing same piece of tasks in order to achive a common goal.

### How to distribute the task among different nodes in a cluster?
Following are the possible ways for distributing the taksk.
- **Manual distribution** 
- **Master slave architecture** - This can be used to distribute the tasks among muliple nodes and then recollect to get the result. But there are several issues while choosing a master node among all posible nodes and also what if the master goes down. The solution is to have a automatic leader selection algorithm. However, there are following challenges
  - Selecting a leader among a set of nodes is not a trivial task.
  - Each node does't have any idea about all other nodes in the cluster. There should some of the service registry and coordination system available.
  - Failure detection algorithm should be there to find the leader failure point. So that any other node can be selected as a leader and when the previous leader rejoins the cluster, it will join as a worker node and follow the leader.

## Master-slave Co-ordination system
**Apache zookeeper** is one of the open source scalable master slave co-ordination systems designed to work for distributed architecture. It usually runs odd no of nodes in a production system. Using zookeeper each node of a cluster will communicate with each other via zookeeper instead of communicating directly.

### Zookeeper Data Model
Zookeeper maintains a abstract tree like data structure where each node is called znode. It is a represenation of data model by the zookeeper.
**Znode**
1. Znode can be a file
2. Znode can be a directory which contains files.

Znode can be of two types
1. Ephemeral znode - Scope of an ephemeral znodes is limited to a session. If an application which created the znode got disconnected, then the znode will be deleted.
2. Permanent znode - This znode remains even if the application got disconnected.
 
### How zookeeper performs a leader selection among multiple nodes?
Each node connecting with zookeeper will try to participate in the leader selection competition by creating one znode under the given parent as a representative. Each znode will be assigned a unique auto-incremented no on its name. Each time a new node being joined, it will query the list of chidren of its parent. If it finds that it is the child with lowest sequence no , then it declared it as a leader and all other will simply wait to follow the instruction from the leader.

Here is an example code of leader selection algorithm. 

![zookeeper_1 (2)](https://user-images.githubusercontent.com/20486206/125638640-a09e0f14-e8e9-45c2-a601-40863d9a7f0c.png)

#### What if the leader fails? How will you detect the leader failure?

**Attempt1**
Each node will subscribe/watch to the ephemeral znode corresponding to the leader znode. Whenever, the leader fails, all other remaining nodes will be notified. Now, all the nodes will try to fetch the existing children of the parent node to find the smallest child i.e, the next leader. But here is an issue. As there moght be large no of nodes fetching data from a cluster, which might be much smaller in comparision, will be bombered with so many requests and hence might got down. 


**Attempt2**

In this case, each node will point to the ephemeral znode of the immidiate predecessor node. As soon as the leader fails, only the next node will be notified and fetch the children from the zookeeper cluster and will find itself as a leader. And if the notified node finds that it was not the leader which got down, it will simply closes the gap.

Here is an example code for this leader re-election algorithm.

Following are 4 nodes running. As per the sequence of node creation, following is the status

![zookeeper_2_LI (3)](https://user-images.githubusercontent.com/20486206/125654326-02f90ae8-076a-4d0d-978f-e2e4392b7a3f.jpg)

Now, the leader dies, status changes as follows

![zookeeper_3_LI](https://user-images.githubusercontent.com/20486206/125654353-3cedf66e-5d7d-4ae4-a153-26dba750e1ca.jpg)

Next, the leader rejoins, status changes as follows

![zookeeper_4_LI](https://user-images.githubusercontent.com/20486206/125654370-c5e99595-434c-43c1-a5cc-852d425aabc2.jpg)

Finally, a non leader node dies.

![zookeeper_5_LI](https://user-images.githubusercontent.com/20486206/125654395-69159b26-1ac8-42ae-8725-b270648e82db.jpg)



### Using Zookeeper CLI tool

1. Create a znode

```
create /parent "parent znode"
```

2. Crete a child znode
```
 create /parent/child "child znode"
```

3. Get a znode
```
get -s -w /parent
```

4. Set value to an existing znode
```
set /znode "some data"
```

5. Delete an existing znode
```
delete /znode
```


### Zookeeper client threading model

Zookeeper creates 2 threads 
1. **IO thread** :- Usually, we don't interact with this thread. IO thread handles the following responsibilities,
   a. Handles all the network communication to the zookeeper server.
   b. Pings to the zookeeper server
   c. Session management
   d. Session timeout etc.
   
2. **Event Thread** :- Event thread handles the following responsibilities,
   a. All kinds of events including Connection, Disconnection.
   b. Custom znode watch and triggers.
   c. Events are executed in sequential order.
   
### Zookeeper watchers
Watcher helps to get a notification about an event. Using subscription, it follows the event driven architecture.
Examples are as follows
- **exists(..)** - It is possible to attach an one time triggered watcher to this method which notifies about any creation/deletion event.
- **getData(..)** - It is possible to attach an one time triggered watcher to this method which notifies about any change in the content of the znode.
- **getChildren(..)** - It is possible to attach an one time triggered watcher to this method which notifies about any change in the children of that znode.

Please refer to this example code.

### Service Registry & Discovery
**Static Service registry configuration** -  Each node contains a configuration which contains the addresss of all other nodes in the cluster. This architecture needs manual intervention when a new node joins or an existing node exits.

**Semi dynamic service registry** - In this architecture, along with maintaining a centralized configuration file, Configuration Management services like pupet or chef is used to update the configurations dynamically.

**Dynamic Service Registry** - Zookeeper provides support for dynamic service registry. It can be designed in the following way.

First, a permanent znode named /service_registry is created and several ephemeral znodes are created as its children. Each ephemeral znode contains address and port of the application running in the corresponding physical node. External node can access the children of the /service_registry node to get the addresses of other nodes. This can be easily fitted to an leader-worker/peer-to-peer to communication. In the leader-wroker architecture, when a worker node joins the cluster, it creates a znode under the serviceregistry node and when a node is elected as leader, it takes the updated addresses and also deregister itself from the cluster i.e. deletes the corresponding ephemeral node.


