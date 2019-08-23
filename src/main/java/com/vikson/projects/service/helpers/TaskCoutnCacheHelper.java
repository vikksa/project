package com.vikson.projects.service.helpers;

import com.vikson.services.issues.TaskApiClient;
import com.vikson.projects.model.TaskCount;
import com.vikson.projects.repositories.TaskCountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Component
public class TaskCoutnCacheHelper {

    private static final int EXPIRERY_TIME_SECONDS = 60 * 5;

    @Autowired
    private TaskCountRepository repository;
    @Autowired
    private TaskApiClient apiClient;

    @Async
    @Transactional
    public void updateTaskCountAsync(TaskCount taskCount, SecurityContext context) {
        SecurityContextHolder.setContext(context);
        if (isExpired(taskCount)) {
            updateTaskCount(taskCount);
        }
    }

    public TaskCount updateTaskCount(TaskCount taskCount) {
        return Optional.ofNullable(apiClient.getTaskCountInPlan(taskCount.getPlanId()))
            .map(count -> {
                taskCount.setOpenCount(count.getOpen());
                taskCount.setDoneCount(count.getDone());
                return repository.saveAndFlush(taskCount);
            }).orElse(taskCount);
    }

    private boolean isExpired(TaskCount taskCount) {
        return taskCount.getLastModified() == null ||
            taskCount.getLastModified().toInstant().isBefore(Instant.now().minusSeconds(EXPIRERY_TIME_SECONDS));
    }

}
