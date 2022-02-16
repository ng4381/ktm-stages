package com.ktmstages.ktmstages.dto;

import lombok.Data;

@Data
public class AssemblyOrderRemainsDTO implements Cloneable{

    private Long orderId;
    private Long extOrderId;
    private Long orderDetailId;
    private Long productId;
    private String productName;
    private int qty;
    private int qtyDone;
    private Long stageId;
    private String stageName;

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}