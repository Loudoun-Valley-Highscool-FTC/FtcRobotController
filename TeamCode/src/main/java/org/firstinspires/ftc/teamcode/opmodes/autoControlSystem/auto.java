package org.firstinspires.ftc.teamcode.opmodes.autoControlSystem;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import org.firstinspires.ftc.teamcode.hardware.RobotHardware;

@Autonomous(name = "Auto Control", group = "Autonomous")
public class auto extends LinearOpMode {

    private final RobotHardware robot = new RobotHardware(this);

    @Override
    public void runOpMode() {
        robot.init();
        robot.closeServos();

        telemetry.addLine("Initialized. Waiting for START.");
        telemetry.update();

        // Wait for the driver to press PLAY
        waitForStart();

        // Run autonomous routine
        if (opModeIsActive()) {
            robot.setLauncherPower(0.8);
            robot.driveStraight(1);
            sleep(100);
            robot.arcadeDrive(0, 1);
            sleep(1000);
            robot.driveStraight(0);
            //if statement needed to know if the Hive is towards or away from me
            //This shows if the hive is towards me.
            robot.Shoot(true);
            sleep(5);
            robot.Shoot(false);
            sleep(5);
            robot.Shoot(true);
            sleep(5);
            robot.Shoot(false);
            sleep(5);
            robot.Shoot(true);
            sleep(10);
            robot.Shoot(false);
            sleep(5);
            robot.Shoot(true);
            sleep(10);


            robot.stop();
        }
    }
}