// Zerui An
// FPGA for Robotics Education
//------------------------------------------------------------------------------
// Core module for Bluetooth Rx (starter code)

module Rx_core (
    clk, rst,
    Rx,
    Rx_data,
    Rx_done
);

    parameter DATA_WIDTH = 8;
    parameter BAUD_RATE = 32'd1667; // 9600 baud rate (1667 cycles per bit)

    input wire clk, rst, Rx;
    output wire [DATA_WIDTH-1:0] Rx_data;
    output reg Rx_done;

    reg [7:0] data_reg, data_reg_next;
    reg [1:0] state, state_next;
    reg [3:0] bit_counter, bit_counter_next;
    reg [31:0] timer, timer_next;

    localparam IDLE = 2'd0;
    localparam INIT = 2'd1;
    localparam READ = 2'd2;
    localparam DONE = 2'd3;

    assign Rx_data = data_reg;
    
    always @(posedge clk) begin
        state <= rst ? IDLE : state_next;
        bit_counter <= bit_counter_next;
        timer <= timer_next;
        data_reg <= data_reg_next;
    end

    always @(*) begin
        state_next = IDLE;
        bit_counter_next = DATA_WIDTH;
        timer_next = 0;
        data_reg_next = 0;
        Rx_done = 0;

        case (state)
            IDLE: begin
                state_next = Rx ? IDLE : INIT;
            end

            INIT: begin
                // TODO: Wait for half a period before moving to the next state.
            end

            READ: begin
                // TODO: Wait for a period before each sample. Repeat 8 times.
            end

            DONE: begin
                // TODO: Update data_reg with new data.
                // Remember to handle the stop bit.
            end
        endcase
    end

endmodule
