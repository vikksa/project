package com.vikson.projects.model.values;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.time.ZonedDateTime;

@Embeddable
public class ProjectDuration {

    @Column(name = "duration_start")
    private ZonedDateTime start;
    @Column(name = "duration_end")
    private ZonedDateTime end;

    public ZonedDateTime getStart() {
        return start;
    }

    public void setStart(ZonedDateTime start) {
        this.start = start;
    }

    public ZonedDateTime getEnd() {
        return end;
    }

    public void setEnd(ZonedDateTime end) {
        this.end = end;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProjectDuration that = (ProjectDuration) o;

        if (start != null ? !start.equals(that.start) : that.start != null) return false;
        return end != null ? end.equals(that.end) : that.end == null;
    }

    @Override
    public int hashCode() {
        int result = start != null ? start.hashCode() : 0;
        result = 31 * result + (end != null ? end.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ProjectDuration{" +
                "start=" + start +
                ", end=" + end +
                '}';
    }
}
