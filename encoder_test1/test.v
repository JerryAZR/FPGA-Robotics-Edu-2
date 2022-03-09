`include "pinmap.v"

module fpga_top (
    input wire WF_CLK, WF_BUTTON,
    input wire motorL_encdr, motorR_encdr,
    output wire motorL_pwm, motorR_pwm,
    output wire motorL_en, motorL_dir, motorR_en, motorR_dir,
    output wire WF_LED
    );

wire enable;

stepctl #(.SPEED(16'd8000)) motorR(WF_CLK, enable, motorR_encdr, 16'd18000, motorR_pwm, motorR_en);
stepctl #(.SPEED(16'd8000)) motorL(WF_CLK, enable, motorL_encdr, 16'd18000, motorL_pwm, motorL_en);

// button edge detector
reg btn1, btn2, btn3;
wire pulse;
always @(posedge WF_CLK) begin
    btn1 <= WF_BUTTON;
    btn2 <= btn1;
    btn3 <= btn2;
end
assign pulse = ~btn3 & btn2;

assign enable = pulse;
assign WF_LED = ~(motorR_en | motorL_en);
assign motorL_dir = 0;
assign motorR_dir = 0;

endmodule