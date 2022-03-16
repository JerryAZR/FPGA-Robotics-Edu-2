// Zerui An
// FPGA for Robotics Education
//------------------------------------------------------------------------------
// Toplevel file of Bluetooth Rx test program

module fpga_top (
    input wire WF_CLK, WF_BUTTON,
    input bump0, bump1, bump2, bump3, bump4, bump5,
    input wire motorL_encdr, motorR_encdr,
    input wire ir_snsrch0,
    output wire ir_snsrch1,
    output wire ir_evenLED, ir_oddLED,
    output wire motorL_pwm, motorR_pwm,
    output wire motorL_en, motorR_en,
    output wire motorL_dir, motorR_dir,
    output reg WF_LED,
    output wire ledFL, ledFR, ledBL, ledBR
    );

// Disable all the unused signals
assign ir_evenLED = 0;
assign ir_oddLED = 0;
assign motorL_pwm = 0;
assign motorR_pwm = 0;
assign motorL_en = 0;
assign motorR_en = 0;
assign motorL_dir = 0;
assign motorR_dir = 0;

wire Rx, Tx; // Bluetooth Rx and Tx signals
assign Rx = ir_snsrch0;
assign ir_snsrch1 = Tx; // Map Bluetooth signals to ir sensor pins

assign Tx = 1'b1; // We are not using Tx in this example

wire [7:0] Rx_data; // Connected to the bluetooth Rx module
reg [7:0] counter, counter_next; // blink counter
reg [31:0] timer, timer_next;

localparam SECOND = 32'd16000000;
localparam SLEEP = 4'b0;
localparam BLINK = 4'b1;

reg [3:0] state, state_next;
reg led_next;

Rx_wrapper receiver(WF_CLK, ~WF_BUTTON, Rx, Rx_data);

assign ledFL = Rx_data[3];
assign ledFR = Rx_data[2];
assign ledBL = Rx_data[1];
assign ledBR = Rx_data[0];

always @(posedge WF_CLK) begin
    state <= WF_BUTTON ? state_next : SLEEP;
    timer <= timer_next;
    counter <= counter_next;
    WF_LED <= led_next;
end

always @(*) begin
    state_next = SLEEP;
    timer_next = 32'd0;
    counter_next = 8'd0;
    led_next = 1'b1;

    case (state)
        SLEEP: begin
            if (timer == (SECOND << 2)) begin // wake up after 2 seconds
                state_next = BLINK;
                timer_next = 32'd0;
                counter_next = Rx_data & 8'h0F; // only keep the last 4 bits
                led_next = 1'b0;
            end else begin
                state_next = SLEEP;
                timer_next = timer + 1;
                counter_next = 8'd0;
                led_next = 1'b1;
            end
        end
        BLINK: begin
            if (timer == (SECOND >> 1)) begin // time to switch LED state
                timer_next = 32'd0;
                led_next = ~WF_LED;
                // Decrement the counter if we are turning off the LED
                counter_next = WF_LED ? counter : (counter - 8'd1);
            end else begin
                timer_next = timer + 1;
                led_next = WF_LED;
                counter_next = counter;
            end

            state_next = (counter == 8'd0) ? SLEEP : BLINK;
        end 
    endcase
end
    
endmodule
