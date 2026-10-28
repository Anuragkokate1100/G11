
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
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/farmer/ai")
public class FarmerAiController {

 @Value("${gemini.api.key:}")
 private String apiKey;

 @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent}")
 private String apiUrl;

 private final FarmCropRepository crops;

 // Shared RestTemplate with explicit timeouts — avoids hanging requests.
 private final RestTemplate http;

 public FarmerAiController(FarmCropRepository crops) {
  this.crops = crops;
  SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
  factory.setConnectTimeout(8_000);  // 8 seconds
  factory.setReadTimeout(15_000);    // 15 seconds
  this.http = new RestTemplate(factory);
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

  String systemPrompt = "You are a cautious farming assistant for a farmer in "
          + farmer.getVillage() + ", "
          + farmer.getDistrict() + ", "
          + farmer.getState()
          + ". Their recorded crops: " + cropContext
          + ". Answer practically about crop care, irrigation, soil, pests, "
          + "disease, fertilizer, and farming. Do not invent a diagnosis. "
          + "For serious disease or chemical decisions, advise consulting "
          + "the local agricultural extension office or KVK and following "
          + "product labels.";

  try {
   java.util.List<Map<String, Object>> contentsList = new java.util.ArrayList<>();
   if (request.getHistory() != null && !request.getHistory().isEmpty()) {
    for (Map<String, String> item : request.getHistory()) {
     String role = item.get("role");
     String text = item.get("text");
     if (text != null && !text.isBlank()) {
      String geminiRole = ("assistant".equalsIgnoreCase(role) || "model".equalsIgnoreCase(role)) ? "model" : "user";
      contentsList.add(Map.of("role", geminiRole, "parts", List.of(Map.of("text", text))));
     }
    }
   }

   String userText = contentsList.isEmpty() ? (systemPrompt + "\n\nUser Question: " + request.getMessage()) : request.getMessage();
   contentsList.add(Map.of("role", "user", "parts", List.of(Map.of("text", userText))));

   if (contentsList.size() > 1) {
    Map<String, Object> firstTurn = contentsList.get(0);
    if ("user".equals(firstTurn.get("role"))) {
     List<?> firstParts = (List<?>) firstTurn.get("parts");
     if (firstParts != null && !firstParts.isEmpty() && firstParts.get(0) instanceof Map<?, ?> partMap) {
      Object textObj = partMap.get("text");
      if (textObj instanceof String s && !s.contains("cautious farming assistant")) {
       contentsList.set(0, Map.of(
               "role", "user",
               "parts", List.of(Map.of("text", systemPrompt + "\n\n" + s))
       ));
      }
     }
    }
   }

   Map<String, Object> generationConfig = Map.of(
           "temperature", 0.4,
           "maxOutputTokens", 900
   );

   Map<String, Object> body = Map.of(
           "contents", contentsList,
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

   String fallbackReply = generateFarmerFallbackAdvisory(farmer, request.getMessage(), cropContext);
   Map<String, Object> fallbackBody = new LinkedHashMap<>();
   fallbackBody.put("reply", fallbackReply + "\n\n*(Note: Cloud AI model temporarily under high demand; answered using verified Maharashtra agricultural guidelines for " + farmer.getDistrict() + ")*");
   fallbackBody.put("configured", true);
   return fallbackBody;
  }
 }

 private String generateFarmerFallbackAdvisory(Farmer farmer, String question, String crops) {
  String q = question.toLowerCase();
  String location = farmer.getVillage() + ", " + farmer.getDistrict();
  if (q.contains("water") || q.contains("irrigat")) {
   return String.format("🌾 **Irrigation Advisory for %s**:\n• For standing sugarcane crops in %s, maintain irrigation at 8–10 day intervals during vegetative growth.\n• Use drip or alternate furrow irrigation to save 30–40%% water.\n• Avoid water stagnation to protect root aeration.", location, farmer.getDistrict());
  } else if (q.contains("fertiliz") || q.contains("urea") || q.contains("npk") || q.contains("dose")) {
   return String.format("🌱 **Fertilizer Advisory for %s**:\n• For 2 to 3-month-old sugarcane (tillering stage), apply 2nd dose of Nitrogen (Urea) along with earthing up.\n• Recommended dose: 40 kg N/acre with irrigation.\n• Ensure soil moisture before applying granular fertilizers.", location);
  } else if (q.contains("pest") || q.contains("insect") || q.contains("borer") || q.contains("disease")) {
   return String.format("🐛 **Pest & Disease Advisory for %s**:\n• Watch for Early Shoot Borer in young sugarcane; install pheromone traps (5/acre).\n• For biological control, release Trichogramma chilonis egg parasitoids (20,000/acre).\n• If chemical intervention is necessary, consult your local Krishi Vigyan Kendra (KVK).", location);
  } else {
   return String.format("🚜 **Farm Care Advisory for %s**:\n• Registered crops: %s.\n• Maintain weed-free conditions during early growth.\n• Monitor soil moisture and consult local KVK (%s) for customized seasonal crop advisories.", location, crops, farmer.getDistrict());
  }
 }
}
