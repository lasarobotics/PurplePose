# PurpleVision
PurpleVision is a vision solution that estimates the robot's pose using PhotonVision and AprilTags. The main advantage of PurpleVision is that it runs on a coprocessor and communicates with the robot code on the roboRIO via NetworkTables.

# Key Points
1. Runs on a coprocessor
2. Sends pose information to the robot via NetworkTables

# Build Instructions
Building is quite simple. All that is required is to run the `build.gradle file` in the terminal like so:
`./gradlew build`

Alternatively, press `Ctrl/Cmd-Shift-P` in WPILib and select the `WPILib: Build Robot Code` option.
- Make sure to use the `Java Build`.

# Status
Currently the program is in development and is not ready for use.