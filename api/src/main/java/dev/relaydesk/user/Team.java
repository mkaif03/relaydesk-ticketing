package dev.relaydesk.user;

import dev.relaydesk.common.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "teams")
public class Team extends BaseEntity {

    private String name;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
