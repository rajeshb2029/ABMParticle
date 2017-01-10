package testbed.ABMParticleSimulation;

import org.apache.commons.math3.util.FastMath;
import org.jbox2d.callbacks.DebugDraw;
import org.jbox2d.collision.AABB;
import org.jbox2d.common.*;
import org.jbox2d.pooling.arrays.IntArray;
import org.jbox2d.pooling.arrays.Vec2Array;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import testbed.utils.MyVec2;

import java.awt.*;

/**
 * @author Rajesh Balagam
 */
public class DebugDrawJ2D extends DebugDraw {

  private static final Logger LOGGER = LoggerFactory.getLogger(DebugDrawJ2D.class);

  private static int circlePoints = 16;

  private final Vec2Array vec2Array = new Vec2Array();
  private final static IntArray xIntsPool = new IntArray();
  private final static IntArray yIntsPool = new IntArray();
  private final Vec2 saxis = new Vec2();
  private final Vec2 temp = new Vec2();
  private final Vec2 temp2 = new Vec2();

  private final Graphics2D g2d;

  private final Vec2 sp1 = new Vec2();
  private final Vec2 sp2 = new Vec2();

  public DebugDrawJ2D(Graphics2D argG2d, int width, int height) {
    super(new OBBViewportTransform());
    viewportTransform.setYFlip(true);
    updateSize(width, height);
//    setCamera(new Vec2(width/2, height/2), 1f);    
    setCamera(new Vec2(Globals.cameraPosX, Globals.cameraPosY), Globals.cameraScale);
    g2d = argG2d;
  }

  private void updateSize(int argWidth, int argHeight) {
    getViewportTranform().setExtents(argWidth / 2, argHeight / 2);
  }

  public final void setCamera(Vec2 argPos, double scale) {
    getViewportTranform().setCamera(argPos.x, argPos.y, (float) scale);
  }

