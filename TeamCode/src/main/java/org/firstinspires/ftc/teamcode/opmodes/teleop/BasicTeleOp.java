package org.firstinspires.ftc.teamcode.opmodes.teleop;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.hardware.RobotHardware;
import org.firstinspires.ftc.teamcode.path.Position2D;
import org.firstinspires.ftc.teamcode.path.RoMap;

import java.util.List;

@TeleOp(name = "Basic TeleOp", group = "TeleOp")
public class BasicTeleOp extends LinearOpMode {

    private static final double TRIGGER_THRESHOLD = 0.5;
    private static final double STICK_DEADBAND = 0.05;
    private static final double TURN_SCALE = 0.65;

    private final RobotHardware robot = new RobotHardware(this);

    // Toggle states
    private boolean intakeOn = false;
    private boolean launcherOn = false;
    private boolean servosOpen = false;

    // Previous button states for rising-edge detection
    private boolean prevIntakeButton = false;
    private boolean prevLauncherButton = false;
    private boolean prevServoButton = false;
    private boolean prevDpadUp = false;
    private boolean prevDpadDown = false;
    private boolean prevRthButton = false;

    // Nav/Map for rth
    private final Position2D homePosition = new Position2D(-6.0, -6.0, 0.0);
    private final RoMap map = new RoMap(12.0, homePosition);

    @Override
    public void runOpMode() {
        robot.init();
        robot.closeServos();

        telemetry.addLine("Initialized. Press PLAY to start.");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            double drive = shape(-gamepad1.left_stick_y);
            double turn  = shape(gamepad1.right_stick_x) * TURN_SCALE;
            robot.arcadeDrive(drive, turn);

            boolean intakeButton = gamepad1.left_trigger > TRIGGER_THRESHOLD;
            if (intakeButton && !prevIntakeButton) {
                intakeOn = !intakeOn;
            }
            prevIntakeButton = intakeButton;
            robot.setIntakePower(intakeOn ? RobotHardware.INTAKE_POWER : 0);

            boolean launcherButton = gamepad1.right_bumper;
            if (launcherButton && !prevLauncherButton) {
                launcherOn = !launcherOn;
            }
            prevLauncherButton = launcherButton;
            robot.Shoot(launcherOn);

            boolean servoButton = gamepad1.left_bumper;
            if (servoButton && !prevServoButton) {
                servosOpen = !servosOpen;
                if (servosOpen) {
                    robot.openServos();
                } else {
                    robot.closeServos();
                }
            }
            prevServoButton = servoButton;

            if (gamepad1.dpad_up && !prevDpadUp) {
                robot.changeLauncherPower(RobotHardware.LAUNCHER_POWER_STEP);
            }
            prevDpadUp = gamepad1.dpad_up;
            if (gamepad1.dpad_down && !prevDpadDown) {
                robot.changeLauncherPower(-RobotHardware.LAUNCHER_POWER_STEP);
            }
            prevDpadDown = gamepad1.dpad_down;

            boolean feeding = gamepad1.right_trigger > TRIGGER_THRESHOLD;
            robot.feed(feeding);

            //speed test maybe work
            double liveSpeed = robot.getEncoderSpeed();
            if (liveSpeed == 0.0 && Math.abs(drive) > 0.05) {
                liveSpeed = Math.abs(drive) * 4.0; // Fallback to power ratio if encoder reads 0
            }

            // Update live map position (enforceBoundary = false allows driver to surpass 12x12)
            map.updatePosition(robot.getHeadingDegrees(), liveSpeed, 0.02, false);

            // Button B: Trigger Return To Home Demonstration
            boolean rthButton = gamepad1.b;
            if (rthButton && !prevRthButton && !map.isOutOfBounds()) {
                executeReturnToHome();
            }
            prevRthButton = rthButton;

            // Telemetry
            telemetry.addData("Drive", "%.2f", drive);
            telemetry.addData("Turn", "%.2f", turn);
            telemetry.addData("Intake", intakeOn ? "ON" : "off");
            telemetry.addData("Launcher", launcherOn ? "REVVING" : "off");
            telemetry.addData("Launcher Power", "%.2f", robot.getLauncherSetPower());
            telemetry.addData("Servos", servosOpen ? "OPEN" : "closed");
            telemetry.addData("Guider", feeding ? "FEED" : "rest");
            telemetry.addData("Position", map.getCurrentPosition().toString());
            if (map.isOutOfBounds()) {
                telemetry.addData("RTH Status", "DISABLED (Out of Bounds > 12x12 ft)");
            }
            else {
                telemetry.addData("RTH Status", "READY (Press [B])");
            }
            telemetry.update();
        }

        robot.stop();
    }

    private void executeReturnToHome() {
        telemetry.addLine("Returning");
        telemetry.update();

        List<RoMap.MovementStep> steps = map.calculateDirectRoute(0.5);
        for (RoMap.MovementStep step : steps) {
            double startTime = getRuntime();
            while (opModeIsActive() && (getRuntime() - startTime) < step.timeSeconds) {
                // Driver override check
                if (Math.abs(gamepad1.left_stick_y) > 0.1 || Math.abs(gamepad1.right_stick_x) > 0.1) {
                    robot.stop();
                    return;
                }
                robot.arcadeDrive(step.speed, 0.0);
            }
            robot.stop();
        }

        map.updatePosition(-6.0, -6.0, 0.0);
        telemetry.addLine("Returned");
        telemetry.update();
        sleep(500);
    }

    private static double shape(double   value) {
        if (Math.abs(value) < STICK_DEADBAND) {
            return 0.0;
        }
        return Math.copySign(value * value, value);
    }
}
