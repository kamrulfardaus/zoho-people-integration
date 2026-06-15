package com.zoho.getemp.controller;

import com.zoho.getemp.dto.ZohoInsertEmployeeRequest;
import com.zoho.getemp.dto.ZohoInsertResponse;
import com.zoho.getemp.dto.ZohoLeaveInsertRequest;
import com.zoho.getemp.dto.ZohoLeaveInsertResponse;
import com.zoho.getemp.dto.ZohoUpdateEmployeeRequest;
import com.zoho.getemp.service.ZohoService;
import org.springframework.web.bind.annotation.*;

@RestController
public class ZohoController {

    private final ZohoService zohoService;

    public ZohoController(ZohoService zohoService) {
        this.zohoService = zohoService;
    }

    @GetMapping("/zoho/raw")
    public String getZohoRaw(
          //  @RequestParam String token,
            @RequestParam(defaultValue = "1") int sIndex,
            @RequestParam(defaultValue = "2") int limit
    ) {
        return zohoService.fetchEmployeesRaw(sIndex, limit);
    }

    @GetMapping("/zoho/parsed")
    public Object getZohoParsed(
           // @RequestParam String token,
            @RequestParam(defaultValue = "1") int sIndex,
            @RequestParam(defaultValue = "2") int limit
    ) throws Exception {
        return zohoService.fetchAndParseEmployees(sIndex, limit);
    }

    @PostMapping("/zoho/sync")
    public String syncZoho(
            //@RequestParam String token,
            @RequestParam(defaultValue = "1") int sIndex,
            @RequestParam(defaultValue = "10") int limit
    ) throws Exception {
        int count = zohoService.syncEmployees(sIndex, limit);
        return "Synced employees: " + count;
    }

    @PostMapping("/zoho/sync-all")
    public String syncAll() throws Exception {
        int total = zohoService.syncAllEmployees();
        return "Total synced: " + total;
    }
    @PostMapping("/zoho/update-employee")
    public String updateEmployee(@RequestBody ZohoUpdateEmployeeRequest request) throws Exception {
        return zohoService.updateEmployeeRecord(request);
    }

    @PostMapping("/zoho/insert-employee")
    public ZohoInsertResponse insertEmployee(@RequestBody ZohoInsertEmployeeRequest request) throws Exception {
        return zohoService.insertEmployee(request);
    }

    @PostMapping("/zoho/insert-leave")
public ZohoLeaveInsertResponse insertLeave(@RequestBody ZohoLeaveInsertRequest request) throws Exception {
    return zohoService.insertLeave(request);
}

@GetMapping("/zoho/leave-records/raw")
public String getLeaveRecordsRaw(
        @RequestParam String from,
        @RequestParam String to
) {
    return zohoService.fetchLeaveRecordsRaw(from, to);
}

@GetMapping("/zoho/leave-types")
public String getLeaveTypes(
        @RequestParam String userId
) {
    return zohoService.getLeaveTypeDetails(userId);
}
}