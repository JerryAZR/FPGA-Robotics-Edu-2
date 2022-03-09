// Eric Schwarz Iglesias
// ECE 484/485 Senior Design
// Team 4: FPGA for Robotics Education
//-------------------------------------------------------------------------------------------
// This is a generic module that outputs a pwm signal at 1kHz
// this means that the PWM period is 1ms (16000 WF_CLK cycles)
// the module takes a 16MHz clock
// an enable input that serves as a active low reset
// timeon is a value form 0 to 16000 that determines the duty cyle of the signal
// PWM is the output
module pwm (
    input wire clk,
    input enable,
    input [15:0] timeon,
    output reg PWM
    );
    
// Register Declaration
    reg [15:0] timerPWM;
    
// Datapath clocked
    always @(posedge clk)
    begin
        if (enable)
        begin
            timerPWM <= timerPWM + 1'b1;
            if (timerPWM == 16'd16000)
            begin
                timerPWM <= 0;
                PWM <= 1'b1;
            end
            
            else if (timerPWM > timeon)
                PWM <= 1'b0;
        end
        
        else
        begin
            timerPWM <=0;
            PWM <= 0;
        end
    end
endmodule
