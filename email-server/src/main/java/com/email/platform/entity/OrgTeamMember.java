package com.email.platform.entity;

/**
 * 团队成员关系实体 —— Hibernate HQL JOIN 用。
 * 映射 org_team_member 表。
 */
public class OrgTeamMember {
    private Long id;
    private Long teamId;
    private Long memberId;

    public Long getId() { return id; }
    public void setId(Long v) { this.id = v; }
    public Long getTeamId() { return teamId; }
    public void setTeamId(Long v) { this.teamId = v; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long v) { this.memberId = v; }
}
