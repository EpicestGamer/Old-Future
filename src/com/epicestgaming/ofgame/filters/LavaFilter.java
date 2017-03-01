package com.epicestgaming.ofgame.filters;

import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.texture.Texture2D;
import com.jme3.water.WaterFilter;

/**
 *
 * @author Michael
 */
@Deprecated
public class LavaFilter extends WaterFilter {

    public static final ColorRGBA lava = new ColorRGBA(207, 16, 32, 1);

    public LavaFilter(Node render, Vector3f lightDir) {
        super(render, lightDir);
        setWaterColor(lava);
        setDeepWaterColor(lava);
        setLightColor(lava);
        setUseCaustics(false);
        setUseFoam(false);
        setUseRefraction(false);
        setSpeed(0.05f);
    }
}