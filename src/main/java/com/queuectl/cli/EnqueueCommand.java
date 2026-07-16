package com.queuectl.cli;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.queuectl.model.Job;
import com.queuectl.service.JobService;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Component
@Command(name = "enqueue", description = "Add a new job to the queue")
public class EnqueueCommand implements Runnable {

    private final JobService jobService;
    private final Gson gson;

    @Parameters(index = "0", description = "JSON string containing job details")
    private String jsonString;

    public EnqueueCommand(JobService jobService) {
        this.jobService = jobService;
        this.gson = new Gson();
    }

    @Override
    public void run() {
        JsonObject json;
        try {
            json = gson.fromJson(jsonString, JsonObject.class);
            if (json == null || !json.has("id") || !json.has("command")) {
                throw new com.queuectl.exception.InvalidInputException("Missing 'id' or 'command' in JSON input. Expected format: '{\"id\":\"job1\",\"command\":\"echo hello\"}'");
            }
            String id = json.get("id").getAsString();
            String command = json.get("command").getAsString();

            Job job = jobService.enqueue(id, command);
            System.out.println("Job enqueued successfully: " + job.getId());
        } catch (com.google.gson.JsonSyntaxException e) {
            throw new com.queuectl.exception.InvalidInputException("Invalid JSON format. Expected format: '{\"id\":\"job1\",\"command\":\"echo hello\"}'", e);
        } catch (com.queuectl.exception.InvalidInputException e) {
            throw e;
        } catch (Exception e) {
            throw new com.queuectl.exception.QueueCtlException("Failed to enqueue job: " + e.getMessage(), e, 1);
        }
    }
}
