package com.epicestgaming.ofgame;

import com.epicestgaming.ofgame.listeners.NIFTYListener;
import com.jme3.app.*;
import com.jme3.audio.*;
import com.jme3.bullet.*;
import com.jme3.bullet.collision.shapes.*;
import com.jme3.bullet.control.*;
import com.jme3.bullet.util.*;
import com.jme3.collision.*;
import com.jme3.effect.*;
import com.jme3.effect.shapes.*;
import com.jme3.font.*;
import com.jme3.input.*;
import com.jme3.input.controls.*;
import com.jme3.light.*;
import com.jme3.material.*;
import com.jme3.math.*;
import com.jme3.niftygui.*;
import com.jme3.post.*;
import com.jme3.renderer.queue.*;
import com.jme3.scene.*;
import com.jme3.scene.shape.*;
import com.jme3.system.*;
import com.jme3.terrain.geomipmap.*;
import com.jme3.terrain.heightmap.*;
import com.jme3.texture.*;
import com.jme3.texture.Texture.*;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.water.*;
import de.lessvoid.nifty.*;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.ImageRenderer;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.render.NiftyImage;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import javax.imageio.ImageIO;

public class Game extends SimpleApplication implements ActionListener {

 //The settings
 public static AppSettings defaults = new AppSettings(false);

 static {
  Logger.getLogger("").setLevel(Level.FINE);

  defaults.setResolution(1280, 960);
  defaults.setBitsPerPixel(24);
  defaults.setFrequency(60);
  defaults.setDepthBits(24);
  defaults.setStencilBits(0);
  defaults.put("Samples", 0);
  defaults.setFullscreen(false);
  defaults.setTitle("Old Future");
  defaults.setRenderer(AppSettings.LWJGL_OPENGL2);
  defaults.setAudioRenderer(AppSettings.LWJGL_OPENAL);
  defaults.setUseJoysticks(false);
  defaults.setUseInput(true);
  defaults.setVSync(true);
  defaults.setSettingsDialogImage("Textures/Gui/Options.png");
  defaults.setMinResolution(640, 480);
  defaults.setEmulateMouse(true);
  defaults.setSamples(8);
  defaults.put("View", "1st");
  try {
   Class<Game> clazz = Game.class;

   defaults.setIcons(new BufferedImage[]{
    ImageIO.read(clazz.getResourceAsStream("/Interface/icons/icon256.png")),
    ImageIO.read(clazz.getResourceAsStream("/Interface/icons/icon128.png")),
    ImageIO.read(clazz.getResourceAsStream("/Interface/icons/icon32.png")),
    ImageIO.read(clazz.getResourceAsStream("/Interface/icons/icon16.png")),});
  } catch (IOException e) {
   Logger.getLogger("").log(java.util.logging.Level.WARNING, "Unable to load program icons!\nIs file corrupt?", e);
  }
 }
 //Critical variables
 public static boolean running = false;
 public static boolean playing = true;
 public boolean hasRunInited = false;
 public static boolean reset = false;
 //
 public static boolean camEnab = false;
 public boolean wasFullscreen;
 public static String view = "1st";
 public CameraNode camNode;
 //organise
 public Node environ = new Node();
 public Node unmovable = new Node();
 public Node movable = new Node();
 public Node characters = new Node();
 public Node PC = new Node();
 public Node NPC = new Node();
 //GUI
 public static Nifty nifty;
 //Gravity
 public float gravity = 40f;
 //Weather
 public FilterPostProcessor fpp;
 public float rainRadius = 400f;
 public float rainHeight = 50f;
 public int rainParticlesPerSec = 4096;
 public int rainMaxLife = 5;
 public int weather = 1;
 public int rainMaxPart = rainMaxLife * rainParticlesPerSec * weather;
 public static final int weatherLevel = 0;
 //Collision
 public Spatial sceneModel;
 public BulletAppState bulletAppState;
 public RigidBodyControl landscape;
 public RigidBodyControl treescape;
 public CharacterControl player;
 public Vector3f walkDirection = new Vector3f();
 public boolean left = false, right = false, up = false, down = false;
 //Map
 public TerrainQuad terrain;
 public Spatial trees;
 public Material mat_terrain;
 //Temporary vectors used on each frame. They here to avoid instanciating new vectors on each frame
 public Vector3f camDir = new Vector3f();
 public Vector3f camLeft = new Vector3f();
 //Sounds
 public AudioNode audio_gun;
 public static AudioNode audio_nature;
 public static AudioNode audio_ocean;
 public AudioNode audio_music;
 //Water
 public float initialWaterHeight = 0;
 public final int SHADOWMAP_SIZE = 1024;
 //Entities
 public static final int maxEntities = 256;
 public int curEntities = 10;
 public Node entities = new Node();
 public ArrayList<Node> entityList = new ArrayList<Node>();
 public ArrayList<RigidBodyControl> entityBodiesList = new ArrayList<RigidBodyControl>();
 public ArrayList<Integer> entityHealths = new ArrayList<Integer>();
 public ArrayList<Integer> entityTypes = new ArrayList<Integer>();
 public float timePassed = 0;
 public int round = 1;
 public int maxRound = 10;
 public static final float roundTime = 90;
 public SphereCollisionShape npcShape;
 public Box playerBox;
 public Geometry playerGeom;
 public Material playerMat;
 //Guns
 public static final String AK47 = "Models/AK-47/AK-47.j3o";
 public static final String USP = "Models/H&K USP 45 Game/H&K USP 45 Game.j3o";
 public static final String M1911 = "Models/M1911/M1911.j3o";
 //Player Stats
 public static int health = 100;
 public int o2 = 500;
 public int attackDam = 10;
 public int score = 0;
 public int ammo = 10;
 public float mana = 50;
 public float spellCoolDown = 0;
 //debug
 BitmapText coords;

