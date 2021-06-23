# 1) Presentation

When developers work on a client/server application, they always need to send and receive data from the remote. One simple way to do so, is to use directly sockets provided by java.net package. However, if the request needs to be sent sent from another thread in order not to freeze the client graphical user interface, then the developer needs to develop its own asynchronous API.
This project has been created in order to send bytes to a remote and to receive bytes from the remote in an asynchronous way.

This API support TCP/IP and UDP protocol.

# 2) Download

First you need to download this project on your computer. To do so, you can use the following command line :

```git
git clone https://github.com/Pierre-Emmanuel41/communication.git --recursive
```

and then double click on the deploy.bat file. This will deploy this project and all its dependencies on your computer. Which means it generates the folder associated to this project and its dependencies in your .m2 folder. Once this has been done, you can add the project as maven dependency on your maven project :

```xml
<dependency>
	<groupId>fr.pederobien</groupId>
	<artifactId>communication</artifactId>
	<version>1.0-SNAPSHOT</version>
</dependency>
```

# 3) Tutorial

### 3.1) TCP/IP protocol

To use the TCP/IP protocol, the developer will need four classes : <code>RequestCallbackMessage</code>, <code>IAnswerExtractor</code>, <code>TcpClientConnection</code> and <code>TcpServerConnection</code>.

#### 3.1.1) RequestCallbackMessage

When the developer wants to send a request to the remote in an asynchronous way, he needs to specify a callback. The callback will be executed when an answer is received from the remote.  
To create such a message :

```java
// The bytes array to send to the remote.
byte[] bytes = new byte[0];

// The request identifier. Useful for the pending request management.
int uniqueIdentifier = 1;

// Code to run when a response is received from the remote.
Consumer<ResponseCallbackArgs> callback = args -> {
	System.out.println("Timeout ? " + args.isTimeout());
	System.out.println("Response identifier : " + args.getResponse().getRequestIdentifier());
}

// The time in ms within the remote has to answer.
int timeout = 1000;
RequestCallbackMessage callback = new RequestCallbackMessage(bytes, uniqueIdentifier, callback, timeout);
```

#### 3.1.2) IAnswerExtractor

This extractor contains only one method : <code>Map<Integer, byte[]> extract(byte[] received);</code>. Indeed, this extractor is completely dependent on how your TCP messages are constructed. When the socket receives data from the remote, it does not know if it contains 1, 2 or more answers. Worse still, it does not know if an answer is complete or not. That why the developer needs an extractor. Only this object knows how messages are constructed, how a message starts, how a message ends, how many bytes from the start of the message the message length is and so on.. It returns a map that contains the association of the identifier (coming from the request) and the bytes array received from the remote.

#### 3.1.3) TcpClientConnection

This object represent the connection with the remote but from the client side.  
To create such a connection :

```java
// The IP address of the remote.
String remoteAddress = "127.0.0.1";

// The port number of the remote.
int remotePort = 42000;

// The extractor
IAnswersExtractor answersExtractor = /*your class here*/;

// True in order to enable the connection to send data, false otherwise.
boolean isEnabled = true;
ITcpConnection connection = new TcpClientConnection(remoteAddress, remotePort, answersExtractor, isEnabled);
```

And finally, to send a request to the remote :

```java
connection.send(callback);
```

It is possible for the server to send request to the client without responding to a previous request. In order to handle those request, the developer should observe the connection. There is an implemented observer pattern with the interface <code>IObsTcpConnection</code> and methods <code>addObserver(IObsTcpConnection obs)</code> and <code>removeObserver(IObsTcpConnection obs)</code>.

#### 3.1.4) TcpServerConnection

This object represent the connection with the remote but from the server side. It only needs a socket (that socket comes from the <code>ServerSocket.accept()</code>) and the answersExtractor used on the client side :

```java
// The port number of the server
int port = 42000;

// The IP address of the server
INetAddress address = INetAddress.getByName("127.0.0.1");

ServerSocket serverSocket = new ServerSocket(port, 20, address);

// Thread paused until a client connection occurs.
Socket clientSocket = serverSocket.accept();

// The extractor
IAnswersExtractor answersExtractor = /*your class here*/;

ITcpConnection clientConnection = new TcpServerConnection(clientSocket, answersExtractor);
```

