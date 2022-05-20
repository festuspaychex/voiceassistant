package com.efeyopixel.voiceassistant.controller;

import com.efeyopixel.voiceassistant.entity.Client;
import com.efeyopixel.voiceassistant.entity.Employee;
import com.efeyopixel.voiceassistant.entity.PayType;
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

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.transaction.Transactional;
import java.net.URISyntaxException;
import java.util.*;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
@Slf4j
public class VoiceAssistantControllerV2 {

    private final ObjectMapper objectMapper;
    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;
    private final PayperiodRepository payperiodRepository;


    @PostMapping(value = "/client", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> processDialogFlowRequest(@RequestBody Object requestBody) {

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
        JsonNode intent = queryResult.get("intent");
        JsonNode intentName = intent.get("displayName");
        if (action.textValue().equalsIgnoreCase("input.welcome")) {
            return queryResult.get("fulfillmentText").textValue();
        }

        if (action.textValue().equalsIgnoreCase("ConfirmClient")) {
            log.info("ConfirmClient logic.");

            //Getting the contact person and client number from user
            JsonNode parameters = queryResult.get("parameters");
            JsonNode payx_client = parameters.get("payx_client");

            String name = parameters.get("person").get("name").textValue();
            //Get client details from DB
            Optional<Client> clientById = clientRepository.findById(payx_client.textValue());
            String contactPerson = clientById.get().getContactPerson().toLowerCase();

            //If user is contact person listed and client number is present, take action
            if (clientById.isPresent() && !contactPerson.isEmpty() && contactPerson.contains(name.toLowerCase())) {

                return "Great! I've fetched the required information for " + clientById.get().getCompanyName() + ". Would you like to make any changes for " + clientById.get().getCompanyName() + ", like adding new employees, modifying employee info or the pay rate, or terminating employees?";
            } else {

                return "Sorry, it looks like I don't have the information for client " + payx_client + " or you are not the authorized contact. Please call 833-299-0168 for assistance!";
            }
        }

        if (action.textValue().equalsIgnoreCase("PayrollReport")) {

            log.info("payroll report logic");

            try {
                //Getting the contact person and client number from user
                JsonNode parameters = queryResult.get("parameters");
                JsonNode payx_client = parameters.get("payx_client");

                String name = parameters.get("person").get("name").textValue();
                //Get client details from DB
                Optional<Client> clientById = clientRepository.findById(payx_client.textValue());

                List<Employee> employees = clientById.get().getEmployees();

                if (!employees.isEmpty()) {

                    boolean check = false;
                    double pay = 0.0;
                    double vacationHours = 0.0;
                    double overtimeHours = 0.0;
                    double regularHours = 0.0;

                    int checkCount = 0;
                    int depositCount = 0;

                    for (Employee emp : employees) {
                        vacationHours += emp.getVacationHours();
                        overtimeHours += emp.getOvertimeHours();
                        if (emp.getPayType().equals(PayType.SALARIED)) {
                            regularHours += 40.0;
                            pay += emp.getPay();
                        } else {
                            double empRegHours = 40.0 - emp.getVacationHours();
                            regularHours += regularHours + empRegHours;
                            double empPay = (empRegHours * emp.getPay()) + (emp.getPay() * 1.5 * emp.getOvertimeHours());
                            pay += empPay;
                        }

                        if (emp.getModeOfPay().contains("DEPOSIT")) {
                            depositCount++;
                        } else {
                            checkCount++;
                        }
                    }
                    if (checkCount > 0) {
                        check = true;
                    }

                    return "I have a total number of " + employees.size() + " transactions with a total pay of " + pay + " and " + regularHours + " regular hours,\n" +
                            +vacationHours + " vacation hours and " + overtimeHours + " overtime hours.\n" +
                            "That amounts to " + checkCount + " live check(s) and " + depositCount + " direct deposit(s). Shall I process this payroll?";
                }
                else {
                    return "It looks like your client has no employees. Please call 833-299-0168 for assistance to add new employees.";
                }

            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        if (action.textValue().equalsIgnoreCase("PayPeriodVerification")) {
            log.info("payperiodverification logic");
            //Getting the contact person and client number from user
            JsonNode parameters = queryResult.get("parameters");
            JsonNode payx_client = parameters.get("payx_client");

            String name = parameters.get("person").get("name").textValue();
            //Get client details from DB
            Optional<Client> clientById = clientRepository.findById(payx_client.textValue());

            Payperiod payperiod = clientById.get().getPayperiods().get(0);
            if (payperiod != null) {
                return "Let’s verify your pay period, pay period begins " + payperiod.getStartDate() + " and ends " + payperiod.getEndDate() + ", for the " + payperiod.getCheckDate() + " check date, is that correct?";

            } else {
                return "Apologies, I can only process your payroll for the next pay period with check date of today. Would you still like to proceed?";
            }

        }

        if (action.textValue().equalsIgnoreCase("PayrollSubmitted")) {

            log.info("PayrollSubmitted logic.");

            //Getting the contact person and client number from user
            JsonNode parameters = queryResult.get("parameters");
            JsonNode payx_client = parameters.get("payx_client");

            //Get client details from DB
            Optional<Client> clientById = clientRepository.findById(payx_client.textValue());
            try {
                sendEmail(clientById.get().getContactEmail());
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            getEstimatedDeliveryTimes();

            return "Okay, your payroll has been processed. Please login to Paychex Flex after 30 minutes to view your reports. Would you like to process your payroll for another client as well?";
        }
        return "Sorry, I didn't get that, please repeat.";

    }

    @Transactional
    @PostMapping(value = "/add-client", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> addClient(@RequestBody Client client) {
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

    private void sendEmail(String recipientEmail) throws Exception {

        String emailPassword = "";//todo add values here

        String emailUsername = "";//todo add values here

        Properties prop = new Properties();

        prop.put("mail.smtp.auth", true);

        prop.put("mail.smtp.starttls.enable", "true");

        prop.put("mail.smtp.host", "smtp.mailtrap.io");
        prop.put("mail.smtp.port", "25");

        prop.put("mail.smtp.ssl.trust", "smtp.mailtrap.io");
        Session session = Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(emailUsername, emailPassword);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(emailUsername));
        message.setRecipients(
        Message.RecipientType.TO, InternetAddress.parse(emailUsername));
        message.setSubject("Thank you for choosing Paychex!");
        String msg = "<h3> We hope our virtual assistant was helpful! Please take a survey to rate your experience at www.survey.com/paychex</h3>\n" +
                "<h5> Please do not reply to this email. This mailbox is not monitored</h5>";
        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(msg, "text/html; charset=utf-8");

        Multipart multipart = new MimeMultipart();

        multipart.addBodyPart(mimeBodyPart);

        message.setContent(multipart);

        Transport.send(message);

    }

    private String getEstimatedDeliveryTimes() {
        Map<String, String[]> postalMap  = new HashMap<String, String[]>() {{
            put("USPS", new String[]{"First-Class Mail", "3 to 5 business days."});
            put("FedEx", new String[]{"FedEx Ground", "1 to 7 business days."});
            put("UPS", new String[]{"UPS Ground", "1 to 5 business days."});
            put("DHL eCommerce", new String[]{"DHL SmartMail Flats", "3-8 average postal days."});
        }};

        List<String> keysAsArray = new ArrayList<String>(postalMap.keySet());
        Random r = new Random();

        String carrier = keysAsArray.get(r.nextInt(keysAsArray.size()));
        String service = postalMap.get(carrier)[0];
        String deliveryTime = postalMap.get(carrier)[1];

        return "The checks will be sent via " + carrier + " using " + service + ". The estimated delivery time is " + deliveryTime;

    }

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