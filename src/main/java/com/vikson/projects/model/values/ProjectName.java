package com.vikson.projects.model.values;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.Optional;

@Embeddable
public class ProjectName {

    @Column(nullable = false, name = "name")
    private String name;
    @Column(nullable = false, name = "initials")
    private String initials;

    public ProjectName() {
    }

    public ProjectName(String name) {
        setName(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        Assert.isTrue(!StringUtils.isEmpty(name), "ProjectName.name must not be NULL or empty!");
        Assert.isTrue(name.length() >= 4, "ProjectName.name must have at least 4 characters!");
        this.name = name;
        if(StringUtils.isEmpty(initials)) {
            setInitials(getInitialsFromName(name).orElse(name.substring(0, 4)));
        }
    }

    public String getInitials() {
        return initials;
    }

    public void setInitials(String initials) {
        Assert.isTrue(!StringUtils.isEmpty(initials), "ProjectName.initials must not be NULL or empty!");
        Assert.isTrue(initials.length() >= 1, "ProjectName.initials must have at least 1 character!");
        this.initials = initials;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProjectName that = (ProjectName) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return initials != null ? initials.equals(that.initials) : that.initials == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (initials != null ? initials.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return name;
    }

    private static Optional<String> getInitialsFromName(String name) {
        String[] words = name.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            getFirstLetter(word)
                    .ifPresent(c -> sb.append(Character.toUpperCase(c)));
            if (sb.length() > 3)
                break;
        }
        if (sb.length() == 0) {
            return Optional.empty();
        } else {
            return Optional.of(sb.toString());
        }
    }

    private static Optional<Character> getFirstLetter(String word) {
        Optional<Character> firstLetter = Optional.empty();
        for (char c : word.toCharArray()) {
            if (Character.isAlphabetic(c)) {
                firstLetter = Optional.of(c);
                break;
            }
        }
        return firstLetter;
    }

}
