package fft_battleground.repo.model;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import lombok.AllArgsConstructor;
import lombok.Data;

@Entity
@Table(name = "team_info_skills")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Data
@AllArgsConstructor
public class TeamInfoSkill {

    @Id
	@SequenceGenerator(name="team_info_skill_generator", sequenceName = "team_info_skill_seq")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "team_info_skill_generator")
    @Column(name = "team_info_skill_id", nullable = false)
	private Long id;
    
    @Column(name="skill", nullable=false)
	private String skill;
    
	@ManyToOne
    @JoinColumn
    private TeamInfo teamInfo;
	
	public TeamInfoSkill() {}
	
	public TeamInfoSkill(String skill, TeamInfo teamInfo) {
		this.skill = skill;
		this.teamInfo = teamInfo;
	}
}
