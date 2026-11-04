package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name="DriveTeleOp", group="TeleOp")
public class opModeIterative extends opModeMain {

    @Override
    public void runOpMode() {
        initHardware();

        telemetry.addData("Status", "TeleOp Initialized");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            double y = -gamepad1.left_stick_y;
            double x = gamepad1.left_stick_x;
            double rx = gamepad1.right_stick_x;

            driveMecanum(x, y, rx);

            telemetry.addData("Drive Y", y);
            telemetry.addData("Drive X", x);
            telemetry.addData("Drive RX", rx);
            telemetry.update();
        }

        stopMotors();
    }
}
