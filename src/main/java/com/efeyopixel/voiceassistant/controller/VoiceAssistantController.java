package com.efeyopixel.voiceassistant.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.dialogflow.v3.model.GoogleCloudDialogflowCxV3WebhookRequest;
import com.google.api.services.dialogflow.v3.model.GoogleCloudDialogflowV2WebhookResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
@Slf4j
public class VoiceAssistantController {

    private final ObjectMapper objectMapper;

    @PostMapping(value = "/client", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getClientNumber(@RequestBody Object requestBody) {
        log.info(requestBody.toString());

        GoogleCloudDialogflowCxV3WebhookRequest request = objectMapper.convertValue(requestBody, GoogleCloudDialogflowCxV3WebhookRequest.class);

        JsonNode actualObj = null;
        try {
            String jsonInString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(request);
            actualObj = objectMapper.readTree(jsonInString);

        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
        }
        GoogleCloudDialogflowV2WebhookResponse response = new GoogleCloudDialogflowV2WebhookResponse();
        response.setFulfillmentText(processRequest(actualObj));
        return ResponseEntity.ok(response);
    }

    private String processRequest(JsonNode request) {
        log.info(request.toString());

        JsonNode queryResult = request.get("queryResult");
        JsonNode action = queryResult.get("action");
        if (action.textValue().equalsIgnoreCase("input.welcome")) {
            return queryResult.get("fulfillmentText").textValue();
        } else {
            return "Sorry, I didn't get that, please repeat.";
        }

    }
}
