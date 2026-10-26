package com.example.trackbill.AIBackend.dto;
import jakarta.validation.constraints.*;
public class FarmerRegisterRequest {
 @NotBlank @Size(max=100) private String name;
 @NotBlank @Email @Size(max=100) private String email;
 @NotBlank @Pattern(regexp="^[0-9+() -]{10,20}$",message="Enter a valid phone number") private String phone;
 @NotBlank @Size(min=8,max=72) private String password;
 @NotBlank @Size(max=100) private String village;
 @NotBlank @Size(max=100) private String taluka;
 @NotBlank @Size(max=100) private String district;
 @Size(max=100) private String state="Maharashtra";
 @Pattern(regexp="^$|^[0-9]{6}$",message="Pincode must contain 6 digits") private String pincode;
 public String getName(){return name;} public void setName(String v){name=v;}
 public String getEmail(){return email;} public void setEmail(String v){email=v;}
 public String getPhone(){return phone;} public void setPhone(String v){phone=v;}
 public String getPassword(){return password;} public void setPassword(String v){password=v;}
 public String getVillage(){return village;} public void setVillage(String v){village=v;}
 public String getTaluka(){return taluka;} public void setTaluka(String v){taluka=v;}
 public String getDistrict(){return district;} public void setDistrict(String v){district=v;}
 public String getState(){return state;} public void setState(String v){state=v;}
 public String getPincode(){return pincode;} public void setPincode(String v){pincode=v;}
}
