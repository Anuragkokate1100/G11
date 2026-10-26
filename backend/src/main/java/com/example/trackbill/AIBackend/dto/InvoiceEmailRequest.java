package com.example.trackbill.AIBackend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class InvoiceEmailRequest {
    @NotBlank @Email private String to;
    @NotNull private Long billId;
    private String billDate;
    private String ownerName;
    private String tractorNumber;
    private String customerName;
    private Integer tripCount;
    private BigDecimal totalWeight;
    private BigDecimal ratePerTon;
    private BigDecimal totalAmount;
    public String getTo(){return to;} public void setTo(String v){to=v;}
    public Long getBillId(){return billId;} public void setBillId(Long v){billId=v;}
    public String getBillDate(){return billDate;} public void setBillDate(String v){billDate=v;}
    public String getOwnerName(){return ownerName;} public void setOwnerName(String v){ownerName=v;}
    public String getTractorNumber(){return tractorNumber;} public void setTractorNumber(String v){tractorNumber=v;}
    public String getCustomerName(){return customerName;} public void setCustomerName(String v){customerName=v;}
    public Integer getTripCount(){return tripCount;} public void setTripCount(Integer v){tripCount=v;}
    public BigDecimal getTotalWeight(){return totalWeight;} public void setTotalWeight(BigDecimal v){totalWeight=v;}
    public BigDecimal getRatePerTon(){return ratePerTon;} public void setRatePerTon(BigDecimal v){ratePerTon=v;}
    public BigDecimal getTotalAmount(){return totalAmount;} public void setTotalAmount(BigDecimal v){totalAmount=v;}
}
