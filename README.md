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
Zookeeper maintains a abstract tree like data structure where each node is called znode. 
**Znode**
1. Znode can be a file
2. Znode can be a directory which contains files.

Znode can be of two types
1. Ephemeral znode - Scope of an ephemeral znodes is limited to a session. If an application which created the znode got disconnected, then the znode will be deleted.
2. Permanent znode - This znode remains even if the application got disconnected.
 
### How zookeeper performs a leader selection among multiple nodes?
Each node connecting with zookeeper will try to participate in the leader selection competition by creating one znode under the given parent as a representative. Each znode will be assigned a unique auto-incremented no on its name. Each time a new node being joined, it will query the list of chidren of its parent. If it finds that it is the child with lowest sequence no , then it declared it as a leader and all other will simply wait to follow the instruction from the leader.












