package com.zoho.getemp.service;

import com.zoho.getemp.dto.ZohoInsertEmployeeRequest;
import com.zoho.getemp.dto.ZohoInsertResponse;
import com.zoho.getemp.dto.ZohoLeaveInsertRequest;
import com.zoho.getemp.dto.ZohoLeaveInsertResponse;
import com.zoho.getemp.repository.EmployeeRepository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zoho.getemp.dto.EmployeeDto;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.zoho.getemp.dto.ZohoUpdateEmployeeRequest;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ZohoService {

    private final com.zoho.getemp.service.ZohoAuthService zohoAuthService;
    private final ZohoLeaveAuthService zohoLeaveAuthService;
    private final com.zoho.getemp.repository.EmployeeRepository employeeRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${zoho.employee.url}")
    private String employeeUrl;

    public ZohoService(ZohoAuthService zohoAuthService,
                   ZohoLeaveAuthService zohoLeaveAuthService,
                   EmployeeRepository employeeRepository) {
    this.zohoAuthService = zohoAuthService;
    this.zohoLeaveAuthService = zohoLeaveAuthService;
    this.employeeRepository = employeeRepository;
}
 /*Update Record */

    public String updateEmployeeRecord(ZohoUpdateEmployeeRequest request) throws Exception {
        String accessToken = zohoAuthService.getValidAccessToken();/*zohoAuthService.refreshAccessToken();*/

        String updateUrl = "https://people.zoho.com/api/forms/json/employee/updateRecord";

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Zoho-oauthtoken " + accessToken);

        Map<String, Object> inputMap = new LinkedHashMap<>();
        putIfNotBlank(inputMap, "FirstName", request.getFirstName());
        putIfNotBlank(inputMap, "LastName", request.getLastName());
        putIfNotBlank(inputMap, "Full_Name", request.getFullName());
        putIfNotBlank(inputMap, "EmailID", request.getEmailId());
        putIfNotBlank(inputMap, "Designation", request.getDesignation());
        putIfNotBlank(inputMap, "Department", request.getDepartment());
        putIfNotBlank(inputMap, "Mobile", request.getMobile());
        putIfNotBlank(inputMap, "Employeestatus", request.getEmployeeStatus());

        String inputData = objectMapper.writeValueAsString(inputMap);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("recordId", request.getRecordId());
        body.add("inputData", inputData);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                updateUrl,
                HttpMethod.POST,
                entity,
                String.class
        );

        return response.getBody();
    }

    /* New Added For Insert Record */

    private String extractZohoId(String response) throws Exception {
        JsonNode root = objectMapper.readTree(response);

        return root
                .path("response")
                .path("result")
                .path("pkId")
                .asText();
    }

    private void put(Map<String, Object> map, String key, String value) {
        if (value != null && !value.trim().isEmpty()) {
            map.put(key, value);
        }
    }
    public ZohoInsertResponse  insertEmployee(ZohoInsertEmployeeRequest req) throws Exception {

        ZohoInsertResponse result = new ZohoInsertResponse();
        String accessToken = zohoAuthService.getValidAccessToken();/*zohoAuthService.refreshAccessToken();*/

        String url = "https://people.zoho.com/api/forms/json/employee/insertRecord";

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Zoho-oauthtoken " + accessToken);

        Map<String, Object> inputMap = new LinkedHashMap<>();

        put(inputMap, "EmployeeID", req.getEmployeeID());
        put(inputMap, "FirstName", req.getFirstName());
        put(inputMap, "LastName", req.getLastName());
        put(inputMap, "Full_Name", req.getFullName());
        put(inputMap, "Designation", req.getDesignation());
        put(inputMap, "Department", req.getDepartment());
        put(inputMap, "Mobile", req.getMobile());
        put(inputMap, "Employeestatus", req.getEmployeeStatus());
        put(inputMap, "EmailID", req.getEmailID());
        put(inputMap, "Work_Type", req.getWorkType());
        put(inputMap, "Secret_Code", req.getSecretCode());
        put(inputMap, "First_Name_B", req.getFirstNameB());
        put(inputMap, "Designation_B", req.getDesignationB());
        put(inputMap, "Fathers_Name_B", req.getFathersNameB());
        //put(inputMap, "employeeStatus", req.getStatus());

        String inputData = objectMapper.writeValueAsString(inputMap);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("inputData", inputData);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
        );

        System.out.println("Zoho insert response: " + response.getBody());
       // return extractZohoId(response.getBody());
        //return response.getBody();
        String responseBody = response.getBody();

        System.out.println("Zoho Response: " + responseBody);

       JsonNode root = objectMapper.readTree(responseBody);

        JsonNode resp = root.path("response");


        // ✅ SUCCESS CASE
        if (resp.path("status").asInt() == 0) {

            String zohoId = resp.path("result").path("pkId").asText();

            result.setSuccess(true);
            result.setZohoId(zohoId);

        } else {
            // ❌ ERROR CASE
            String errorMsg = resp.path("errors").path("message").asText();

            result.setSuccess(false);
            result.setError(errorMsg);
        }

        return result;

    }
    /* End */

    /*Get Leave */
