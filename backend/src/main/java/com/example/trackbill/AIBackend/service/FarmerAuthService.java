package com.example.trackbill.AIBackend.service;
import com.example.trackbill.AIBackend.dto.*; import com.example.trackbill.AIBackend.exception.BadRequestException; import com.example.trackbill.AIBackend.model.Farmer; import com.example.trackbill.AIBackend.repository.FarmerRepository; import com.example.trackbill.AIBackend.security.JwtUtil;
import org.springframework.security.authentication.BadCredentialsException; import org.springframework.security.crypto.password.PasswordEncoder; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
@Service public class FarmerAuthService {
 private final FarmerRepository repo; private final PasswordEncoder encoder; private final JwtUtil jwt;
 public FarmerAuthService(FarmerRepository r,PasswordEncoder e,JwtUtil j){repo=r;encoder=e;jwt=j;}
 @Transactional public FarmerAuthResponse register(FarmerRegisterRequest r){String email=r.getEmail().trim().toLowerCase();String phone=r.getPhone().trim();if(repo.existsByEmail(email))throw new BadRequestException("An account with this email already exists.");if(repo.existsByPhone(phone))throw new BadRequestException("An account with this phone number already exists.");Farmer f=new Farmer();f.setName(r.getName().trim());f.setEmail(email);f.setPhone(phone);f.setPassword(encoder.encode(r.getPassword()));f.setVillage(r.getVillage().trim());f.setTaluka(r.getTaluka().trim());f.setDistrict(r.getDistrict().trim());f.setState(r.getState()==null||r.getState().isBlank()?"Maharashtra":r.getState().trim());f.setPincode(r.getPincode()==null?"":r.getPincode().trim());f=repo.save(f);
 FarmerAuthResponse.FarmerProfile profile=profile(f);
 // Registration deliberately returns no token: the farmer must authenticate separately.
 return new FarmerAuthResponse(true,"Farmer account created successfully. Please log in.",null,profile);}
 public FarmerAuthResponse login(LoginRequest r){Farmer f=repo.findByEmail(r.getEmail().trim().toLowerCase()).orElseThrow(()->new BadCredentialsException("Invalid email or password"));if(!encoder.matches(r.getPassword(),f.getPassword()))throw new BadCredentialsException("Invalid email or password");return response(f,"Farmer login successful!");}
 private FarmerAuthResponse response(Farmer f,String message){String token=jwt.generateToken(f.getEmail(),f.getId(),f.getName(),"FARMER");return new FarmerAuthResponse(true,message,token,profile(f));}
 private FarmerAuthResponse.FarmerProfile profile(Farmer f){return new FarmerAuthResponse.FarmerProfile(f.getId(),f.getName(),f.getEmail(),f.getPhone(),f.getVillage(),f.getTaluka(),f.getDistrict(),f.getState(),f.getPincode());}
}
