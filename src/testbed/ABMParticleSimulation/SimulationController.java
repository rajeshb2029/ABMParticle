package testbed.ABMParticleSimulation;

import org.jbox2d.common.Color3f;
import org.jbox2d.common.Vec2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author Rajesh Balagam
 */
public class SimulationController implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(SimulationController.class);

  public static final int DEFAULT_FPS = 60;
  private final int width;
  private final int height;
  private Insets frameInsets;

  private long startTime;
  private long frameCount;
  private int targetFrameRate;
  private double frameRate = 0;
  private boolean animating = false;
  private final Thread animator;

  public GraphicsPanel panel;
  public TestbedSidePanel sidePanel;

  public final Graphics2D g2d;
  public BufferedImage bImage;
  public DebugDrawJ2D dbgDraw;

  boolean drawSimImage = false;
  boolean takeSnapshot = false;

  public SimulationController(int width, int height) {
    this.width = width;
    this.height = height;

    animator = new Thread(this, "Testbed");

    bImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    LOGGER.debug("Image Dimensions: " + bImage.getWidth() + " x " + bImage.getHeight());
    g2d = (Graphics2D) bImage.getGraphics();
    dbgDraw = new DebugDrawJ2D(g2d, width, height);
    Globals.dbgDraw = dbgDraw;

    if (Globals.GUI) {
      panel = new GraphicsPanel(width, height);
      panel.setPreferredSize(new Dimension(width, height));
      panel.setFrameRate(DEFAULT_FPS);
      Globals.frameRate = DEFAULT_FPS;
//    panel.setBackground(Color.black);  
      sidePanel = new TestbedSidePanel();
      panel.g2d = g2d;
      panel.bImage = bImage;
      panel.dbgDraw = dbgDraw;
    }

    Globals.simulation = new Simulation(Parameters.worldWidth, Parameters.worldHeight);
//    Globals.simulation.drawSimulation();
//    loopInit();
  }

  protected final void loopInit() {
    setFrameRate(DEFAULT_FPS);
//    LOGGER.debug(this.getInsets().toString());
    clearScreen();
//    drawFramerate();
//    drawBorders();
    Globals.simulation.drawSimulation();
    if (Parameters.saveImages) {
      takeSnapshot();
    }
    if (Parameters.writeCellInformation) {
      SimUtils.writeCellInformation();
    }
  }

  protected void update() {
    if (!Globals.simulation.simPaused || Globals.simulation.singleStep) {
      clearScreen();
//      drawFramerate();
//    drawBorders();
//    drawMyGraphics();
      takeSnapshot = checkSnapshotTime();
      if (Globals.GUI) {
        drawSimImage = true;
      } else if (takeSnapshot) {
        drawSimImage = true;
      }

      if (drawSimImage) {
//        Globals.simulation.clearCanvas();        
        Globals.simulation.drawSimulation();
        if (takeSnapshot && Parameters.saveImages) {
          takeSnapshot();
        }
        drawSimImage = false;
      }

      Globals.simulation.step();
      Globals.simulation.singleStep = false;
    }
//    Globals.simulation.drawSimulation();
//    panel.update();
  }

  boolean checkSnapshotTime() {
    return (Parameters.saveImages && (Globals.currentTime - Globals.simulation.lastSnapShotTime)
            >= Parameters.snapShotInterval);
  }

  void takeSnapshot() {
    Globals.simImage = bImage.getSubimage(0, 0, bImage.getWidth(), bImage.getHeight());
    Globals.simulation.writeSnapshot();
    Globals.simulation.lastSnapShotTime = Globals.currentTime;
  }

//  final void drawFramerate() {
//    g2d.setColor(Color.red);
//    String s = String.format("Framerate: %.2f", frameRate);
//    g2d.drawString(s, 5, Globals.stringSepY);
//  }

  final void drawMyGraphics() {
//    dbgDraw.drawCircle(new Vec2(0, 0), 500, Color3f.BLUE);
    dbgDraw.drawSmoothCircle(new Vec2(0, 0), 500, Color3f.BLUE);
  }

  final void clearScreen() {
    g2d.setColor(Color.black);
    g2d.fillRect(0, 0, width, height);
//    g2d.fillRect(0, 0, Globals.imageWidth, Globals.imageHeight);
//    g2d.fillRect(0, 0, Parameters.worldWidth, Parameters.worldHeight);
  }

  //  final void drawBorders() {
