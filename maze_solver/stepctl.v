// Zerui An
// FPGA for Robotics Education
//------------------------------------------------------------------------------
// A step control module without PWM generation
//
// inputs:
//      clk     -- a 16MHz clock
//      rst     -- active high reset
//      enable  -- start the action
//      encoder -- the encoder signal
//      ndegs   -- number of degrees to rotate
// output:
//      motor_en     -- if motor should be enabled
// parameters:
//      SPEED   -- degrees per second 
//

module stepctl (
    input clk,
    input rst,
    input enable,
    input encoder, // encoder pulse
    input [15:0] ndegs, // desired number of ticks (degrees)
    output reg motor_en
);

// default speed
parameter SPEED = 16'd720;

// states
localparam IDLE = 4'd0;
localparam COUNTDOWN = 4'd1;

// registers
reg [3:0] state, next;
reg [15:0] counter, counter_next;

// encoder edge detector
reg enc1, enc2, enc3;
wire pulse;
always @(posedge clk) begin
    enc1 <= encoder;
    enc2 <= enc1;
    enc3 <= enc2;
end
assign pulse = ~enc3 & enc2;

always @(posedge clk) begin
    state <= rst ? IDLE : next;
    counter <= counter_next;
end

always @(*) begin
    // defaults
    next = IDLE;
    counter_next = 16'd0;
    motor_en = 0;

    case (state)
        IDLE: begin
            next = enable ? COUNTDOWN : IDLE;

            counter_next = enable ? ndegs : 16'd0;
            motor_en = 0;
        end

        COUNTDOWN: begin
            next = (counter != 16'd0) ? COUNTDOWN : IDLE;

            counter_next = counter - {15'd0, pulse};
            motor_en = 1;
        end
    endcase
end

    
endmodule