
module rising (
    input clk,
    input signal,
    output wire is_rising
);

    reg sig1, sig2, sig3;
    always @(posedge clk) begin
        sig1 <= signal;
        sig2 <= sig1;
        sig3 <= sig2;
    end
    assign is_rising = ~sig3 & sig2;
    
endmodule

module falling (
    input clk,
    input signal,
    output wire is_falling
);

    reg sig1, sig2, sig3;
    always @(posedge clk) begin
        sig1 <= signal;
        sig2 <= sig1;
        sig3 <= sig2;
    end
    assign is_falling = ~sig2 & sig3;
    
endmodule

module debouncer (
    input clk,
    input signal,
    output reg stable
);

    parameter delay = 16'd160;

    reg [15:0] counter;

    always @(posedge clk) begin
        counter <= (signal == stable) ? 16'd0 : (counter + 16'd1);
        stable <= (counter >= delay) ? signal : stable;
    end

endmodule
