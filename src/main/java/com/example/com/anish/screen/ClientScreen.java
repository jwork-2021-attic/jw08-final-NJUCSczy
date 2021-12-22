package com.example.com.anish.screen;

import java.awt.event.KeyEvent;
import java.awt.Color;
import com.example.com.anish.calabash.World;
import com.example.asciiPanel.AsciiPanel;


public class ClientScreen implements Screen {

    public char[][] worldGlyph=new char[World.WIDTH][World.HEIGHT];
    public Color[][] worldColor=new Color[World.WIDTH][World.HEIGHT];

    public ClientScreen() {
    }

    @Override
    public void displayOutput(AsciiPanel terminal) {
        for (int x = 0; x < World.WIDTH; x++) {
            for (int y = 0; y < World.HEIGHT; y++) {

                terminal.write(worldGlyph[x][y], x, y, worldColor[x][y]);

            }
        }
    }

    @Override
    public Screen respondToUserInput(KeyEvent key) {
        // TODO Auto-generated method stub
        return null;
    }

}