 public static void main(String[] args) {
  try {
   defaults.load("com.epicestgaming.ofgame");
  } catch (BackingStoreException ex) {
   saveOptions();
  }
  Game app = new Game();
  app.setSettings(defaults);
  app.setShowSettings(false);
  app.start();
 }

 @Override
 public void simpleInitApp() {

  wasFullscreen = defaults.getBoolean("Fullscreen");
  NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(
          assetManager, inputManager, audioRenderer, guiViewPort);
  nifty = niftyDisplay.getNifty();
  guiViewPort.addProcessor(niftyDisplay);
  flyCam.setDragToRotate(true);

  nifty.loadStyleFile("Interface/Styles/OF_Default/OF_Default_Style.xml");
  nifty.loadControlFile("nifty-default-controls.xml");

  makeNiftyUse(nifty);

  rootNode.attachChild(unmovable);
  rootNode.attachChild(movable);
  unmovable.attachChild(environ);
  movable.attachChild(characters);
  characters.attachChild(PC);
  characters.attachChild(NPC);

  fpp = new FilterPostProcessor(assetManager);
  demoMap();
  viewPort.addProcessor(fpp);
  simpleEntityInit();
 }

 public void makeNiftyUse(Nifty nifty) {
  nifty.fromXml("Interface/Presets/start.xml", "start", new NIFTYListener());
  //NIFTYListener.setScreen("hud");
 }

 public void demoMap() {
  /**
   * Set up Physics
   */
  bulletAppState = new BulletAppState();
  stateManager.attach(bulletAppState);
  //bulletAppState.getPhysicsSpace().enableDebug(assetManager);

  // We re-use the flyby camera for rotation, while positioning is handled by physics
  flyCam.setMoveSpeed(100);
  setUpKeys();

  //setting up some extra things for the game
  setUpLight();
  setUpEffects();
  initGuiText();
  initAudio();

  /*Spatial teapot = assetManager.loadModel(AK47);
         Material mat_default = new Material(
         assetManager, "Common/MatDefs/Misc/ShowNormals.j3md");
         teapot.setMaterial(mat_default);
         teapot.setLocalTranslation(0, 64, 0);
         rootNode.attachChild(teapot);*/
  /**
   * 1. Create terrain material and load four textures into it.
   */
  mat_terrain = new Material(assetManager,
          "Common/MatDefs/Terrain/Terrain.j3md");

  /**
   * 1.1) Add ALPHA map (for red-blue-green coded splat textures)
   */
  mat_terrain.setTexture("Alpha", assetManager.loadTexture(
          "Textures/Terrain/splat/alphamapOF.png"));

  /**
   * 1.2) Add GRASS texture into the red layer (Tex1).
   */
  Texture grass = assetManager.loadTexture(
          "Textures/Terrain/grass.jpg");
  grass.setWrap(WrapMode.Repeat);
  mat_terrain.setTexture("Tex1", grass);
  mat_terrain.setFloat("Tex1Scale", 64f);

  /**
   * 1.3) Add DIRT texture into the green layer (Tex2)
   */
  Texture dirt = assetManager.loadTexture(
          "Textures/Terrain/rock.jpg");
  dirt.setWrap(WrapMode.Repeat);
  mat_terrain.setTexture("Tex2", dirt);
  mat_terrain.setFloat("Tex2Scale", 32f);

  /**
   * 1.4) Add ROAD texture into the blue layer (Tex3)
   */
  Texture rock = assetManager.loadTexture(
          "Textures/Terrain/leafs.png");
  rock.setWrap(WrapMode.Repeat);
  mat_terrain.setTexture("Tex3", rock);
  mat_terrain.setFloat("Tex3Scale", 128f);

  /**
   * 2. Create the height map
   */
  AbstractHeightMap heightmap;
  Texture heightMapImage = assetManager.loadTexture(
          "Textures/Terrain/splat/OF-HF.png");
  heightmap = new ImageBasedHeightMap(heightMapImage.getImage());
  heightmap.load();

  /**
   * 3. We have prepared material and heightmap. Now we create the actual
   * terrain 3.1) Create a TerrainQuad and name it "my terrain". 3.2) A good
   * value for terrain tiles is 64x64 -- so we supply 64+1=65. 3.3) We prepared
   * a heightmap of size 512x512 -- so we supply 512+1=513. 3.4) As LOD step
   * scale we supply Vector3f(1,1,1). 3.5) We supply the prepared heightmap
   * itself.
   */
  int patchSize = 33;
  terrain = new TerrainQuad("Terrain", patchSize, 513, heightmap.getHeightMap());

  /**
   * 4. We give the terrain its material, position & scale it, and attach it.
   */
  terrain.setMaterial(mat_terrain);
  terrain.setLocalTranslation(0, -20, 0);
  terrain.setLocalScale(1f, 0.5f, 1f);
  environ.attachChild(terrain);

  /**
   * 5. The LOD (level of detail) depends on were the camera is:
   */
  TerrainLodControl control = new TerrainLodControl(terrain, getCamera());
  terrain.addControl(control);

  CollisionShape sceneShape = CollisionShapeFactory.createMeshShape(terrain);
  landscape = new RigidBodyControl(sceneShape, 0);
  terrain.addControl(landscape);

  trees = assetManager.loadModel("Scenes/trees.j3o");
  //trees.setMaterial(mat_default);
  trees.setLocalTranslation(0f, -21f, 0f);
  environ.attachChild(trees);

  CollisionShape treesShape = CollisionShapeFactory.createMeshShape(trees);
  treescape = new RigidBodyControl(treesShape, 0);
  trees.addControl(treescape);

  CapsuleCollisionShape playerShape = new CapsuleCollisionShape(1.5f, 6f, 1);
  player = new CharacterControl(playerShape, 0.05f);
  player.setJumpSpeed(25);
  player.setFallSpeed(gravity);
  player.setGravity(gravity);
  player.setPhysicsLocation(new Vector3f(0, 48, 0));

  // We attach the scene and the player to the rootnode and the physics space,
  // to make them appear in the game world.
  bulletAppState.getPhysicsSpace().add(landscape);
  bulletAppState.getPhysicsSpace().add(treescape);
  bulletAppState.getPhysicsSpace().add(player);

 }

