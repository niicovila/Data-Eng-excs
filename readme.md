# Simple Distributed Ping-Pong Implementation

## Overview

This solution uses a master-slave architecture with two main classes: `MasterNode` and `WorkerNode`. The `MasterNode` manages and communicates with the worker nodes, while the `WorkerNode` handles incoming requests and processes messages.

1. **MasterNode**: Manages interactions with multiple worker nodes.
2. **WorkerNode**: Handles incoming requests from the master node and processes messages accordingly.

## MasterNode Class

The `MasterNode` class interacts with multiple `WorkerNode` instances and provides these functionalities:

1. **Ping Workers**: Sends a "PING" message to each worker node sequentially to check if they're available. This ensures each node is individually acknowledged.

2. **Broadcast to Workers**: Sends a "BROADCAST" message to all worker nodes simultaneously. The master node uses a fixed thread pool to manage these concurrent connections efficiently (each worker node gets contacted in parallel). This parallel approach reduces latency and speeds up the broadcast process.

3. **Round-Robin Messaging**: Initiates a message chain starting from the first worker node. The message passes through a sequence of nodes, with each node adding its own identifier before forwarding it to the next node. If the message reaches the end of the chain or a node can’t forward it, the message is sent back to the master node. This method ensures that messages are processed in a controlled and orderly fashion.

## WorkerNode Class

The `WorkerNode` class functions as a server, managing incoming connections and processing messages from the master node. Here’s how it works:

- **Socket Management**: Each `WorkerNode` listens on a specific port and manages connections using separate threads. A `WorkerHandler` thread is created for each connection to handle communication with the master node.

- **Message Handling**: The `WorkerHandler` class processes different types of messages:
  - **PING Messages**: Replies with "PONG" to indicate the node is responsive.
  - **BROADCAST Messages**: Acknowledges receipt with "BROADCAST_RECEIVED".
  - **Chain Messages**: Adds its own identifier to the message and forwards it to the next worker node in the chain. If a node is the last in the chain or cannot forward the message, it sends the message back to the master node.

## Requirements

- Java Development Kit (JDK) 8 or later

## Running the System

1. **Compile the Java Files**

    ```bash
    javac MasterNode.java WorkerNode.java
    ```

2. **Start Worker Nodes**

    Open multiple terminal windows and start worker nodes on different ports:

    ```bash
    java WorkerNode 5001
    java WorkerNode 5002
    java WorkerNode 5003
    ```

3. **Start the Master Node**

    In a new terminal window, start the master node:

    ```bash
    java MasterNode
    ```



