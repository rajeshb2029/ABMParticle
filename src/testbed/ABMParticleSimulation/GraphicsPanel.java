package testbed.ABMParticleSimulation;

import org.jbox2d.common.Mat22;
import org.jbox2d.common.OBBViewportTransform;
import org.jbox2d.common.Vec2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

/**
 * @author Rajesh Balagam
 */
public class GraphicsPanel extends JPanel {

  private static final Logger LOGGER = LoggerFactory.getLogger(GraphicsPanel.class);

  //  private int width;
//  private int height;
  private double frameRate = 60f;

  private static final double ZOOM_OUT_SCALE = 0.95;
  private static final double ZOOM_IN_SCALE = 1.05;

  private final Vec2 mouse = new Vec2();
  private final Vec2 mouseWorld = new Vec2();
  private final Vec2 dragginMouse = new Vec2();
  private boolean drag = false;
  private boolean mousePressed = false;

  private double cachedCameraScale;
  private final Vec2 cachedCameraPos = new Vec2();
  private boolean hasCachedCamera = false;

  public Graphics2D g2d;
  public BufferedImage bImage;
  public DebugDrawJ2D dbgDraw;

  public GraphicsPanel(int width, int height) {
//    this.width = width;
//    this.height = height;
    setPreferredSize(new Dimension(width, height));

//    bImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
//    if (Globals.scaleImages) {
//      bImage = new BufferedImage(Globals.imageWidth, Globals.imageHeight, BufferedImage.TYPE_INT_ARGB);
//    }    
//    bImage = new BufferedImage(Parameters.worldWidth, Parameters.worldHeight, BufferedImage.TYPE_INT_ARGB);
//    LOGGER.debug("Image Dimensions: " + bImage.getWidth() + " x " + bImage.getHeight());
//    g2d = (Graphics2D) bImage.getGraphics();
//    dbgDraw = new DebugDrawJ2D(g2d, width, height);
//    dbgDraw = new DebugDrawJ2D(g2d, Globals.imageWidth, Globals.imageHeight);
//    Globals.dbgDraw = dbgDraw;

    addMouseWheelListener(new MouseWheelListener() {
      private final Vec2 oldCenter = new Vec2();
      private final Vec2 newCenter = new Vec2();
      private final Mat22 upScale = Mat22.createScaleTransform((float) ZOOM_IN_SCALE);
      private final Mat22 downScale = Mat22.createScaleTransform((float) ZOOM_OUT_SCALE);

      @Override
      public void mouseWheelMoved(MouseWheelEvent e) {
        if (mousePressed) {
          int notches = e.getWheelRotation();

          OBBViewportTransform trans = (OBBViewportTransform) dbgDraw.getViewportTranform();
          oldCenter.set(getWorldMouse());

          if (notches < 0) {
            trans.mulByTransform(upScale);
            setCachedCameraScale(getCachedCameraScale() * ZOOM_IN_SCALE);
          } else if (notches > 0) {
            trans.mulByTransform(downScale);
            setCachedCameraScale(getCachedCameraScale() * ZOOM_OUT_SCALE);
          }

          dbgDraw.getScreenToWorldToOut(getMouse(), newCenter);

          Vec2 transformedMove = oldCenter.subLocal(newCenter);
          dbgDraw.getViewportTranform().setCenter(
                  dbgDraw.getViewportTranform().getCenter().addLocal(transformedMove));

          setCachedCameraPos(dbgDraw.getViewportTranform().getCenter());
        }
      }
    });

    addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        dragginMouse.set(e.getX(), e.getY());
        drag = e.getButton() == MouseEvent.BUTTON3;

        Vec2 pos = new Vec2(e.getX(), e.getY());
        setMouse(pos);
        dbgDraw.getScreenToWorldToOut(new Vec2(e.getX(), e.getY()), pos);
//        LOGGER.debug("Screen: ("+e.getX()+", "+e.getY()+") World: " + pos);
        mouseWorld.set(pos);
//        mouseWorld.set(new Vec2(e.getX(), e.getY()));
        mousePressed = true;
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        mousePressed = false;
      }
    });

    addMouseMotionListener(new MouseMotionAdapter() {
      @Override
      public void mouseDragged(MouseEvent e) {
        setMouse(new Vec2(e.getX(), e.getY()));

        if (!drag) {
          return;
        }

        Vec2 diff = new Vec2(e.getX(), e.getY());
        diff.subLocal(dragginMouse);
        dbgDraw.getViewportTranform().getScreenVectorToWorld(diff, diff);
        dbgDraw.getViewportTranform().getCenter().subLocal(diff);

        dragginMouse.set(e.getX(), e.getY());
      }
    });
  }

  public void update() {
//    clearScreen();
////    drawBorders();    
//    drawFramerate();
////    drawMyGraphics();
//    if (!Globals.simulation.simPaused || Globals.simulation.singleStep) {
//      Globals.simulation.step();
//      Globals.simulation.singleStep = false;
//    }
//    Globals.simulation.drawSimulation();
  }

  public Vec2 getMouse() {
    return mouse;
  }

  public void setMouse(Vec2 argMouse) {
    mouse.set(argMouse);
  }

  public Vec2 getWorldMouse() {
    return mouseWorld;
  }

  public double getCachedCameraScale() {
    return cachedCameraScale;
  }

  public void setCachedCameraScale(double cachedCameraScale) {
    this.cachedCameraScale = cachedCameraScale;
  }

  public Vec2 getCachedCameraPos() {
    return cachedCameraPos;
  }

  public void setCachedCameraPos(Vec2 argPos) {
    cachedCameraPos.set(argPos);
  }

  public boolean isHasCachedCamera() {
    return hasCachedCamera;
  }

  public void setHasCachedCamera(boolean hasCachedCamera) {
    this.hasCachedCamera = hasCachedCamera;
  }

  public void setFrameRate(double frameRate) {
    this.frameRate = frameRate;
  }

}
