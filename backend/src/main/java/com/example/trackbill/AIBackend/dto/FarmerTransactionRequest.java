package com.example.trackbill.AIBackend.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
public class FarmerTransactionRequest {
 @NotBlank @Pattern(regexp="EXPENSE|INCOME") private String transactionType;
 @NotBlank @Size(max=60) private String category;
 @NotNull @DecimalMin(value="0.01") @Digits(integer=10,fraction=2) private BigDecimal amount;
 @NotNull private LocalDate transactionDate;
 @Size(max=200) private String description;
 @Size(max=150) private String farmOrCrop;
 public String getTransactionType(){return transactionType;} public void setTransactionType(String v){transactionType=v;}
 public String getCategory(){return category;} public void setCategory(String v){category=v;}
 public BigDecimal getAmount(){return amount;} public void setAmount(BigDecimal v){amount=v;}
 public LocalDate getTransactionDate(){return transactionDate;} public void setTransactionDate(LocalDate v){transactionDate=v;}
 public String getDescription(){return description;} public void setDescription(String v){description=v;}
 public String getFarmOrCrop(){return farmOrCrop;} public void setFarmOrCrop(String v){farmOrCrop=v;}
}
