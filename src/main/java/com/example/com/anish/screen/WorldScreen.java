package com.example.com.anish.screen;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.crypto.dsig.spec.HMACParameterSpec;

import java.awt.Color;
import com.example.com.anish.calabash.Calabash;
import com.example.com.anish.calabash.Creature;
import com.example.com.anish.calabash.Floor;
import com.example.com.anish.calabash.BFS;
import com.example.com.anish.calabash.Bullet;
import com.example.com.anish.calabash.Monster;
import com.example.com.anish.calabash.Wall;
import com.example.com.anish.calabash.World;
import com.example.com.anish.calabash.Creature.Function;
import com.example.asciiPanel.AsciiPanel;
import com.example.maze.Node;

public class WorldScreen implements Screen {

    public World world = null;
    public ExecutorService exec;
    private Lock lock = new ReentrantLock();
    Calabash[] players = new Calabash[4];
    int playerNum = 0;
    String[] unhandledMoveInput = { "", "", "", "" };
    String[] unhandledAttackInput = { "", "", "", "" };
    public boolean clearing = false;

    public WorldScreen() {
        exec = Executors.newCachedThreadPool();
        start(0);
    }

    void start(int mode) {
        if (world == null) {
            world = new World();
        } else {
            world.init_world();
            if (mode == 1) {
                load_world();
            }
        }
        clearing = false;
        if (mode == 1) {
            load_creatures();
        } else {
            int _playerNum=playerNum;
            playerNum=0;
            init_creatures(_playerNum, 7);
        }
        for (int i = 0; i < playerNum; i++) {
            players[i] = (Calabash) world.Calabashes.get(i);
        }
    }

    void restart(int mode) {
        clearing = true;
        exec.shutdown();
        lock.lock();
        while (!exec.isTerminated()) {
            lock.unlock();
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            lock.lock();
        }
        lock.unlock();
        exec = Executors.newCachedThreadPool();
        start(mode);
    }

    public void init_creatures(int numOfCalabashes, int numOfMonsters) {
        for(int i=0;i<numOfCalabashes;i++){
            addPlayer();
        }
        ArrayList<Creature> creatures = new ArrayList<>();
        ArrayList<Node> nodes = new ArrayList<>();
        for (int i = 0; i < numOfMonsters; i++) {
            Random r = new Random();
            int x = -1, y = -1;
            boolean ok = false;
            while (!ok) {
                ok = true;
                x = world.get_tile().length / 6 + r.nextInt(world.get_tile().length * 2 / 3);
                y = world.get_tile()[0].length / 6 + r.nextInt(world.get_tile()[0].length * 2 / 3);
                if (!(world.get_tile()[x][y].getThing() instanceof Floor))
                    ok = false;
                for (int j = 0; j < nodes.size(); j++) {
                    if (nodes.get(j).x == x && nodes.get(j).y == y) {
                        ok = false;
                        break;
                    }
                }
                if (world.find_way(0, 0, x, y).size() == 0)
                    ok = false;
            }
            nodes.add(new Node(x, y));

                creatures.add(new Monster(
                        new Color(255, i * 128 / numOfMonsters,
                                i* 128 / numOfMonsters),
                        i, world, 90 + r.nextInt(20), 25 + r.nextInt(10)));


        }
        for (int i = 0; i < creatures.size(); i++) {
            world.add_creature(creatures.get(i), nodes.get(i));
        }
        for (int i = 0; i < creatures.size(); i++) {
            exec.execute(new CreatureGenerator(creatures.get(i)));
        }
    }

    public int getPlayerNum() {
        return playerNum;
    }

    public int addPlayer() {
        if (playerNum >= 4) {
            return -1;
        }
        Node pos=new Node(0,0);
        switch (playerNum) {
            case 0:
                pos = new Node(0, 0);
                break;
            case 1:
                pos=new Node(World.WIDTH-1,0);
                break;
                case 2:
                pos = new Node(0, World.HEIGHT-1);
                break;
            case 3:
                pos=new Node(World.WIDTH-1,World.HEIGHT-1);
                break;
        }
        playerNum+=1;
        Calabash newPlayer=new Calabash(new Color(playerNum*32,255,playerNum*32), playerNum-1, world, 500, 50);
        world.add_creature(newPlayer, pos);
        exec.execute(new CreatureGenerator(newPlayer));
        return playerNum;
    }

