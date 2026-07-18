package com.queuectl.service;

import com.queuectl.model.Job;
import com.queuectl.model.JobState;
import com.queuectl.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class RetryLogicTest {

    @Mock
    JobRepository repository;

    @Mock
    ConfigService configService;

    @InjectMocks
    JobService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void failedJobMovesToDeadAfterMaxRetries() {

        Job job = new Job();
        job.setId("job1");
        job.setAttempts(2);
        job.setMaxRetries(3);

        when(repository.findById("job1"))
                .thenReturn(Optional.of(job));

        service.markFailed("job1");

        assertEquals(JobState.DEAD, job.getState());

        verify(repository).save(job);
    }

    @Test
    void failedJobStaysFailedWhenRetriesRemain() {

        Job job = new Job();
        job.setId("job1");
        job.setAttempts(0);
        job.setMaxRetries(3);

        when(repository.findById("job1"))
                .thenReturn(Optional.of(job));

        service.markFailed("job1");

        assertEquals(JobState.FAILED, job.getState());

        verify(repository).save(job);
    }

    @Test
    void retryDeadJobResetsAttempts() {

        Job job = new Job();
        job.setId("job1");
        job.setAttempts(3);
        job.setState(JobState.DEAD);

        when(repository.findById("job1"))
                .thenReturn(Optional.of(job));

        service.retryDeadJob("job1");

        assertEquals(JobState.PENDING, job.getState());
        assertEquals(0, job.getAttempts());

        verify(repository).save(job);
    }
}