  @Override
  public void drawPoint(Vec2 center, float strokeWidth, Color3f color) {
    getWorldToScreenToOut(center, sp1);
    temp.set(center.add(new Vec2(strokeWidth, 0)));
    getWorldToScreenToOut(temp, sp2);
    float sStrokeW = sp2.sub(sp1).length();
    g2d.setColor(new Color(color.x, color.y, color.z));
    g2d.setStroke(new BasicStroke(sStrokeW, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    g2d.drawLine((int) sp1.x, (int) sp1.y, (int) sp1.x, (int) sp1.y);
    g2d.setStroke(new BasicStroke());
//    sp1.x -= radius;
//    sp1.y -= radius;
//    g2d.fillOval((int) sp1.x, (int) sp1.y, (int) radius * 2, (int) radius * 2);
  }

  @Override
  public void drawSolidPolygon(Vec2[] vertices, int vertexCount, Color3f color) {
    // inside    
    int[] xInts = xIntsPool.get(vertexCount);
    int[] yInts = yIntsPool.get(vertexCount);

    for (int i = 0; i < vertexCount; i++) {
      getWorldToScreenToOut(vertices[i], temp);
      xInts[i] = (int) temp.x;
      yInts[i] = (int) temp.y;
//      LOGGER.debug("x: "+xInts[i]+" y: "+yInts[i]);
    }

    Color c = new Color(color.x, color.y, color.z, .4f);
    g2d.setColor(c);
    g2d.fillPolygon(xInts, yInts, vertexCount);

    // outside
    drawPolygon(vertices, vertexCount, color);
  }

  @Override
  public void drawCircle(Vec2 center, float radius, Color3f color) {
//    Vec2[] vec2 = vec2Array.get(circlePoints);
//    generateCircle(center, radius, vec2, circlePoints);
//    drawPolygon(vec2, circlePoints, color);

    // drawing circle from java graphics 
    // good for the case when only portion of total simulation space visualized
    getWorldToScreenToOut(center, sp1);
    temp.set(center.add(new Vec2(radius, 0)));
    getWorldToScreenToOut(temp, sp2);
    double sradius = sp2.sub(sp1).length();
    g2d.setColor(new Color(color.x, color.y, color.z, 1.0f));
    g2d.drawOval((int) (sp1.x - sradius), (int) (sp1.y - sradius), (int) (2 * sradius), (int) (2 * sradius));
  }

  @Override
  public void drawSolidCircle(Vec2 center, float radius, Vec2 axis, Color3f color) {
//    Vec2[] vecs = vec2Array.get(circlePoints);
//    generateCircle(center, radius, vecs, circlePoints);
//    drawSolidPolygon(vecs, circlePoints, color);
//    if (axis != null) {
//      saxis.set(axis).mulLocal(radius).addLocal(center);
//      drawSegment(center, saxis, color);
//    }

    // drawing circle from java graphics 
    // good for the case when only portion of total simulation space visualized
    getWorldToScreenToOut(center, sp1);
    temp.set(center.add(new Vec2(radius, 0)));
    getWorldToScreenToOut(temp, sp2);
    double sradius = sp2.sub(sp1).length();
    g2d.setColor(new Color(color.x, color.y, color.z, 0.4f));
    g2d.fillOval((int) (sp1.x - sradius), (int) (sp1.y - sradius), (int) (2 * sradius), (int) (2 * sradius));

    g2d.setColor(new Color(color.x, color.y, color.z, 1.0f));
    g2d.drawOval((int) (sp1.x - sradius), (int) (sp1.y - sradius), (int) (2 * sradius), (int) (2 * sradius));
    if (axis != null) {
      sp2.set(sp1.add(axis.mul((float) sradius)));
      g2d.drawLine((int) sp1.x, (int) sp1.y, (int) sp2.x, (int) sp2.y);
    }
  }

  public void drawSmoothCircle(Vec2 center, float radius, Color3f color) {
    int localCirclePoints = 60;
    Vec2[] vec2 = vec2Array.get(localCirclePoints);
    generateCircle(center, radius, vec2, localCirclePoints);
    drawPolygon(vec2, localCirclePoints, color);
  }

  @Override
  public void drawSegment(Vec2 p1, Vec2 p2, Color3f color) {
    getWorldToScreenToOut(p1, sp1);
    getWorldToScreenToOut(p2, sp2);

    g2d.setColor(new Color(color.x, color.y, color.z));
//    g2d.setStroke(new BasicStroke(2));
    g2d.drawLine((int) sp1.x, (int) sp1.y, (int) sp2.x, (int) sp2.y);
//    g2d.setStroke(new BasicStroke(1));
  }

  public void drawVector(Vec2 origin, Vec2 dir, float length, Color color) {
    Vec2 side1, side2;
    Vec2 originPos, finalPos, sidePos1, sidePos2;

    if (color == null) {
      color = new Color(1.0f, 1.0f, 0.0f);
    }

    Vec2 target = MyVec2.unitVector(dir);
    side1 = MyVec2.rotate(MyVec2.negative(target), MathUtils.QUARTER_PI);
    side2 = MyVec2.rotate(MyVec2.negative(target), -MathUtils.QUARTER_PI);

    side1.mulLocal(length * 0.3f);
    side2.mulLocal(length * 0.3f);
    target.mulLocal(length);

    originPos = origin;

    finalPos = target;
    finalPos.addLocal(originPos);

    sidePos1 = side1;
    sidePos1.addLocal(finalPos);

    sidePos2 = side2;
    sidePos2.addLocal(finalPos);

    drawSegment(originPos, finalPos, getColor3f(color));
    drawSegment(finalPos, sidePos1, getColor3f(color));
    drawSegment(finalPos, sidePos2, getColor3f(color));
  }

  public void drawDoubleArrow(Vec2 origin, Vec2 orientation, float length, Color color) {
    Vec2 side1, side2;
    Vec2 originPos, finalPos, sidePos1, sidePos2;

    if (color == null) {
      color = new Color(1.0f, 1.0f, 0.0f);
    }

    Vec2 target = MyVec2.unitVector(orientation);

    side1 = MyVec2.rotate(MyVec2.negative(target), MathUtils.QUARTER_PI);
    side2 = MyVec2.rotate(MyVec2.negative(target), -MathUtils.QUARTER_PI);

    side1.mulLocal(length * 0.6f);
    side2.mulLocal(length * 0.6f);
    target.mulLocal(length);

    originPos = origin;

    finalPos = target;
    finalPos.addLocal(originPos);

    sidePos1 = side1;
    sidePos1.addLocal(finalPos);

    sidePos2 = side2;
    sidePos2.addLocal(finalPos);

    drawSegment(originPos, finalPos, getColor3f(color));
    drawSegment(finalPos, sidePos1, getColor3f(color));
    drawSegment(finalPos, sidePos2, getColor3f(color));

    // negative arrow
    target = MyVec2.unitVector(orientation);
    target = MyVec2.negative(target);

    side1 = MyVec2.rotate(MyVec2.negative(target), MathUtils.QUARTER_PI);
    side2 = MyVec2.rotate(MyVec2.negative(target), -MathUtils.QUARTER_PI);

    side1.mulLocal(length * 0.6f);
    side2.mulLocal(length * 0.6f);
    target.mulLocal(length);

    originPos = origin;

    finalPos = target;
    finalPos.addLocal(originPos);

    sidePos1 = side1;
    sidePos1.addLocal(finalPos);

    sidePos2 = side2;
    sidePos2.addLocal(finalPos);

    drawSegment(originPos, finalPos, getColor3f(color));
    drawSegment(finalPos, sidePos1, getColor3f(color));
    drawSegment(finalPos, sidePos2, getColor3f(color));
  }

  @Override
  public void drawString(float x, float y, String s, Color3f color) {
    getWorldToScreenToOut(new Vec2(x, y), sp1);
    g2d.setColor(new Color(color.x, color.y, color.z));
    g2d.drawString(s, (int) sp1.x, (int) sp1.y);
  }

  public void drawAABB(AABB argAABB, Color3f color) {
    Vec2 vecs[] = vec2Array.get(4);
    argAABB.getVertices(vecs);
    drawPolygon(vecs, 4, color);
  }

  @Override
  public void drawTransform(Transform xf) {
    getWorldToScreenToOut(xf.p, temp);
    temp2.setZero();
    double k_axisScale = 0.4f;

    Color c = new Color(1, 0, 0);
    g2d.setColor(c);

    temp2.x = (float) (xf.p.x + k_axisScale * xf.q.c);
    temp2.y = (float) (xf.p.y + k_axisScale * xf.q.s);
    getWorldToScreenToOut(temp2, temp2);
    g2d.drawLine((int) temp.x, (int) temp.y, (int) temp2.x, (int) temp2.y);

    c = new Color(0, 1, 0);
    g2d.setColor(c);
    temp2.x = (float) (xf.p.x + k_axisScale * xf.q.c);
    temp2.y = (float) (xf.p.y + k_axisScale * xf.q.s);
    getWorldToScreenToOut(temp2, temp2);
    g2d.drawLine((int) temp.x, (int) temp.y, (int) temp2.x, (int) temp2.y);
  }

  public void drawFilledRectangle(Vec2 center, float width, float height, Color3f color) {
    Vec2[] vecs = new Vec2[4];
    vecs[0] = new Vec2(center.x - width / 2, center.y - height / 2);
    vecs[1] = new Vec2(center.x + width / 2, center.y - height / 2);
    vecs[2] = new Vec2(center.x + width / 2, center.y + height / 2);
    vecs[3] = new Vec2(center.x - width / 2, center.y + height / 2);

    drawSolidPolygon(vecs, 4, color);
//    LOGGER.debug("draw Rectangle at: " + pos);
//    LOGGER.debug("vertices: "+vecs[0]+" "+vecs[1]+" "+vecs[2]+" "+vecs[3]);
  }

  public void drawRectangle(Vec2 center, float width, float height, Color3f color) {
    Vec2[] vecs = new Vec2[4];
    vecs[0] = new Vec2(center.x - width / 2, center.y - height / 2);
    vecs[1] = new Vec2(center.x + width / 2, center.y - height / 2);
    vecs[2] = new Vec2(center.x + width / 2, center.y + height / 2);
    vecs[3] = new Vec2(center.x - width / 2, center.y + height / 2);

    drawPolygon(vecs, 4, color);
//    LOGGER.debug("draw Rectangle at: " + pos);
//    LOGGER.debug("vertices: "+vecs[0]+" "+vecs[1]+" "+vecs[2]+" "+vecs[3]);
  }

  // CIRCLE GENERATOR
  private void generateCircle(Vec2 argCenter, double argRadius,
                              Vec2[] argPoints, int argNumPoints) {
    double inc = MathUtils.TWOPI / argNumPoints;

    for (int i = 0; i < argNumPoints; i++) {
      argPoints[i].x = (float) (argCenter.x + FastMath.cos(i * inc) * argRadius);
      argPoints[i].y = (float) (argCenter.y + FastMath.sin(i * inc) * argRadius);
    }
  }

  public static final Color3f getColor3f(Color c) {
    return new Color3f(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f);
  }

  public Graphics2D getGraphics() {
    return g2d;
  }

}