 public void simpleEntityInit() {
  npcShape = new SphereCollisionShape(2.25f);
  playerBox = new Box(1.5f, 3f, 1.5f);
  playerGeom = new Geometry("Player", playerBox);
  playerMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
  playerGeom.setLocalTranslation(new Vector3f(0, 0, 0));
  playerMat.setColor("Color", ColorRGBA.Red);
  playerGeom.setMaterial(playerMat);
  Node playerNode = new Node();
  playerNode.setName("PlayerNode");
  playerNode.attachChild(playerGeom);
  playerNode.setLocalTranslation(0f, 64f, 0f);
  PC.attachChild(playerNode);

  if (entityList.toArray().length != entityBodiesList.toArray().length) {
   throw new RuntimeException("WTH? entities and entityBodies length are not the same?");
  }
  if (curEntities > maxEntities) {
   throw new RuntimeException("Error: current Enities Number is greater than max Entities Number");
  }

  for (int i = 0; i < curEntities + 1; i++) {
   Box box1 = new Box(1.5f, 1.5f, 1.5f);
   Geometry npcHoldIn = new Geometry("Box" + i, box1);
   npcHoldIn.setLocalTranslation(new Vector3f(0, 0, 0));
   Material mat1 = new Material(assetManager,
           "Common/MatDefs/Misc/Unshaded.j3md");
   mat1.setTexture("ColorMap", assetManager.loadTexture("Textures/EnemyPlaceHolder.png"));
   npcHoldIn.setMaterial(mat1);
   Node npcDefault = new Node();
   npcDefault.setName("" + i);
   npcDefault.attachChild(npcHoldIn);
   npcDefault.setLocalTranslation(0f, 64f, 100f);

   Vector3f coords = TreeCoords.makeRandomSpawn();

   //Display
   entityList.add(i, npcDefault);
   entityList.get(i).setName("ent" + i);
   NPC.attachChild(entityList.get(i));
   //Physics
   entityBodiesList.add(i, new RigidBodyControl(npcShape, 0.05f));
   entityBodiesList.get(i).setGravity(new Vector3f(0f, gravity, 0f));
   entityBodiesList.get(i).setPhysicsLocation(coords);
   bulletAppState.getPhysicsSpace().add(entityBodiesList.get(i));
   //Health
   entityHealths.add(i, 20);
   //Type
   entityTypes.add(i, 0);
   if (i == curEntities) {
    kill(entityList.get(i));
   }
  }
  curEntities++;
 }

 public static void run() {
  running = true;
 }

 public static void quit() {
  playing = false;
 }

 public static void cameraEnable() {
  camEnab = true;
 }

 public static void cameraDisable() {
  camEnab = false;
 }

 public static void setView() {
  if (view.equals("3rd")) {
   view = "1st";
  } else {
   view = "3rd";
  }
  defaults.put("View", view);
 }

 public static void setFullScreen() {
  if (defaults.getBoolean("Fullscreen")) {
   defaults.setFullscreen(false);
  } else {
   defaults.setFullscreen(true);
  }
  saveOptions();
 }

 public static void reset() {
  reset = true;
 }

 public static void saveOptions() {
  try {
   defaults.save("com.epicestgaming.ofgame");
  } catch (BackingStoreException ex) {
   Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
  }
 }

