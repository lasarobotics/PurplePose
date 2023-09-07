import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.geometry.Translation3d;

/**
 * The Constants class provides a convenient place to hold
 * numerical or boolean
 * constants. This class should not be used for any other purpose. All constants
 * should be declared
 * globally (i.e. public static). Do not put anything functional in this class.
 *
 * <p>
 * It is advised to statically import this class (or one of its inner classes)
 * wherever the
 * constants are needed, to reduce verbosity.
 */
public final class Constants {
  public static class Global {
    public static final int ROBOT_LOOP_HZ = 50;
    public static final double ROBOT_LOOP_PERIOD = 1.0 / ROBOT_LOOP_HZ;
  }
  
  public static class Field {
    public static final double FIELD_WIDTH = 8.1026;
    public static final double FIELD_LENGTH = 16.4846;
  }

  public static class VisionHardware {
    public static final String CAMERA_0_NAME = "camera0";
    public static final Transform3d CAMERA_0_LOCATION = new Transform3d(
      new Translation3d(0.0, 0.0, 0.0),
      new Rotation3d(0.0, 0.0, 0.0)
    );
  }
}