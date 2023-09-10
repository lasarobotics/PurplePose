import java.util.ArrayList;
import java.util.List;

import org.photonvision.EstimatedRobotPose;

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
   * @return  an EstimatedRobotPose with an estimated pose, the timestamp, and targets used to create the estimate
   */
  public List<EstimatedRobotPose> getEstimatedGlobalPose() {
    List<EstimatedRobotPose> estimatedPoses = new ArrayList<EstimatedRobotPose>();

    for (var camera : m_cameras) {
      var result = camera.getLatestEstimatedPose();
      if (result != null) estimatedPoses.add(result);
    }
   
    return estimatedPoses;
  }

  @Override
  public void close() {
    for (var camera : m_cameras) camera.close();
    m_cameraNotifier.close();
  }
}