 @Override
 public void simpleUpdate(float tpf) {
  coords.setText("X: " + player.getPhysicsLocation().x + " Y: " + player.getPhysicsLocation().y + " Z: " + player.getPhysicsLocation().z);
  if (running) {
   bulletAppState.setSpeed(speed);
   simpleEntityUpdate(tpf);
   NiftyImage img = nifty.getRenderEngine().createImage(nifty.getScreen("hud"), "Textures/Health/" + NIFTYListener.getHealthImg() + ".png", false);
   Element healthImg = nifty.getScreen("hud").findElementByName("healthImg");
   healthImg.getRenderer(ImageRenderer.class).setImage(img);

   Element healthText = nifty.getScreen("hud").findElementByName("health");
   healthText.getRenderer(TextRenderer.class).setText("Health: " + health);

   Element o2Text = nifty.getScreen("hud").findElementByName("O2");
   o2Text.getRenderer(TextRenderer.class).setText("O2: " + o2);

   Element scText = nifty.getScreen("hud").findElementByName("score");
   scText.getRenderer(TextRenderer.class).setText("Score: " + score);

   Element amText = nifty.getScreen("hud").findElementByName("ammo");
   amText.getRenderer(TextRenderer.class).setText("Ammo: " + ammo);

   Element maText = nifty.getScreen("hud").findElementByName("mana");
   maText.getRenderer(TextRenderer.class).setText("Mana: " + mana);

   camDir.set(cam.getDirection()).multLocal(0.6f);
   camLeft.set(cam.getLeft()).multLocal(0.4f);
   walkDirection.set(0, 0, 0);
   if (left) {
    walkDirection.addLocal(camLeft);
   }
   if (right) {
    walkDirection.addLocal(camLeft.negate());
   }
   if (up) {
    walkDirection.addLocal(camDir);
   }
   if (down) {
    walkDirection.addLocal(camDir.negate());
   }
   player.setWalkDirection(walkDirection);
   cam.setLocation(player.getPhysicsLocation());
   listener.setLocation(cam.getLocation());
   listener.setRotation(cam.getRotation());
   if (player.getPhysicsLocation().y < initialWaterHeight) {
    player.setJumpSpeed(12.5f);
    player.setFallSpeed(gravity / 2);
    player.setGravity(gravity / 2);
    if (o2 > 0) {
     o2--;
    }
   } else {
    player.setJumpSpeed(25);
    player.setFallSpeed(gravity);
    player.setGravity(gravity);
    if (mana < 50.0f) {
     mana += 0.01f;
     if (mana > 50.0f) {
      mana = 50.0f;
     }
    }
    if (o2 < 500) {
     o2 += 2;
     if (o2 > 500) {
      o2 = 500;
     }
    }
   }
   if (ammo > 50) {
    ammo = 50;
   }
   if (o2 == 0) {
    health--;
   }
   if (health <= 0) {
    player.setPhysicsLocation(new Vector3f(0f, 48f, 0f));
    health = 100;
    o2 = 500;
   }
   if (!hasRunInited) {
    audio_nature.play(); // play continuously!
    audio_ocean.play();
    hasRunInited = true;
   }
   if (spellCoolDown < 0) {
    spellCoolDown = 0;
   } else if (spellCoolDown > 0) {
    spellCoolDown -= tpf;
   }
  } else {
   bulletAppState.setSpeed(0);
  }
  if (!playing) {
   stop();
  }
  if (camEnab) {
   flyCam.setDragToRotate(false);
  } else {
   flyCam.setDragToRotate(true);
  }
  if (wasFullscreen != defaults.getBoolean("Fullscreen")) {
   wasFullscreen = defaults.getBoolean("Fullscreen");
   setSettings(defaults);
   restart();
   //Element fullbutton = nifty.getScreen("settings").findElementByName("fullButton");
   //fullbutton.getRenderer(TextRenderer.class).setText("O2: " + o2);
   NIFTYListener.setScreen("settings");
  }
  if (reset) {
   setSettings(defaults);
   restart();
   reset = false;
  }

 }

