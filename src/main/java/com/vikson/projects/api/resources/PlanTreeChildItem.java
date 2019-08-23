package com.vikson.projects.api.resources;

import com.vikson.projects.model.values.PlanType;
import com.vikson.projects.util.ParseUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@ApiModel(value = "Plan Tree Child Resource")
public class PlanTreeChildItem implements Serializable {

    public static PlanTreeChildItem from(ResultSet resultSet, Integer openTasks, Integer openPins, boolean canManagePlans) throws SQLException {
        return new PlanTreeChildItem(UUID.fromString(resultSet.getString("id")),
                resultSet.getString("name"),
                UUID.fromString(resultSet.getString("parent_id")),
                resultSet.getString("version"),
                Type.valueOf(resultSet.getString("type")),
                resultSet.getBoolean("active"),
                resultSet.getTimestamp("actual_last_activity"),
                PlanType.valueOfNullable(resultSet.getString("plan_type")),
                ParseUtils.parseUUIDNullable(resultSet.getString("current_revision_id")),
                resultSet.getInt("items"), openTasks, openPins, canManagePlans, "LINKED".equals(resultSet.getString("bimplus")));
    }

    @ApiModelProperty(value = "Type of Object (Folder, Plan)")
    private Type type;
    @ApiModelProperty(value = "Id of the Object (Folder, Plan)")
    private UUID id;
    @ApiModelProperty(value = "Name of the Object (Folder, Plan)")
    private String name;
    @ApiModelProperty(value = "Id of the Parent Folder/Project (If Root Item)")
    private UUID parentId;
    @ApiModelProperty(value = "Version of the Object (Folder, Plan)")
    private String version;
    @ApiModelProperty(value = "Whether the object is active")
    private boolean active;
    @ApiModelProperty(value = "Timestamp of the last activity in/on the Object")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    private Instant lastActivity;
    @ApiModelProperty(value = "Type of Plan, if a Plan (Map, File)")
    private PlanType planType;
    @ApiModelProperty(value = "Amount of Open Tasks in the Plan")
    private int openTasks;
    @ApiModelProperty(value = "Amount of Open Pins in the Plab")
    private int openPins;
    @ApiModelProperty(value = "Id of the current Plan Revision")
    private UUID currentRevisionId;
    @ApiModelProperty(value = "List of Permission of the current User on the Object")
    private List<Permission> permissions;
    @ApiModelProperty(value = "Amount of Objects in the Folder")
    private int items;
    @ApiModelProperty(value = "Whether it is a bimplus plan")
    private boolean bimplus;

    private PlanTreeChildItem(UUID id, String name, UUID parentId, String version, Type type, boolean active,
                              Timestamp lastActivity, PlanType planType,
                              UUID currentRevisionId, int items, int openTasks, int openPins,boolean canManagePlan,
                              boolean bimplus) {
        this.id = id;
        this.name = name;
        this.parentId = parentId;
        this.version = version;
        this.type = type;
        this.active = active;
        this.lastActivity = lastActivity.toInstant();
        this.planType = planType;
        this.currentRevisionId = currentRevisionId;
        this.items = items;
        this.openTasks = openTasks;
        this.openPins = openPins;
        if (canManagePlan) {
            permissions = Collections.singletonList(Permission.Edit);
        } else {
            permissions = Collections.emptyList();
        }
        this.bimplus = bimplus;
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

    public String getVersion() {
        return version;
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

    public PlanType getPlanType() {
        return planType;
    }

    public int getOpenTasks() {
        return openTasks;
    }

    public int getOpenPins() {
        return openPins;
    }

    public UUID getCurrentRevisionId() {
        return currentRevisionId;
    }

    public List<Permission> getPermissions() {
        return permissions;
    }

    public int getItems() {
        return items;
    }

    public boolean isBimplus() {
        return bimplus;
    }

    public enum Type {
        Folder,Plan
    }

    public enum Permission {
        Edit
    }

}
