// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import org.photonvision.EstimatedRobotPose;
import org.photonvision.PhotonCamera;
import org.photonvision.PhotonPoseEstimator;
import org.photonvision.PhotonPoseEstimator.PoseStrategy;
import org.photonvision.targeting.PhotonPipelineResult;

import edu.wpi.first.apriltag.AprilTagFieldLayout.OriginPosition;
import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.apriltag.AprilTagFields;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.wpilibj.DriverStation;

/** Create a camera */
public class VisionCamera implements Runnable, AutoCloseable {
  private final double APRILTAG_POSE_AMBIGUITY_THRESHOLD = 0.2;
  
  private PhotonCamera m_camera;
  private PhotonPoseEstimator m_poseEstimator;
  private Transform3d m_transform;
  private AtomicReference<EstimatedRobotPose> m_atomicEstimatedRobotPose;

  public VisionCamera(String name, Transform3d transform) {
    this.m_camera = new PhotonCamera(name);
    this.m_transform = transform;
    try {
      var fieldLayout = AprilTagFieldLayout.loadFromResource(AprilTagFields.k2023ChargedUp.m_resourceFile);
      // PV estimates will always be blue, they'll get flipped by robot thread
      fieldLayout.setOrigin(OriginPosition.kBlueAllianceWallRightSide);
      this.m_poseEstimator = new PhotonPoseEstimator(
          fieldLayout, PoseStrategy.MULTI_TAG_PNP, m_camera, m_transform);
      m_poseEstimator.setMultiTagFallbackStrategy(PoseStrategy.LOWEST_AMBIGUITY);
    } catch (IOException e) {
      DriverStation.reportError("Failed to load AprilTagFieldLayout", e.getStackTrace());
      this.m_poseEstimator = null;
    }
    this.m_atomicEstimatedRobotPose = new AtomicReference<EstimatedRobotPose>();
  }

  @Override
  public void run() {
    // Return if camera or field layout failed to load
    if (m_poseEstimator == null || m_camera == null) return;
    
    // Update and log inputs
    PhotonPipelineResult pipelineResult = m_camera.getLatestResult();

    // Return if result is non-existent or invalid
    if (!pipelineResult.hasTargets()) return;
    if (pipelineResult.targets.size() == 1
        && pipelineResult.targets.get(0).getPoseAmbiguity() > APRILTAG_POSE_AMBIGUITY_THRESHOLD) return;
    
    // Update pose estimate
    m_poseEstimator.update(pipelineResult).ifPresent(estimatedRobotPose -> {
      var estimatedPose = estimatedRobotPose.estimatedPose;
        // Make sure the measurement is on the field
        if (estimatedPose.getX() > 0.0 && estimatedPose.getX() <= Constants.Field.FIELD_LENGTH
            && estimatedPose.getY() > 0.0 && estimatedPose.getY() <= Constants.Field.FIELD_WIDTH) {
          m_atomicEstimatedRobotPose.set(estimatedRobotPose);
        }
    });
  }

  /**
   * Gets the latest robot pose. Calling this will only return the pose once. 
   * If it returns a non-null value, it is a new estimate that hasn't been returned before.
   * This pose will always be for the BLUE alliance.
   * @return Latest estimated pose
   */
  public EstimatedRobotPose getLatestEstimatedPose() {
    return m_atomicEstimatedRobotPose.getAndSet(null);
  }

  /**
   * Allows user to select the active pipeline index
   * @param index The active pipeline index
   */
  public void setPipelineIndex(int index) {
    m_camera.setPipelineIndex(index);
  }

  /**
   * Allows user to set pipeline index
   */
  public void getCamera(int index) {
    m_camera.setPipelineIndex(index);
  }

  /***
   * Get camera to robot transform
   * @return Camera to robot transform
   */
  public Transform3d getTransform() {
    return m_transform;
  }

  @Override
  public void close() {
    m_camera.close();
  }
}