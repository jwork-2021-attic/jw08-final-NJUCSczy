package com.example.Client;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.awt.Color;
import javax.swing.JFrame;

import com.example.com.anish.calabash.World;
import com.example.com.anish.screen.ClientScreen;
import com.example.com.anish.screen.Screen;
import com.example.com.anish.screen.WorldScreen;

import com.example.asciiPanel.AsciiFont;
import com.example.asciiPanel.AsciiPanel;

public class Client extends JFrame implements KeyListener {

    private AsciiPanel terminal;
    private Screen screen;
    private Lock lock = new ReentrantLock();
    int clientID=-1;
    SocketChannel serverClient;

    public class repaintTimer implements Runnable {
        public void run() {
            try {

                while (true) {
                    lock.lock();
                    ByteBuffer buffer = ByteBuffer.allocate(4);
                    buffer.putInt(1);// 1代表查询地图
                    buffer.flip();
                    serverClient.write(buffer);
                    ByteBuffer res = ByteBuffer.allocate(World.WIDTH * World.HEIGHT * 16+4);
                    int len = serverClient.read(res);
                    // System.out.println(len);
                    res.flip();
                    if(res.getInt()==1){
                    ClientScreen clientScreen = (ClientScreen) screen;
                    for (int i = 0; i * 16 < len && i < World.WIDTH * World.HEIGHT; i++) {
                        clientScreen.worldGlyph[i / World.WIDTH][i % World.HEIGHT] = (char) res.getInt();
                        clientScreen.worldColor[i / World.WIDTH][i % World.HEIGHT] = new Color(res.getInt(),
                                res.getInt(), res.getInt());
                    }
                }
                    lock.unlock();
                    repaint();
                    TimeUnit.MILLISECONDS.sleep(20);
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public Client() {
        super();
        terminal = new AsciiPanel(World.WIDTH, World.HEIGHT, AsciiFont.TEST_1);
        add(terminal);
        pack();
        screen = new ClientScreen();
        addKeyListener(this);
        repaint();
        ExecutorService exec = Executors.newCachedThreadPool();
        String hostName = "LAPTOP-7NR12N5J";
        int portNumber = 1920;
        SocketAddress address = new InetSocketAddress(hostName, portNumber);
        try {
            serverClient = SocketChannel.open(address);
            ByteBuffer buffer=ByteBuffer.allocate(4);
            buffer.putInt(3);//3代表新建角色
            buffer.flip();
            serverClient.write(buffer);
            ByteBuffer res=ByteBuffer.allocate(4);
            while(serverClient.read(res)==0);
            res.flip();
            clientID=res.getInt();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        exec.execute(new repaintTimer());
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
        if(this.clientID==-1){
            return;
        }
        int actionNum = e.getKeyCode();
        lock.lock();
        ByteBuffer buffer = ByteBuffer.allocate(12);
        buffer.putInt(2);// 2代表进行操作
        buffer.putInt(clientID);
        buffer.putInt(actionNum);
        buffer.flip();
        try {
			serverClient.write(buffer);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        lock.unlock();
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    public static void main(String[] args) {
        Client client = new Client();
        client.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.setVisible(true);
    }
}
