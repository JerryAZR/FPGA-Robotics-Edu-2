// Zerui An
// FPGA for Robotics Education
//------------------------------------------------------------------------------
module fpga_top (
	input wire WF_CLK, WF_BUTTON,
	input bump0, bump1, bump2, bump3, bump4, bump5,
	input wire motorL_encdr, motorR_encdr,
	inout wire ir_snsrch0, ir_snsrch1, ir_snsrch2, ir_snsrch3,
			ir_snsrch4, ir_snsrch5, ir_snsrch6, ir_snsrch7,	
	output wire ir_evenLED, ir_oddLED,
	output wire motorL_pwm, motorR_pwm,
	output wire motorL_en, motorR_en,
    output reg motorL_dir, motorR_dir,
    output reg WF_LED
	);
	
// Parameters
	localparam INIT	    = 3'b000;
    localparam WAIT     = 3'b001;
	localparam SEARCH	= 3'b010;
	localparam TURN_RIGHT_1	= 3'b011;
	localparam TURN_RIGHT_2	= 3'b100;
	localparam TURN_RIGHT_3	= 3'b101;
	
// Register and Wire declaration
	reg[2:0] next_state, current_state;
	
	reg [7:0] channel_sel;
	wire [16:0] ttd0, ttd1, ttd2, ttd3, ttd4, ttd5, ttd6, ttd7; // int values
    wire [7:0] ir_color; // 1: black; 0: white
	wire [16:0] ttd_min, ttd_max; // min & max of ttd. Used for calibration
	reg [19:0] threshold, thresh_next; // Saved during calibration (INIT)
    wire bump, btn, debounce; // consolidated bump switch signals

    wire right, on_track; // signals for special ir patterns
    wire [2:0] left_sum, right_sum;

    assign bump = bump0 & bump1 & bump2 & bump3 & bump4 & bump5;

    assign ir_color[0] = ttd0 > threshold;
    assign ir_color[1] = ttd1 > threshold;
    assign ir_color[2] = ttd2 > threshold;
    assign ir_color[3] = ttd3 > threshold;
    assign ir_color[4] = ttd4 > threshold;
    assign ir_color[5] = ttd5 > threshold;
    assign ir_color[6] = ttd6 > threshold;
    assign ir_color[7] = ttd7 > threshold;

    assign on_track = ir_color[3] & ir_color[4];
    assign right = ir_color[4:0] == 5'b11111;
    assign left_sum = {2'd0, ir_color[4]} + {2'd0, ir_color[5]}
                    + {2'd0, ir_color[6]} + {2'd0, ir_color[7]};
    assign right_sum = {2'd0, ir_color[0]} + {2'd0, ir_color[1]}
                    + {2'd0, ir_color[2]} + {2'd0, ir_color[3]};

    reg driver_sel; // 0: default driver; 1: stepctl driver
    wire driverL0_en, driverR0_en;
    wire driverL0_pwm, driverR0_pwm;
    wire driverL1_en, driverR1_en;
    wire driverL1_pwm, driverR1_pwm;
    reg speedctl_en, stepctl_en;
    wire step_done;
    reg [15:0] degreeL, degreeR;
    reg [15:0] speedL, speedR;

	assign motorL_en = driver_sel ? driverL1_en : driverL0_en;
	assign motorR_en = driver_sel ? driverR1_en : driverR0_en;

    assign driverL0_en = speedctl_en;
    assign driverR0_en = speedctl_en;

    assign step_done = ~(driverL1_en | driverR1_en);

// Module instantiations
	debouncer db (WF_CLK, bump, debounce);
	falling fd (WF_CLK, debounce, btn);
	minmax8 #(17) comp (
		ttd0, ttd1, ttd2, ttd3, ttd4, ttd5, ttd6, ttd7, ttd_min, ttd_max
	);

	IRcontrol QRTX8ch (
		WF_CLK, channel_sel, 
		ir_snsrch0, ir_snsrch1, ir_snsrch2, ir_snsrch3,
		ir_snsrch4, ir_snsrch5, ir_snsrch6, ir_snsrch7,
		ttd0, ttd1, ttd2, ttd3, ttd4, ttd5, ttd6, ttd7,
		ir_evenLED, ir_oddLED
	);

    speedctl speedctlL(WF_CLK, motorL_en, motorL_encdr, speedL, motorL_pwm);
    speedctl speedctlR(WF_CLK, motorR_en, motorR_encdr, speedR, motorR_pwm);

    stepctl #(.SPEED(16'd360)) stepL(
        WF_CLK, ~bump, stepctl_en, motorL_encdr, degreeL, driverL1_en
    );
    stepctl #(.SPEED(16'd360)) stepR(
        WF_CLK, ~bump, stepctl_en, motorR_encdr, degreeR, driverR1_en
    );

// State Machine

	always @(posedge WF_CLK) begin
		current_state <= next_state;
        threshold <= thresh_next;
    end

		
	always @(*)
	begin
        channel_sel	= 8'hFF;
        thresh_next = threshold;
        next_state = INIT;
        WF_LED = 1;
        driver_sel = 0;
        speedctl_en = 0;
        stepctl_en = 0;
        motorL_dir = 0;
        motorR_dir = 0;
        degreeL = 0;
        degreeR = 0;
        speedL = 0;
        speedR = 0;
		casex(current_state)
			//callibration
			INIT: begin
                WF_LED = 0;
				if (~bump) begin
					next_state = WAIT;
                    thresh_next = ({3'd0, ttd_min} + {3'd0, ttd_max}) >> 1;
                end
				else
					next_state = INIT;
			end

            // WAIT for button press
            WAIT: begin
                next_state = WF_BUTTON ? WAIT : SEARCH;
            end

			// move forward until the sensor sees the black line
			SEARCH: begin
				driver_sel = 0;
                speedctl_en = 1;
                speedL = 16'd360;
                speedR = 16'd360;

                if (~bump) next_state = WAIT;
                else if (on_track) begin
                    next_state = TURN_RIGHT_1;
                    
                end
                else next_state = SEARCH;
			end

            TURN_RIGHT_1: begin
                driver_sel = 1;
                motorL_dir = 0;
                motorR_dir = 1;

                stepctl_en = 1;
                degreeL = 16'd240;
                degreeR = 16'd120;

                next_state = TURN_RIGHT_2;
            end

            TURN_RIGHT_2: begin
                driver_sel = 1;
                motorL_dir = 0;
                motorR_dir = 1;
                speedL = 16'd360;
                speedR = 16'd180;

                if (~bump) next_state = WAIT;
                else if (step_done) next_state = WAIT;
                else next_state = TURN_RIGHT_2;
            end

		endcase
	end
	
endmodule