public String fetchLeaveRecordsRaw(String fromDate, String toDate) {

    RestTemplate restTemplate = new RestTemplate();

    String url = "https://people.zoho.com/api/v2/leavetracker/leaves/records"
            + "?from=" + fromDate
            + "&to=" + toDate;

    try {

       // String accessToken = zohoAuthService.getValidAccessToken();

        String accessToken = zohoLeaveAuthService.getValidAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Zoho-oauthtoken " + accessToken);
        headers.set("Accept", "application/json");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
        );

        return response.getBody();

    } catch (org.springframework.web.client.HttpClientErrorException.Unauthorized ex) {

      //  zohoAuthService.clearToken();

       // String newToken = zohoAuthService.getValidAccessToken();
       zohoLeaveAuthService.clearToken();
       String newToken = zohoLeaveAuthService.getValidAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Zoho-oauthtoken " + newToken);
        headers.set("Accept", "application/json");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
        );

        return response.getBody();
    }
}
    /*End Get Leave */

    ////////Get Leave Type
public String getLeaveTypeDetails(String userId) {

    RestTemplate restTemplate = new RestTemplate();

   // String accessToken = zohoAuthService.getValidAccessToken();
    String accessToken = zohoLeaveAuthService.getValidAccessToken();

    String url =
            "https://people.zoho.com/people/api/leave/getLeaveTypeDetails"
            + "?userId=" + userId;

    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Zoho-oauthtoken " + accessToken);
    headers.set("Accept", "application/json");

    HttpEntity<String> entity = new HttpEntity<>(headers);

    ResponseEntity<String> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            entity,
            String.class
    );

    return response.getBody();
}
////End Get Leave Type

    public ZohoLeaveInsertResponse insertLeave(ZohoLeaveInsertRequest req) throws Exception {

    String url = "https://people.zoho.com/people/api/forms/json/leave/insertRecord";

    try {
        
        //String accessToken = zohoLeaveAuthService.getValidAccessToken();
        String accessToken = zohoAuthService.getValidAccessToken();

        return callLeaveInsertApi(req, accessToken, url);

    } catch (org.springframework.web.client.HttpClientErrorException.Unauthorized ex) {

        System.out.println("Leave insert token unauthorized. Refreshing token and retrying...");

       // zohoLeaveAuthService.clearToken();

          zohoAuthService.clearToken();

      //  String newAccessToken = zohoLeaveAuthService.getValidAccessToken();

        String newAccessToken = zohoAuthService.getValidAccessToken();

        return callLeaveInsertApi(req, newAccessToken, url);
    }
}

