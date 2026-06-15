package com.zoho.getemp.dto;

public class ZohoLeaveInsertRequest {
    private String employeeId;
    private String leaveTypeId;
    private String fromDate;
    private String toDate;
    private String leaveDate;
    private Double leaveCount;
    private Integer session;

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getLeaveTypeId() { return leaveTypeId; }
    public void setLeaveTypeId(String leaveTypeId) { this.leaveTypeId = leaveTypeId; }

    public String getFromDate() { return fromDate; }
    public void setFromDate(String fromDate) { this.fromDate = fromDate; }

    public String getToDate() { return toDate; }
    public void setToDate(String toDate) { this.toDate = toDate; }

    public String getLeaveDate() { return leaveDate; }
    public void setLeaveDate(String leaveDate) { this.leaveDate = leaveDate; }

    public Double getLeaveCount() { return leaveCount; }
    public void setLeaveCount(Double leaveCount) { this.leaveCount = leaveCount; }

    public Integer getSession() { return session; }
    public void setSession(Integer session) { this.session = session; }
}