package com.vikson.projects.model.values;

import org.springframework.util.StringUtils;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class ProjectClient {

    @Column(nullable = false, name = "client_name")
    private String clientName = "";
    @Column(nullable = false, name = "client_assistant_name")
    private String assistantName = "";

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        if(StringUtils.isEmpty(clientName))
            clientName = "";
        this.clientName = clientName;
    }

    public String getAssistantName() {
        return assistantName;
    }

    public void setAssistantName(String assistantName) {
        if(StringUtils.isEmpty(assistantName))
            assistantName = "";
        this.assistantName = assistantName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProjectClient that = (ProjectClient) o;

        if (clientName != null ? !clientName.equals(that.clientName) : that.clientName != null) return false;
        return assistantName != null ? assistantName.equals(that.assistantName) : that.assistantName == null;
    }

    @Override
    public int hashCode() {
        int result = clientName != null ? clientName.hashCode() : 0;
        result = 31 * result + (assistantName != null ? assistantName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ProjectClient{" +
                "clientName='" + clientName + '\'' +
                ", assistantName='" + assistantName + '\'' +
                '}';
    }
}
