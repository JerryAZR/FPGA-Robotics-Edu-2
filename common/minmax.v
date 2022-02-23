module minmax4 (
    data0, data1, data2, data3,
    min, max
);
    parameter BIT_WIDTH = 16;

    input [BIT_WIDTH-1:0] data0, data1, data2, data3;
    output wire [BIT_WIDTH-1:0] min, max;

    wire [BIT_WIDTH-1:0] min01, min23, max01, max23;
    
    assign min01 = data0 < data1 ? data0 : data1;
    assign max01 = data0 < data1 ? data1 : data0;
    assign min23 = data2 < data3 ? data2 : data3;
    assign max23 = data2 < data3 ? data3 : data2;

    assign min = min01 < min23 ? min01 : min23;
    assign max = max01 > max23 ? max01 : max23;

endmodule

module minmax8 (
    data0, data1, data2, data3,
    data4, data5, data6, data7,
    min, max
);
    parameter BIT_WIDTH = 16;

    input [BIT_WIDTH-1:0] data0, data1, data2, data3;
    input [BIT_WIDTH-1:0] data4, data5, data6, data7;
    output wire [BIT_WIDTH-1:0] min, max;

    wire [BIT_WIDTH-1:0] min0, min1, max0, max1;

    minmax4 #(BIT_WIDTH) comp1 (data0, data1, data2, data3, min0, max0);
    minmax4 #(BIT_WIDTH) comp2 (data4, data5, data6, data7, min1, max1);

    assign min = min0 < min1 ? min0 : min1;
    assign max = max0 > max1 ? max0 : max1;

endmodule