    @Override
    public void displayOutput(AsciiPanel terminal) {
        if (clearing) {
            return;
        }
        for (int x = 0; x < World.WIDTH; x++) {
            for (int y = 0; y < World.HEIGHT; y++) {

                terminal.write(world.get(x, y).getGlyph(), x, y, world.get(x, y).getColor());

            }
        }
    }

    int i = 0;

    @Override
    public Screen respondToUserInput(KeyEvent key) {
        // Integer keyCode = key.getKeyCode();
        // keycode_action(keyCode);
        return this;
    }

    public void keycode_action(int playerID, int keyCode) {
        switch (keyCode) {
            case 87:
                unhandledMoveInput[playerID - 1] = "up";
                break;
            case 83:
                unhandledMoveInput[playerID - 1] = "down";
                break;
            case 65:
                unhandledMoveInput[playerID - 1] = "left";
                break;
            case 68:
                unhandledMoveInput[playerID - 1] = "right";
                break;
            case 38:
                unhandledAttackInput[playerID - 1] = "up";
                break;
            case 40:
                unhandledAttackInput[playerID - 1] = "down";
                break;
            case 37:
                unhandledAttackInput[playerID - 1] = "left";
                break;
            case 39:
                unhandledAttackInput[playerID - 1] = "right";
                break;

            case 82:
                if (!clearing) {
                    restart(0);
                }
                break;

            case 49:
                save_world();
                save_creatures();
                break;
            case 50:
                restart(1);
                break;
        }
    }