 public void simpleEntityUpdate(float tpf) {
  for (int i = 0; i < curEntities; i++) {
   boolean htm;
   try {
    htm = !(entityHealths.get(i) < 0);

   } catch (NullPointerException npe) {
    htm = false;
   }
   int type = entityTypes.get(i);
   if (htm && (entityList.get(i) != null) && (type == 0)) { //Ghouls
    //Display
    entityList.get(i).setLocalTranslation(entityBodiesList.get(i).getPhysicsLocation());
    //entityList.get(i).setLocalRotation(entityBodiesList.get(i).getPhysicsRotation());
    entityList.get(i).lookAt(player.getPhysicsLocation(), new Vector3f(0f, 1f, 0f));
    //Physics
    //Vector3f oldPhysicsLocation = entityBodiesList.get(i).getPhysicsLocation();
    Vector3f newPhysicsLocation = entityBodiesList.get(i).getPhysicsLocation();
    float entityX = newPhysicsLocation.x;
    float entityZ = newPhysicsLocation.z;
    float entityY = newPhysicsLocation.y;
    float playerX = player.getPhysicsLocation().x;
    float playerZ = player.getPhysicsLocation().z;
    float playerY = player.getPhysicsLocation().y;
    //Will not go underwater
    if (newPhysicsLocation.y < 0) {
     entityY = 0;
    }
    //doesn't go to far fromn the isle
    int maprange = 255;
    if (entityX > maprange) {
     entityX = maprange;
    } else if (entityX < -maprange) {
     entityX = -maprange;
    }
    if (entityZ > maprange) {
     entityZ = maprange;
    } else if (entityZ < -maprange) {
     entityZ = -maprange;
    }
    //AI (DO NOT MESS WITH Y Coordinate!!)
    float speed = tpf * 15;
    if (playerX < entityX) {
     entityX -= speed;
     if (playerX > entityX) {
      entityX = playerX;
     }
    } else if (playerX > entityX) {
     entityX += speed;
     if (playerX < entityX) {
      entityX = playerX;
     }
    }
    if (playerZ < entityZ) {
     entityZ -= speed;
     if (playerZ > entityZ) {
      entityZ = playerZ;
     }
    } else if (playerZ > entityZ) {
     entityZ += speed;
     if (playerZ < entityZ) {
      entityZ = playerZ;
     }
    }
    //Finish
    newPhysicsLocation.y = entityY;
    newPhysicsLocation.x = entityX;
    newPhysicsLocation.z = entityZ;
    int range = 5;
    int damage = 2;
    boolean x = false;
    boolean y = false;
    boolean z = false;
    if (((playerX + range < entityX) && (playerX - range > entityX)) || ((playerX + range > entityX) && (playerX - range < entityX))) {
     x = true;
    }
    if (((playerY + range < entityY) && (playerY - range > entityY)) || ((playerY + range > entityY) && (playerY - range < entityY))) {
     y = true;
    }
    if (((playerZ + range < entityZ) && (playerZ - range > entityZ)) || ((playerZ + range > entityZ) && (playerZ - range < entityZ))) {
     z = true;
    }

    if (x && y && z) {
     health -= damage;
    }
    entityBodiesList.get(i).setPhysicsLocation(newPhysicsLocation);
   } else if (htm && (type == 1)) { //Items
    //Display
    entityList.get(i).setLocalTranslation(entityBodiesList.get(i).getPhysicsLocation());
    entityList.get(i).setLocalRotation(entityBodiesList.get(i).getPhysicsRotation());
    //entity[i].lookAt(player.getPhysicsLocation(), new Vector3f(0f, 1f, 0f));
    //stuff
    Vector3f oldPhysicsLocation = entityBodiesList.get(i).getPhysicsLocation();
    Vector3f newPhysicsLocation = entityBodiesList.get(i).getPhysicsLocation();
    float entityX = newPhysicsLocation.x;
    float entityZ = newPhysicsLocation.z;
    float entityY = newPhysicsLocation.y;
    float playerX = player.getPhysicsLocation().x;
    float playerZ = player.getPhysicsLocation().z;
    float playerY = player.getPhysicsLocation().y;
    int range = 5;
    boolean x = false;
    boolean y = false;
    boolean z = false;
    if (((playerX + range < entityX) && (playerX - range > entityX)) || ((playerX + range > entityX) && (playerX - range < entityX))) {
     x = true;
    }
    if (((playerY + range < entityY) && (playerY - range > entityY)) || ((playerY + range > entityY) && (playerY - range < entityY))) {
     y = true;
    }
    if (((playerZ + range < entityZ) && (playerZ - range > entityZ)) || ((playerZ + range > entityZ) && (playerZ - range < entityZ))) {
     z = true;
    }

    if (x && y && z) {
     ammo += entityHealths.get(i);
     entityHealths.set(i, 0);
    }
   } else if (htm && (type == 2)) { //Grenades
    //Display
    entityList.get(i).setLocalTranslation(entityBodiesList.get(i).getPhysicsLocation());
    entityList.get(i).setLocalRotation(entityBodiesList.get(i).getPhysicsRotation());
    //entity[i].lookAt(player.getPhysicsLocation(), new Vector3f(0f, 1f, 0f));

    entityHealths.set(i, entityHealths.get(i) - 1);
   } else if (entityList.get(i) == null) {
    //do nothing
   } else {
    double random15 = Math.random() * 5;
    if (type == 0) {
     if (random15 < 1) {
      random15 = 1;
     }
     ammo += (int) random15;
     bulletAppState.getPhysicsSpace().remove(entityBodiesList.get(i));//remove collision
     entityList.get(i).removeFromParent();//remove display
     entityBodiesList.set(i, null);
     entityList.set(i, null);
     entityHealths.set(i, null);
     //entityTypes.set(i, null);
     score++;
     health += 3;
     if (health > 100) {
      health = 100;
     }
    } else if (type == 1) {
     bulletAppState.getPhysicsSpace().remove(entityBodiesList.get(i));//remove collision
     entityList.get(i).removeFromParent();//remove display
     entityBodiesList.set(i, null);
     entityList.set(i, null);
     entityHealths.set(i, null);
     //entityTypes.set(i, null);
    } else if (type == 2) {
     //explode
    }
   }
  }
  PC.getChild("PlayerNode").setLocalTranslation(player.getPhysicsLocation());
  for (int i = 0; i < maxRound; i++) {
   if (timePassed > (round * roundTime) && i > round) {
    addEntities(10, 0);
    round++;
    //System.out.println("round passed");
   }
  }
  timePassed += tpf;
  //System.out.println(entTimePassed);
 }

