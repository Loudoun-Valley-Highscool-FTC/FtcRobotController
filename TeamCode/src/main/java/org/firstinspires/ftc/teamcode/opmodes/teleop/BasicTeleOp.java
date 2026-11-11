package org.firstinspires.ftc.teamcode.opmodes.teleop;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.teamcode.hardware.RobotHardware;

<<<<<<< HEAD
@TeleOp(name = "Basic TeleOp", group = "Competition")
=======
@TeleOp(name = "Basic TeleOp", group = "TeleOp")
>>>>>>> 095ab8e6f5f8c3bbe72334e6ed591235d8c94a09
public class BasicTeleOp extends LinearOpMode {

    private static final double TRIGGER_THRESHOLD = 0.1;

    private final RobotHardware robot = new RobotHardware(this);

    @Override
    public void runOpMode() {
        robot.init();

        DcMotor launcher = hardwareMap.get(DcMotor.class, "launcher");

        telemetry.addLine("Initialized. Press PLAY to start.");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            //drive
            double drive = -gamepad1.left_stick_y;
            double turn  = gamepad1.right_stick_x;
            robot.arcadeDrive(drive, turn);

            //launcher 
            boolean launcherOn = gamepad1.right_bumper;
            launcher.setPower(launcherOn ? 1 : 0);

            //intake
            if (gamepad1.right_bumper) {
                robot.setIntakePower(RobotHardware.INTAKE_POWER); //suck
            } else if (gamepad1.left_bumper) {
                robot.setIntakePower(-RobotHardware.INTAKE_POWER);  //spit
            } else {
                robot.setIntakePower(0);
            }

            //servos
            //servos
            if (gamepad1.a) {
                robot.openServos();
            } else if (gamepad1.b) {
                robot.closeServos();
            }

            //telemetry
            telemetry.addData("Drive", "%.2f", drive);
            telemetry.addData("Turn", "%.2f", turn);
            telemetry.addData("Launcher", launcherOn ? "ON" : "off");
            telemetry.addData("Intake", "%.2f", robot.getIntakePower());
            telemetry.update();
        }

        robot.stop();
    }
}
