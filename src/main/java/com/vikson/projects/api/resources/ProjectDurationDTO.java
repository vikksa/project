package com.vikson.projects.api.resources;

import com.vikson.projects.model.values.ProjectDuration;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.Instant;

@ApiModel(value = "Project Duration Resource")
public class ProjectDurationDTO {

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    @ApiModelProperty(value = "Timestamp of the start")
    private Instant start;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    @ApiModelProperty(value = "Timestamp of the end")
    private Instant end;
    private boolean removeStart;
    private boolean removeEnd;

    @JsonCreator
    public ProjectDurationDTO(@JsonProperty(value = "start") Instant start,
                              @JsonProperty(value = "end") Instant end,
                              @JsonProperty(value = "removeStart") boolean removeStart,
                              @JsonProperty(value = "removeEnd") boolean removeEnd) {
        this.start = start;
        this.end = end;
        this.removeStart = removeStart;
        this.removeEnd = removeEnd;
    }

    public ProjectDurationDTO(ProjectDuration duration) {
        if (duration != null) {
            if(duration.getStart() != null)
                this.start = duration.getStart().toInstant();
            if(duration.getEnd() != null)
                this.end = duration.getEnd().toInstant();
        }
    }

    public Instant getStart() {
        return start;
    }

    public Instant getEnd() {
        return end;
    }

    public boolean isRemoveStart() {
        return removeStart;
    }

    public boolean isRemoveEnd() {
        return removeEnd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProjectDurationDTO that = (ProjectDurationDTO) o;

        if (start != null ? !start.equals(that.start) : that.start != null) return false;
        return end != null ? end.equals(that.end) : that.end == null;
    }

    @Override
    public int hashCode() {
        int result = start != null ? start.hashCode() : 0;
        result = 31 * result + (end != null ? end.hashCode() : 0);
        return result;
    }
}
