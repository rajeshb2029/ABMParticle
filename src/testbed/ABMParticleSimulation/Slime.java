package testbed.ABMParticleSimulation;

import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rajesh Balagam
 */
public class Slime {

  private static final Logger LOGGER = LoggerFactory.getLogger(Slime.class);
  public long slimeID;

  public Vec2 pos; // grid position numbers(x,y)
  public double volume;
  public double orientation;

  Slime(Vec2 pos, double angle, long ID) {
    slimeID = ID;
    this.pos = pos;
    this.volume = 0f;
    this.orientation = angle;
//    LOGGER.debug("Slime box is created at :" + pos + "ID: "+slimeID);
  } // end constructor       

  public void updateVolume() {
    volume -= Parameters.slimeDegradeRateConstant * Parameters.timeStep * volume;
  } // end method updatetime

//    public void clearSlime() {
//        volume = 0f;
//    }

  public double getOrientation() {
    double angle;

    angle = this.orientation % (2 * Math.PI);

    if (angle > 0) {
      return angle;
    } else {
      return (angle + 2 * Math.PI);
    }
  } // end method getOrientation

  public double getNematicOrientation() {
    double angle;

    angle = this.orientation % Math.PI;

    if (angle > 0) {
      return angle;
    } else {
      return (angle + MathUtils.PI);
    }
  } // end method getNematicOrientation

} //end class Slime