    void save_world() {
        try {
            FileOutputStream tar = new FileOutputStream("save/world_save.txt");
            BufferedOutputStream stream = new BufferedOutputStream(tar);
            for (int i = 0; i < world.get_tile().length; i++) {
                for (int j = 0; j < world.get_tile()[0].length; j++) {
                    if (world.get_tile()[i][j].getThing() instanceof Wall) {
                        stream.write(1);
                    } else {
                        stream.write(0);
                    }
                }
                stream.write('\n');
            }
            stream.flush();
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void load_world() {
        try {
            FileInputStream tar = new FileInputStream("save/world_save.txt");
            BufferedInputStream stream = new BufferedInputStream(tar);
            for (int i = 0; i < world.get_tile().length; i++) {
                for (int j = 0; j < world.get_tile()[0].length; j++) {
                    int info = stream.read();
                    if (info == 0) {
                        world.put(new Floor(world), i, j);
                    } else {
                        world.put(new Wall(world), i, j);
                    }
                }
                stream.read();
            }
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void save_creatures() {
        try {
            FileOutputStream tar = new FileOutputStream("save/creature_save.txt");
            BufferedOutputStream stream = new BufferedOutputStream(tar);
            for (int i = 0; i < world.get_calabashes().size(); i++) {
                Calabash calabash = (Calabash) world.get_calabashes().get(i);
                if (!calabash.is_alive()) {
                    continue;
                }
                stream.write(1); // 1代表葫芦娃
                stream.write(calabash.getColor().getRed());
                stream.write(calabash.getColor().getGreen());
                stream.write(calabash.getColor().getBlue());
                stream.write(calabash.getRank());
                stream.write(calabash.getHealth());
                stream.write(calabash.getAttack());
                stream.write(calabash.getX());
                stream.write(calabash.getY());
                stream.write('\n');
            }
            for (int i = 0; i < world.get_monsters().size(); i++) {
                Monster monster = (Monster) world.get_monsters().get(i);
                if (!monster.is_alive()) {
                    continue;
                }
                stream.write(2); // 2代表妖怪
                stream.write(monster.getColor().getRed());
                stream.write(monster.getColor().getGreen());
                stream.write(monster.getColor().getBlue());
                stream.write(monster.getRank());
                stream.write(monster.getHealth());
                stream.write(monster.getAttack());
                stream.write(monster.getX());
                stream.write(monster.getY());
                stream.write('\n');
            }
            stream.flush();
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void load_creatures() {
        try {
            FileInputStream tar = new FileInputStream("save/creature_save.txt");
            BufferedInputStream stream = new BufferedInputStream(tar);
            ArrayList<Creature> creatures = new ArrayList<>();
            int type = stream.read();
            while (type != -1) {
                if (type == 1) {
                    Calabash calabash = new Calabash(new Color(stream.read(), stream.read(), stream.read()),
                            stream.read(), world, stream.read(), stream.read());
                    creatures.add(calabash);
                    world.add_creature(calabash, new Node(stream.read(), stream.read()));
                } else if (type == 2) {
                    Monster monster = new Monster(new Color(stream.read(), stream.read(), stream.read()), stream.read(),
                            world, stream.read(), stream.read());
                    creatures.add(monster);
                    world.add_creature(monster, new Node(stream.read(), stream.read()));
                }
                stream.read();
                type = stream.read();
            }
            stream.close();
            for (int i = 0; i < creatures.size(); i++) {
                exec.execute(new CreatureGenerator(creatures.get(i)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class CreatureGenerator implements Runnable {
        private Creature creature;

        CreatureGenerator(Creature _creature) {
            creature = _creature;
        }

        public void run() {
            if (creature instanceof Monster) {
                while (creature.is_alive() && !clearing) {
                    try {
                        lock.lock();
                        Function func = creature.func();
                        if (func.attackTarget != null) {
                            func.attackTarget.get_hurt(func.attack);
                        } else if (func.move.x != -1 && func.move.y != -1) {
                            creature.tryMove(func.move.x, func.move.y);
                        }
                    } finally {
                        lock.unlock();
                        try {
                            Random r = new Random();
                            TimeUnit.MILLISECONDS.sleep(290 + r.nextInt(10));
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
                try {
                    lock.lock();
                    world.get_tile()[creature.getX()][creature.getY()].setThing(new Floor(world));
                } finally {
                    lock.unlock();
                }
            } else if (creature instanceof Calabash) {
                Calabash player = (Calabash) creature;
                int playerID = player.getRank() + 1;
                while (creature.is_alive() && !clearing) {
                    try {
                        lock.lock();
                        switch (unhandledMoveInput[playerID - 1]) {
                            case "left":
                                player.tryMove(player.getX() - 1, player.getY());
                                break;
                            case "right":
                                player.tryMove(player.getX() + 1, player.getY());
                                break;
                            case "up":
                                player.tryMove(player.getX(), player.getY() - 1);
                                break;
                            case "down":
                                player.tryMove(player.getX(), player.getY() + 1);
                                break;
                        }
                        player.set_attack_direction(unhandledAttackInput[playerID - 1]);

                        if (unhandledAttackInput[playerID - 1] != "") {
                            Function func = player.try_attack();
                            if (func.attackTarget != null) {
                                func.attackTarget.get_hurt(func.attack);
                            } else if (func.move.x != -1 && func.move.y != -1) {
                                Bullet bullet = new Bullet(world, func.attack, player.get_direction(), player);
                                world.get_tile()[func.move.x][func.move.y].setThing(bullet);
                                exec.execute(new BulletGenerator(bullet));
                            }
                        }
                        unhandledMoveInput[playerID - 1] = "";
                        unhandledAttackInput[playerID - 1] = "";
                    } finally {
                        lock.unlock();
                        try {
                            TimeUnit.MILLISECONDS.sleep(10);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
                try {
                    lock.lock();
                    world.get_tile()[creature.getX()][creature.getY()].setThing(new Floor(world));
                } finally {
                    lock.unlock();
                }
            }
        }
    }

    public class BulletGenerator implements Runnable {
        private Bullet bullet;

        BulletGenerator(Bullet _bullet) {
            bullet = _bullet;
        }

        public void run() {
            while (bullet.is_alive() && !clearing) {
                try {
                    lock.lock();
                    bullet.func();
                } finally {
                    lock.unlock();
                    try {
                        TimeUnit.MILLISECONDS.sleep(1000 / bullet.get_speed());
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            try {
                lock.lock();
                world.get_tile()[bullet.getX()][bullet.getY()].setThing(new Floor(world));
            } finally {
                lock.unlock();
            }
        }
    }

}
