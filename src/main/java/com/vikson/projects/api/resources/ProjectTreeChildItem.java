package com.vikson.projects.api.resources;

import com.vikson.projects.model.values.BimplusProjectConfig;
import com.vikson.projects.model.values.BimplusProjectState;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectTreeChildItem implements Serializable {

    public static ProjectTreeChildItem from(ResultSet resultSet, int rowNum) throws SQLException {
        return new ProjectTreeChildItem(UUID.fromString(resultSet.getString("id")),
                resultSet.getString("name"),
                UUID.fromString(resultSet.getString("parent_id")),
                Type.valueOf(resultSet.getString("type")),
                resultSet.getBoolean("active"),
                resultSet.getTimestamp("actual_last_activity"),
                resultSet.getLong("items"),
                new BimplusProjectConfig(
                        resultSet.getString("bimplus_id") == null ? null : UUID.fromString(resultSet.getString("bimplus_id")),
                        resultSet.getString("bimplus_project_name"),
                        resultSet.getString("bimplus_project_state") == null ? null : BimplusProjectState.valueOf(resultSet.getString("bimplus_project_state")),
                        resultSet.getString("team_slug")));
    }

    private Type type;
    private UUID id;
    private String name;
    private UUID parentId;
    private boolean active;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    private Instant lastActivity;
    private Long items;
    private BimplusProjectConfigDto bimplusProjectConfigDto;

    private ProjectTreeChildItem(UUID id, String name, UUID parentId, Type type, boolean active, Timestamp lastActivity, Long items, BimplusProjectConfig bimplusProjectConfig) {
        this.id = id;
        this.name = name;
        this.parentId = parentId;
        this.type = type;
        this.active = active;
        this.lastActivity = lastActivity.toInstant();
        this.items = items;
        this.bimplusProjectConfigDto = toBimplusProjectConfigDto(bimplusProjectConfig);
    }

    private BimplusProjectConfigDto toBimplusProjectConfigDto(BimplusProjectConfig bimplusProjectConfig) {
        if ((bimplusProjectConfig == null) || (bimplusProjectConfig.getId() == null &&
                bimplusProjectConfig.getProjectName() == null &&
                bimplusProjectConfig.getState() == null &&
                bimplusProjectConfig.getTeamSlug() == null)) {
            return null;
        } else {
            return new BimplusProjectConfigDto(bimplusProjectConfig);
        }
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public UUID getParentId() {
        return parentId;
    }

    public Type getType() {
        return type;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getLastActivity() {
        return lastActivity;
    }

    public Long getItems() {
        return items;
    }

    public Permission[] getPermissions() {
        return Permission.values();
    }

    public enum Type {
        Folder, Project
    }

    public BimplusProjectConfigDto getBimplusProjectConfigDto() {
        return bimplusProjectConfigDto;
    }

    public enum Permission {
        ViewSettings,
        EditSettings,
        CreatePlan,
        CreatePlanFolder,
        ViewTeamMember,
        EditTeamMember,
        ViewPins,
        CreatePins,
        ViewTasks,
        CreateTasks,
        ViewComments,
        CreateComments,
        ViewMedia,
        CreateVisuals,
        CreateAudios,
        CreateNotes,
        CreateDocuments,
        ViewDataSets,
        CreateDataSets,
        ViewGroups,
        CreateGroups,
        CreateReports,
        CloseTasks,
        DelegateTasks,

        ManagePins,
        ManageTasks,
        ManagePosts,
        ManageComments,
        ManageGroups
    }
}
