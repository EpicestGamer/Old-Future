package com.epicestgaming.ofgame;

import java.awt.Dimension;

public class Resolution {

    public static final Dimension[] res = {
        new Dimension(640, 480),//0
        new Dimension(720, 480),//1
        new Dimension(800, 600),//2
        new Dimension(1024, 768),//3
        new Dimension(1176, 664),//4
        new Dimension(1280, 720),//5
        new Dimension(1280, 768),//6
        new Dimension(1280, 800),//7
        new Dimension(1280, 960),//8
        new Dimension(1280, 1024),//9
        new Dimension(1360, 768),//10
        new Dimension(1366, 768),//11
        new Dimension(1440, 900),//12
        new Dimension(1600, 900),//13
        new Dimension(1600, 1024),//14
        new Dimension(1600, 1200),//15
        new Dimension(1680, 1050),//16
        new Dimension(1920, 1080),//17
    };

    public static int getResolutionNumber() {
        return res.length;
    }
}