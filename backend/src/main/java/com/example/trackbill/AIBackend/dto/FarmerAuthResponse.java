package com.example.trackbill.AIBackend.dto;
public class FarmerAuthResponse {
 private boolean success; private String message; private String token; private FarmerProfile profile;
 public FarmerAuthResponse(){}
 public FarmerAuthResponse(boolean s,String m,String t,FarmerProfile p){success=s;message=m;token=t;profile=p;}
 public boolean isSuccess(){return success;} public void setSuccess(boolean v){success=v;}
 public String getMessage(){return message;} public void setMessage(String v){message=v;}
 public String getToken(){return token;} public void setToken(String v){token=v;}
 public FarmerProfile getProfile(){return profile;} public void setProfile(FarmerProfile v){profile=v;}
 public static class FarmerProfile {
  private Long id; private String name,email,phone,village,taluka,district,state,pincode;
  public FarmerProfile(){} public FarmerProfile(Long id,String name,String email,String phone,String village,String taluka,String district,String state,String pincode){this.id=id;this.name=name;this.email=email;this.phone=phone;this.village=village;this.taluka=taluka;this.district=district;this.state=state;this.pincode=pincode;}
  public Long getId(){return id;} public String getName(){return name;} public String getEmail(){return email;} public String getPhone(){return phone;} public String getVillage(){return village;} public String getTaluka(){return taluka;} public String getDistrict(){return district;} public String getState(){return state;} public String getPincode(){return pincode;}
 }
}
