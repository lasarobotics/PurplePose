import java.io.IOException;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Main Class
 */
public class PurpleVision {
    VisionSystem visionSystem;

    private static void run() { // Temp function for now, eventually will replace with the visionSystem run function
        System.out.println("Running: " + new java.util.Date());
    }

    public static void main(String[] args) throws IOException {
        ScheduledExecutorService executorService;
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(PurpleVision::run, 0, 20, TimeUnit.MILLISECONDS); // Run every 10ms
    }
}
