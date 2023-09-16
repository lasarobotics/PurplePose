import java.io.IOException;

import org.photonvision.EstimatedRobotPose;

import edu.wpi.first.cscore.CameraServerJNI;
import edu.wpi.first.math.WPIMathJNI;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.networktables.DoubleArrayPublisher;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTablesJNI;
import edu.wpi.first.util.CombinedRuntimeLoader;
import edu.wpi.first.util.WPIUtilJNI;

/**
 * Main Class
 */
public class PurpleVision {
  VisionSystem visionSystem = new VisionSystem(VisionSystem.initializeHardware());

  private final String SERVER_NAME = "localhost";
  private final String CLIENT_NAME = "PurpleVision";
  private final String POSE_TOPIC = "Pose";

  private void run() {
    NetworkTableInstance inst = NetworkTableInstance.getDefault();
    NetworkTable table = inst.getTable("datatable");
    DoubleArrayPublisher posePublisher = table.getDoubleArrayTopic(POSE_TOPIC).publish();;

    inst.startClient4(CLIENT_NAME);
    inst.setServer(SERVER_NAME); // where TEAM=190, 294, etc, or use inst.setServer("hostname") or similar

    while (true) {
      try {
        Thread.sleep((int)(Constants.Global.ROBOT_LOOP_PERIOD * 100000)); // 20ms
      } catch (InterruptedException ex) {
        System.out.println("Interrupted");
        break;
      }
      
      EstimatedRobotPose currentEstimatedPose = visionSystem.getAverageEstimatedGlobalPose();
      Pose3d currentPose = currentEstimatedPose.estimatedPose;
      double[] poseArray = { 
        currentPose.getX(),
        currentPose.getY(),
        currentPose.getZ(),
        currentPose.getRotation().toRotation2d().getDegrees(),
        currentEstimatedPose.timestampSeconds
      };
      posePublisher.set(poseArray);
    }

    posePublisher.close();
  }

  public static void main(String... args) throws IOException {
    NetworkTablesJNI.Helper.setExtractOnStaticLoad(false);
    WPIUtilJNI.Helper.setExtractOnStaticLoad(false);
    WPIMathJNI.Helper.setExtractOnStaticLoad(false);
    CameraServerJNI.Helper.setExtractOnStaticLoad(false);

    CombinedRuntimeLoader.loadLibraries(PurpleVision.class, "wpiutiljni", "wpimathjni", "ntcorejni",
        "cscorejnicvstatic");
    new PurpleVision().run();
  }
}
