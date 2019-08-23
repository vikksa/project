package com.vikson.projects.exceptions;

import com.vikson.apierrors.ApiException;
import com.vikson.services.users.resources.Privilege;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.vikson.apierrors.ApiErrors.apiError;

public class ApiExceptionHelper {

    private ApiExceptionHelper() {
    }

    public static ApiException newConflictError(ErrorCodes code, String value) {
        return apiError().conflict().code(code.getCode()).info("value", value).getApiException();
    }

    public static ApiException newLimitExceedError(ErrorCodes code) {
        return apiError().paymentRequired().code(code.getCode()).getApiException();
    }
    public static ApiException newResourceNotFoundError(String resourceName, UUID id) {
        return apiError().notFound()
            .code(ErrorCodes.ENTITY_NOT_FOUND.getCode())
            .info("entityName", resourceName)
            .info("id", Objects.toString(id))
            .getApiException();
    }

    public static void throwNewForbiddenError(ErrorCodes code) {
        apiError()
            .forbidden()
            .code(code.getCode())
            .throwException();
    }

    public static ApiException newForbiddenError(ErrorCodes code, String value) {
        return apiError()
            .forbidden()
            .info("value", value)
            .code(code.getCode())
            .getApiException();
    }

    public static ApiException newInternalServerError(String reason, Throwable cause ) {
        return apiError()
            .internalServerError()
            .code(ErrorCodes.INTERNAL_SERVER_ERROR.getCode())
            .cause(cause)
            .customMessage(reason)
            .getApiException();
    }
    public static ApiException newInternalServerError(String reason) {
       return apiError()
            .internalServerError()
            .code(ErrorCodes.INTERNAL_SERVER_ERROR.getCode())
            .customMessage(reason)
            .getApiException();
    }

    public static ApiException newUnPrivilegedError(Privilege... privileges) {
        return apiError()
            .forbidden()
            .code(ErrorCodes.PRIVILEGE_REQUIRED.getCode())
            .info("value", Arrays.stream(privileges).map(Privilege::toString).collect(Collectors.joining(",")))
            .getApiException();
    }

    public static ApiException newUnPrivilegedError(String message) {
        return apiError()
            .forbidden()
            .code(ErrorCodes.PRIVILEGE_REQUIRED.getCode())
            .info("value", message)
            .getApiException();
    }

    public static ApiException newBadRequestError(ErrorCodes errorCode, Throwable e) {
        return apiError()
            .code(errorCode.getCode())
            .cause(e)
            .badRequest()
            .getApiException();
    }

    public static ApiException newBadRequestError(ErrorCodes errorCode, String value, Throwable cause) {
        return apiError()
            .code(errorCode.getCode())
            .cause(cause)
            .info("value", value)
            .badRequest()
            .getApiException();
    }

    public static ApiException newBadRequestError(ErrorCodes errorCode, String value) {
        return apiError()
            .code(errorCode.getCode())
            .info("value", value)
            .badRequest()
            .getApiException();
    }
    public static ApiException newBadRequestError(ErrorCodes errorCode, String value, String value2) {
        return apiError()
            .code(errorCode.getCode())
            .info("value", value)
            .info("value2", value2)
            .badRequest()
            .getApiException();
    }
    public static ApiException newBadRequestError(ErrorCodes errorCode) {
        return apiError()
            .code(errorCode.getCode())
            .badRequest()
            .getApiException();
    }
}
