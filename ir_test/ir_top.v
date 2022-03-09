// Eric Schwarz Iglesias
// ECE 484/485 Senior Design
// Team 4: FPGA for Robotics Education
// roomba_top.v

// This Verilog file is to be synthesized and flashed into a WebFPGA Shasta Board
// interfaced to a TI-RSLK-MAX chassis board.
// This specific design allows you to test the values of the IR sensors by saving
// two values (in this case they are meant to be black and white) then you can move
// the robot around and the LEDs on the RSLK will reflect if the value that the
// sensor is looking at is more or less than what you saved


module fpga_top (
    input wire WF_CLK, WF_BUTTON,
    input bump0, bump1, bump2, bump3, bump4, bump5,
    input wire motorL_encdr, motorR_encdr,
    inout wire ir_snsrch0, ir_snsrch1, ir_snsrch2, ir_snsrch3,
            ir_snsrch4, ir_snsrch5, ir_snsrch6, ir_snsrch7,	
    output wire ir_evenLED, ir_oddLED,
    output wire motorL_pwm, motorR_pwm,
    output wire motorL_en, motorL_dir, motorR_en, motorR_dir,
    output reg WF_LED
    );
    
    assign motorL_en = 0;
    assign motorR_en = 0;
    
    assign motorL_pwm = 0;
    assign motorR_pwm = 0;
    
// Parameters
    localparam s0	= 3'b000;
    localparam s1	= 3'b001;
    localparam s2	= 3'b010;
    localparam s3	= 3'b011;
    localparam s4	= 3'b100;
    localparam s5	= 3'b101;
    
// Register and Wire declaration
    reg[2:0] next_state, current_state;
    
    reg [7:0] channel_sel;
    wire [16:0] ttd0, ttd1, ttd2, ttd3, ttd4, ttd5, ttd6, ttd7;
    wire [16:0] ttd_min, ttd_max;
    reg [19:0] threshold, thresh_next;
    wire bump, btn, debounce;
    wire [19:0] reading;

    assign bump = bump0 & bump1 & bump2 & bump3 & bump4 & bump5;
    assign reading = ({3'd0, ttd0} + {3'd0, ttd1} + {3'd0, ttd2} + {3'd0, ttd3}
                    + {3'd0, ttd4} + {3'd0, ttd5} + {3'd0, ttd6} + {3'd0, ttd7})
                    >> 3;
    
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

// State Machine

    always @(posedge WF_CLK) begin
        current_state <= next_state;
        threshold <= thresh_next;
    end

        
    always @(*)
    begin
        channel_sel	= 8'hFF;
        thresh_next = threshold;
        next_state = s0;
        WF_LED = 1;
        casex(current_state)
            //reset state
            s0: begin
                channel_sel	= 8'hFF;

                next_state = s1;
            end
            
            //callibration
            s1: begin
                WF_LED = 0;
                if (btn) begin
                    next_state = s3;
                    thresh_next = ({3'd0, ttd_min} + {3'd0, ttd_max}) >> 1;
                end
                else
                    next_state = s1;
            end

            // now compare
            s3: begin
                WF_LED = reading > threshold ? 1 /* black */ : 0 /* white */;
                next_state = btn ? s0 : s3;
            end
        endcase
    end
    
endmodule
