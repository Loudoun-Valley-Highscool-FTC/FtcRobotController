package org.firstinspires.ftc.teamcode.testing;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.teamcode.hardware.RobotHardware;
import org.firstinspires.ftc.teamcode.path.Position2D;
import org.firstinspires.ftc.teamcode.path.RoMap;

import java.util.List;
//I HAD GEMINI PUT THIS TOGETHER I HAVE NOT CHECKED IT AT ALL

@TeleOp(name = "Path Tracker Test OpMode", group = "Test")
public class PathTrackerTestOpMode extends LinearOpMode {

    @Override
    public void runOpMode() {
        RobotHardware robot = new RobotHardware(this);
        robot.init();

        // 12x12 ft field with starting home position at (-6, -6)
        Position2D homePosition = new Position2D(-6.0, -6.0, 0.0);
        RoMap map = new RoMap(12.0, homePosition);

        waitForStart();

        boolean aPressedLast = false;
        boolean bPressedLast = false;
        boolean xPressedLast = false;
        boolean yPressedLast = false;

        while (opModeIsActive()) {
            double drive = -gamepad1.left_stick_y * 0.5;
            double turn  = gamepad1.left_stick_x * 0.5;
            robot.arcadeDrive(drive, turn);

            // Button A: Update position
            if (gamepad1.a && !aPressedLast) {
                map.updatePosition(0.0, 0.5, 1.0);
            }
            aPressedLast = gamepad1.a;

            // Button B: Calculate Direct Route
            if (gamepad1.b && !bPressedLast) {
                List<RoMap.MovementStep> directRoute = map.calculateDirectRoute(0.5);
                telemetry.addData("Direct Route Generated", "%d steps", directRoute.size());
            }
            bPressedLast = gamepad1.b;

            // Button X: Calculate Edge Route
            if (gamepad1.x && !xPressedLast) {
                List<RoMap.MovementStep> edgeRoute = map.calculateEdgeRoute(0.5);
                telemetry.addData("Edge Route Generated", "%d steps", edgeRoute.size());
            }
            xPressedLast = gamepad1.x;

            // Button Y: Run Self Tests
            if (gamepad1.y && !yPressedLast) {
                try {
                    FTCPathTrackerUnitTest.runTests();
                    telemetry.addData("Self Test", "ALL UNIT TESTS PASSED!");
                } catch (Exception e) {
                    telemetry.addData("Self Test FAILED", e.getMessage());
                }
            }
            yPressedLast = gamepad1.y;

            telemetry.addData("Current Position", map.getCurrentPosition().toString());
            telemetry.addLine("\nControls:");
            telemetry.addLine(" [A] Record | [B] Direct Route | [X] Edge Route | [Y] Tests");
            telemetry.update();

            sleep(20);
        }

        robot.stop();
    }
}
