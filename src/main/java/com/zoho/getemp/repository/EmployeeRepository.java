package com.zoho.getemp.repository;

import com.zoho.getemp.dto.EmployeeDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class EmployeeRepository {

    private final JdbcTemplate jdbcTemplate;

    public EmployeeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void mergeEmployee(EmployeeDto dto) {
        String sql = """
            MERGE INTO XFACTORPRO.EMPLOYEE t
            USING (
                SELECT
                    ? AS ZOHO_ID,
                    ? AS EMPLOYEE_ID,
                    ? AS FULL_NAME,
                    ? AS FIRST_NAME,
                    ? AS LAST_NAME,
                    ? AS EMAIL_ID,
                    ? AS DESIGNATION,
                    ? AS DEPARTMENT,
                    ? AS MOBILE,
                    ? AS DATE_OF_JOINING,
                    ? AS DATE_OF_BIRTH,
                    ? AS EMPLOYEE_STATUS,
                    ? AS BLOOD_GROUP,
                    ? AS GENDER,
                    ? AS GROSS_SALARY,
                    ? AS MARITAL_STATUS,
                    ? AS NID
                FROM dual
            ) s
            ON (t.ZOHO_ID = s.ZOHO_ID)
            WHEN MATCHED THEN
                UPDATE SET
                    t.EMPLOYEE_ID     = s.EMPLOYEE_ID,
                    t.FULL_NAME       = s.FULL_NAME,
                    t.FIRST_NAME      = s.FIRST_NAME,
                    t.LAST_NAME       = s.LAST_NAME,
                    t.EMAIL_ID        = s.EMAIL_ID,
                    t.DESIGNATION     = s.DESIGNATION,
                    t.DEPARTMENT      = s.DEPARTMENT,
                    t.MOBILE          = s.MOBILE,
                    t.DATE_OF_JOINING = CASE WHEN s.DATE_OF_JOINING IS NOT NULL THEN TO_DATE(s.DATE_OF_JOINING, 'YYYY-MM-DD') ELSE NULL END,
                    t.DATE_OF_BIRTH   = CASE WHEN s.DATE_OF_BIRTH IS NOT NULL THEN TO_DATE(s.DATE_OF_BIRTH, 'YYYY-MM-DD') ELSE NULL END,
                    t.EMPLOYEE_STATUS = s.EMPLOYEE_STATUS,
                    t.BLOOD_GROUP     = s.BLOOD_GROUP,
                    t.GENDER          = s.GENDER,
                    t.GROSS_SALARY    = s.GROSS_SALARY,
                    t.MARITAL_STATUS  = s.MARITAL_STATUS,
                    t.NID             = s.NID,
                    t.UPDATED_AT      = SYSDATE
            WHEN NOT MATCHED THEN
                INSERT (
                    ZOHO_ID, EMPLOYEE_ID, FULL_NAME, FIRST_NAME, LAST_NAME,
                    EMAIL_ID, DESIGNATION, DEPARTMENT, MOBILE,
                    DATE_OF_JOINING, DATE_OF_BIRTH, EMPLOYEE_STATUS,
                    BLOOD_GROUP, GENDER, GROSS_SALARY,MARITAL_STATUS,NID,CREATED_TIME, UPDATED_AT
                )
                VALUES (
                    s.ZOHO_ID, s.EMPLOYEE_ID, s.FULL_NAME, s.FIRST_NAME, s.LAST_NAME,
                    s.EMAIL_ID, s.DESIGNATION, s.DEPARTMENT, s.MOBILE,
                    CASE WHEN s.DATE_OF_JOINING IS NOT NULL THEN TO_DATE(s.DATE_OF_JOINING, 'YYYY-MM-DD') ELSE NULL END,
                    CASE WHEN s.DATE_OF_BIRTH IS NOT NULL THEN TO_DATE(s.DATE_OF_BIRTH, 'YYYY-MM-DD') ELSE NULL END,
                    s.EMPLOYEE_STATUS, s.BLOOD_GROUP,s.GENDER, s.GROSS_SALARY,s.MARITAL_STATUS,s.NID,SYSDATE, SYSDATE
                )
            """;

        jdbcTemplate.update(sql,
                dto.getZohoId(),
                dto.getEmployeeId(),
                dto.getFullName(),
                dto.getFirstName(),
                dto.getLastName(),
                dto.getEmailId(),
                dto.getDesignation(),
                dto.getDepartment(),
                dto.getMobile(),
                dto.getDateOfJoining(),
                dto.getDateOfBirth(),
                dto.getEmployeeStatus(),
                dto.getBloodGroup(),
                dto.getGender(),
                dto.getGrossSalary(),
                dto.getMaritialStatus(),
                dto.getNid()
        );
    }
}