Just like the TcpClientConnection, it possible to observer the connection in order to handle data received from the client in order to interpret and answer.

### 3.2) UDP protocol

To use the UDP protocol, the developer will need five classes : <code>RequestMessage</code>, <code>IAnswerExtractor</code>, <code>UdpClientConnection</code> <code>UdpServerConnection</code> and <code>AddressMessage</code>.

#### 3.2.1) RequestMessage

The request message class is the equivalent of <code>RequestCallbackMessage</code> but it is slightly different. Indeed, with UDP protocol the developer does not know if the data he sent has arrived at the destination. That's why there is neither callback mechanism nor timeout. A request message is only composed of a identifier and a bytes array to send.  
To create such a message :

```java
// The bytes array to send to the remote.
byte[] bytes = new byte[0];

// The request identifier. Useful for the pending request management.
int uniqueIdentifier = 1;

RequestMessage message = new RequestMessage(bytes, uniqueIdentifier);
```

#### 3.2.2) IAnswerExtractor

This extractor contains only one method : <code>Map<Integer, byte[]> extract(byte[] received);</code>. Indeed, this extractor is completely dependent on how your UDP messages are constructed. When the socket receives data from the remote, it does not know if it contains 1, 2 or more answers. Worse still, it does not know if an answer is complete or not. That why the developer needs an extractor. Only this object knows how messages are constructed, how a message starts, how a message ends, how many bytes from the start of the message the message length is and so on.. It returns a map that contains the association of the identifier (coming from the request) and the bytes array received from the remote.

#### 3.2.3) UdpClientConnection

The object represent the connection with the remote but from the client side.  
To create such a connection:

```java
// The remote IP address
String remoteAddress = "127.0.0.1";

// The remote port number
int remotePort = 42000;

// The extractor
IAnswersExtractor answersExtractor = /*your class here*/;

// True in order to enable the connection to send data, false otherwise.
boolean isEnabled = true;

// The size of the buffer that receive data from the remote
int receptionBufferSize = 2048;
UdpClientConnection connection = new UdpClientConnection(remoteAddress, remotePort, answersExtractor, isEnabled, receptionBufferSize);
```

And finally, to send a request to the remote :

```java
connection.send(message);
```

It is possible for the server to send request to the client without responding to a previous request. In order to handle those request, the developer should observe the connection. There is an implemented observer pattern with the interface <code>IObsConnection</code> and methods <code>addObserver(IObsConnection obs)</code> and <code>removeObserver(IObsConnection obs)</code>.

#### 3.2.4) UdpServerConnection

The object represent the connection with the remote but from the server side.  
To create such a connection:

```java
// The server IP address
InetSocketAddress address = InetSocketAddress.getByName("127.0.0.1");

// The size of the buffer that receive data from the remote
int receptionBufferSize = 2048;

// The supplier that provide an extractor for each connected remote
Supplier<IAnswersExtractor> extractorSupplier = () -> /*your class here*/;

IUdpServerConnection connection = new UdpServerConnection(address, receptionBufferSize, extractorSupplier)
```

#### 3.2.5) AddressMessage

Because of the structure in java of a datagram packet, on the server side, the developer needs to provide an AddressMessage instead of a RequestMessage. This AddressMessage provides the address at which the data needs to be sent.  
To create such a message :

```java
// The bytes array to send to the remote
byte[] bytes = new byte[0];

// The request identifier
int uniqueIdentifier = 1;

// The address to sent the data
InetSocketAddress address = new InetSocketAddress("127.0.0.1", 42000);

IAddressMessage message = new AddressMessage(bytes, uniqueIdentifier, address);
```

Finally, to send a message to the remote from the server side :

```java
connection.send(message);
```

Just like the UdpClientConnection, it possible to observer the connection in order to handle data received from the client in order to interpret and answer.