private ZohoLeaveInsertResponse callLeaveInsertApi(
        ZohoLeaveInsertRequest req,
        String accessToken,
        String url
) throws Exception {

    ZohoLeaveInsertResponse result = new ZohoLeaveInsertResponse();

    RestTemplate restTemplate = new RestTemplate();

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    headers.set("Authorization", "Zoho-oauthtoken " + accessToken);

    Map<String, Object> dayDetail = new LinkedHashMap<>();
    dayDetail.put("LeaveCount", req.getLeaveCount());
    dayDetail.put("Session", req.getSession());

    Map<String, Object> days = new LinkedHashMap<>();
    days.put(req.getLeaveDate(), dayDetail);

    Map<String, Object> inputMap = new LinkedHashMap<>();
    inputMap.put("Employee_ID", req.getEmployeeId());
    inputMap.put("Leavetype", req.getLeaveTypeId());
    inputMap.put("From", req.getFromDate());
    inputMap.put("To", req.getToDate());
    inputMap.put("days", days);

    String inputData = objectMapper.writeValueAsString(inputMap);

    System.out.println("Leave insert inputData: " + inputData);

    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("inputData", inputData);

    HttpEntity<MultiValueMap<String, String>> entity =
            new HttpEntity<>(body, headers);

    ResponseEntity<String> response = restTemplate.exchange(
            url,
            HttpMethod.POST,
            entity,
            String.class
    );

    String responseBody = response.getBody();

    System.out.println("Leave insert Zoho response: " + responseBody);

    result.setRawResponse(responseBody);

    JsonNode root = objectMapper.readTree(responseBody);
    JsonNode resp = root.path("response");

    int status = resp.path("status").asInt(-1);

    if (status == 0) {
        String pkId = resp.path("result").path("pkId").asText(null);

        result.setSuccess(true);
        result.setZohoId(pkId);
    } else {
        String errorMsg = null;

        if (resp.has("errors")) {
            JsonNode errors = resp.path("errors");

            if (errors.has("message")) {
                errorMsg = errors.path("message").asText();
            } else {
                errorMsg = errors.toString();
            }
        }

        if (errorMsg == null || errorMsg.isBlank()) {
            errorMsg = resp.path("message").asText("Unknown Zoho error");
        }

        result.setSuccess(false);
        result.setError(errorMsg);
    }

    return result;
}

    private void putIfNotBlank(Map<String, Object> map, String key, String value) {
        if (value != null && !value.trim().isEmpty()) {
            map.put(key, value);
        }
    }
    /*End Update Record */

    public int syncEmployees(int sIndex, int limit) throws Exception {
        List<EmployeeDto> employees = fetchAndParseEmployees( sIndex, limit);

        for (EmployeeDto dto : employees) {
            employeeRepository.mergeEmployee(dto);
        }

        return employees.size();
    }

    public int syncAllEmployees() throws Exception {
        String token = zohoAuthService.getValidAccessToken();
        int sIndex = 1;
        int limit = 200;
        int total = 0;

        while (true) {
            List<EmployeeDto> employees = fetchAndParseEmployees(sIndex, limit);

            if (employees.isEmpty()) {
                break;
            }

            for (EmployeeDto dto : employees) {
                employeeRepository.mergeEmployee(dto);
            }

            total += employees.size();

            if (employees.size() < limit) {
                break;
            }

            sIndex += limit;
        }

        return total;
    }

    public String fetchEmployeesRaw(int sIndex, int limit) {

        RestTemplate restTemplate = new RestTemplate();

        String url = "https://people.zoho.com/people/api/forms/employee/getRecords"
                + "?formName=employee"
                + "&sIndex=" + sIndex
                + "&limit=" + limit;

        try {
            // 🔑 Get token (cached or refreshed automatically)
            String accessToken = zohoAuthService.getValidAccessToken();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Zoho-oauthtoken " + accessToken);
            headers.set("Accept", "application/json");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            return response.getBody();

        } catch (org.springframework.web.client.HttpClientErrorException.Unauthorized ex) {

            // 🔄 Token expired → force refresh + retry
            System.out.println("Token expired → refreshing and retrying...");

            zohoAuthService.clearToken();

            String newToken = zohoAuthService.getValidAccessToken();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Zoho-oauthtoken " + newToken);
            headers.set("Accept", "application/json");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            return response.getBody();
        }
    }

    public List<EmployeeDto> fetchAndParseEmployees(int sIndex, int limit) throws Exception {
        String json = fetchEmployeesRaw(sIndex, limit);

        List<EmployeeDto> employees = new ArrayList<>();

        JsonNode root = objectMapper.readTree(json);
        JsonNode resultArray = root.path("response").path("result");

        for (JsonNode item : resultArray) {
            Iterator<String> keys = item.fieldNames();

            while (keys.hasNext()) {
                String zohoId = keys.next();

                JsonNode empArray = item.get(zohoId);
                if (empArray != null && empArray.isArray() && empArray.size() > 0) {
                    JsonNode emp = empArray.get(0);

                    EmployeeDto dto = new EmployeeDto();
                    dto.setZohoId(zohoId);
                    dto.setEmployeeId(getText(emp, "EmployeeID"));
                    dto.setFullName(getText(emp, "Full_Name"));
                    dto.setFirstName(getText(emp, "FirstName"));
                    dto.setLastName(getText(emp, "LastName"));
                    dto.setEmailId(getText(emp, "EmailID"));
                    dto.setDesignation(getText(emp, "Designation"));
                    dto.setDepartment(getText(emp, "Department"));
                    dto.setMobile(getText(emp, "Mobile"));
                    dto.setDateOfJoining(getText(emp, "Dateofjoining"));
                    dto.setDateOfBirth(getText(emp, "Date_of_birth"));
                    dto.setEmployeeStatus(getText(emp, "Employeestatus"));
                    dto.setBloodGroup(getText(emp,"Blood_Group"));
                    dto.setDateOfConfirmation(getText(emp,"DateofConfirmation"));
                    dto.setGender(getText(emp,"Gender"));
                    dto.setGrossSalary(getText(emp,"Gross_Salary"));
                    dto.setMaritialStatus(getText(emp,"Marital_status"));
                    dto.setNid(getText(emp,"NID"));
                    dto.setpaymentType(getText(emp,"Payment_Type"));
                    dto.setPermanentAddress(getText(emp,"Permanent_Address"));
                    dto.setPresentAddress(getText(emp,"Present_Address"));
                    dto.setPhoneAllowence(getText(emp,""));
                    dto.setReligion(getText(emp,"Religion"));



                    dto.setRawJson(emp.toString());

                    employees.add(dto);
                }
            }
        }

        return employees;
    }

    private String getText(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        return field == null || field.isNull() ? null : field.asText();
    }

}