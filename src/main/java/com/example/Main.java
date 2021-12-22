package com.example;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.IntBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;

import com.example.com.anish.calabash.World;
import com.example.com.anish.screen.Screen;
import com.example.com.anish.screen.WorldScreen;

import com.example.asciiPanel.AsciiFont;
import com.example.asciiPanel.AsciiPanel;

public class Main extends JFrame implements KeyListener {

    private AsciiPanel terminal;
    private Screen screen;

    public class repaintTimer implements Runnable {
        public void run() {
            while (true) {
                try {
                    repaint();
                    TimeUnit.MILLISECONDS.sleep(20);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    public class playerServer implements Runnable {
        public void run() {
            WorldScreen worldScreen=(WorldScreen)screen;
            try {
                String hostName = "LAPTOP-7NR12N5J";
                int portNumber = 1920;
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
                            int BUFFER_SIZE = 10000;
                            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
                            try {
                                client.read(buffer);
                                buffer.flip();
                                int _action = buffer.getInt();
                                // Charset charset = Charset.forName("utf-8");
                                // CharsetDecoder decoder = charset.newDecoder();
                                // CharBuffer charBuffer = decoder.decode(buffer);
                                // String s = charBuffer.toString();
                                // System.out.println("client: " + s);
                                if (_action == 1) {
                                    ByteBuffer res = ByteBuffer.allocate(World.WIDTH * World.HEIGHT * 16 + 4);
                                    if (!worldScreen.clearing) {
                                        res.putInt(1);
                                        for (int i = 0; i < World.WIDTH; i++) {
                                            for (int j = 0; j < World.HEIGHT; j++) {
                                                res.putInt(worldScreen.world.get(i, j).getGlyph());
                                                res.putInt(worldScreen.world.get(i, j).getColor().getRed());
                                                res.putInt(worldScreen.world.get(i, j).getColor().getGreen());
                                                res.putInt(worldScreen.world.get(i, j).getColor().getBlue());
                                            }
                                        }
                                    } else {
                                        res.putInt(-1);
                                    }
                                    res.flip();
                                    client.write(res);
                                } else if(_action==2) {
                                    int ClientID=buffer.getInt();
                                    int actionKeyCode=buffer.getInt();
                                    System.out.println(actionKeyCode);
                                    worldScreen.keycode_action(ClientID,actionKeyCode);
                                }else if(_action==3){
                                    ByteBuffer res=ByteBuffer.allocate(4);
                                    res.putInt(worldScreen.addPlayer());
                                    res.flip();
                                    client.write(res);
                                }

                            } catch (Exception e) {
                                // client is no longer active
                                e.printStackTrace();
                                continue;
                            }
                        }
                        if (key.isWritable()) {
                            SocketChannel client = (SocketChannel) key.channel();
                            // write data to client...
                        }
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Main() {
        super();
        terminal = new AsciiPanel(World.WIDTH, World.HEIGHT, AsciiFont.TEST_1);
        add(terminal);
        pack();
        screen = new WorldScreen();
        addKeyListener(this);
        repaint();
        ExecutorService exec = Executors.newCachedThreadPool();
        exec.execute(new repaintTimer());
        exec.execute(new playerServer());
    }

    @Override
    public void repaint() {
        terminal.clear();
        screen.displayOutput(terminal);
        super.repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        screen = screen.respondToUserInput(e);
        repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    public static void main(String[] args) {
        Main app = new Main();
        app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        app.setVisible(true);
    }

}