 @Override
 public void onAction(String binding, boolean isPressed, float tpf) {
  if (running) {
   if (binding.equals("Left")) {
    left = isPressed;
   } else if (binding.equals("Right")) {
    right = isPressed;
   } else if (binding.equals("Up")) {
    up = isPressed;
   } else if (binding.equals("Down")) {
    down = isPressed;
   } else if (binding.equals("Jump")) {
    if (isPressed) {
     player.jump();
    }
   } else if (binding.equals("Inventory")) {
    NIFTYListener.setScreen("pause");
   } else if (binding.equals("Pause")) {
    NIFTYListener.setScreen("pause");
   }
   if (spellCoolDown == 0) {
    if (binding.equals("RightClick")) {
     spellCoolDown = 2;
    } else if (binding.equals("LeftClick") && !isPressed && (ammo > 0)) {
     ammo--;
     audio_gun.playInstance();
     // 1. Reset results list.
     CollisionResults results = new CollisionResults();
     // 2. Aim the ray from cam loc to cam direction.
     Ray ray = new Ray(cam.getLocation(), cam.getDirection());
     // 3. Collect intersections between Ray and Shootables in results list.
     NPC.collideWith(ray, results);
     // 4. Print the results
     for (int i = 0; i < results.size(); i++) {
      // For each hit, we know distance, impact point, name of geometry.
      float dist = results.getCollision(i).getDistance();
      Vector3f pt = results.getCollision(i).getContactPoint();
      Spatial thing = results.getCollision(i).getGeometry();
      String hit = thing.getName();
      attack(thing);
     }
     // 5. Use the results (we mark the hit object)
     if (results.size() > 0) {
      // The closest collision point is what was truly hit:
      CollisionResult closest = results.getClosestCollision();
     }

    }
   } else {
    if (binding.equals("RightClick") && mana >= 25) {
     addEntities(5, 1);
     mana -= 25;
     spellCoolDown = 0;
    } else if (binding.equals("LeftClick")) {
     spellCoolDown = 0;
    }
   }
  }

 }

 @Override
 public void restart() {
  super.restart();
  guiNode.detachChildNamed("crosshairs");
  BitmapText ch = new BitmapText(guiFont, false);
  ch.setName("crosshairs");
  ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
  ch.setText("+"); // crosshairs
  ch.setLocalTranslation( // center
          settings.getWidth() / 2 - ch.getLineWidth() / 2, settings.getHeight() / 2 + ch.getLineHeight() / 2, 0);
  guiNode.attachChild(ch);
 }

 public void setUpLight() {
  viewPort.setBackgroundColor(new ColorRGBA(0f, 1f, 1f, 1f));
  /**
   * A white, directional light source
   */
  DirectionalLight sun = new DirectionalLight();
  sun.setColor(ColorRGBA.White);
  sun.setDirection(new Vector3f(-0.5f, -0.5f, -0.5f).normalizeLocal());
  rootNode.addLight(sun);
 }

