# 1) Presentation

When developers work on a client/server application, they always need to send and receive data from the remote. One simple way to do so is to use directly sockets provided by java.net package. However the Java API does not provide pending queues to send/receive data from the remote. This project has been created in order to send bytes to a remote and to receive bytes from the remote in an asynchronous way.

This API support TCP/IP and UDP protocol.

# 2) Download and compilation

First you need to download this project on your computer. To do so, you can use the following command line:

```git
git clone https://github.com/Pierre-Emmanuel41/communication.git
```

Then go inside the folder of the project and run the following maven command:

```maven
mvn clean package install
```

Finally, you can add the project as maven dependency to your maven project :

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

A <code>ClientConfig</code> is mandatory to create a Client:

```java
String clientName = "Demo Client";
String address = "127.0.0.1";
int port = 12345;

// Creating a client configuration with default parameter values
ClientConfig clientConfig = Communication.createClientConfig(clientName, address, port);
```

A <code>ServerConfig</code> is mandatory to create a Server:

```java
String serverName = "Demo server"
int port = 12345;

// Creating a server configuration with default parameter values
ServerConfig serverConfig = Communication.createServerConfig(serverName, port);
```

A <code>ConnectionConfig</code> is mandatory to create a Connection:

```java
String address = "127.0.0.1";
int port = 12345;

IConfiguration configuration = null;

// Compilation error as configuration is null
ConnectionConfig connectionConfig = Communication.createConnection(address, port, configuration);
```

A part of the connection configuration is directly defined in the client configuration or in the server configuration. That is why, depending on the direction of the communication a connection can be defined<br>

From a client configuration:

```java
// Creating a client configuration used to create a connection configuration
ClientConfig clientConfig = Communication.createClientConfig("Demo client", "127.0.0.1", 12345);

String address = clientConfig.getAddress();
int port = clientConfig.getPort();
ConnectionConfig connectionConfig = Communication.createConnectionConfig(address, port, clientConfig);
```

The server configuration does not embed a specific IP address as well as a specific port, but those values are coming from the socket connected with the client:

```java
ServerConfig serverConfig = Communication.createServerConfig("Demo server", 12345);

// Waiting for a new client
Socket socket = serverSocket.received();

// Socket contains the client IP address and port number
// The getAddress() method does not exists but depends on the socket implementation
String address = socket.getAddress();

// The getPort() method does not exists but depends on the socket implementation
int port = socket.getPort();

ConnectionConfig connectionConfig = Communication.createConnectionConfig(address, port, serverConfig);
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
// The configuration value should be replaced with the client or server configuration
IConfiguration configuration = null;

// Address defined by the client configuration or the socket connected to the client
String address;

// Port defined by the client configuration or the socket connected to the client
int port;
ConnectionConfig connectionConfig = Communication.createConnectionConfig(address, port, configuration);
IConnectionImpl myCustomConnectionImpl = new MyCustomConnectionImplementation();

IConnection connection = Communication.createCustomConnection(connectionConfig, myCustomConnectionImpl);
```

### 3.1) TCP/IP protocol

Two methods exists to create a TCP client, depending if you want to use default parameter values for the client configuration or not.

```java
// Using default parameter values
IClient client = Communication.createDefaultTcpClient("TCP client", "127.0.0.1", 12345);

// Specifying client configuration
ClientConfig clientConfig = Communication.createClientConfig("TCP client", "127.0.0.1", 12345");

// Change configuration parameter values here

client = Communication.createTcpClient(clientConfig);
```

Two methods exist to create a TCP server, depending if you want to use default parameter values for the server configuration or not.

```java
// Using default parameter values
IServer server = Communication.createDefaultTcpClient("TCP server", 12345);

// Specifying server configuration
ServerConfig serverConfig = Communication.createServerConfig("TCP server", 12345);

// Change configuration parameter values here

server = Communication.createTcpServer(serverConfig);
```

To create a TCP connection:

```java
// The configuration value should be replaced with the client or server configuration
IConfiguration configuration = null;

// Address defined by the client configuration or the socket connected to the client
String address;

// Port defined by the client configuration or the socket connected to the client
int port;
ConnectionConfig connectionConfig = Communication.createConnectionConfig("127.0.0.1", 12345, configuration);
IConnectionImpl myCustomConnectionImpl = new MyCustomConnectionImplementation();

// The socket connected to the remote.
socket;

IConnection connection = Communication.createCustomConnection(connectionConfig, new ConnectionImpl(socket));
```

To have a better understanding on how the TCP connection is created, please have a look at <code>TcpClientImpl</code> and <code>TcpServerImpl</code>.