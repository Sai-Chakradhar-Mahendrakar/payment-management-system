package miniproject1.paymentmanagementsystem.model;

import java.time.LocalDateTime;

public class Team {
    private int teamId;
    private String teamName;
    private int createdByUserId;
    private LocalDateTime createdDate;

    public Team() {}

    public Team(int teamId, String teamName, int createdByUserId, LocalDateTime createdDate) {
        this.teamId = teamId;
        this.teamName = teamName;
        this.createdByUserId = createdByUserId;
        this.createdDate = createdDate;
    }

    public Team(String teamName, int createdByUserId) {
        this.teamName = teamName;
        this.createdByUserId = createdByUserId;
        this.createdDate = LocalDateTime.now();
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public int getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(int createdByUserId) {
        this.createdByUserId = createdByUserId;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public String toString() {
        return "Team{" +
                "teamId=" + teamId +
                ", teamName='" + teamName + '\'' +
                ", createdByUserId=" + createdByUserId +
                ", createdDate=" + createdDate +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Team team = (Team) o;
        return teamId == team.teamId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(teamId);
    }
}
