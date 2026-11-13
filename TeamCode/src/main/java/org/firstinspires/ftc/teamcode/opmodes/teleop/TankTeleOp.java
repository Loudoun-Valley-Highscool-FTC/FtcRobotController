package org.firstinspires.ftc.teamcode.opmodes.teleop;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.hardware.RobotHardware;

@TeleOp(name = "Tank TeleOp", group = "TeleOp")
public class TankTeleOp extends LinearOpMode {

    private static final double TRIGGER_THRESHOLD = 0.5;
    private static final double STICK_DEADBAND = 0.05;
    private static final double KICK = 0.12;

    private final RobotHardware robot = new RobotHardware(this);

    private boolean intakeOn = false;
    private boolean launcherOn = false;
    private boolean servosOpen = false;

    private boolean prevIntakeButton = false;
    private boolean prevLauncherButton = false;
    private boolean prevServoButton = false;
    private boolean prevDpadUp = false;
    private boolean prevDpadDown = false;

    @Override
    public void runOpMode() {
        robot.init();
        robot.closeServos();

        telemetry.addLine("Tank TeleOp initialized. Press PLAY to start.");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            double left  = shape(-gamepad1.left_stick_y);
            double right = shape(-gamepad1.right_stick_y);

            double drive = (left + right) / 2.0;
            double turn  = (left - right) / 2.0;
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

            telemetry.addData("Left", "%.2f", left);
            telemetry.addData("Right", "%.2f", right);
            telemetry.addData("Intake", intakeOn ? "ON" : "off");
            telemetry.addData("Launcher", launcherOn ? "REVVING" : "off");
            telemetry.addData("Launcher Power", "%.2f", robot.getLauncherSetPower());
            telemetry.addData("Servos", servosOpen ? "OPEN" : "closed");
            telemetry.addData("Guider", feeding ? "FEED" : "rest");
            telemetry.update();
        }

        robot.stop();
    }

    private static double shape(double value) {
        if (Math.abs(value) < STICK_DEADBAND) {
            return 0.0;
        }
        double magnitude = KICK + (1.0 - KICK) * value * value;
        return Math.copySign(magnitude, value);
    }
}
