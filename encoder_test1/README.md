# Encoder Test 1

## Build & Run Instructions

Run `make synth` to generate the bitstream and `make flash` to program the FPGA.

## Summary

In this test program, we create a module named `stepctl` in
[stepctl.v](stepctl.v). Upon activation, this module rotates the connected
wheel by a given number of degrees.

In the [top level module](test.v), we instantiate two `stepctl` modules,
one for each motor. Both motors are driven using the same PWM and are instructed
to rotate 18000 degrees (10 rotations). One might expect both motors to stop at
the same time because they seem to have the same speed. However, this is very
likely not the case.

Due to variations in the manufacturing process, it is common to see two motors
driven with the same PWM having different speed. Therefore, using PWM along is
not enough for precise speed control.

Basic movement control with encoders is covered in **FPGA for Robotics Education**
[Lab 5](https://www.hackster.io/fpga-for-robotics-education/lab-5-encoders-and-precision-movement-b87cd3).
Next we will discuss a more advanced usage of the encoders:
[feedback control](../feedback/)
