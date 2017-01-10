package testbed.utils;

import org.apache.commons.math3.util.FastMath;
import org.jbox2d.common.Color3f;
import org.jbox2d.common.Vec2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import testbed.ABMParticleSimulation.Globals;

/**
 * Created by Rajesh Balagam on 1/17/14.
 */
public class MyRectangle {

  private static final Logger LOGGER = LoggerFactory.getLogger(MyRectangle.class);

  public int ID = 0;
  public double height = 1.0f;
  public double width = 1.0f;
  public double orientation = 0f;

  public Vec2 pos;

  /**
   * @param pos
   * @param width
   * @param height
   * @param orientation
   */
  public MyRectangle(Vec2 pos, double width, double height, double orientation) {
    this.width = width;
    this.height = height;
    this.pos = new Vec2(pos.x, pos.y);
    this.orientation = orientation;
  }

  public MyRectangle(double x, double y, double width, double height, double orientation) {
    this.width = width;
    this.height = height;
    this.pos = new Vec2((float) x, (float) y);
    this.orientation = orientation;
  }

  public MyRectangle(int ID, Vec2 pos, double width, double height, double orientation) {
    this.ID = ID;
    this.height = height;
    this.width = width;
    this.pos = new Vec2(pos.x, pos.y);
    this.orientation = orientation;
  }

  public double getWidth() {
    return width;
  }

  public double getHeight() {
    return height;
  }

  public int contains(MyRectangle childRectangle) {
    int quadrant = -2; //lies outside

    MyRectangle translatedParentRectangle = new MyRectangle(this.pos.x, this.pos.y, this.getWidth(), this.getHeight(), this.orientation);
    translatedParentRectangle.translateBLCornerToOrigin();

    double xTranslate = translatedParentRectangle.pos.x - this.pos.x;
    double yTranslate = translatedParentRectangle.pos.y - this.pos.y;
//        System.out.println("parentPos:" + this.pos +
//                " translatedParentPos: " + translatedParentRectangle.pos +
//                " xTranslate: " + xTranslate + " yTranslate: " + yTranslate);
//        Vec2[] vecs = translatedParentRectangle.getCorners();
//        System.out.println(vecs[0] + " " + vecs[1] + " " + vecs[2] + " " + vecs[3]);

    MyRectangle translatedChildRectangle = new MyRectangle(childRectangle.pos.x, childRectangle.pos.y,
            childRectangle.getWidth(), childRectangle.getHeight(), childRectangle.orientation);
    translatedChildRectangle.pos.addLocal((float) xTranslate, (float) yTranslate);

    Vec2[] corners = translatedChildRectangle.getCorners();
    for (int i = 0; i < 4; i++) {
      if (corners[i].x < 0 || corners[i].y < 0
              || corners[i].x > this.getWidth()
              || corners[i].y > this.getHeight()) {
        quadrant = -2;
        return quadrant;
      }
    }

    quadrant = -1; //lies within parent
    translatedParentRectangle = new MyRectangle(this.pos.x, this.pos.y, this.getWidth(), this.getHeight(), this.orientation);
    translatedParentRectangle.pos = new Vec2(0, 0);
    xTranslate = translatedParentRectangle.pos.x - this.pos.x;
    yTranslate = translatedParentRectangle.pos.y - this.pos.y;

    childRectangle.pos.addLocal((float) xTranslate, (float) yTranslate);
    Vec2 TLCorner = childRectangle.pos.add(new Vec2((float) -childRectangle.getWidth() / 2, (float) childRectangle.getHeight() / 2));
    Vec2 BLCorner = childRectangle.pos.add(new Vec2((float) -childRectangle.getWidth() / 2, (float) -childRectangle.getHeight() / 2));
    Vec2 BRCorner = childRectangle.pos.add(new Vec2((float) childRectangle.getWidth() / 2, (float) -childRectangle.getHeight() / 2));
    Vec2 TRCorner = childRectangle.pos.add(new Vec2((float) childRectangle.getWidth() / 2, (float) childRectangle.getHeight() / 2));

    if (childRectangle.pos.x > 0 && childRectangle.pos.y > 0 && BLCorner.x > 0 && BLCorner.y > 0) {
      quadrant = 0;
    }
    if (childRectangle.pos.x < 0 && childRectangle.pos.y > 0 && BRCorner.x < 0 && BRCorner.y > 0) {
      quadrant = 1;
    }
    if (childRectangle.pos.x < 0 && childRectangle.pos.y < 0 && TRCorner.x < 0 && TRCorner.y < 0) {
      quadrant = 2;
    }
    if (childRectangle.pos.x > 0 && childRectangle.pos.y < 0 && TLCorner.x > 0 && TLCorner.y < 0) {
      quadrant = 3;
    }

    return quadrant;
  }

  public Vec2 translateBLCornerToOrigin() {
//    Vec2 BLCorner =  this.pos.add(new Vec2(-this.getWidth()/2, -this.getHeight()/2));
    Vec2[] corners = getOrientedCorners();
    Vec2 BLCorner = corners[2];

    double xTranslate, yTranslate;
    if (BLCorner.x < 0) {
      xTranslate = Math.abs(BLCorner.x);
    } else {
      xTranslate = -BLCorner.x;
    }
    if (BLCorner.y < 0) {
      yTranslate = Math.abs(BLCorner.y);
    } else {
      yTranslate = -BLCorner.y;
    }

    this.pos.addLocal((float) xTranslate, (float) yTranslate);

//    corners = getOrientedCorners();
//    System.out.println(corners[2]);
    return new Vec2((float) xTranslate, (float) yTranslate);
  }

