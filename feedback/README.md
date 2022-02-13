# Feedback Control

## Build & Run Instructions

Run `make synth` to generate the bitstream and `make flash` to program the FPGA.

## Summary
The proportional–integral–derivative (PID) controller is a feedback control
system widely used in industry. Because the hardware implementations of the
integral (I) and derivative (D) terms are not very intuitive, we will only use
the proportional (P) term to build a simple feedback control system.

The control system implementation can be found in [speedctl.v](../common/speedctl.v).
We have also updated the [stepctl module](stepctl.v) to generate PWM using the
`speedctl` module. The top level implementation is almost identical to the one
in [encoder test 1](../encoder_test1). With the updates modules, the two
motors now run at the same speed!

## Challenge
Try to implement a PI, PD, or PID system by approximating the I and/or D terms.
