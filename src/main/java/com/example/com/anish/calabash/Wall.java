package com.example.com.anish.calabash;

import com.example.asciiPanel.AsciiPanel;

public class Wall extends Thing {

    public Wall(World world) {
        super(AsciiPanel.cyan, (char) 1, world);
    }

}
