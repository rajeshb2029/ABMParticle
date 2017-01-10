package testbed.ABMParticleSimulation;

import org.jbox2d.common.Vec2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import testbed.utils.Gradient;
import testbed.utils.MyVec2;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Set;

/**
 * @author Rajesh Balagam
 */
public class SlimeGrid {

  private static final Logger LOGGER = LoggerFactory.getLogger(SlimeGrid.class);

  public int gridNumX, gridNumY;
  public double gridTotalWidth, gridTotalHeight;
  public double gridWidth;
  public double gridAreaDensity;
  public double slimeField[][];
  final private int MaxColors = 100;
  private Color[] gradientColors;
  private boolean writeGridNumFlag = true;

  public SlimeGrid(double gridWidth) {
    this.gridWidth = gridWidth;
    gridTotalWidth = Parameters.worldWidth + 1;
    gridTotalHeight = Parameters.worldHeight + 1;

    gridNumX = (int) (gridTotalWidth / gridWidth) + 1;
    gridNumY = (int) (gridTotalHeight / gridWidth) + 1;
    gridAreaDensity = (gridNumX * gridNumY) / (gridTotalWidth * gridTotalHeight);

    slimeField = new double[gridNumX][gridNumY];
    clearSlimeField();

    gradientColors = new Color[MaxColors];
    for (int i = 0; i < MaxColors; i++) {
      gradientColors[i] = Gradient.GRADIENT_JETMAP[i * 5];
    }
  }

  public final void clearSlimeField() {
    for (int i = 0; i < gridNumX; i++) {
      for (int j = 0; j < gridNumY; j++) {
        slimeField[i][j] = 0;
      }
    }
  }

  public Vec2 getGridPos(Vec2 pos) {
    Vec2 gridPos = new Vec2();
    gridPos.x = (int) ((pos.x) / gridWidth);
    gridPos.y = (int) ((pos.y) / gridWidth);
    return gridPos;
  }

  public long getGridUniqueID(Vec2 gridPos) {
    // using Cantor pairing function to generate unique ID
    long x = (long) gridPos.x;
    long y = (long) gridPos.y;

    double z = ((x + y) * (x + y + 1)) / 2 + y;

    return (long) z;
  }

  public Vec2 getGridPosFromUniqueID(long uniqueID) {
    double w, t, x, y, z;

    // using inverse Cantor pairing function 
    z = uniqueID;
    w = (int) Math.abs((Math.sqrt(8 * z + 1) - 1) / 2);
    t = (w + w * w) / 2;

    y = z - t;
    x = w - y;

    Vec2 gridPos = new Vec2((int) x, (int) y);

    return gridPos;
  }

  public void depositSlime(Vec2 gridPos, double angle) {
    long uniqueID = getGridUniqueID(gridPos);

    Slime s;
    if (Globals.simulation.slimeTrailArray.get(uniqueID) == null) {
      s = new Slime(gridPos, angle, uniqueID);
      Globals.simulation.slimeTrailArray.put(uniqueID, s);
    } else {
      s = Globals.simulation.slimeTrailArray.get(uniqueID);
    }
    s.volume += Parameters.slimeProduceRate * Parameters.timeStep;
    s.orientation = angle;
  }

  public Vec2 getPositionFromGridNum(Vec2 gridPos) {
    return new Vec2((float) (gridPos.x * gridWidth + gridWidth / 2),
            (float) (gridPos.y * gridWidth + gridWidth / 2));
  }

  public void drawSlimeGrid() {
    Vec2 pos1, pos2;
    // horizontal lines
    for (int i = 1; i < gridNumX - 1; i++) {
      pos1 = new Vec2(i * (float) gridWidth, 0);
      pos2 = new Vec2(i * (float) gridWidth, (float) gridTotalHeight);

      Globals.dbgDraw.drawSegment(pos1, pos2, DebugDrawJ2D.getColor3f(Color.pink));
      if (Parameters.drawSlimeGridNum && i % 5 == 0) {
        Globals.dbgDraw.drawString(pos1.x, pos1.y + 15f,
                Integer.toString(i), DebugDrawJ2D.getColor3f(Color.pink));
      }
    }

    // vertical lines
    for (int j = 1; j < gridNumY - 1; j++) {
      pos1 = new Vec2(0, j * (float) gridWidth);
      pos2 = new Vec2((float) gridTotalWidth, j * (float) gridWidth);
      Globals.dbgDraw.drawSegment(pos1, pos2, DebugDrawJ2D.getColor3f(Color.pink));
      if (Parameters.drawSlimeGridNum && j % 5 == 0) {
        Globals.dbgDraw.drawString(pos1.x + 15f, pos1.y,
                Integer.toString(j), DebugDrawJ2D.getColor3f(Color.pink));
      }
    }
  } // end method drawSlimeGrid