  public Vec2[] getCorners() {
    Vec2[] vecs = new Vec2[4];
    vecs[0] = this.pos.add(new Vec2((float) this.getWidth() / 2, (float) this.getHeight() / 2)); // TOP-RIGHT
    vecs[1] = this.pos.add(new Vec2((float) -this.getWidth() / 2, (float) this.getHeight() / 2));  // TOP-LEFT
    vecs[2] = this.pos.add(new Vec2((float) -this.getWidth() / 2, (float) -this.getHeight() / 2)); // BOTTOM-LEFT
    vecs[3] = this.pos.add(new Vec2((float) this.getWidth() / 2, (float) -this.getHeight() / 2));  // BOTTOM-RIGHT

    return vecs;
  }

  public Vec2[] getOrientedCorners() {
    Vec2 pos1, pos2;
    pos1 = MyVec2.pointAtDistance(pos, (float) orientation, (float) width / 2);
    pos2 = MyVec2.pointAtDistance(pos, (float) orientation, (float) -width / 2);
    Vec2[] vecs = new Vec2[4];
    vecs[0] = MyVec2.pointAtDistance(pos1, (float) (orientation + Math.PI / 2), (float) height / 2);
    vecs[1] = MyVec2.pointAtDistance(pos2, (float) (orientation + Math.PI / 2), (float) height / 2);
    vecs[2] = MyVec2.pointAtDistance(pos2, (float) (orientation - Math.PI / 2), (float) height / 2);
    vecs[3] = MyVec2.pointAtDistance(pos1, (float) (orientation - Math.PI / 2), (float) height / 2);

    return vecs;
  }

  public float projectionLength(Vec2 projectionDir) {
    Vec2[] corners = getOrientedCorners();

//    float len1 = Math.abs(Vec2.dot(MyVec2.unitVector(corners[0], corners[2]), projectionDir));
//    float len2 = Math.abs(Vec2.dot(MyVec2.unitVector(corners[1], corners[3]), projectionDir));
    float len1 = Math.abs(Vec2.dot(corners[0].sub(corners[2]), projectionDir));
    float len2 = Math.abs(Vec2.dot(corners[1].sub(corners[3]), projectionDir));
//    float len1 = Math.abs(Vec2.dot(corners[0], projectionDir)) + Math.abs(Vec2.dot(corners[2], projectionDir));
//    float len2 = Math.abs(Vec2.dot(corners[1], projectionDir)) + Math.abs(Vec2.dot(corners[3], projectionDir));
    return Math.max(len1, len2);
  }

  public Vec2[] getNormals() {
    Vec2[] normals = new Vec2[2];
    normals[0] = MyVec2.unitVector((float) orientation);
    normals[1] = MyVec2.unitVector((float) (orientation + Math.PI / 2));

    return normals;
  }

  //    public boolean contains(Vec2 point) {
//        MyRectangle translatedParentRectangle = new MyRectangle(this.pos.x, this.pos.y, this.getWidth(), this.getHeight(), this.orientation);
//        Vec2 translateAmount = translatedParentRectangle.translateBLCornerToOrigin();
//
////        System.out.println(translatedParentRectangle.getCorners()[2]);
//
////        float xTranslate = translatedParentRectangle.pos.x - this.pos.x;
////        float yTranslate = translatedParentRectangle.pos.y - this.pos.y;
//
//        point = MyVec2.rotateAroundOrigin(this.getCorners()[2], point, -orientation);
//        point.addLocal(translateAmount);
////        System.out.println("point before and after rotate and translate: " + point);
//        return (point.x > 0 && point.y > 0 && point.x < width/2 && point.y < height/2);
//    }
  public boolean contains(Vec2 point) {

    Vec2 pointToCenterVector = point.sub(pos);
    float distance = pointToCenterVector.length();
    float currentPointAngle = (float) Math.atan2(pointToCenterVector.y, pointToCenterVector.x);
    // angle of point rotated by the rectangle amount around the centre of rectangle.
    double newAngle = currentPointAngle - orientation;
    // x2 and y2 are the new positions of the point when rotated to offset the rectangles orientation.
    double x2 = FastMath.cos(newAngle) * distance;
    double y2 = FastMath.sin(newAngle) * distance;

    if (x2 > -0.5 * width && x2 < 0.5 * width && y2 > -0.5 * height && y2 < 0.5 * height) {
      return true;
    }

    return false;
  }

  public void drawRectangle() {
    drawRectangle(getOrientedCorners(), Color3f.GREEN);
  }

  public void drawRectangle(Vec2[] vecs, Color3f color) {
    Globals.dbgDraw.drawSegment(vecs[0], vecs[1], color);
    Globals.dbgDraw.drawSegment(vecs[1], vecs[2], color);
    Globals.dbgDraw.drawSegment(vecs[2], vecs[3], color);
    Globals.dbgDraw.drawSegment(vecs[3], vecs[0], color);
  }
}
