package com.queuectl.repository;

import com.queuectl.model.Job;
import com.queuectl.model.JobState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, String> {

    List<Job> findByState(JobState state);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Job> findFirstByStateOrderByCreatedAtAsc(JobState state);
}
