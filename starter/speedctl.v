// Zerui An
// FPGA for Robotics Education
//------------------------------------------------------------------------------
// This is the starter code for the feedback speed control module.
// inputs:
//      clk     -- a 16MHz clock
//      enable  -- active low reset
//      encoder -- the encoder signal
//      deg_s   -- desired degrees per second (suggested range: [0, 1440])
// output:
//      PWM     -- the pulse width modulation used to drive the motor


module speedctl (
    input clk,
    input enable,
    input encoder, // encoder pulse
    input [15:0] deg_s, // desired degrees per second. Should be less than 1440
    output wire PWM
);

localparam TICKS = 32'd16000000 // Assume that each interval is 1 second
                                // Feel free to change this value if necessary

// states
localparam IDLE = 2'b00;
localparam COUNT = 2'b01;
localparam UPDATE = 2'b10;
reg [1:0] current_state, next_state;

reg enc1, enc2, enc3; // double buffering and edge detection
wire pulse;
always @(posedge clk) begin
    enc1 <= encoder;
    enc2 <= enc1;
    enc3 <= enc2;
end
assign pulse = ~enc3 & enc2;

reg signed [15:0] speed; // The current speed
reg signed [15:0] acceleration;
reg [31:0] timer, timer_next; // The countdown timer
reg signed [15:0] counter, counter_next; // Count the number of ticks in a period

pwm pwm_generator(clk, enable, speed, PWM);

always @(posedge clk) begin
    // TODO: Register Updates
end

always @(*) begin
    // TODO: default values
    // It is a common mistake to leave some cases not covered when writing
    // a case statement. Such a mistake would often lead to an "inferred latch"
    // error.
    // Assigning default values at the beginning of an always block is a simple
    // but useful trick to avoid this mistake.

    case (current_state)
        IDLE: begin
            next_state = enable ? COUNT : IDLE;
        end

        COUNT: begin // TODO: count the number of encoder pulses

        end

        UPDATE: begin // TODO: adjust speed

        end
    endcase
end

endmodule
