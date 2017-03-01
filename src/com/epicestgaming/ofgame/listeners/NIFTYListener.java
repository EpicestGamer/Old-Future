package com.epicestgaming.ofgame.listeners;

import com.epicestgaming.ofgame.Game;
import com.epicestgaming.ofgame.Resolution;
import com.jme3.app.*;
import com.jme3.app.state.*;
import de.lessvoid.nifty.*;
import de.lessvoid.nifty.screen.*;

public class NIFTYListener implements ScreenController {

    public static String lastPage;
    public static final float nat_vol = 7.5f;
    public static final float oce_vol = 0.05f;
    public static final float mus_vol = 0.2f;
    public static int resnum = 17;

    public static void quitGame() {
        Game.quit();
    }

    public static void setScreen(String nextScreen) {
        lastPage = Game.nifty.getCurrentScreen().getScreenId();
        if (nextScreen.equals("hud")) {
            Game.run();
            Game.cameraEnable();
            Game.audio_nature.setVolume(nat_vol);
            Game.audio_ocean.setVolume(oce_vol);
        } else if (nextScreen.equals("pause")) {
            Game.audio_nature.setVolume(nat_vol / 2);
            Game.audio_ocean.setVolume(oce_vol / 2);
            Game.cameraDisable();
            Game.running = false;
        } else {
            Game.cameraDisable();
        }
        Game.nifty.gotoScreen(nextScreen);
    }

    public static int getHealth() {
        return Game.health;
    }

    public static String getHealthImg() {
        int health = getHealth();
        String himg = "0";
        if (health < 6) {
            himg = "0";
        } else if (health >= 6 && health < 16) {
            himg = "10";
        } else if (health >= 16 && health < 26) {
            himg = "20";
        } else if (health >= 26 && health < 36) {
            himg = "30";
        } else if (health >= 36 && health < 46) {
            himg = "40";
        } else if (health >= 46 && health < 56) {
            himg = "50";
        } else if (health >= 56 && health < 66) {
            himg = "60";
        } else if (health >= 66 && health < 76) {
            himg = "70";
        } else if (health >= 76 && health < 86) {
            himg = "80";
        } else if (health >= 86 && health < 96) {
            himg = "90";
        } else if (health >= 96 && health <= 100) {
            himg = "100";
        }
        return himg;
    }

    public static void setView() {
        Game.setView();
    }

    public static void setFull() {
        Game.setFullScreen();
    }

    public static void setRes() {
        if (resnum > 17){
            resnum = 0;
        }
        Game.defaults.setResolution(Resolution.res[resnum].width, Resolution.res[resnum].height);
        resnum++;
        Game.saveOptions();
        Game.reset();
    }

    public void bind(Nifty nifty, Screen screen) {
    }

    public void onStartScreen() {
    }

    public void onEndScreen() {
    }
}