//    g2d.setColor(Color.red);
////    Insets insets = this.getInsets();
////    LOGGER.debug(insets.toString());    
//    g2d.drawRect(0, 0, width - (frameInsets.left + frameInsets.right + 1),
//            height - (frameInsets.top + frameInsets.bottom + 1));
//    g2d.drawLine(width / 2, 0, width / 2, height);
//    g2d.drawLine(0, height / 2, width, height / 2);
//    int w = 100, h = 100;
//    g2d.drawRect(width / 2 - w / 2, height / 2 - h / 2, w, h);
//    w = 500;
//    h = 500;
//    g2d.drawRect(width / 2 - w / 2, height / 2 - h / 2, w, h);
//  }
  public void setFrameInsets(Insets insets) {
    this.frameInsets = insets;
  }

  private void setFrameRate(int fps) {
    if (fps <= 0) {
      throw new IllegalArgumentException("Fps cannot be less than or equal to zero");
    }
    targetFrameRate = fps;
    frameRate = fps;
  }

  public int getFrameRate() {
    return targetFrameRate;
  }

  public double getCalculatedFrameRate() {
    return frameRate;
  }

  public long getStartTime() {
    return startTime;
  }

  public long getFrameCount() {
    return frameCount;
  }

  public boolean isAnimating() {
    return animating;
  }

  public synchronized void start() {
    if (!animating) {
      frameCount = 0;
      animator.start();
    } else {
      LOGGER.warn("Animation is already animating");
    }
  }

  public synchronized void stop() {
    animating = false;
  }

  @Override
  public void run() {
    long beforeTime, afterTime, updateTime, timeDiff, sleepTime, timeSpent;
    double timeInSecs;
    beforeTime = startTime = updateTime = System.nanoTime();
    sleepTime = 0;

    animating = true;
    loopInit();
    while (animating) {
      if (Globals.GUI) {
        timeSpent = beforeTime - updateTime;
        if (timeSpent > 0) {
          timeInSecs = timeSpent * 1.0f / 1000000000.0f;
          updateTime = System.nanoTime();
          frameRate = (frameRate * 0.9f) + (1.0f / timeInSecs) * 0.1f;
          panel.setFrameRate(frameRate);
          Globals.frameRate = frameRate;
        } else {
          updateTime = System.nanoTime();
        }

        update();
        paintScreen();
        frameCount++;

        afterTime = System.nanoTime();
        timeDiff = afterTime - beforeTime;
        sleepTime = (1000000000 / targetFrameRate - timeDiff) / 1000000;
        if (sleepTime > 0) {
          try {
            Thread.sleep(sleepTime);
          } catch (InterruptedException ex) {
          }
        }

        beforeTime = System.nanoTime();
      } else {
        update();
        if (Globals.currentTime > Parameters.finalTime || Globals.simulation.simStopped) {
          System.exit(0);
        }
      }
    }
  }

  public void paintScreen() {
    try {
      Graphics g = panel.getGraphics();
      if ((g != null) && panel.bImage != null) {
        g.drawImage(panel.bImage, 0, 0, null);
        Toolkit.getDefaultToolkit().sync();
        g.dispose();
        if (Globals.takeSnapShotBtn) {
//          if(Globals.scaleImages) {
//            Globals.simImage = panel.bImage.getSubimage(0, 0, Globals.imageWidth, Globals.imageHeight);
//          } else {
//            Globals.simImage = panel.bImage.getSubimage(0, 0, width, height);
          Globals.simImage = bImage.getSubimage(0, 0, bImage.getWidth(), bImage.getHeight());
//          }
          Globals.simulation.writeSnapshot();
          Globals.takeSnapShotBtn = false;
        }
      }
    } catch (AWTError e) {
      LOGGER.error("Graphics context error", e);
    }
  }
}
