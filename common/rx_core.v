// Zerui An
// FPGA for Robotics Education
//------------------------------------------------------------------------------
// Core module for Bluetooth Rx

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
                if (timer == (BAUD_RATE >> 1)) begin // Wait for half a period
                    timer_next = 0;
                    state_next = READ;
                end else begin
                    timer_next = timer + 1;
                    state_next = INIT;
                end
            end

            READ: begin
                if (timer == BAUD_RATE) begin // wait for a period before each sample
                    timer_next = 0;
                    bit_counter_next = bit_counter - 1;
                    data_reg_next = {Rx, data_reg[7:1]}; // Shift in the new bit
                end else begin
                    timer_next = timer + 1;
                    bit_counter_next = bit_counter;
                    data_reg_next = data_reg;
                end

                state_next = (bit_counter == 0) ? DONE : READ;
            end

            DONE: begin
                data_reg_next = data_reg; // keep the read data unchanged

                if (timer == BAUD_RATE) begin // Read the end bit
                    if (Rx) begin
                        // All is well
                        state_next = IDLE;
                        Rx_done = 1'b1;
                    end else begin
                        // Very simple error handling: wait until Rx high
                        state_next = DONE;
                        timer_next = timer;
                    end
                end else begin
                    state_next = DONE;
                    timer_next = timer + 1;
                end
            end
        endcase
    end

endmodule
