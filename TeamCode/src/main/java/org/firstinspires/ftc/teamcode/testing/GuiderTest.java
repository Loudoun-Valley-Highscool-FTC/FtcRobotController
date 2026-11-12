package org.firstinspires.ftc.teamcode.testing;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.hardware.RobotHardware;


@TeleOp(name = "Guider Test", group = "Test")
public class GuiderTest extends LinearOpMode {

    private static final double STEP = 0.02;

    private final RobotHardware robot = new RobotHardware(this);

    @Override
    public void runOpMode() {
        robot.init();

        double position = RobotHardware.GUIDER_REST;
        robot.setGuiderPosition(position);

        telemetry.update();

        waitForStart();

        // Track previous button states so each press moves the servo once,
        // instead of racing up/down while the button is held.
        boolean prevUp = false;
        boolean prevDown = false;

        while (opModeIsActive()) {
            if (gamepad1.dpad_up && !prevUp) {
                position = Math.min(1.0, position + STEP);
            } else if (gamepad1.dpad_down && !prevDown) {
                position = Math.max(0.0, position - STEP);
            }
            prevUp = gamepad1.dpad_up;
            prevDown = gamepad1.dpad_down;

            if (gamepad1.a) {
                position = RobotHardware.GUIDER_FEED;
            } else if (gamepad1.b) {
                position = RobotHardware.GUIDER_REST;
            }

            robot.setGuiderPosition(position);

            telemetry.addData("Commanded position", "%.2f", position);
            telemetry.addData("Servo reports", "%.2f", robot.getGuiderPosition());
            telemetry.addData("FEED / REST", "%.2f / %.2f",
                    RobotHardware.GUIDER_FEED, RobotHardware.GUIDER_REST);
            telemetry.update();
        }

        robot.stop();
    }
}
