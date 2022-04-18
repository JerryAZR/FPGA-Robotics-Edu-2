// Cheng Zhao 
// Refer to Zerui An - Bluetooth_top.v
// FPGA for Robotics Education
//------------------------------------------------------------------------------
// Robot Control

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
    output wire WF_LED,
    output wire ledFL, ledFR, ledBL, ledBR
    );

    // Disable all the unused signals
    assign ir_evenLED = 0;
    assign ir_oddLED = 0;

    wire Rx, Tx; // Bluetooth Rx and Tx signals
    assign Rx = ir_snsrch0;
    assign ir_snsrch1 = Tx; // Map Bluetooth signals to ir sensor pins

    assign Tx = 1'b1; // We are not using Tx in this example

    wire [7:0] Rx_data; // Connected to the bluetooth Rx module
    reg [7:0] left, left_next;
    reg [7:0] right, right_next;
    reg bump0, bump1, bump2, bump3, bump4, bump5;
    reg [5:0] bumper;
    wire [15:0] left_spd, right_spd;
    wire PWM_L, PWM_R;

    localparam REST = 8'b0;

    Rx_wrapper receiver(WF_CLK, ~WF_BUTTON, Rx, Rx_data);

    // LEDs for debugging
    assign WF_LED = Rx_data[7];
    assign ledFL = left[3];
    assign ledFR = left[2];
    assign ledBL = left[1];
    assign ledBR = left[0];

    always @(posedge WF_CLK) begin
        left <= WF_BUTTON ? left_next : REST;
        right <= WF_BUTTON ? right_next : REST;
    end    

    always @(*) begin
        left_next = 8'b0;
        right_next = 8'b0;
        bump0, bump1, bump2, bump3, bump4, bump5 = 0;
        bumper = {bump0,nump1,bump2,bump3,bump4,bump5};
        for (int i=0;i<5;i++) begin
          if (bumper[i] = 1) begin
            if (Rx_data[7]) begin
              if (Rx_data[5] == 0) begin
                left_next = 0;
                right_next = 0;
              end 
              else begin
                if (Rx_data[6] == 0) begin
                  left_next = {2'b1,Rx_data[5:0]};
                  right_next = right;
                end 
                else begin
                  right_next = {2'b1,Rx_data[5:0]};
                  left_next = left;
                end
              end
            end
          end
          else begin
            if (Rx_data[7]) begin
              if (Rx_data[6] == 0) begin
                left_next = {2'b1,Rx_data[5:0]};
                right_next = right;
              end 
              else begin
                right_next = {2'b1,Rx_data[5:0]};
                left_next = left;
              end
            end
          end
        end
    end    
    
    speedctl2 #(.LOG_DIV(3), .LOG_PROP(4), .LOG_DERIV(3)) leftCtl(
        WF_CLK,WF_BUTTON,motorL_encdr,left_spd,PWM_L
    );
    speedctl2 #(.LOG_DIV(3), .LOG_PROP(4), .LOG_DERIV(3)) rightCtl(
        WF_CLK,WF_BUTTON,motorR_encdr,right_spd,PWM_R
    );
    
    assign left_spd = {6'd0, left[4:0], 5'd0};
    assign right_spd = {6'd0, right[4:0], 5'd0};
    assign motorL_pwm = PWM_L;
    assign motorR_pwm = PWM_R; 
    assign motorL_dir = left[5];
    assign motorR_dir = right[5];
    assign motorL_en = left[6];
    assign motorR_en = right[6];
            
endmodule          
