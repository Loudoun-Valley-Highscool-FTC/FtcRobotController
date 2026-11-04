package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.util.ElapsedTime;

@Autonomous(name="BlueAuto", group = "Autonomous")
public class opModeLinear extends opModeMain {

    private ElapsedTime runtime = new ElapsedTime();

    @Override
    public void runOpMode() {
        initHardware();

        telemetry.addData("Status", "Run Time: " + runtime.toString());
        telemetry.update();

        waitForStart();
        runtime.reset();

        if (opModeIsActive()) {
            driveForTime(0, 0.5, 0, 1500);
            sleep(500);
            driveForTime(0.5, 0.0, 0.0, 1000);
            sleep(500);
            driveForTime(0.0, 0.0, 0.5, 500);
            stopMotors();
        }
    }
}
