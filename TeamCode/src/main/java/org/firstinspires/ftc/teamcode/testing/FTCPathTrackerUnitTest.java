package org.firstinspires.ftc.teamcode.testing;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.path.Position2D;
import org.firstinspires.ftc.teamcode.path.RoMap;

import java.util.List;
//I HAD GEMINI PUT THIS TOGETHER I HAVE NOT CHECKED IT AT ALL
/**
 * Unit Test runner for verifying RoMap tracking data, 360-degree vector calculations,
 * direct return routes, and edge-of-the-map routes.
 */
@Autonomous(name = "Path Tracker Unit Test", group = "Test")
public class FTCPathTrackerUnitTest extends LinearOpMode {

    @Override
    public void runOpMode() {
        telemetry.addLine("=== PATH TRACKER UNIT TEST OPMODE ===");
        telemetry.addLine("Press PLAY to execute test suite.");
        telemetry.update();

        waitForStart();

        try {
            runTests(telemetry);
            telemetry.addLine("\n[SUCCESS] ALL MATH & NAVIGATION TESTS PASSED!");
        } catch (Throwable t) {
            telemetry.addData("\n[FAILED] TEST ERROR", t.getMessage());
        }
        telemetry.update();

        while (opModeIsActive()) {
            sleep(100);
        }
    }

    public static void main(String[] args) {
        runTests(null);
    }

    public static void runTests() {
        runTests(null);
    }

    public static void runTests(Telemetry telemetry) {
        log(telemetry, "=== STARTING FTC PATH TRACKER MATH TESTS ===");
        
        test360DegreeVectorDecomposition(telemetry);
        testDirectRoute(telemetry);
        testEdgeRoute(telemetry);
        testBoundaryCheck(telemetry);

        log(telemetry, "=== ALL TESTS PASSED SUCCESSFULLY! ===");
    }

    private static void log(Telemetry telemetry, String message) {
        System.out.println(message);
        if (telemetry != null) {
            telemetry.addLine(message);
            telemetry.update();
        }
    }

    public static void test360DegreeVectorDecomposition(Telemetry telemetry) {
        Position2D origin = new Position2D(0, 0, 0);
        RoMap map = new RoMap(3.6576, origin);

        // Move 1m at 0° (Forward along +X)
        map.recordMovement(0.0, 1.0, 1.0);
        assertEquals(1.0, map.getCurrentPosition().getX(), 0.001);
        assertEquals(0.0, map.getCurrentPosition().getY(), 0.001);

        // Move 1m at 90° (Left along +Y)
        map.recordMovement(90.0, 1.0, 1.0);
        assertEquals(1.0, map.getCurrentPosition().getX(), 0.001);
        assertEquals(1.0, map.getCurrentPosition().getY(), 0.001);

        // Move 1m at 180° (Backward along -X)
        map.recordMovement(180.0, 1.0, 1.0);
        assertEquals(0.0, map.getCurrentPosition().getX(), 0.001);
        assertEquals(1.0, map.getCurrentPosition().getY(), 0.001);

        // Move 1m at 270° / -90° (Right along -Y)
        map.recordMovement(270.0, 1.0, 1.0);
        assertEquals(0.0, map.getCurrentPosition().getX(), 0.001);
        assertEquals(0.0, map.getCurrentPosition().getY(), 0.001);

        log(telemetry, "✓ 360-Degree Vector Decomposition PASSED");
    }

    public static void testDirectRoute(Telemetry telemetry) {
        Position2D home = new Position2D(0, 0, 0);
        RoMap map = new RoMap(3.6576, home);

        // Robot moves to (2.0, 2.0)
        map.recordMovement(45.0, Math.sqrt(8.0), 1.0);

        assertEquals(2.0, map.getCurrentPosition().getX(), 0.01);
        assertEquals(2.0, map.getCurrentPosition().getY(), 0.01);

        List<RoMap.MovementStep> returnSteps = map.calculateDirectRoute(1.0);

        assertEquals(1, returnSteps.size());
        RoMap.MovementStep returnStep = returnSteps.get(0);

        assertEquals(-135.0, returnStep.getHeadingDegrees(), 0.1);
        assertEquals(Math.sqrt(8.0), returnStep.distance, 0.01);

        log(telemetry, "✓ Direct Vector Route PASSED");
    }

    public static void testEdgeRoute(Telemetry telemetry) {
        Position2D home = new Position2D(0, 0, 0);
        RoMap map = new RoMap(3.6576, home);

        map.recordMovement(45.0, Math.sqrt(8.0), 1.0);

        List<RoMap.MovementStep> returnSteps = map.calculateEdgeRoute(1.0);

        // Edge route generates 3 perimeter steps
        assertEquals(3, returnSteps.size());

        log(telemetry, "✓ Edge of the Map Perimeter Route PASSED");
    }

    public static void testBoundaryCheck(Telemetry telemetry) {
        Position2D home = new Position2D(0, 0, 0);
        RoMap map = new RoMap(2.0, home);

        try {
            map.recordMovement(0.0, 5.0, 1.0);
            throw new RuntimeException("Test Failed: Should have thrown IllegalArgumentException!");
        } catch (IllegalArgumentException e) {
            log(telemetry, "✓ Field Boundary Check PASSED");
        }
    }

    private static void assertEquals(double expected, double actual, double delta) {
        if (Math.abs(expected - actual) > delta) {
            throw new AssertionError(String.format("Expected: %.4f, Actual: %.4f", expected, actual));
        }
    }

    private static void assertEquals(int expected, int actual) {
        if (expected != actual) {
            throw new AssertionError(String.format("Expected: %d, Actual: %d", expected, actual));
        }
    }
}
