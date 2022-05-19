package com.efeyopixel.voiceassistant.controller;

import com.efeyopixel.voiceassistant.entity.Client;
import com.efeyopixel.voiceassistant.entity.Employee;
import com.efeyopixel.voiceassistant.entity.Payperiod;
import com.efeyopixel.voiceassistant.repository.ClientRepository;
import com.efeyopixel.voiceassistant.repository.EmployeeRepository;
import com.efeyopixel.voiceassistant.repository.PayperiodRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.dialogflow.v3.model.GoogleCloudDialogflowCxV3WebhookRequest;
import com.google.api.services.dialogflow.v3.model.GoogleCloudDialogflowV2WebhookResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
@Slf4j
public class VoiceAssistantControllerV2 {

    private final ObjectMapper objectMapper;
    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;
    private final PayperiodRepository payperiodRepository;

    Map<String, Object> database = new ConcurrentHashMap<>();

    @PostConstruct
    public void initializeDB() {
        database.put("12345678", "CompanyB");
        database.put("12345676", "Company6");
        database.put("12345675", "Company5");
        database.put("12345674", "Company4");
        log.info("db size {}",database.isEmpty());
    }

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

    @PostMapping(value = "/dialogFlowWebHook", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getClientDetails(@RequestBody Object requestBody) {
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
        try {
            JsonNode queryResult = request.get("queryResult");
            JsonNode action = queryResult.get("action");
            JsonNode intent = queryResult.get("intent");
            JsonNode intentName = intent.get("displayName");
            if (action.textValue().equalsIgnoreCase("input.welcome")) {
                return queryResult.get("fulfillmentText").textValue();
            }
            if (intentName.textValue().equals("Yes Process Payroll Intent")) {
                JsonNode parameters = queryResult.get("parameters");
                if (parameters != null) {
                    JsonNode payx_client = parameters.get("payx_client");
                    Object clientName = database.get(payx_client.textValue());
                    return clientName.toString();
                } else
                    return "Can I confirm your client is Donuts To Go?";
            } else {
                return "Sorry, I didn't get that, please repeat.";
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return ex.getMessage();
        }
    }

    @Transactional
    @PostMapping(value = "/add-client", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> addClient(@RequestBody Client client) throws URISyntaxException {
        log.info("adding client {}", client);

        Client save = clientRepository.save(client);
        if (!client.getEmployees().isEmpty()) {
            save.setEmployees(client.getEmployees());
            employeeRepository.saveAll(save.getEmployees());
        }

        if (!client.getPayperiods().isEmpty()) {
            save.setPayperiods(client.getPayperiods());
            payperiodRepository.saveAll(save.getPayperiods());
        }
        return ResponseEntity.ok(save);
    }

//    @PostMapping(value = "/add-employee", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<?> addWorkers(@RequestBody List<Employee> employees) throws URISyntaxException {
//        List<Employee> s = employeeRepository.saveAll(employees);
//        return ResponseEntity.ok("successfully added " + s.size() + " employees!");
//    }
//
//    @PostMapping(value = "/add-payperiods", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<?> addPayroll(@RequestBody List<Payperiod> payperiods) throws URISyntaxException {
//        List<Payperiod> s = payperiodRepository.saveAll(payperiods);
//        return ResponseEntity.ok("successfully added " + s.size() + " payperiods!");
//    }

    @GetMapping("/clients")
    public ResponseEntity<?> getClients() {
        return ResponseEntity.ok(clientRepository.findAll());
    }
    @GetMapping("/employees")
    public ResponseEntity<?> getEmployees() {
        return ResponseEntity.ok(employeeRepository.findAll());
    }

    @GetMapping("/payperiods")
    public ResponseEntity<?> getPayperiods() {
        return ResponseEntity.ok(payperiodRepository.findAll());
    }
}