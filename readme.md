# Distributed System Implementation

## Overview

This system consists of a master node and multiple worker nodes running on the same machine. The master node can perform one-to-one messaging, broadcasting, and round-robin messaging.

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
    java WorkerNode 5000
    java WorkerNode 5001
    java WorkerNode 5002
    ```

3. **Start the Master Node**

    In a new terminal window, start the master node:

    ```bash
    java MasterNode
    ```

## Description

- **Master Node**: Sends "PING", "BROADCAST", and "CHAIN" messages to worker nodes.
- **Worker Nodes**: Handle incoming messages, respond to "PING" and "BROADCAST", and pass "CHAIN" messages in a round-robin fashion.

## Notes

- For simplicity, the round-robin implementation is hardcoded to use fixed ports for demonstration. In a real-world scenario, dynamic determination of the next worker node would be required.
