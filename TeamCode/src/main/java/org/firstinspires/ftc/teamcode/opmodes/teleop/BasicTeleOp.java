package org.firstinspires.ftc.teamcode.opmodes.teleop;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.hardware.RobotHardware;

@TeleOp(name = "Basic TeleOp", group = "TeleOp")
public class BasicTeleOp extends LinearOpMode {

    private static final double TRIGGER_THRESHOLD = 0.1;

    private final RobotHardware robot = new RobotHardware(this);

    @Override
    public void runOpMode() {
        robot.init();

        telemetry.addLine("Initialized. Press PLAY to start.");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            //drive
            double drive = -gamepad1.left_stick_y;
            double turn  = gamepad1.right_stick_x;
            robot.arcadeDrive(drive, turn);

            //shooter rev (right trigger)
            boolean revUp = gamepad1.right_trigger > TRIGGER_THRESHOLD;
            robot.Shoot(revUp);

            //guide/feed ball into shooter (left trigger)
            boolean feeding = gamepad1.left_trigger > TRIGGER_THRESHOLD;
            robot.feed(feeding);

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
            telemetry.addData("Shooter", revUp ? "REVVING" : "off");
            telemetry.addData("Feed", feeding ? "ON" : "off");
            telemetry.addData("Intake", "%.2f", robot.getIntakePower());
            telemetry.update();
        }

        robot.stop();
    }
}
