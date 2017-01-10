package testbed.ABMParticleSimulation;

import org.jbox2d.common.Vec2;

import java.awt.image.BufferedImage;

/**
 * @author Rajesh Balagam
 */
public class Globals {

  public static boolean GUI = true;
  public static int GUIWIDTH = 1000;
  public static int GUIHEIGHT = 1000;

  public static Vec2 cameraPos = new Vec2(0, 0);
  public static int Hz = 60;
  public static double frameRate = 60;

  public static BufferedImage simImage;
  public static boolean takeSnapShotBtn = false;

  // used only for saving final image
  public static int imageWidth = 1000;
  public static int imageHeight = 1000;
  public static boolean scaleImages = false;

  public static float cameraPosX = 500;
  public static float cameraPosY = 500;
  public static double cameraScale = 0.995; //0.497f;  // GUIDim/worldDim

  public static int background = 0; // 0 - black, 1 - white

  public static boolean drawDebugDrawShapes = true;
  public static boolean nonGUISnapShot = true;

  public static double currentTime = 0;

  public static DebugDrawJ2D dbgDraw;
  public static Simulation simulation;

  public static int stringSepY = 15;
}
