# 1) Presentation

When developers work on a client/server application, they always need to send and receive data from the remote. One simple way to do so is to use directly sockets provided by java.net package. However the Java API does not provide pending queues to send/receive data from the remote. This project has been created in order to send bytes to a remote and to receive bytes from the remote in an asynchronous way.

This API support TCP/IP and UDP protocol.

# 2) Download and compilation

First you need to download this project on your computer. To do so, you can use the following command line:

```git
git clone https://github.com/Pierre-Emmanuel41/communication.git
```

Executing the batch file <code>deploy.bat</code> will download each dependency and build everything. Finally, you can add the project as maven dependency to your maven project :

```xml
<dependency>
	<groupId>fr.pederobien</groupId>
	<artifactId>communication</artifactId>
	<version>2.0-SNAPSHOT</version>
</dependency>
```

# 3) Tutorial

The class <code>Communication</code> provides all the methods you may need.

The network implementation is encapsulated using the following interfaces:<br>
<code>IClientImpl</code> which specify how to connect/disconnect with the remote<br>
<code>IServerImpl</code> which specify how to open/close a server but also how to wait for a new client<br>
<code>IConnectionImpl</code> which specify how to send/receive data from the remote but also how to close the connection with the remote<br>

The library has been conceived to be used independently from the network properties, that is why on both client and side, interfaces are generic a needs an intermediate class that contains data to be used to connect with the remote (on the client side), or to open a communication point (on the server side). The library has only been tested with ethernet network therefore we will use this kind of network as our communication network.

The interface / class we will use to gather information used to connect with the remote is <code>IEthernetEndPoint</code> and it's implementation class is <code>EthernetEndPoint</code>. This class contains only two parameters: The remote IP address and the remote port number. The IP address is only used on client side as the server will open a communication point on each ethernet interface.

A <code>ClientConfig</code> is mandatory to create a Client:

```java
String clientName = "Demo Client";
IEthernetEndPoint endPoint = new EthernetEndPoint("127.0.0.1", 12345);

// Creating a client configuration with default parameter values
ClientConfig<IEthernetEndPoint> clientConfig = Communication.createClientConfig(clientName, endPoint);
```

A <code>ServerConfig</code> is mandatory to create a Server:

```java
String serverName = "Demo server"
IEthernetEndPoint endPoint = new EthernetEndPoint(12345);

// Creating a server configuration with default parameter values
ServerConfig<IEthernetEndPoint> serverConfig = Communication.createServerConfig(serverName, endPoint);
```

Finally once the configurations are created, the implementation needs to be defined. As mentioned at the beginning, the network implementation is completely encapsulated so that all the client, server, connection's architecture related can be used whatever the network is.

To define you own client implementation, the interface <code>IClientImpl</code> has to be implemented and to create the associated client:

```java
ClientConfig clientConfig = Communication.createClientConfig("My custom client", "127.0.0.1", 12345);
IClientImpl myCustomClientImpl = new MyCustomClientImplementation();

IClient myCustomClient = Communication.createCustomClient(clientConfig, myCustomClientImpl);
```

To define your own server implementation, the interface <code>IServerImpl</code> has to be implement and to create the associated server:

```java
ServerConfig serverConfig = Communication.createServerConfig("My custom server", "127.0.0.1", 12345);
IServerImpl myCustomServerImpl = new MyCustomServerImplementation();

IServer server = Communication.createCustomServer(serverConfig, myCustomServerImpl);
```

To define your own connection implementation, the interface <code>IConnectionImpl</code> has to be implemented and to create the associated connection:

```java

// Client side
IConnection connection = Communication.createConnection(clientConfig, clientConfig.getEndPoint(), new MyCustomConnectionImplementation(clientConfig));


// Server side

// Waiting for a new client
Socket socket = serverSocket.received();

// Socket contains the client IP address and port number
// The getAddress() method does not exists but depends on the socket implementation
String address = socket.getAddress();

// The getPort() method does not exists but depends on the socket implementation
int port = socket.getPort();

// Creating endPoint specific for ethernet communication
IEthernetEndPoint endPoint = new EthernetEndPoint(address, port);

IConnection connection = Communication.createConnection(serverConfig, endPoint, new MyCustomConnectionImplementation(serverConfig));
```


