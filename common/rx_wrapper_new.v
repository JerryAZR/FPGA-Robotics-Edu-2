module Rx_wrapper (
    clk, rst,
    Rx,
    Rx_data,
);

    parameter DATA_WIDTH = 8;
    parameter BAUD_RATE = 32'd1667; // 9600 baud rate (1667 cycles per bit)

    localparam SLEEP = 32'd48000000; // Sleep if no movement after 3sec

    input wire clk, rst, Rx;
    output reg [DATA_WIDTH-1:0] Rx_data;

    wire Rx_done;
    wire [DATA_WIDTH-1:0] Rx_data_raw;
    reg [31:0] counter;

    Rx_core #(DATA_WIDTH, BAUD_RATE) core(clk, rst, Rx, Rx_data_raw, Rx_done);

    always @(posedge clk) begin
        if (rst) begin
            Rx_data <= 0;
            counter <= 0;
        end else begin
            counter <= counter +1;
            if (counter == SLEEP) begin
                Rx_data <= 0;
                counter <= 0; 
            end
            else begin 
                Rx_data <= Rx_done ? Rx_data_raw : Rx_data;
                counter <= 0;
            end
        end
    end

endmodule
