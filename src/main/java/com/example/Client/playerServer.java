package com.example.Client;

import java.net.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;
import java.io.*;

public class playerServer {
    public static void main(String[] args) throws IOException {

        if (args.length != 2) {
            System.err.println("Usage: java EchoServer <host name> <port number>");
            System.exit(1);
        }

        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);
        Selector selector = Selector.open();
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        InetSocketAddress hostAddress = new InetSocketAddress(hostName, portNumber);
        serverChannel.bind(hostAddress);
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        while (true) {
            int readyCount = selector.select();
            if (readyCount == 0) {
                continue;
            }
            // process selected keys...
            Set<SelectionKey> readyKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = readyKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                // Remove key from set so we don't process it twice
                iterator.remove();
                // operate on the channel...
                // client requires a connection
                if (key.isAcceptable()) {
                    ServerSocketChannel server = (ServerSocketChannel) key.channel();
                    // get client socket channel
                    SocketChannel client = server.accept();
                    // Non Blocking I/O
                    client.configureBlocking(false);
                    // record it for read/write operations (Here we have used it for read)
                    client.register(selector, SelectionKey.OP_READ);
                    continue;
                }
                // if readable then the server is ready to read
                if (key.isReadable()) {

                    SocketChannel client = (SocketChannel) key.channel();

                    // Read byte coming from the client
                    int BUFFER_SIZE = 1024;
                    ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
                    try {
                        client.read(buffer);
                        buffer.flip();
                        Charset charset = Charset.forName("utf-8");
                        CharsetDecoder decoder = charset.newDecoder();
                        CharBuffer charBuffer = decoder.decode(buffer);
                        String s = charBuffer.toString();
                        System.out.println("client: " + s);
                    } catch (Exception e) {
                        // client is no longer active
                        //e.printStackTrace();
                        continue;
                    }
                }
                if (key.isWritable()) {
                    SocketChannel client = (SocketChannel) key.channel();
                    // write data to client...
                  }
            }

        }
    }
}