// Eric Schwarz Iglesias
// ECE 485 Senior Design
// pinmap.v

// This file contains the official Pinmap for WebPFGA to TI-RSLK-MAX CHassis board.
// Even though this file is a Verilog file it only contains the pinmap in a commented
// format to allow the WebFPGA synthesis engine to parse the comments to set the 
// inputs and outputs.
// If using the CLI, this file can simply by added to the list of files to be 
// synthesized. If using the WEB IDE it must be copied and pasted in the beginning.


// Pin Map
// @MAP_IO motorL_pwm	0
// @MAP_IO motorL_dir 	1
// @MAP_IO motorL_en 	2
// @MAP_IO motorL_encdr	17

// @MAP_IO motorR_pwm 	3
// @MAP_IO motorR_dir 	4
// @MAP_IO motorR_en 	5
// @MAP_IO motorR_encdr	15

// @MAP_IO bump0	6
// @MAP_IO bump1	7
// @MAP_IO bump2	8
// @MAP_IO bump3	9
// @MAP_IO bump4	10
// @MAP_IO bump5	11

// @MAP_IO ledFL	26
// @MAP_IO ledFR	27
// @MAP_IO ledBL	30
// @MAP_IO ledBR	31

// @MAP_IO ir_evenLED	12
// @MAP_IO ir_oddLED	14
// @MAP_IO ir_snsrch0	18
// @MAP_IO ir_snsrch1	19
// @MAP_IO ir_snsrch2	20
// @MAP_IO ir_snsrch3	21
// @MAP_IO ir_snsrch4	22
// @MAP_IO ir_snsrch5	23
// @MAP_IO ir_snsrch6	24
// @MAP_IO ir_snsrch7	25