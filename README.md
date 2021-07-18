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

**Please refer to this example code here.**

### Service Registry & Discovery
**Static Service registry configuration** -  Each node contains a configuration which contains the addresss of all other nodes in the cluster. This architecture needs manual intervention when a new node joins or an existing node exits.

**Semi dynamic service registry** - In this architecture, along with maintaining a centralized configuration file, Configuration Management services like pupet or chef is used to update the configurations dynamically.

**Dynamic Service Registry** - Zookeeper provides support for dynamic service registry. It can be designed in the following way.

First, a permanent znode named /service_registry is created and several ephemeral znodes are created as its children. Each ephemeral znode contains address and port of the application running in the corresponding physical node. External node can access the children of the /service_registry node to get the addresses of other nodes. This can be easily fitted to a leader-worker/peer-to-peer communication. In the leader-wroker architecture, when a worker node joins the cluster, it creates a znode under the /service_registry node and when a node is elected as leader, it takes the updated addresses and also deregister itself from the cluster i.e. deletes the corresponding ephemeral node from the cluster.

**Please refer to this example code here.**

Following are 4 nodes running. The top left corner one is elected as leader. As per the sequence of node creation, following is the status
![zoo1_LI](https://user-images.githubusercontent.com/20486206/125905342-193a741d-ba00-4ea2-a4e9-0a023ca3cc47.jpg)

When the 2nd node dies
![zoo2_LI](https://user-images.githubusercontent.com/20486206/125905446-35461083-3566-4c2e-9cd5-a410cd83d3f2.jpg)

when the 4th node dies
![zoo3_LI](https://user-images.githubusercontent.com/20486206/125905468-798efd00-75a9-42bb-9cef-a1a807824158.jpg)

when all the nodes rejoined and leader dies.
![zoo4_LI](https://user-images.githubusercontent.com/20486206/125905490-58bd31ee-59e6-47ae-b11d-4c9fe850c20d.jpg)


## Communication among nodes
Different nodes in a cluster communicates among each other using HTTP protocol. Let's discuss the difference between HTTP 1.1 and HTTP 1.2

**HTTP 1.1**
1. For every request 1 connection is being created between server and client. Next request usually needs to wait for a specific time until the response for the previous request returns. If it takes more time to return, then a new connection is being created to send the 2nd request. So, there might be some time, when there will be many connections created and which may block the system. However, the no of connection is limited by the no of available ports and operating system.

2. Headers are basically key/value pair and hence it is easy to debug using some packet tracing tool like wireshark.

3. The only advantage is if a connection breaks, it will not cause any harm to other connections.


**HTTP 1.2**

1. Multiple requests are interleaved in a single connection.

2. Headers are being compressed which reduces the payload size, but it will make this hard to debug.


HTTP Connection can be either of the two types.
1. Synchronous
2. Asynchronous

In order to achieve the best performance, HTTP Connection pooling is being used. 
1. HTTP 1.2 by default supports Connection pooling
2. For HTTP 1.1, some client provides the pooling by default, if not we need to se it by saying "keep alive" to true.


### Different semantics of message delivery in a distributed system

1. **At most once** - Each messaged will be sent by the client to server at most once. But there may be some cases where message is lost because the server doesn't receive or process the message for some reasons. This method is applicable to some of the following cases.
   - Sending notification to user
   - Message delivery or logging

2. **At least once** - A message is delivered between client and server atleast once. This is applicable to following instances where state of the system would be same when an operation is performed only once and n times. Following are some of the examples.
   - Reading the first line of a file.
   - Updating the status of an user to active.
   - Deleting a record.

But this might not be applicable to the other cases like,
   - Appending a line to a file.
   - Banking transaction etc.

There are basically two types of operation
   - **IDEMPOTENT** - Some of the operations which should be performed only once.
   - **NON-IDEMPOTENT** - Operations that has same effect even if it is performed multiple times instead of once.

**Strategy which may be used in distributed system**
Client sends a sequence no and retry variable for each message to server. Server checks the sequence no and retry variable. Server keeps the sequence no each time it updates a message. Next time, if it receives a message with same sequence id and a true retry value, then it will match the received sequence no with the one stored in the database, if it matches, then basically last time, the server has performed the operation, but the client hasn't received any response yet. So, it will send an acknowledgement to the client. Now, when the server finds that the sequence no doesn't matches, it will perform the updates and send the acknowledgement to the client.


## Load Balancing

Load balancing is defined as the methodical and efficient distribution of network or application traffic across multiple servers in a server farm.

### What is the motivation behind load balancing?
Let's say, you have a cluster of backend servers and an user facing front end server. When the load on front end server increases, it may got down. So, we may scale the front end servers and create a cluster. But then, user has to know all the addresses of different front end server to send request to. There will be a need to maintain the addresses via a service registry implementation. Moreover, for each client, we have to duplicate the logic for balancing the load among different front end servers. 

Here comes the load balancer which not only abstract the cluster of front end server from the user, but also it maintains high availability of the system by distributing the load among multiple front end servers.

### Different types of load balancers
1. **Layer 4 (Transport layer) load balancer** - Transport layer load balancer is the simplest kind of load balancer. It checks 4 tuples i.e. source and destination IP and port to distribute the load amond multiple servers.
2. **Layer 7 (Application layer) load balancer** - Application layer load balancer inspect the request method, parameter, header to distribute the load.


### Different strategies of load balancing

1. **Round Robin**  - In this method, load is evenly distributed. But it has flaws. Let's say, we have a load balancer which is balancing the load among 3 different backend servers. Now the 2nd server is receiving all the POST requests and after sometime, it may break down. And similarly if this continues, entire cluster will become stale.
2. **Weighted Round Robin** - In this method, a weight is assigned to each server and accordingly load is distributed. Let's say, there are 2 server and assigned weights are 2,1. Now, for every 3 requests, 1st server will receive 2 requests and 2nd one will receive only 1 request.
3. **Source IP Hashing** - In order to maintain the sticky session, it is possible to hash the source ip using a deterministic hashing algorithm and forward the request accordingly.

But these approaches have flaws. One of them is these methods don't consider the present load on the servers. Following are some of the alternatives.

1. **Least Connection** - Load balancer forward the requests based on the number of connections each server currently holds.
2. **Weighted Response time** - Load balancer checks the health of each server and forward the request to that server which response at the earliest.
3. **Agent based** - In the agent based load balancing, an agent calculates the cpu, memory usage and send those information to the load balancer which then decides and routes the traffic based on that.


## Message Brokers

Message broker (also known as an integration broker or interface engine) is an intermediary computer program module that translates a message from the formal messaging protocol of the sender to the formal messaging protocol of the receiver.

### What is the motivation behind message broker?
Let's discuss problems with the following scenarios,

1. **Direct Communication** - Let's say, there are 4 services, purchase, billing, shipping and notification. When an user makes a purchase, gateway transfers the calls to the purchase service and then it consecutively calls other services. Now, when any of the service in the chain gets down, the entire chain breaks.
2. **Publisher vs subscriber model** - Let's say, the purchase service needs to sends the message to a sets of analytical servers which have subscribed to the event when an user makes a purchase. But in this scenario, the purchase service needs to open several connections between client and server.
3. **Heavy traffic** - Let's say, in case of a direct communication between two services, one service may gets overwhelmed when other service is sending data to it.

So, the solution is to use a message broker which follows event driven publisher/subscriber architecture and act as an intermidiator between two services to decouple the operations of two services while providing some of the following additionl functionalities beside sending data between two parties asynchronously,
 - Data Queuing
 - Data Validation
 - Routing
 
 Message brokers are designed to work in a distributed manner to maintain the scalability while avoiding the single point of failure and fault tolerance. However, as a new component is being introduced, it increases the latency.
 
So, the  idea is to use message broker as 
1. distributed queue to be used in direct communication.
2. as a midiator in a publisher and subscriber model.


## Apache Kafka - distributed streaming and messaging platform
Apache kafka is a distributed, fault tolerand and highly scalable hogh performance streaming platform.

### Terminologies
1. Producer - Actor which sends the data.
2. Consumer - Actor which receives the message.
3. Kafka broker - middle man between producer and consumer. 
4. Topic - Kafka maintains a sets of message of same type in a predefined structure named Topic.

### Distributed and scalable architecture of Topic
Topic is a collection of ordered queues and each queue is called Partition. Each partition is basically an ordered queue which stores the messages in an order it in queued. Each message is assigned an unique number w.r.t to a partition, named Offset. So, a size of a topic is basically limited to that machine in which that broker is running. In order to scale the topic, it divides it into multiple partition and each partition is stored in multiple kafka brokers. Now, what if any of kafka broker fails? The partition stored in that broker machine will also be lost. Hence, kafka maintains multiple copies of a single partition. Each partition is being hold by a leader broker which performs all the read and write. Whereas, all other remainging broker which will store the other copies will just follow leader. So, when the leader fails, any of the followers can replace the leader for that partition.

### Distributed and scalable architecture of Consumer
1. In order to scale the message processing among multiple consumer, kafka maintains a predefined consumer group for a set of consumers.
2. In order to use kafka in a publisher/subscriber architecture, each consumer needs to be present in different consumer group and when publisher publish a message, it will be broadcasted to multiple consumers.


### Basic Commands

1. **Start Kafka Server** - kafka-server-start.bat config/server.properties
2. **Create topic** - kafka-topics.bat --create --topic chat --bootstrap-server localhost:9092 --replication-factor 3 --partitions 3
3. **Describe a topic** - kafka-topics.bat --bootstrap-server localhost:9092 --describe chat1
 ![image](https://user-images.githubusercontent.com/20486206/126038809-18ababec-ae5f-4d82-b907-261d67dcd408.png)
4. **Send message using a producer** - kafka-console-producer.bat --bootstrap-server localhost:9092 --topic chat 1st 2nd ...  .If producer doesn't mention the partition of the topic while sending a message, kafka will hash the key and based on that store the message in that partition. If no key is mentioned, then kafka will use round robin method to distriute the load among multiple partition. 
5. **Consume message using a consumer** - kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic chat --from-beginning


**Please refer to this basic example.**

## Distributed Database
A distributed database (DDB) is an integrated collection of databases that is physically distributed across sites in a computer network. 

### Problem with the centralized database
1. **Single point of failure** 
2. **Latency while accessing the application from different continents.**
3. **Parallesim is limited by no of cores available in the machine.**
4. **Amount of data can be stored is also limited by the storage of the system.**

### Sharding
Sharding is one of the techniques being used to scale a database. The entire data is divided into multiple chunks and stored in different computers. Sharding can be of either of two types,

1. **Hash Based Sharding** - In case of hash based sharding a deterministic has function is used over the key to find the hash. But in this method, the closely related data can be distributed evenly and hence for the range based queries, it will not be optimized.
2. **Range Based Sharding** - In case of a range based sharding, data is divided into chunks based on some range of a given column/field.

Sharding introduces complexity as we need to aggregate the data from different shard to find the result of a query. NOSQL databases are designed to be distributed. However, these dbs doesn't gurrantee the ACID property.

#### Issues with the hash based sharding
1. When a new shard is added or a shard is removed, we need to redistribute the data.
2. What if some of the computing nodes doesn't have enough capacity.

### Dynamic Hashing
Solution to the hash based sharding is Dynamic Hashing. In the approach. entire key space is considered as a ring and a range is assigned to individual nodes. When a new node is added, only those range of keys need to be assigned to this node and the node from which these keys have been taked would be restructured. This method is also called consistent hashing. Moreover, loads can be distributed based on capacity of the nodes.

![image](https://user-images.githubusercontent.com/20486206/126060500-29cfeb1b-bad8-4074-a6ce-167b0d4d3c45.png)

There are some problems with consistent hashing too. For example, if the deterministic hash function always maps more keys to a single node, then that node may be overloaded. Hence multiple hash function can be used to map the keys. In this way, a single node can be assigned to keys from multiple different ranges from the hash space.

### Data Replication.
In order to avoid any failure of a node and loss of a shard, multiple copies of same data would be maintained. Now, the question is how data between different copies would be synced.

#### Different Architentures

1. **Eventual Consistency** - All the writes will be done in master node and data will be synced asynchronously to slave nodes. But user may find stale data at any point. But it will ensure high availibity and lower latency.
2. **Strict Consistency** -  Write operation would be completed only after all the data would be synced to slave. But it will slow down the system. so the solution is to use Quorum Consensus.

#### Quorum Consensus
Let's define,
R = no of nodes a reader needs to read from.
W = no of nodes a writer needs to write to.
N = total no of nodes in a cluster.

R + W > N

if the abode formula is maintained then, there would be atleast 1 overlapping node. For eahc record, along with the data, version is maintained. So when a reader performs a read, it will check the version field of all the different versions of the data read from different cluster and atleast one will show the updated data.

For read intensive operation, R can be kept as 2 to increase the performance of read operation.
