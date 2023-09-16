import java.util.ArrayList;
import java.util.List;

import org.photonvision.EstimatedRobotPose;
import org.photonvision.targeting.PhotonTrackedTarget;

import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.wpilibj.Notifier;

public class VisionSystem implements AutoCloseable {
  public static class Hardware {
    VisionCamera[] cameras;

    public Hardware(VisionCamera... cameras) {
      this.cameras = cameras;
    }
  }

  private static VisionSystem m_subsystem;

  private VisionCamera[] m_cameras;
  private Notifier m_cameraNotifier;

  /**
   * Create a new vision subsystem
   * @param visionHardware Vision hardware
   */
  public VisionSystem(Hardware visionHardware) {
    this.m_cameras = visionHardware.cameras;
  
    // Setup camera pose estimation threads
    this.m_cameraNotifier = new Notifier(() -> {
      for (var camera : m_cameras) camera.run();
    });

    // Set all cameras to primary pipeline
    for (var camera : m_cameras) camera.setPipelineIndex(0);

    // Start camera threads
    m_cameraNotifier.setName(getClass().getSimpleName());
    m_cameraNotifier.startPeriodic(Constants.Global.ROBOT_LOOP_PERIOD);
  }

  public static Hardware initializeHardware() {
    Hardware visionHardware = new Hardware( 
      new VisionCamera(Constants.VisionHardware.CAMERA_0_NAME, Constants.VisionHardware.CAMERA_0_LOCATION)
    );

    return visionHardware;
  }

  public static VisionSystem getInstance() {
    if (m_subsystem == null) m_subsystem = new VisionSystem(initializeHardware());
    return m_subsystem;
  }

  /**
   * Get currently estimated robot pose
   * @return an EstimatedRobotPose with an estimated pose, the timestamp, and targets used to create the estimate
   */
  public List<EstimatedRobotPose> getEstimatedGlobalPose() {
    List<EstimatedRobotPose> estimatedPoses = new ArrayList<EstimatedRobotPose>();

    for (var camera : m_cameras) {
      var result = camera.getLatestEstimatedPose();
      if (result != null) estimatedPoses.add(result);
    }
   
    return estimatedPoses;
  }

  /**
   * Get average currently estimated robot pose
   * @return an EstimatedRobotPose with an estimated pose, the timestamp, and targets used to create the estimate
   */
  public EstimatedRobotPose getAverageEstimatedGlobalPose() {
    List<EstimatedRobotPose> estimatedPoses = getEstimatedGlobalPose();

    double averageX = 0.0;
    double averageY = 0.0;
    double averageZ = 0.0;
    double averageRadians = 0.0;

    // Get timestamp and targets from first estimated pose
    double timestamp = estimatedPoses.get(0).timestampSeconds;
    List<PhotonTrackedTarget> targets = estimatedPoses.get(0).targetsUsed;

    // Average list of estimated poses
    for (EstimatedRobotPose pose : estimatedPoses) {
      Pose3d currentPose = pose.estimatedPose;
      averageX += currentPose.getX();
      averageY += currentPose.getY();
      averageZ += currentPose.getZ();
      averageRadians += currentPose.getRotation().toRotation2d().getRadians();
    }

    int numPoses = estimatedPoses.size();
    averageX /= numPoses;
    averageY /= numPoses;
    averageZ /= numPoses;
    averageRadians /= numPoses;

    // Return average
    EstimatedRobotPose averageEstimatedPose =
    new EstimatedRobotPose(new Pose3d(averageX, averageY, averageZ,
                                      new Rotation3d(0.0, 0.0, averageRadians)),
                           timestamp, targets);
    return averageEstimatedPose;
  }

  @Override
  public void close() {
    for (var camera : m_cameras) camera.close();
    m_cameraNotifier.close();
  }
}
