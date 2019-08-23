package com.vikson.projects.service;

import com.vikson.projects.model.TaskCount;
import com.vikson.projects.model.values.TaskCountPk;
import com.vikson.projects.repositories.TaskCountRepository;
import com.vikson.projects.service.helpers.TaskCoutnCacheHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TaskCountCacheComponent {

    @Autowired
    private TaskCountRepository repository;
    @Autowired
    private TaskCoutnCacheHelper helper;

    public TaskCount getTaskCount(UUID planId) {
        TaskCountPk pk = new TaskCountPk(getUsername(), planId);
        TaskCount currentCount = repository.findById(pk)
            .orElseGet(() -> new TaskCount(pk.getUsername(), planId, 0, 0));
        if (currentCount.getTotal() == 0) {
            //Immediate Cache Update
            return helper.updateTaskCount(currentCount);
        }
        helper.updateTaskCountAsync(currentCount, SecurityContextHolder.getContext());
        return currentCount;
    }

    private String getUsername() {
        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
    }

}
