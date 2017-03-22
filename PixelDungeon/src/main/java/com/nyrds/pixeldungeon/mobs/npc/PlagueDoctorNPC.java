package com.nyrds.pixeldungeon.mobs.npc;

import com.nyrds.pixeldungeon.items.common.RatArmor;
import com.nyrds.pixeldungeon.items.common.RatHide;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.Journal;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Paralysis;
import com.watabou.pixeldungeon.actors.buffs.Roots;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.npcs.NPC;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.WndQuest;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.HashSet;
import java.util.Set;

public class PlagueDoctorNPC extends NPC {

	private static final String TXT_QUEST = Game.getVar(R.string.PlagueDoctorNPC_Quest_Reminder);
	private static final String TXT_QUEST_END = Game.getVar(R.string.PlagueDoctorNPC_Quest_End);
	private static final String TXT_QUEST_START_M = Game.getVar(R.string.PlagueDoctorNPC_Quest_Start_Male);
	private static final String TXT_QUEST_START_F = Game.getVar(R.string.PlagueDoctorNPC_Quest_Start_Female);
	private static final String TXT_QUEST_COMPLETED = Game.getVar(R.string.PlagueDoctorNPC_After_Quest);

	public PlagueDoctorNPC() {
	}
	
	@Override
	public int defenseSkill( Char enemy ) {
		return 1000;
	}
	
	@Override
	public String defenseVerb() {
		return Game.getVar(R.string.Ghost_Defense);
	}
	
	@Override
	public float speed() {
		return 0.5f;
	}
	
	@Override
	protected Char chooseEnemy() {
		return DUMMY;
	}
	
	@Override
	public void damage( int dmg, Object src ) {
	}
	
	@Override
	public void add( Buff buff ) {
	}

	@Override
	public boolean reset() {
		return true;
	}

	private static final HashSet<Class<?>> IMMUNITIES = new HashSet<>();
	static {
		IMMUNITIES.add( Paralysis.class );
		IMMUNITIES.add( Roots.class );
	}
	
	@Override
	public Set<Class<?>> immunities() {
		return IMMUNITIES;
	}

	@Override
	public boolean interact(final Hero hero) {
		getSprite().turnTo(getPos(), hero.getPos());
		if (Quest.completed) {
			GameScene.show(new WndQuest(this, TXT_QUEST_COMPLETED));
			return true;
		}

		if (Quest.given) {

			Item item = hero.belongings.getItem(RatHide.class);
			if (item != null && item.quantity() == 5) {

				item.removeItemFrom(Dungeon.hero);

				Item reward = new RatArmor();

				if (reward.doPickUp(Dungeon.hero)) {
					GLog.i(Hero.TXT_YOU_NOW_HAVE, reward.name());
				} else {
					Dungeon.level.drop(reward, hero.getPos()).sprite.drop();
				}
				Quest.complete();
				GameScene.show(new WndQuest(this, TXT_QUEST_END));
			} else {
				GameScene.show(new WndQuest(this, (Utils.format(TXT_QUEST, 5)) ) );
			}

		} else {
			String txtQuestStart = Utils.format(TXT_QUEST_START_M, 5);
			if (Dungeon.hero.getGender() == Utils.FEMININE) {
				txtQuestStart = Utils.format(TXT_QUEST_START_F, 5);
			}
			GameScene.show(new WndQuest(this, txtQuestStart));
			Quest.given = true;
			Quest.process(hero.getPos(), false);
			Journal.add(Journal.Feature.PLAGUEDOCTOR);
		}
		return true;
	}

	public static class Quest {

		private static boolean completed;
		private static boolean given;
		private static boolean processed;

		private static int   depth;

		public static void reset() {
			completed = false;
			processed = false;
			given = false;
		}

		private static final String COMPLETED = "completed";
		private static final String NODE      = "plaguedoctornpc";
		private static final String GIVEN     = "given";
		private static final String PROCESSED = "processed";
		private static final String DEPTH     = "depth";

		public static void storeInBundle(Bundle bundle) {
			Bundle node = new Bundle();

			node.put(GIVEN, given);
			node.put(DEPTH, depth);
			node.put(PROCESSED, processed);
			node.put(COMPLETED, completed);

			bundle.put(NODE, node);
		}

		public static void restoreFromBundle(Bundle bundle) {

			Bundle node = bundle.getBundle(NODE);

			if (!node.isNull()) {
				given = node.getBoolean(GIVEN);
				depth = node.getInt(DEPTH);
				processed = node.getBoolean(PROCESSED);
				completed = node.getBoolean(COMPLETED);
			}
		}

		public static void process(int pos, boolean killCheck) {
			if (given && !processed) {
				Item item = Dungeon.hero.belongings.getItem(RatHide.class);
				if (!killCheck) { return; }
				if (item != null && item.quantity() == 5) {
					processed = true;
				}
				else{
					if (Random.Int(2) == 1) {
						Dungeon.level.drop(new RatHide(), pos).sprite.drop();
					}
				}
			}
		}

		static void complete() {
			completed = true;
			Journal.remove(Journal.Feature.PLAGUEDOCTOR);
		}
	}
}