package com.example;

import org.junit.jupiter.api.Test;
import org.w3c.dom.events.Event;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.swing.JFrame;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.concurrent.TimeUnit;

import com.example.Client.Client;
import com.example.com.anish.calabash.Bullet;
import com.example.com.anish.calabash.Calabash;
import com.example.com.anish.calabash.Floor;
import com.example.com.anish.calabash.Target;
import com.example.com.anish.calabash.Track;
import com.example.com.anish.calabash.Wall;
import com.example.com.anish.calabash.World;


/**
 * Unit test for simple App.
 */
 class AppTest {
    /**
     * Rigorous Test.
     */
    @Test
     void testApp() throws Exception {
        assertEquals(120, 120);
        Main app = new Main();
        app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        app.setVisible(true);
        Client client = new Client();
        client.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.setVisible(true);
        client.keyPressed(new KeyEvent(client, 401, 163956, 0, 82, 'r'));
        TimeUnit.MILLISECONDS.sleep(100);
        client.keyPressed(new KeyEvent(client, 401, 163956, 0, 87, 'w'));
        TimeUnit.MILLISECONDS.sleep(100);
        client.keyPressed(new KeyEvent(client, 401, 163956, 0, 83, 'a'));
        TimeUnit.MILLISECONDS.sleep(100);
        client.keyPressed(new KeyEvent(client, 401, 163956, 0, 65, 's'));
        TimeUnit.MILLISECONDS.sleep(100);
        client.keyPressed(new KeyEvent(client, 401, 163956, 0, 68, 'd'));
        TimeUnit.MILLISECONDS.sleep(100);
        client.keyPressed(new KeyEvent(client, 401, 163956, 0, 82, 'r'));
        TimeUnit.MILLISECONDS.sleep(100);
        client.keyPressed(new KeyEvent(client, 401, 163956, 0, 38, 'w'));
        TimeUnit.MILLISECONDS.sleep(100);
        client.keyPressed(new KeyEvent(client, 401, 163956, 0, 40, 'a'));
        TimeUnit.MILLISECONDS.sleep(100);
        client.keyPressed(new KeyEvent(client, 401, 163956, 0, 37, 's'));
        TimeUnit.MILLISECONDS.sleep(100);
        client.keyPressed(new KeyEvent(client, 401, 163956, 0, 39, 'd'));
        TimeUnit.MILLISECONDS.sleep(100);
        client.keyPressed(new KeyEvent(client, 401, 163956, 0, 49, '1'));
        TimeUnit.MILLISECONDS.sleep(100);
        client.keyPressed(new KeyEvent(client, 401, 163956, 0, 50, '2'));

    }

    @Test
     void testThing() throws Exception {
        assertEquals(120, 120);
        World world=new World();
        Floor floor=new Floor(world);
        assertEquals(floor.getGlyph(), (char) 0);
        Wall wall=new Wall(world);
        assertEquals(wall.getGlyph(), (char) 1);
        Target target=new Target(world);
        assertEquals(target.getGlyph(), (char) 33);
        Track track=new Track(world);
        assertEquals(track.getGlyph(), (char) 9);
    }

    @Test
     void testPlayer() throws Exception {
        World world=new World();
        Calabash player=new Calabash(new Color(0, 255, 0), 0,world, 500, 50);
        assertEquals(player.getRank(),0);
        assertEquals(player.get_direction(),"");
        world.put(player, 1, 1);
        player.tryMove(1, 2);
        player.try_attack();
        Bullet bullet=new Bullet(world,10,"left",player);
        world.put(bullet, 2, 2);
        bullet.func();
        assertEquals(bullet.get_attack(),10);
        assertEquals(bullet.getOwnerClass(),player.getClass());
        assertEquals(bullet.get_speed(),10);
        bullet.destroy();
     }
}
