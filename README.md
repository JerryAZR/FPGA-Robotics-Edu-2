# FPGA for Robotics Education (Part 2)

## Overview

This repository contains the source code of the **FPGA for Robotics Education**
project (part 2).

Part 1 of the project can be found
[here](https://www.hackster.io/fpga-for-robotics-education/interfacing-the-ti-rslk-max-with-webfpga-for-fpga-education-7eeff0)

## Directory Organization

The [common](common/) directory contains generic modules that are used in multiple
projects.

The other directories each corresponds to a project and contains a
top level module, a Makefile, and some other private modules.

## Notes

* 8-bit
* 1 0/1  0/1 (speed / 32)
*   l/r  f/b	

## Tutorial

Also available on [hackster.io](https://www.hackster.io/jerryazr/interfacing-the-ti-rslk-max-with-webfpga-part-2-003630)

### Before You Start

Please make sure that you have completed the original project [(Interfacing the
TI RSLK MAX with WebFPGA for FPGA Education)](https://www.hackster.io/fpga-for-robotics-education/interfacing-the-ti-rslk-max-with-webfpga-for-fpga-education-7eeff0)
before starting the activities discussed in this project.

### Introduction

If you have completed the labs in the original project, you are probably
familiar with FPGA and basic Verilog programming by now. In this project, we
will demonstrate some more advanced use cases of the WebFPGA as a control unit.

### Labs

* Lab 5.5: Feedback Control

This lab can be attempted once you have completed [Lab 4: Motors and Movement](https://www.hackster.io/fpga-for-robotics-education/lab-4-motors-and-movement-5b9a55)
and [Lab 5: Encoders and Precision Movement](https://www.hackster.io/fpga-for-robotics-education/lab-5-encoders-and-precision-movement-b87cd3)
in the original project. Lab 6 is not a prerequisite because the IR sensor
is not used in this lab.

* Lab 7: Maze Solver
