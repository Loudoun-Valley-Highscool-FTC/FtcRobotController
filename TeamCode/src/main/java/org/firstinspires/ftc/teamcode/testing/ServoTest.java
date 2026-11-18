package org.firstinspires.ftc.teamcode.testing;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;

@TeleOp(name = "Servo Test", group = "Test")
public class ServoTest extends LinearOpMode {

    @Override
    public void runOpMode() {
        Servo servo = null;
        try {
            servo = hardwareMap.get(Servo.class, "servo");
        } catch (Exception e) {
            try {
                servo = hardwareMap.get(Servo.class, "frontLeft");
            } catch (Exception ignored) {}
        }

        if (servo == null) {
            telemetry.addLine("[ERROR] No Servo found in HardwareMap under 'servo' or 'frontLeft'!");
            telemetry.update();
        } else {
            telemetry.addLine("Servo initialized. Press PLAY to test.");
            telemetry.update();
        }

        waitForStart();

        double targetPosition = 0.5;
        boolean prevDpadUp = false;
        boolean prevDpadDown = false;

        while (opModeIsActive()) {
            if (servo != null) {
                // Gamepad controls: A = 0.0 (Closed), B = 0.5 (Mid), Y = 1.0 (Open)
                if (gamepad1.a) targetPosition = 0.0;
                if (gamepad1.b) targetPosition = 0.5;
                if (gamepad1.y) targetPosition = 1.0;

                // Step controls via D-Pad
                if (gamepad1.dpad_up && !prevDpadUp) {
                    targetPosition = Math.min(1.0, targetPosition + 0.05);
                }
                prevDpadUp = gamepad1.dpad_up;

                if (gamepad1.dpad_down && !prevDpadDown) {
                    targetPosition = Math.max(0.0, targetPosition - 0.05);
                }
                prevDpadDown = gamepad1.dpad_down;

                servo.setPosition(targetPosition);

                telemetry.addData("Commanded Position", "%.2f", targetPosition);
                telemetry.addLine("Controls: [A] 0.0 | [B] 0.5 | [Y] 1.0 | [DPad Up/Down] +/- 0.05");
            } else {
                telemetry.addLine("[ERROR] Servo Hardware Missing!");
            }
            telemetry.update();
            sleep(20);
        }
    }
}