 protected void setUpKeys() {
  flyCam.setZoomSpeed(0);
  inputManager.deleteMapping(SimpleApplication.INPUT_MAPPING_EXIT);
  inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
  inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
  inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_W));
  inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S));
  inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
  inputManager.addMapping("RightClick", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
  inputManager.addMapping("LeftClick", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
  inputManager.addMapping("Pause", new KeyTrigger(KeyInput.KEY_ESCAPE));
  inputManager.addMapping("Interact", new KeyTrigger(KeyInput.KEY_E));
  inputManager.addMapping("Inventory", new KeyTrigger(KeyInput.KEY_TAB));
  inputManager.addListener(this, "Inventory");
  inputManager.addListener(this, "Interact");
  inputManager.addListener(this, "RightClick");
  inputManager.addListener(this, "LeftClick");
  inputManager.addListener(this, "Left");
  inputManager.addListener(this, "Right");
  inputManager.addListener(this, "Up");
  inputManager.addListener(this, "Down");
  inputManager.addListener(this, "Jump");
  inputManager.addListener(this, "Pause");
 }

 protected void setUpEffects() {
  Material rainMat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
  // "raindrop.png" is just "spark.png", rotated by 90 degrees.
  rainMat.setTexture("Texture", assetManager.loadTexture("Textures/Effects/rain.png"));

  for (int LOW = 0; LOW < weatherLevel; LOW++) {
   ParticleEmitter rain = new ParticleEmitter(
           "rainPoints", ParticleMesh.Type.Triangle, rainParticlesPerSec * weather);
   rain.setShape(new EmitterSphereShape(Vector3f.ZERO, rainRadius));
   rain.setLocalTranslation(new Vector3f(0f, rainHeight, 0f));
   rain.getParticleInfluencer().setInitialVelocity(new Vector3f(0.0f, -1.0f, 0.0f));
   rain.getParticleInfluencer().setVelocityVariation(0.1f);
   rain.setImagesX(1);
   rain.setImagesY(1);
   rain.setGravity(0, gravity * weather, 0);
   rain.setLowLife(2);
   rain.setHighLife(rainMaxLife);
   rain.setStartSize(2f);
   rain.setEndSize(2f);
   rain.setStartColor(new ColorRGBA(0.0f, 0.0f, 1.0f, 0.8f));
   rain.setEndColor(new ColorRGBA(0.8f, 0.8f, 1.0f, 0.6f));
   rain.setFacingVelocity(false);
   rain.setParticlesPerSec(rainParticlesPerSec * weather);
   rain.setRotateSpeed(0.0f);
   rain.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
   rain.setNumParticles(rainMaxPart);

   rain.setMaterial(rainMat);
   rain.setQueueBucket(RenderQueue.Bucket.Transparent);

   environ.attachChild(rain);
  }

  //Water
  WaterFilter water;
  Vector3f lightDir = new Vector3f(-0.5f, -0.5f, -0.5f);
  water = new WaterFilter(rootNode, lightDir);
  water.setWaterHeight(initialWaterHeight);
  fpp.addFilter(water);
 }

 protected void initGuiText() {
  guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
  BitmapText ch = new BitmapText(guiFont, false);
  ch.setName("crosshairs");
  ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
  ch.setText("+"); // crosshairs
  ch.setLocalTranslation( // center
          settings.getWidth() / 2 - ch.getLineWidth() / 2, settings.getHeight() / 2 + ch.getLineHeight() / 2, 0);
  guiNode.attachChild(ch);

  coords = new BitmapText(guiFont, false);
  coords.setSize(guiFont.getCharSet().getRenderedSize());
  coords.setText("X: Y: Z:");
  coords.setLocalTranslation(0, 180, 0);
  guiNode.attachChild(coords);
 }

 protected void initAudio() {
  //music
  audio_music = new AudioNode(assetManager, "Sounds/Music/closecombat_1_LOOP.wav", false);
  audio_music.setLooping(true);  // activate continuous playing
  audio_music.setPositional(false);
  audio_music.setVolume(0.4f);
  environ.attachChild(audio_music);
  audio_music.play();
  //ambient background
  audio_nature = new AudioNode(assetManager, "Sounds/Environment/Nature.ogg", false);
  audio_nature.setLooping(true);  // activate continuous playing
  audio_nature.setPositional(false);
  audio_nature.setVolume(15);
  environ.attachChild(audio_nature);
  audio_ocean = new AudioNode(assetManager, "Sounds/Environment/Ocean Waves.ogg", false);
  audio_ocean.setLooping(true);  // activate continuous playing
  audio_ocean.setPositional(false);
  audio_ocean.setVolume(0.1f);
  environ.attachChild(audio_ocean);
  //ambient sounds
  audio_gun = new AudioNode(assetManager, "Sounds/Effects/Gun.wav", false);
  audio_gun.setPositional(false);
  audio_gun.setLooping(false);
  audio_gun.setVolume(0.2f);
  PC.attachChild(audio_gun);
 }

 private void attack(Spatial entity) {
  String name = entity.getName();
  int id;
  int lowestNumPos = 3;
  id = Integer.parseInt(name.substring(lowestNumPos));
  double d = Math.random();
  double mod;
  if (d < 0.33) {
   mod = Math.random() * -2;
  } else if (d > 0.60) {
   mod = Math.random() * 2;
  } else {
   mod = Math.random();
  }
  int modifier = (int) mod;
  int newHealth = entityHealths.get(id).intValue() - (attackDam + modifier);
  entityHealths.set(id, newHealth);
 }

 private void kill(Spatial entity) {
  String name = entity.getName();
  int id;
  int lowestNumPos = 3;
  id = Integer.parseInt(name.substring(lowestNumPos));
  int newHealth = -1;
  entityHealths.set(id, newHealth);
 }

 private void addEntities(int am, int type) {
  for (int a = 0; a < am; a++) {
   if (curEntities <= maxEntities) {

    int nul = entityList.size();
    for (int i = 0; i < entityList.size(); i++) {
     if (entityList.get(i) == null) {
      nul = i;
      break;
     }
    }
    if (type == 0) {
     if (nul == entityList.size()) {
      Box box1 = new Box(1.5f, 1.5f, 1.5f);
      Geometry npcHoldIn = new Geometry("Box" + entityList.size(), box1);
      npcHoldIn.setLocalTranslation(new Vector3f(0, 0, 0));
      Material mat1 = new Material(assetManager,
              "Common/MatDefs/Misc/Unshaded.j3md");
      mat1.setTexture("ColorMap", assetManager.loadTexture("Textures/EnemyPlaceHolder.png"));
      npcHoldIn.setMaterial(mat1);
      Node npcDefault = new Node();
      npcDefault.attachChild(npcHoldIn);
      npcDefault.setLocalTranslation(0f, 64f, 100f);

      Vector3f coords = TreeCoords.makeRandomSpawn();

      //Display
      int i = nul;
      entityList.set(i, npcDefault);
      entityList.get(i).setName("ent" + entityList.size());
      NPC.attachChild(entityList.get(i));
      //Physics
      entityBodiesList.set(i, new RigidBodyControl(npcShape, 0.05f));
      entityBodiesList.get(i).setGravity(new Vector3f(0f, gravity, 0f));

      entityBodiesList.get(i).setPhysicsLocation(coords);
      bulletAppState.getPhysicsSpace().add(entityBodiesList.get(i));
      //Health
      entityHealths.set(i, new Integer(20));
      //Type
      entityTypes.add(i, type);
      //
      curEntities++;
     } else {
      Box box1 = new Box(1.5f, 1.5f, 1.5f);
      Geometry npcHoldIn = new Geometry("Box" + entityList.size(), box1);
      npcHoldIn.setLocalTranslation(new Vector3f(0, 0, 0));
      Material mat1 = new Material(assetManager,
              "Common/MatDefs/Misc/Unshaded.j3md");
      mat1.setTexture("ColorMap", assetManager.loadTexture("Textures/EnemyPlaceHolder.png"));
      npcHoldIn.setMaterial(mat1);
      Node npcDefault = new Node();
      npcDefault.attachChild(npcHoldIn);
      npcDefault.setLocalTranslation(0f, 64f, 100f);

      Vector3f coords = TreeCoords.makeRandomSpawn();

      //Display
      int i = entityList.size();
      entityList.add(i, npcDefault);
      entityList.get(i).setName("ent" + entityList.size());
      NPC.attachChild(entityList.get(i));
      //Physics
      entityBodiesList.add(i, new RigidBodyControl(npcShape, 0.05f));
      entityBodiesList.get(i).setGravity(new Vector3f(0f, gravity, 0f));
      entityBodiesList.get(i).setPhysicsLocation(coords);
      bulletAppState.getPhysicsSpace().add(entityBodiesList.get(i));
      //Health
      entityHealths.add(i, new Integer(20));
      //Type
      entityTypes.add(i, type);
      //
      curEntities++;
     }
    } else if (type == 1) {
     BoxCollisionShape ammo = new BoxCollisionShape(new Vector3f(1.5f, 1.5f, 1.5f));
     if (nul == entityList.size()) {
      Box box1 = new Box(1.5f, 1.5f, 1.5f);
      Geometry npcHoldIn = new Geometry("Box" + entityList.size(), box1);
      npcHoldIn.setLocalTranslation(new Vector3f(0, 0, 0));
      Material mat1 = new Material(assetManager,
              "Common/MatDefs/Misc/Unshaded.j3md");
      mat1.setTexture("ColorMap", assetManager.loadTexture("Textures/Bullets.png"));
      npcHoldIn.setMaterial(mat1);
      Node npcDefault = new Node();
      npcDefault.attachChild(npcHoldIn);
      npcDefault.setLocalTranslation(0f, 64f, 100f);

      Vector3f coords = TreeCoords.makeRandomSpawn();

      //Display
      int i = nul;
      entityList.set(i, npcDefault);
      entityList.get(i).setName("ent" + entityList.size());
      NPC.attachChild(entityList.get(i));
      //Physics
      entityBodiesList.set(i, new RigidBodyControl(ammo, 0.05f));
      entityBodiesList.get(i).setGravity(new Vector3f(0f, gravity, 0f));

      entityBodiesList.get(i).setPhysicsLocation(coords);
      bulletAppState.getPhysicsSpace().add(entityBodiesList.get(i));
      //Health
      entityHealths.set(i, new Integer(20));
      //Type
      entityTypes.add(i, type);
      //
      curEntities++;
     } else {
      Box box1 = new Box(1.5f, 1.5f, 1.5f);
      Geometry npcHoldIn = new Geometry("Box" + entityList.size(), box1);
      npcHoldIn.setLocalTranslation(new Vector3f(0, 0, 0));
      Material mat1 = new Material(assetManager,
              "Common/MatDefs/Misc/Unshaded.j3md");
      mat1.setTexture("ColorMap", assetManager.loadTexture("Textures/Bullets.png"));
      npcHoldIn.setMaterial(mat1);
      Node npcDefault = new Node();
      npcDefault.attachChild(npcHoldIn);
      npcDefault.setLocalTranslation(0f, 64f, 100f);

      Vector3f coords = TreeCoords.makeRandomSpawn();

      //Display
      int i = entityList.size();
      entityList.add(i, npcDefault);
      entityList.get(i).setName("ent" + entityList.size());
      NPC.attachChild(entityList.get(i));
      //Physics
      entityBodiesList.add(i, new RigidBodyControl(ammo, 0.05f));
      entityBodiesList.get(i).setGravity(new Vector3f(0f, gravity, 0f));
      entityBodiesList.get(i).setPhysicsLocation(coords);
      bulletAppState.getPhysicsSpace().add(entityBodiesList.get(i));
      //Health
      entityHealths.add(i, new Integer(20));
      //Type
      entityTypes.add(i, type);
      //
      curEntities++;
     }
    }
   }
  }
 }
}
