package com.nyrds.pixeldungeon.mobs.npc;

import com.nyrds.android.util.Util;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.support.RewardVideo;
import com.nyrds.pixeldungeon.windows.WndMovieTheatre;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.WndQuest;
import com.watabou.utils.Bundle;

public class ServiceManNPC extends ImmortalNPC {

	private int filmsSeen = 0;
	final private String FILMS_SEEN = "films_seen";
	private static final int BASIC_GOLD_REWARD = 150;

	public ServiceManNPC() {
		RewardVideo.init();
	}

	private int getReward(){
		return BASIC_GOLD_REWARD + (filmsSeen / 5) * 50;
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(FILMS_SEEN,filmsSeen);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		filmsSeen = bundle.optInt(FILMS_SEEN,0);
	}

	public void reward() {
		filmsSeen++;
		Dungeon.hero.collect(new Gold(getReward()));
	}

	@Override
	public boolean interact(final Hero hero) {
		getSprite().turnTo( getPos(), hero.getPos() );

		if(!Util.isConnectedToInternet()) {
			GameScene.show(new WndQuest(this, Game.getVar(R.string.ServiceManNPC_NoConnection)));
			return true;
		}

		if(filmsSeen >= getLimit()){
			GameScene.show(new WndQuest(this, Utils.format(Game.getVar(R.string.ServiceManNPC_Limit_Reached), getLimit())));
			return true;
		}

		if(RewardVideo.isReady()) {
			GameScene.show(new WndMovieTheatre(this, filmsSeen, getLimit(), getReward()));
		} else {
			say(Game.getVar(R.string.ServiceManNPC_NotReady));
		}

		return true;
	}

	private int getLimit(){
		return 4 + Dungeon.hero.lvl();
	}

}
