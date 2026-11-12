package org.firstinspires.ftc.teamcode.hardware;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

public class RobotHardware {
    public static final String BACK_LEFT_NAME   = "backLeft";
    public static final String BACK_RIGHT_NAME  = "backRight";
    public static final String INTAKE_NAME      = "intake";
    public static final String LAUNCHER_NAME    = "launcher";
    public static final String LEFT_SERVO_NAME  = "frontLeft";
    public static final String RIGHT_SERVO_NAME = "frontRight";
    public static final String GUIDER_NAME      = "guider";

    public static final double DRIVE_SPEED     = 0.8;
    public static final double INTAKE_POWER    = 1.0;
    public static final double LAUNCHER_POWER  = 1.0;  //1.0;
    public static final double SERVO_OPEN      = 1.0;
    public static final double SERVO_CLOSED    = 0.0;
    public static final double GUIDER_FEED     = 1.0;
    public static final double GUIDER_REST     = 0.0;

    private static final double ACCEL_RATE = 0.8;
    private static final double DECEL_RATE = 1.2;
    private static final double REVERSAL_DELAY = 0.15;

    private DcMotor backLeft;
    private DcMotor backRight;
    private DcMotor intake;
    private DcMotor launcher;
    private Servo leftServo;
    private Servo rightServo;
    private Servo guider;

    private double currentLeftPower = 0.0;
    private double currentRightPower = 0.0;
    private double leftReverseUntil = 0.0;
    private double rightReverseUntil = 0.0;
    private long lastDriveTime = System.nanoTime();

    private final LinearOpMode opMode;

    public RobotHardware(LinearOpMode opMode) {
        this.opMode = opMode;
    }

    public void init() {
        HardwareMap hardwareMap = opMode.hardwareMap;

        backLeft  = hardwareMap.get(DcMotor.class, BACK_LEFT_NAME);
        backRight = hardwareMap.get(DcMotor.class, BACK_RIGHT_NAME);
        intake    = hardwareMap.get(DcMotor.class, INTAKE_NAME);
        launcher  = hardwareMap.get(DcMotor.class, LAUNCHER_NAME);
        leftServo  = hardwareMap.get(Servo.class, LEFT_SERVO_NAME);
        rightServo = hardwareMap.get(Servo.class, RIGHT_SERVO_NAME);
        guider     = hardwareMap.get(Servo.class, GUIDER_NAME);

        backLeft.setDirection(DcMotorSimple.Direction.FORWARD);
        backRight.setDirection(DcMotorSimple.Direction.REVERSE);

        backLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        stop();
    }

    public void arcadeDrive(double drive, double turn) {
        double leftPower  = drive + turn;
        double rightPower = drive - turn;

        double max = Math.max(Math.abs(leftPower), Math.abs(rightPower));
        if (max > 1.0) {
            leftPower  /= max;
            rightPower /= max;
        }

        double targetLeft = leftPower * DRIVE_SPEED;
        double targetRight = rightPower * DRIVE_SPEED;

        long now = System.nanoTime();
        double dt = Math.max(0.0, Math.min((now - lastDriveTime) / 1e9, 0.05));
        lastDriveTime = now;
        double time = now / 1e9;

        if (leftReverseUntil == 0 && currentLeftPower * targetLeft < 0) {
        leftReverseUntil = -1;
        }

        if (currentRightPower * targetRight < 0) {
            rightReverseUntil = -1;
        }

        if (leftReverseUntil != 0) {
            if (Math.abs(currentLeftPower) > 0.001) {
                targetLeft = 0;
            } else {
                currentLeftPower = 0;
                if (leftReverseUntil < 0) {
                    leftReverseUntil = time + REVERSAL_DELAY;
                }
                if (time < leftReverseUntil) {
                    targetLeft = 0;
                } else {
                    leftReverseUntil = 0;
                }
            }
        }

        if (rightReverseUntil != 0) {
            if (Math.abs(currentRightPower) > 0.001) {
                targetRight = 0;
            } else {
                currentRightPower = 0;
                if (rightReverseUntil < 0) {
                    rightReverseUntil = time + REVERSAL_DELAY;
                }
                if (time < rightReverseUntil) {
                    targetRight = 0;
                } else {
                    rightReverseUntil = 0;
                }
            }
        }

        currentLeftPower = rampPower(currentLeftPower, targetLeft, dt);
        currentRightPower = rampPower(currentRightPower, targetRight, dt);

        backLeft.setPower(currentLeftPower);
        backRight.setPower(currentRightPower);
    }

    private double rampPower(double current, double target, double dt) {
        double rate = Math.abs(target) > Math.abs(current)
                ? ACCEL_RATE : DECEL_RATE;
        double maxChange = rate * dt;
        double difference = target - current;

        if (Math.abs(difference) <= maxChange) {
            return target;
        }

        return current + Math.copySign(maxChange, difference);
    }

    public void driveStraight(double power) {
        arcadeDrive(power, 0);
    }

    public void setIntakePower(double power) {
        intake.setPower(power);
    }

    public double getIntakePower() {
        return intake.getPower();
    }

    public void setLauncherPower(double power) {
        launcher.setPower(power);
    }

    public double getLauncherPower() {
        return launcher.getPower();
    }

    public void openServos() {
        setServoPositions(SERVO_OPEN);
    }

    public void closeServos() {
        setServoPositions(SERVO_CLOSED);
    }

    public void setServoPositions(double position) {
        leftServo.setPosition(position);
        rightServo.setPosition(position);
    }
    
    public void guideAndShoot(boolean on) {
        launcher.setPower(on ? LAUNCHER_POWER : 0);
        guider.setPosition(on ? GUIDER_FEED : GUIDER_REST);
    }
    public void Shoot(boolean on) {
        launcher.setPower(on ? LAUNCHER_POWER : 0);
    }

    public void feed(boolean on) {
        guider.setPosition(on ? GUIDER_FEED : GUIDER_REST);
    }


    public void setGuiderPosition(double position) {
        guider.setPosition(position);
    }

    public double getGuiderPosition() {
        return guider.getPosition();
    }

    public void stop() {
        currentLeftPower = 0;
        currentRightPower = 0;
        leftReverseUntil = 0;
        rightReverseUntil = 0;
        lastDriveTime = System.nanoTime();

        backLeft.setPower(0);
        backRight.setPower(0);
        intake.setPower(0);
        launcher.setPower(0);
        guider.setPosition(GUIDER_REST);
    }
}
