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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private ConfigService configService;

    @InjectMocks
    private JobService jobService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void enqueueCreatesPendingJob() {
        when(configService.getMaxRetries()).thenReturn(3);

        when(jobRepository.save(any(Job.class)))
                .thenAnswer(i -> i.getArgument(0));

        Job job = jobService.enqueue("job1", "echo hello");

        assertEquals("job1", job.getId());
        assertEquals(JobState.PENDING, job.getState());
        assertEquals(3, job.getMaxRetries());

        verify(jobRepository).save(any(Job.class));
    }

    @Test
    void claimNextJobMarksProcessing() {

        Job job = new Job();
        job.setId("job1");
        job.setState(JobState.PENDING);

        when(jobRepository.findFirstByStateOrderByCreatedAtAsc(JobState.PENDING))
                .thenReturn(Optional.of(job));

        when(jobRepository.save(any(Job.class)))
                .thenAnswer(i -> i.getArgument(0));

        Optional<Job> claimed = jobService.claimNextJob();

        assertTrue(claimed.isPresent());
        assertEquals(JobState.PROCESSING, claimed.get().getState());
    }

    @Test
    void markCompletedChangesState() {

        Job job = new Job();
        job.setId("job1");

        when(jobRepository.findById("job1"))
                .thenReturn(Optional.of(job));

        jobService.markCompleted("job1");

        assertEquals(JobState.COMPLETED, job.getState());

        verify(jobRepository).save(job);
    }
}