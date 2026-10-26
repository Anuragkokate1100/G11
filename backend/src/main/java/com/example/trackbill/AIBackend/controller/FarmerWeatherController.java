package com.example.trackbill.AIBackend.controller;
import com.example.trackbill.AIBackend.model.Farmer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
@RestController @RequestMapping("/api/farmer/weather")
public class FarmerWeatherController {
 @Value("${weather.geocoding-url:https://geocoding-api.open-meteo.com/v1/search}") private String geocodingUrl;
 @Value("${weather.forecast-url:https://api.open-meteo.com/v1/forecast}") private String forecastUrl;
 private final RestTemplate http=new RestTemplate();
 @GetMapping public Map<String,Object> weather(@RequestParam(required=false) String location,Authentication auth){
  if(auth==null||!(auth.getPrincipal() instanceof Farmer farmer))throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Farmer login required");
  String place=(location==null||location.isBlank())?farmer.getVillage()+", "+farmer.getDistrict()+", "+farmer.getState():location.trim();
  try {
   String encoded=URLEncoder.encode(place,StandardCharsets.UTF_8);
   Map geo=http.getForObject(geocodingUrl+"?name="+encoded+"&count=1&language=en&format=json",Map.class);
   List results=geo==null?null:(List)geo.get("results");
   if(results==null||results.isEmpty())throw new ResponseStatusException(HttpStatus.NOT_FOUND,"No weather location found. Try a nearby town or district.");
   Map first=(Map)results.get(0); Object lat=first.get("latitude"),lon=first.get("longitude");
   String resolved=String.valueOf(first.get("name"))+(first.get("admin1")!=null?", "+first.get("admin1"):"");
   Map data=http.getForObject(forecastUrl+"?latitude="+lat+"&longitude="+lon+"&current=temperature_2m,relative_humidity_2m,apparent_temperature,is_day,precipitation,rain,showers,wind_speed_10m&hourly=precipitation_probability,temperature_2m&daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max&timezone=auto&forecast_days=5",Map.class);
   if(data==null)throw new IllegalStateException("Empty weather response");
   Map<String,Object> out=new LinkedHashMap<>();out.put("location",resolved);out.put("latitude",lat);out.put("longitude",lon);out.put("timezone",data.get("timezone"));out.put("current",data.get("current"));out.put("hourly",data.get("hourly"));out.put("daily",data.get("daily"));out.put("fetchedAt",java.time.OffsetDateTime.now().toString());return out;
  } catch(ResponseStatusException e){throw e;} catch(Exception e){throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,"Weather service is temporarily unavailable. Please try again later.");}
 }
}