  public void drawSlime() {
    Vec2 pos;
    int val;
    double maxVal = (Parameters.slimeProduceRate / Parameters.slimeDegradeRateConstant) * 20;
    Color color;
    Set<Long> keySet = Globals.simulation.slimeTrailArray.keySet();
    ArrayList<Long> keys = new ArrayList<>();
    keys.addAll(keySet);
//    keys.addAll(Simulation.slimeTrailArray.keys());

    for (long uniqueID : keys) {
      Slime s = Globals.simulation.slimeTrailArray.get(uniqueID);
      if (s != null) {
        pos = getPositionFromGridNum(new Vec2(s.pos.x, s.pos.y));
        val = (int) (s.volume * MaxColors / maxVal) + 1;
        if (val >= 100f) {
          val = 100 - 1;
        }
        if (val > 0) {
          color = gradientColors[val];
//        color = new Color(255, 0, 0);          
          Globals.dbgDraw.drawFilledRectangle(pos, (float) gridWidth, (float) gridWidth, DebugDrawJ2D.getColor3f(color));
        }
      }
    }

  } // end method drawSlime

  public void drawSlimeOutline() {
    Vec2 pos, pos1, pos2;
    Color color;
    int val;

    double maxVal = (Parameters.slimeProduceRate / Parameters.slimeDegradeRateConstant) * 20;

    Set<Long> keySet = Globals.simulation.slimeTrailArray.keySet();
    ArrayList<Long> keys = new ArrayList<>();
    keys.addAll(keySet);

    for (long uniqueID : keys) {
      Slime s = Globals.simulation.slimeTrailArray.get(uniqueID);
      if (s != null) {
        pos = getPositionFromGridNum(new Vec2(s.pos.x, s.pos.y));
        pos1 = MyVec2.pointAtDistance(pos, (float) s.orientation, (float) -Parameters.slimeGridWidth / 2);
        pos2 = MyVec2.pointAtDistance(pos, (float) s.orientation, (float) Parameters.slimeGridWidth / 2);

        val = (int) (s.volume * MaxColors / maxVal) + 1;
        if (val >= 100f) {
          val = 100 - 1;
        }
        if (val > 0) {
          color = gradientColors[val];
//        color = new Color(255, 0, 0);          
          Globals.dbgDraw.drawSegment(pos1, pos2, DebugDrawJ2D.getColor3f(color));
        }
      }
    }
  } // end method drawSlimeOutline

  public void drawSlimeDirection() {
    Set<Long> keySet = Globals.simulation.slimeTrailArray.keySet();
    ArrayList<Long> keys = new ArrayList<>();
    keys.addAll(keySet);

    for (long uniqueID : keys) {
      Slime s = Globals.simulation.slimeTrailArray.get(uniqueID);
      if (s != null) {
        Globals.dbgDraw.drawDoubleArrow(getPositionFromGridNum(s.pos),
                MyVec2.unitVector((float) s.getOrientation()), 0.22f, Color.WHITE);
      }
    }
  } // end method drawSlimeDirection

  public void applySlimeAging() {
    ArrayList<Long> slimeClearList = new ArrayList<>();
    for (long uniqueID : Globals.simulation.slimeTrailArray.keySet()) {
      Slime s = Globals.simulation.slimeTrailArray.get(uniqueID);
      if (s.volume <= 0.01f) {
        slimeClearList.add(uniqueID);
      } else {
        s.updateVolume();
      }
    }

    for (long slimeID : slimeClearList) {
      Globals.simulation.slimeTrailArray.remove(slimeID);
    }
//    LOGGER.debug("Number of slime grid occupied: " + Parameters.slimeTrailArray.size());

    slimeClearList.clear();
  } // end method applySlimeAging

  public void updateSlimeField() {
//    Vec2 gridPos;        
    Slime s = null;
    try {
      for (long uniqueID : Globals.simulation.slimeTrailArray.keySet()) {
        s = Globals.simulation.slimeTrailArray.get(uniqueID);
        slimeField[(int) s.pos.x][(int) s.pos.y] = s.volume;
      }
    } catch (ArrayIndexOutOfBoundsException ex) {
      ex.printStackTrace(System.err);
      LOGGER.error("slime pos: " + s.pos, ex);
    }
  }

  public void writeSlimeGridPositions() {
    String filename = "./" + Parameters.dataDir + "/SlimeGridPositions.txt";
    File file = new File(filename);
    try { //if file doesnt exists, then create it
      if (!file.exists()) {
        file.createNewFile();
//        LOGGER.debug(file.getName());
      }

      FileWriter fw = new FileWriter(filename, true); //true = append file
      PrintWriter pw = new PrintWriter(fw);

      if (writeGridNumFlag) {
        pw.println(gridNumX + " " + gridNumY);
        writeGridNumFlag = false;
      }

      pw.print(Globals.currentTime + " ");
      for (int i = 0; i < gridNumX; i++) {
        for (int j = 0; j < gridNumY; j++) {
          pw.print(slimeField[i][j]);
          pw.print(" ");
        }
      }
      pw.println();

      pw.close();
      fw.close();
    } catch (IOException ex) {
      ex.printStackTrace(System.err);
    }
  } // end method writeSlimeGridPositions
}
