package com.voltrex.bank.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "job_run", uniqueConstraints = @UniqueConstraint(columnNames = {"job_name", "period_year", "period_month"}))
public class JobRun {
    @Id
    @GeneratedValue
    private Long id;
    private String jobName;
    private int periodYear;
    private int periodMonth;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private String status; // IN_PROGRESS / COMPLETED / FAILED
    private String details;
    // getters/setters
}



