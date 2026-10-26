
package com.example.trackbill.AIBackend.controller;

import com.example.trackbill.AIBackend.dto.AiChatRequest;
import com.example.trackbill.AIBackend.model.Farmer;
import com.example.trackbill.AIBackend.repository.FarmCropRepository;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/farmer/ai")
public class FarmerAiController {

 @Value("${gemini.api.key:}")
 private String apiKey;

 @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent}")
 private String apiUrl;

 private final FarmCropRepository crops;
 private final RestTemplate http = new RestTemplate();

 public FarmerAiController(FarmCropRepository crops) {
  this.crops = crops;
 }

 @PostMapping("/chat")
 public Map<String, Object> chat(
         @Valid @RequestBody AiChatRequest request,
         Authentication auth) {

  if (auth == null
          || !(auth.getPrincipal() instanceof Farmer farmer)) {
   throw new ResponseStatusException(
           HttpStatus.UNAUTHORIZED,
           "Farmer login required"
   );
  }

  if (apiKey == null || apiKey.isBlank()) {
   throw new ResponseStatusException(
           HttpStatus.SERVICE_UNAVAILABLE,
           "AI assistant is not configured. Set GEMINI_API_KEY on the backend."
   );
  }

  String cropContext = crops
          .findAllByFarmerIdOrderByCreatedAtDesc(farmer.getId())
          .stream()
          .limit(20)
          .map(c -> c.getCropName()
                  + " (" + c.getStatus()
                  + ", " + c.getLandArea()
                  + " " + c.getAreaUnit() + ")")
          .reduce((a, b) -> a + "; " + b)
          .orElse("No crop records saved");

  String prompt = "You are a cautious farming assistant for a farmer in "
          + farmer.getVillage() + ", "
          + farmer.getDistrict() + ", "
          + farmer.getState()
          + ". Their recorded crops: " + cropContext
          + ". Answer practically about crop care, irrigation, soil, pests, "
          + "disease, fertilizer, and farming. Do not invent a diagnosis. "
          + "For serious disease or chemical decisions, advise consulting "
          + "the local agricultural extension office or KVK and following "
          + "product labels. User question: "
          + request.getMessage();

  try {
   Map<String, Object> part = Map.of("text", prompt);
   Map<String, Object> contents = Map.of(
           "parts", List.of(part)
   );

   Map<String, Object> generationConfig = Map.of(
           "temperature", 0.4,
           "maxOutputTokens", 900
   );

   Map<String, Object> body = Map.of(
           "contents", List.of(contents),
           "generationConfig", generationConfig
   );

   HttpHeaders headers = new HttpHeaders();
   headers.setContentType(MediaType.APPLICATION_JSON);

   ResponseEntity<Map> response = http.exchange(
           apiUrl + "?key=" + apiKey,
           HttpMethod.POST,
           new HttpEntity<>(body, headers),
           Map.class
   );

   Map<?, ?> result = response.getBody();

   if (result == null) {
    throw new IllegalStateException(
            "Empty response from Gemini"
    );
   }

   Object candidatesObject = result.get("candidates");

   if (!(candidatesObject instanceof List<?> candidates)
           || candidates.isEmpty()) {
    throw new IllegalStateException(
            "Gemini returned no candidates"
    );
   }

   Object candidateObject = candidates.get(0);

   if (!(candidateObject instanceof Map<?, ?> candidate)) {
    throw new IllegalStateException(
            "Invalid Gemini candidate"
    );
   }

   Object contentObject = candidate.get("content");

   if (!(contentObject instanceof Map<?, ?> content)) {
    throw new IllegalStateException(
            "Gemini response has no content"
    );
   }

   Object partsObject = content.get("parts");

   if (!(partsObject instanceof List<?> parts)) {
    throw new IllegalStateException(
            "Gemini response has no text parts"
    );
   }

   // Correctly extract text without Object + Object errors.
   StringBuilder replyBuilder = new StringBuilder();

   for (Object item : parts) {
    if (item instanceof Map<?, ?> responsePart) {
     Object text = responsePart.get("text");

     if (text instanceof String textValue) {
      replyBuilder.append(textValue);
     }
    }
   }

   String reply = replyBuilder.toString().trim();

   if (reply.isBlank()) {
    throw new IllegalStateException(
            "Gemini returned an empty response"
    );
   }

   Map<String, Object> resultBody = new LinkedHashMap<>();
   resultBody.put("reply", reply);
   resultBody.put("configured", true);

   return resultBody;

  } catch (ResponseStatusException e) {
   throw e;
  } catch (Exception e) {
   // Log the underlying error on the server for debugging.
   System.err.println(
           "Gemini API request failed: "
                   + e.getClass().getSimpleName()
                   + ": " + e.getMessage()
   );

   throw new ResponseStatusException(
           HttpStatus.BAD_GATEWAY,
           "The AI service could not respond. Please try again shortly."
   );
  }
 }
}