### 3.1) TCP/IP protocol

Two methods exists to create a TCP client, depending if you want to use default parameter values for the client configuration or not.

```java
// Using default parameter values
IClient<IEthernetEndPoint> client = Communication.createDefaultTcpClient("TCP client", "127.0.0.1", 12345);

// Specifying client configuration
IEthernetEndPoint endPoint = new EthernetEndPoint("127.0.0.1", 12345");
ClientConfig<IEthernetEndPoint> clientConfig = Communication.createClientConfig("TCP client", endPoint);

// Change configuration parameter values here

client = Communication.createTcpClient(clientConfig);
```

Two methods exist to create a TCP server, depending if you want to use default parameter values for the server configuration or not.

```java
// Using default parameter values
IServer<IEthernetEndPoint> server = Communication.createDefaultTcpServer("TCP server", 12345);

// Specifying server configuration
IEthernetEndPoint endPoint = new EthernetEndPoint(12345);
ServerConfig<IEthernetEndPoint> serverConfig = Communication.createServerConfig("TCP server", endPoint);

// Change configuration parameter values here

server = Communication.createTcpServer(serverConfig);
```

### 3.2) UDP protocol

Two methods exists to create a UDP client, depending if you want to use default parameter values for the client configuration or not.

```java
// Using default parameter values
IClient<IEthernetEndPoint> client = Communication.createDefaultUdpClient("UDP client", "127.0.0.1", 12345);

// Specifying client configuration
IEthernetEndPoint endPoint = new EthernetEndPoint("127.0.0.1", 12345");
ClientConfig<IEthernetEndPoint> clientConfig = Communication.createClientConfig("UDP client", endPoint);

// Change configuration parameter values here

client = Communication.createUdpClient(clientConfig);
```

Two methods exist to create a TCP server, depending if you want to use default parameter values for the server configuration or not.

```java
// Using default parameter values
IServer<IEthernetEndPoint> server = Communication.createDefaultUdpServer("UDP server", 12345);

// Specifying server configuration
IEthernetEndPoint endPoint = new EthernetEndPoint(12345);
ServerConfig<IEthernetEndPoint> serverConfig = Communication.createServerConfig("UDP server", endPoint);

// Change configuration parameter values here

server = Communication.createUdpServer(serverConfig);
```

### 3.3) Data exchange

Once the client and the server is connected with each other, data can be sent. The connection interface proposes two function to send data: <code>send</code> and <code>answer</code>. Both functions expects a <code>Message</code> that gather message properties to be sent to the remote. The message contains the payload to send to the remote, but it contains also a callback if a response is expected. Moreover, it is possible to send data synchronously to the remote.

```java
IServer<IEthernetEndPoint> server = Communication.createDefaultTcpServer("TCP Server", 12345);
server.open();

IClient<IEthernetEndPoint> client = Communication.createDefaultTcpClient("TCP Client", "127.0.0.1", 12345);
client.connect();

try {
	// Waiting to let the connection happen
	Thread.sleep(250);
} catch (Exception e) {
	// Do nothing
}

// Sending a simple message to the server
client.getConnection().send(new Message("Hello world".getBytes()));

try {
	// Waiting before sending another message
	Thread.sleep(250);
} catch (Exception e) {
	// Do nothing
}

// Sending a message that expects a response
client.getConnection().send(new Message("Hello world".getBytes(), args -> {
	if (!args.isTimeout()) {
		System.out.println(String.format("Client received: %s", new String(args.getResponse().getBytes())));
	}
	else {
		// Expected as the server has not been configured to send a response
		System.err.println("No response from server");
	}
});
```

For more details, please have a look in the example folder.