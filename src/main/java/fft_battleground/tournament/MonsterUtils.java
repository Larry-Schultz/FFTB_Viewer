package fft_battleground.tournament;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.base.Functions;

import fft_battleground.exception.TournamentApiException;
import fft_battleground.model.Gender;
import fft_battleground.repo.model.PlayerSkills;
import fft_battleground.repo.util.SkillCategory;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MonsterUtils {
	private static final List<String> ELITE_MONSTERS = Arrays.asList(new String[]{"UltimaDemon", "SteelGiant", "Byblos", "Serpentarius", "Tiamat", "DarkBehemoth", "HolyDragon"});
	private static final List<String> STRONG_MONSTERS = Arrays.asList(new String[]{"Apanda", "ArchaicDemon", "KingBehemoth", "Hydra", "RedDragon", "Sehkret"});
	
	private static final List<String> JOB_SKILLS = Arrays.asList(new String[] {"BasicSkill","BattleSkill","Item","Charge","PunchArt","Elemental","Jump","DrawOut","Throw","Steal",
			"TalkSkill","Dance","Sing","WhiteMagic","BlackMagic","TimeMagic","SummonMagic","YinYangMagic","BlueMagic",});
	
	private static final List<String> REACTION_SKILLS = Arrays.asList(new String[] {"Caution","Parry","ArrowGuard","SunkenState","PASave","MASave","SpeedSave","BraveSave","FaithSave",
			"AutoPotion","HPRestore","MPRestore","AbsorbUsedMP","Regenerator","DragonSpirit","CriticalQuick","MeatboneSlash","Counter","CounterTackle","CounterMagic","CounterFlood",
			"Hamedo","DamageSplit","Catch","Earplug","Abandon","Distribute","ManaShield",});
	
	private static final List<String> MOVEMENT_SKILLS = Arrays.asList(new String[] {"Move+1","Move+2","Move+3","Jump+1","Jump+2","Jump+3","Swim","Waterbreathing","Waterwalking",
			"LavaWalking","Levitate","Move-HPUp","Move-MPUp","Teleport","Fly","IgnoreHeight","IgnoreTerrain","Retreat",});
	
	private static final List<String> EQUIPMENT_SKILLS  = Arrays.asList(new String[] {"108Gems","AngelRing","CursedRing","DefenseRing","MagicRing","ReflectRing","Bracer","DefenseArmlet",
			"DiamondArmlet","JadeArmlet","N-KaiArmlet","PowerWrist","MagicGauntlet","GenjiGauntlet","BattleBoots","FeatherBoots","GerminasBoots","RedShoes","RubberShoes","SpikeShoes",
			"SprintShoes","LeatherMantle","FeatherMantle","WizardMantle","SmallMantle","ElfMantle","VanishMantle","DraculaMantle",});
	
	private static final List<String> SUPPORT_SKILLS  = Arrays.asList(new String[] {"Concentrate","MartialArts","Maintenance","Doublehand","DualWield","Defend","ShortCharge","HalveMP",
			"Beastmaster","SecretHunt","Sicken","MonsterTalk","ThrowItem","LongStatus","ShortStatus","AttackUP","DefenseUP","MagicAttackUP","MagicDefenseUP","EquipArmor","EquipShield",
			"EquipKnife","EquipBow","EquipSword","EquipGun","EquipAxe","EquipPolearm",});
	
	private static final List<String> ENTRY_SKILLS  = Arrays.asList(new String[] {"BraveBoost","FaithBoost","FashionSense","PreferredArms","NeutralZodiac","GearedUp","HighlySkilled",
			"GilgameHeart","EXPBoost",});
	
	private static final List<String> LEGENDARY_SKILLS = Arrays.asList(new String[] {"BirbBrain", "ProgrammingUp"});
	
	@Autowired
	private TournamentService tournamentService;
	
	private static final String monsterSetCacheKey = "MONSTERSET";
	private Cache<String, Set<String>> monsterSetCache = Caffeine.newBuilder()
			  .expireAfterWrite(24, TimeUnit.HOURS)
			  .maximumSize(1)
			  .build();
	private Object monsterSetCacheLock = new Object();
	
	public Set<String> monsterSet() throws TournamentApiException {
		synchronized(this.monsterSetCacheLock) {
			Set<String> monsterSet = this.monsterSetCache.getIfPresent(monsterSetCacheKey);
			if(monsterSet == null) {
				monsterSet = this.generateMonsterSet();
				this.monsterSetCache.put(monsterSetCacheKey, monsterSet);
			}
			
			return monsterSet;
		}
	}
	
	private Set<String> generateMonsterSet() throws TournamentApiException {
		Tips tips = this.tournamentService.getCurrentTips();
		Set<String> monsterList = tips.getClassMap().keySet().parallelStream()
				.filter(className -> !StringUtils.contains(className, Gender.MALE.toString()))
				.filter(className -> !StringUtils.contains(className, Gender.FEMALE.toString()))
				.collect(Collectors.toSet());
		return monsterList;
	}
	
	/**
	 * Strong Monster and Elite Monsters skills each all share the same cooldowns.  This adds these cooldows to all related skills.
	 * @param playerSkills
	 */
	public void regulateMonsterSkillCooldowns(final Collection<PlayerSkills> playerSkills) {
		Integer strongMonsterCooldown = 0;
		Integer eliteMonsterCooldown = 0;
		List<PlayerSkills> playerSkillsWithCooldowns = playerSkills.parallelStream().filter(playerSkill -> playerSkill.getCooldown() != null && playerSkill.getCooldown() > 0).collect(Collectors.toList());
		for(PlayerSkills playerSkill : playerSkillsWithCooldowns) {
			if(playerSkill.getSkillCategory() == SkillCategory.ELITE_MONSTER) {
				eliteMonsterCooldown = playerSkill.getCooldown();
			} else if(playerSkill.getSkillCategory() == SkillCategory.STRONG_MONSTER) {
				strongMonsterCooldown = playerSkill.getCooldown();
			}
		}
		
		final Integer strongMonsterCooldownFinal = strongMonsterCooldown;
		final Integer eliteMonsterCooldownFinal = eliteMonsterCooldown;
		playerSkills.parallelStream().filter(playerSkill -> playerSkill.getSkillCategory() == SkillCategory.STRONG_MONSTER).forEach(playerSkill -> playerSkill.setCooldown(strongMonsterCooldownFinal));
		playerSkills.parallelStream().filter(playerSkill -> playerSkill.getSkillCategory() == SkillCategory.ELITE_MONSTER).forEach(playerSkill -> playerSkill.setCooldown(eliteMonsterCooldownFinal));
		//log.info("The strong monster cooldown was {} and the elite monster cooldown was {}", strongMonsterCooldown, eliteMonsterCooldown);
	}
	
	public void categorizeSkillsList(final Collection<PlayerSkills> playerSkills) throws TournamentApiException {
		List<SkillCategory> skillCategoryChanges = new ArrayList<>();
		for(PlayerSkills playerSkill : playerSkills) {
			SkillCategory category = this.categorizeSkill(playerSkill);
			playerSkill.setSkillCategory(category);
			skillCategoryChanges.add(category);
		}
		Function<SkillCategory, Long> skillCategoryCountFunction = category -> skillCategoryChanges.stream().filter(currentCategory -> currentCategory == category).count();
		Map<SkillCategory, Long> skillCategoryCounts = Arrays.stream(SkillCategory.values()).collect(Collectors.toMap(Functions.<SkillCategory>identity(), skillCategoryCountFunction));
		
		//log the categories to verify it works
		//skillCategoryCounts.keySet().forEach(skillCategory -> log.info("Categorized {} of category {}", skillCategoryCounts.get(skillCategory), skillCategory));
	}
	
	public SkillCategory categorizeSkill(final PlayerSkills playerSkill) throws TournamentApiException {
		String skillName = playerSkill.getSkill();
		SkillCategory category = SkillCategory.NORMAL;
		if(this.monsterSet().contains(skillName)) {
			category = SkillCategory.MONSTER;
			if(STRONG_MONSTERS.contains(skillName)) {
				category = SkillCategory.STRONG_MONSTER;
			} else if(ELITE_MONSTERS.contains(skillName)) {
				category = SkillCategory.ELITE_MONSTER;
			}
		} else if(JOB_SKILLS.contains(skillName)) {
			category = SkillCategory.JOB;
		} else if(REACTION_SKILLS.contains(skillName)) {
			category = SkillCategory.REACTION;
		} else if(MOVEMENT_SKILLS.contains(skillName)) {
			category = SkillCategory.MOVEMENT;
		} else if(EQUIPMENT_SKILLS.contains(skillName)) {
			category = SkillCategory.EQUIPMENT;
		} else if(SUPPORT_SKILLS.contains(skillName)) {
			category = SkillCategory.SUPPORT;
		} else if(ENTRY_SKILLS.contains(skillName)) {
			category = SkillCategory.ENTRY;
		} else if(LEGENDARY_SKILLS.contains(skillName)) {
			category = SkillCategory.LEGENDARY;
		}
		
		return category;
	}
	
	public List<String> getEliteMonsters() {
		return ELITE_MONSTERS;
	}
	
	public List<String> getStrongMonsters() {
		return STRONG_MONSTERS;
	}
}
