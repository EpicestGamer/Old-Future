/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.epicestgaming.ofgame;

import com.jme3.math.Vector3f;

/**
 *
 * @author Michael
 */
public class TreeCoords {

    public static final float y = 60;
    public static final Vector3f[] trees = {
        new Vector3f(-9.415264f * 5, y, -9.559183f * 5),
        new Vector3f(-26.497387f * 5, y, -5.371163f * 5),
        new Vector3f(15.604599f * 5, y, 16.393879f * 5),
        new Vector3f(-10.843588f * 5, y, 15.360285f * 5),
        new Vector3f(19.098372f * 5, y, -13.552879f * 5),
        new Vector3f(7.750743f * 5, y, -0.99457264f * 5),
        new Vector3f(-30.711643f * 5, y, 3.8689942f * 5),
        new Vector3f(33.923683f * 5, y, -0.4779374f * 5),
        new Vector3f(3.6412883f * 5, y, 28.327108f * 5)};

    public static Vector3f makeRandomSpawn() {
        double randoNumby = Math.random() * trees.length;
        double ran = Math.random() * 20;
        int tree = (int) randoNumby;
        Vector3f returned = trees[tree];
        int pom = (int) (Math.random() * 4);
        if (pom == 1) {
            returned.x += (float) ran;
            returned.z += (float) ran;
        } else if (pom == 2) {
            returned.x += (float) ran;
            returned.z -= (float) ran;
        } else if (pom == 3) {
            returned.x -= (float) ran;
            returned.z += (float) ran;
        } else if (pom == 4) {
            returned.x -= (float) ran;
            returned.z -= (float) ran;
        }
        return returned;
    }
}
