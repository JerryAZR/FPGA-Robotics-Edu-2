module speedctl (
    input clk,
    input enable,
    input encoder, // encoder pulse
    input [15:0] deg_s, // desired degrees per second. Should be less than 1440
    output wire PWM
);

parameter LOG_DIVIDER = 3; // Adjust speed every 1/8 second
parameter LOG_DELTA = 3; // The "p" term in PID control

localparam TICKS = 32'd16000000 >> LOG_DIVIDER;

// states
localparam IDLE = 2'b00;
localparam COUNT = 2'b01;
localparam UPDATE = 2'b10;
reg [1:0] current_state, next_state;

reg enc1, enc2, enc3; // double buffering
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

wire signed [15:0] target; // The target number of ticks
assign target = deg_s >> LOG_DIVIDER;

pwm pwm_generator(clk, enable, speed, PWM);

always @(posedge clk) begin
    current_state <= next_state;
    speed <= enable ? speed + acceleration : 0;
    timer <= timer_next;
    counter <= counter_next;
end

always @(*) begin
    // default values
    acceleration = 0;
    timer_next = TICKS;
    counter_next = 0;
    next_state = IDLE;
    case (current_state)
        IDLE: begin
            next_state = enable ? COUNT : IDLE;
        end

        COUNT: begin
            acceleration = 0;
            counter_next = counter + {15'd0, pulse};
            timer_next = timer - 32'd1;
            if (enable) begin
                next_state = (timer == 32'd0) ? UPDATE : COUNT;
            end else begin
                next_state = IDLE;
            end
        end

        UPDATE: begin
            acceleration = (target - counter) << LOG_DELTA;
            counter_next = 0;
            timer_next = TICKS;
            next_state = enable ? COUNT : IDLE;
        end
    endcase
end

endmodule
