package com.vikson.projects.repositories;

import com.vikson.projects.model.TaskCount;
import com.vikson.projects.model.values.TaskCountPk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskCountRepository extends JpaRepository<TaskCount, TaskCountPk> {
}
