package com.voltrex.bank.repositories;

import com.voltrex.bank.entities.JobRun;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JobRunRepository extends JpaRepository<JobRun, Long> {
    Optional<JobRun> findByJobNameAndPeriodYearAndPeriodMonth(String jobName, int year, int month);
}