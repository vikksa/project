package com.vikson.projects.lambda;

import com.amazonaws.services.stepfunctions.AWSStepFunctions;
import com.amazonaws.services.stepfunctions.AWSStepFunctionsAsyncClient;
import com.amazonaws.services.stepfunctions.model.StartExecutionRequest;
import com.vikson.projects.api.resources.ProjectLocationDTO;
import com.vikson.projects.exceptions.ApiExceptionHelper;
import com.vikson.projects.model.ProjectLocation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class LambdaService {

    @Value("${vikson.aws.lambda.weatherFunctionArn}")
    private String weatherFunctionArn;

    private static final Logger log = LoggerFactory.getLogger(LambdaService.class);

    private ObjectMapper mapper = new ObjectMapper();
    private AWSStepFunctions awsStepFunctionsAsync = AWSStepFunctionsAsyncClient.builder()
            .build();


    public void retrieveWeatherData(ProjectLocation projectLocation) {
        ProjectLocationDTO projectLocationDTO = new ProjectLocationDTO(projectLocation);
        try {
            String json = mapper.writeValueAsString(projectLocationDTO);

            StartExecutionRequest startExecutionRequest = new StartExecutionRequest();
            startExecutionRequest.setInput(json);
            startExecutionRequest.setName(UUID.randomUUID().toString());
            startExecutionRequest.setStateMachineArn(weatherFunctionArn);

            awsStepFunctionsAsync.startExecution(startExecutionRequest);
        } catch (JsonProcessingException e) {
            log.error("Could not write project location dto to json", e);
            throw ApiExceptionHelper.newInternalServerError("Could not write project location dto to json", e);
        }
    }
}
