package com.queuectl.cli;

import com.queuectl.exception.QueueCtlException;
import picocli.CommandLine;
import picocli.CommandLine.IExecutionExceptionHandler;
import picocli.CommandLine.ParseResult;

public class GlobalExceptionHandler implements IExecutionExceptionHandler {

    @Override
    public int handleExecutionException(Exception ex, CommandLine commandLine, ParseResult parseResult) throws Exception {
        if (ex instanceof QueueCtlException) {
            QueueCtlException qce = (QueueCtlException) ex;
            System.err.println(commandLine.getColorScheme().errorText("Error: " + qce.getMessage()));
            return qce.getExitCode();
        }

        // Unhandled/unexpected exceptions
        System.err.println(commandLine.getColorScheme().errorText("An unexpected error occurred: " + ex.getMessage()));
        ex.printStackTrace(System.err);
        return 1;
    }
}
