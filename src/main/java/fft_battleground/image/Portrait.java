package fft_battleground.image;

import java.nio.file.Path;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.model.BattleGroundTeam;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Portrait implements Comparable<Portrait> {
	private String className;
	private String gender;
	private BattleGroundTeam color;
	private String location;
	
	public Portrait() {}
	
	public Portrait(Path path) {
		String fileName = path.getFileName().toString();
		if(StringUtils.contains(fileName, "M_")) {
			this.className = StringUtils.substringBefore(fileName, "M_");
			this.gender = "Male";
			this.location = fileName;
			this.color = this.getColor(fileName);
		} else if(StringUtils.contains(fileName, "M.")) {
			this.className = StringUtils.substringBefore(fileName, "M.");
			this.gender = "Male";
			this.location = fileName;
			this.color = BattleGroundTeam.NONE;
		} else if(StringUtils.contains(fileName, "F_")) {
			this.className = StringUtils.substringBefore(fileName, "F_");
			this.gender = "Female";
			this.color = this.getColor(fileName);
		} else if(StringUtils.contains(fileName, "F.")) {
			this.className = StringUtils.substringBefore(fileName, "F.");
			this.gender = "Female";
			this.color = BattleGroundTeam.NONE;
		} else {
			this.className = StringUtils.substringBefore(fileName, ".");
			this.gender = "Monster";
			this.color = BattleGroundTeam.NONE;
		}
		
		this.location = fileName;
	}
	
	public boolean isMonster() {
		boolean result = StringUtils.equalsIgnoreCase(gender, "Monster");
		return result;
	}
	
	protected BattleGroundTeam getColor(String str) {
		String substring = StringUtils.substringBetween(str, "_", ".");
		BattleGroundTeam team = BattleGroundTeam.parse(substring);
		if(team == null) {
			team = BattleGroundTeam.NONE;
		}
		
		return team;
	}

	@Override
	public int compareTo(Portrait arg0) {
		int result = this.className.compareTo(arg0.getClassName());
		return result;
	}
}