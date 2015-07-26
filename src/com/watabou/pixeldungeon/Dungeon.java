/*
 * Pixel Dungeon
 * Copyright (C) 2012-2014  Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.watabou.pixeldungeon;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Rankings.gameOver;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Amok;
import com.watabou.pixeldungeon.actors.buffs.Invisibility;
import com.watabou.pixeldungeon.actors.buffs.Light;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.actors.mobs.Mimic;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.actors.mobs.npcs.Blacksmith;
import com.watabou.pixeldungeon.actors.mobs.npcs.Ghost;
import com.watabou.pixeldungeon.actors.mobs.npcs.Imp;
import com.watabou.pixeldungeon.actors.mobs.npcs.WandMaker;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.items.Ankh;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.items.potions.Potion;
import com.watabou.pixeldungeon.items.rings.Ring;
import com.watabou.pixeldungeon.items.scrolls.Scroll;
import com.watabou.pixeldungeon.items.wands.Wand;
import com.watabou.pixeldungeon.levels.CavesBossLevel;
import com.watabou.pixeldungeon.levels.CavesLevel;
import com.watabou.pixeldungeon.levels.CityBossLevel;
import com.watabou.pixeldungeon.levels.CityLevel;
import com.watabou.pixeldungeon.levels.DeadEndLevel;
import com.watabou.pixeldungeon.levels.HallsBossLevel;
import com.watabou.pixeldungeon.levels.HallsLevel;
import com.watabou.pixeldungeon.levels.LastLevel;
import com.watabou.pixeldungeon.levels.LastShopLevel;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.ModderLevel;
import com.watabou.pixeldungeon.levels.PrisonBossLevel;
import com.watabou.pixeldungeon.levels.PrisonLevel;
import com.watabou.pixeldungeon.levels.Room;
import com.watabou.pixeldungeon.levels.SewerBossLevel;
import com.watabou.pixeldungeon.levels.SewerLevel;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.utils.BArray;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.windows.WndResurrect;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class Dungeon {

	public static int potionOfStrength;
	public static int scrollsOfUpgrade;
	public static int arcaneStyli;
	public static boolean dewVial;		// true if the dew vial can be spawned
	public static int transmutation;	// depth number for a well of transmutation
	
	public static int challenges;
	
	public static Hero hero;
	public static Level level;
	
	public static int depth;
	public static int gold;
	// Reason of death
	public static String resultDescription;
	
	public static HashSet<Integer> chapters;
	
	private static boolean gameOver = false;
	
	// Hero's field of view
	public static boolean[] visible;
	
	public static boolean nightMode;
	
	private static boolean[] passable;
	
	public static HeroClass heroClass;
	
	private static void initSizeDependentStuff(int w, int h) {
		int size = w*h;
		Actor.clear(size);
		visible = new boolean[size];
		passable = new boolean[size];
		
		Arrays.fill( visible, false );
		
		PathFinder.setMapSize( w, h );
	}
	
	public static void init() {
		challenges = PixelDungeon.challenges();
		
		Scroll.initLabels();
		Potion.initColors();
		Wand.initWoods();
		Ring.initGems();
		
		Statistics.reset();
		Journal.reset();
		
		depth = 0;
		gold = 0;
		
		potionOfStrength = 0;
		scrollsOfUpgrade = 0;
		arcaneStyli = 0;
		dewVial = true;
		transmutation = Random.IntRange( 6, 14 );
		
		chapters = new HashSet<Integer>();
		
		Ghost.Quest.reset();
		WandMaker.Quest.reset();
		Blacksmith.Quest.reset();
		Imp.Quest.reset();
		
		Room.shuffleTypes();
		
		hero = new Hero();
		hero.live();
		
		Badges.reset();
		
		heroClass.initHero( hero );
		
		gameOver = false;
	}
	
	public static boolean isChallenged( int mask ) {
		return (challenges & mask) != 0;
	}
	
	public static Level testLevel() {
		Dungeon.level = null;
		
		level = new ModderLevel();
		
		initSizeDependentStuff(64,64);
		
		level.create(64,64);
		
		return level;
	}
	
	public static Level newLevel() {
		
		Dungeon.level = null;
		
		depth++;
		if (depth > Statistics.deepestFloor) {
			Statistics.deepestFloor = depth;
			
			if (Statistics.qualifiedForNoKilling) {
				Statistics.completedWithNoKilling = true;
			} else {
				Statistics.completedWithNoKilling = false;
			}
		}
		
		Level level;
		switch (depth) {
		case 1:
		case 2:
		case 3:
		case 4:
			level = new SewerLevel();
			break;
		case 5:
			level = new SewerBossLevel();
			break;
		case 6:
		case 7:
		case 8:
		case 9:
			level = new PrisonLevel();
			break;
		case 10:
			level = new PrisonBossLevel();
			break;
		case 11:
		case 12:
		case 13:
		case 14:
			level = new CavesLevel();
			break;
		case 15:
			level = new CavesBossLevel();
			break;
		case 16:
		case 17:
		case 18:
		case 19:
			level = new CityLevel();
			break;
		case 20:
			level = new CityBossLevel();
			break;
		case 21:
			level = new LastShopLevel();
			break;
		case 22:
		case 23:
		case 24:
			level = new HallsLevel();
			break;
		case 25:
			level = new HallsBossLevel();
			break;
		case 26:
			level = new LastLevel();
			break;
		default:
			level = new DeadEndLevel();
			Statistics.deepestFloor--;
		}
/*
		int lw = 32 + Random.Int(8);
		int lh = 32 + Random.Int(8);
*/		
		
		int lw = 32;
		int lh = 32;
		
		initSizeDependentStuff(lw, lh);

		level.create(lw, lh);
		
		Statistics.qualifiedForNoKilling = !bossLevel();
		
		return level;
	}
	
	public static void resetLevel() {
		
		initSizeDependentStuff(level.getWidth(),level.getHeight());
		
		level.reset();
		switchLevel( level, level.entrance );
	}
	
	public static String tip() {		
		if (level instanceof DeadEndLevel) {
			
			return Game.getVar(R.string.Dungeon_DeadEnd);
			
		} else {
			String[] tips = Game.getVars(R.array.Dungeon_Tips);
			int index = depth - 1;
			
			if( index == -1) {
				return "Welcome to test level";
			}
			
			if (index < tips.length) {
				return tips[index];
			} else {
				return Game.getVar(R.string.Dungeon_NoTips);
			}
		}
	}
	
	public static boolean shopOnLevel() {
		return depth == 6 || depth == 11 || depth == 16;
	}
	
	public static boolean bossLevel() {
		return bossLevel( depth );
	}
	
	public static boolean bossLevel( int depth ) {
		return depth == 5 || depth == 10 || depth == 15 || depth == 20 || depth == 25;
	}
	
	@SuppressWarnings("deprecation")
	public static void switchLevel( final Level level, int pos ) {
		
		nightMode = new Date().getHours() < 7;
		
		Dungeon.level = level;
		
		Actor.init(level);
		
		Actor respawner = level.respawner();
		if (respawner != null) {
			Actor.add( level.respawner() );
		}
		
		hero.pos = pos;
		
		if(!level.cellValid(hero.pos)) {
			hero.pos = level.entrance;
		}
		
		Light light = hero.buff( Light.class );
		hero.viewDistance = light == null ? level.viewDistance : Math.max( Light.DISTANCE, level.viewDistance );
		
		observe();
	}
	
	public static boolean posNeeded() {
		int[] quota = {4, 2, 9, 4, 14, 6, 19, 8, 24, 9};
		return chance( quota, potionOfStrength );
	}
	
	public static boolean soeNeeded() {
		int[] quota = {5, 3, 10, 6, 15, 9, 20, 12, 25, 13};
		return chance( quota, scrollsOfUpgrade );
	}
	
	private static boolean chance( int[] quota, int number ) {
		
		for (int i=0; i < quota.length; i += 2) {
			int qDepth = quota[i];
			if (depth <= qDepth) {
				int qNumber = quota[i + 1];
				return Random.Float() < (float)(qNumber - number) / (qDepth - depth + 1);
			}
		}
		
		return false;
	}
	
	public static boolean asNeeded() {
		return Random.Int( 12 * (1 + arcaneStyli) ) < depth;
	}
	
	
    private static final String VERSION   = "version";
	private static final String CHALLENGES= "challenges";
    private static final String HERO      = "hero";
    private static final String GOLD      = "gold";
    private static final String DEPTH     = "depth";
    private static final String LEVEL     = "level";
    private static final String POS       = "potionsOfStrength";
    private static final String SOU       = "scrollsOfEnhancement";
    private static final String AS        = "arcaneStyli";
    private static final String DV        = "dewVial";
    private static final String WT        = "transmutation";
    private static final String CHAPTERS  = "chapters";
    private static final String QUESTS    = "quests";
    private static final String BADGES    = "badges";

	
	public static void gameOver(){
		gameOver = true;
		Dungeon.deleteGame( true );
	}
	
	public static void saveGame( String fileName, boolean ignoreGameOver ) throws IOException {
		
		if(!ignoreGameOver && gameOver) {
			return;
		}
		
		try {
			Bundle bundle = new Bundle();
			
			bundle.put( VERSION, Game.version );
			bundle.put( CHALLENGES, challenges );
			bundle.put( HERO, hero );
			bundle.put( GOLD, gold );
			bundle.put( DEPTH, depth );
			
			bundle.put( POS, potionOfStrength );
			bundle.put( SOU, scrollsOfUpgrade );
			bundle.put( AS, arcaneStyli );
			bundle.put( DV, dewVial );
			bundle.put( WT, transmutation );
			
			int count = 0;
			int ids[] = new int[chapters.size()];
			for (Integer id : chapters) {
				ids[count++] = id;
			}
			bundle.put( CHAPTERS, ids );
			
			Bundle quests = new Bundle();
			Ghost		.Quest.storeInBundle( quests );
			WandMaker	.Quest.storeInBundle( quests );
			Blacksmith	.Quest.storeInBundle( quests );
			Imp			.Quest.storeInBundle( quests );
			bundle.put( QUESTS, quests );
			
			Room.storeRoomsInBundle( bundle );
			
			Statistics.storeInBundle( bundle );
			Journal.storeInBundle( bundle );
						
			Scroll.save( bundle );
			Potion.save( bundle );
			Wand.save( bundle );
			Ring.save( bundle );
			
			Bundle badges = new Bundle();
			Badges.saveLocal( badges );
			bundle.put( BADGES, badges );
			
			OutputStream output = Game.instance().openFileOutput( fileName, Game.MODE_PRIVATE );
			Bundle.write( bundle, output );
			output.close();
			
		} catch (Exception e) {

			GamesInProgress.setUnknown( hero.heroClass );
		}
	}
	
	public static void saveLevel() throws IOException {
		Bundle bundle = new Bundle();
		bundle.put( LEVEL, level );
		
		OutputStream output = Game.instance().openFileOutput( SaveUtils.depthFile( hero.heroClass, depth, level.levelKind() ), Game.MODE_PRIVATE );
		Bundle.write( bundle, output );
		output.close();
	}
	
	public static void saveAll() throws IOException {
		float MBytesAvaliable = Game.getAvailableInternalMemorySize()/1024f/1024f;
		
		if(MBytesAvaliable < 2){
			Game.toast("Low memory condition, ");
		}
		
		GLog.i("Saving: %5.2f MBytes avaliable", MBytesAvaliable);
		if (hero.isAlive()) {
			
			Actor.fixTime();
			saveGame( SaveUtils.gameFile( hero.heroClass ), false );
			saveLevel();
			
			GamesInProgress.set( hero.heroClass, depth, hero.lvl );
			
		} else if (WndResurrect.instance != null) {
			
			WndResurrect.instance.hide();
			Hero.reallyDie( WndResurrect.causeOfDeath );
		}
	}
	
	public static void loadGame( ) throws IOException {
		loadGame( SaveUtils.gameFile( heroClass ), true );
	}
	
	public static void loadGame( String fileName ) throws IOException {
		loadGame( fileName, false );
	}
	
	public static void loadGame( String fileName, boolean fullLoad ) throws IOException {
		
		Bundle bundle = gameBundle( fileName );
		
		Dungeon.challenges = bundle.getInt( CHALLENGES );
		
		Dungeon.level = null;
		Dungeon.depth = -1;
		
		Scroll.restore( bundle );
		Potion.restore( bundle );
		Wand.restore( bundle );
		Ring.restore( bundle );
		
		potionOfStrength = bundle.getInt( POS );
		scrollsOfUpgrade = bundle.getInt( SOU );
		arcaneStyli      = bundle.getInt( AS );
		dewVial          = bundle.getBoolean( DV );
		transmutation    = bundle.getInt( WT );
		
		if (fullLoad) {
			chapters = new HashSet<Integer>();
			int ids[] = bundle.getIntArray( CHAPTERS );
			if (ids != null) {
				for (int id : ids) {
					chapters.add( id );
				}
			}
			
			Bundle quests = bundle.getBundle( QUESTS );
			if (!quests.isNull()) {
				Ghost.Quest.restoreFromBundle( quests );
				WandMaker.Quest.restoreFromBundle( quests );
				Blacksmith.Quest.restoreFromBundle( quests );
				Imp.Quest.restoreFromBundle( quests );
			} else {
				Ghost.Quest.reset();
				WandMaker.Quest.reset();
				Blacksmith.Quest.reset();
				Imp.Quest.reset();
			}
			
			Room.restoreRoomsFromBundle( bundle );
		}
		
		Bundle badges = bundle.getBundle( BADGES );
		if (!badges.isNull()) {
			Badges.loadLocal( badges );
		} else {
			Badges.reset();
		}
		
		@SuppressWarnings("unused")
		String version = bundle.getString( VERSION );
		
		hero = (Hero)bundle.get( HERO );
		
		gold = bundle.getInt( GOLD );
		depth = bundle.getInt( DEPTH );
		
		Statistics.restoreFromBundle( bundle );
		Journal.restoreFromBundle( bundle );
	}
	
	public static Level loadLevel( ) throws IOException {
		Dungeon.level = null;
		
		String fileName = SaveUtils.depthFile( heroClass , depth );
		
		InputStream input = Game.instance().openFileInput( fileName ) ;
		Bundle bundle = Bundle.read( input );
		input.close();
		
		Level level = (Level)bundle.get( "level" );
		if(level != null){
			initSizeDependentStuff(level.getWidth(), level.getHeight());
		} else {
			GLog.w("cannot load %s \n", fileName);
		}
		return level;
	}
	
	public static void deleteGame(boolean deleteLevels ) {
		
		SaveUtils.deleteGameFile(heroClass);
		
		if (deleteLevels) {
			SaveUtils.deleteLevels(heroClass);
		}
		
		GamesInProgress.delete( heroClass );
	}
	
	public static Bundle gameBundle( String fileName ) throws IOException {
		
		InputStream input = Game.instance().openFileInput( fileName );
		Bundle bundle = Bundle.read( input );
		input.close();
		
		return bundle;
	}
	
	public static void preview( GamesInProgress.Info info, Bundle bundle ) {
		info.depth = bundle.getInt( DEPTH );
		if (info.depth == -1) {
			info.depth = bundle.getInt( "maxDepth" );	// FIXME
		}
		Hero.preview( info, bundle.getBundle( HERO ) );
	}
	
	public static void fail( String desc ) {
		resultDescription = desc;
		if (hero.belongings.getItem( Ankh.class ) == null) { 
			Rankings.INSTANCE.submit( Rankings.gameOver.LOSE);
		}
	}
	
	public static void win( String desc, gameOver kind ) {
		
		if (challenges != 0) {
			Badges.validateChampion();
		}
		
		resultDescription = desc;
		Rankings.INSTANCE.submit( kind );
	}
	
	public static void observe() {

		if (level == null) {
			return;
		}
		
		level.updateFieldOfView( hero );
		System.arraycopy( level.fieldOfView, 0, visible, 0, visible.length );
		
		BArray.or( level.visited, visible, level.visited );
		
		GameScene.afterObserve();
	}

	private static void markActorsAsUnpassableIgnoreFov(){
		for (Actor actor : Actor.all()) {
			if (actor instanceof Char) {
				int pos = ((Char)actor).pos;
				passable[pos] = false;
			}
		}
	}
	
	private static void markActorsAsUnpassable(boolean[] visible){
		for (Actor actor : Actor.all()) {
			if (actor instanceof Char) {
				int pos = ((Char)actor).pos;
				if (visible[pos]) {
					passable[pos] = false;
				}
			}
		}		
	}
	
	public static int findPath( Char ch, int from, int to, boolean pass[], boolean[] visible ) {
		
		if (level.adjacent( from, to )) {
			return Actor.findChar( to ) == null && (pass[to] || level.avoid[to]) ? to : -1;
		}
		
		if (ch.flying || ch.buff( Amok.class ) != null) {
			BArray.or( pass, level.avoid, passable );
		} else {
			System.arraycopy( pass, 0, passable, 0, level.getLength() );
		}
		
		if(visible != null){
			markActorsAsUnpassable(visible);
		} else {
			markActorsAsUnpassableIgnoreFov();
		}
		
		return PathFinder.getStep( from, to, passable );
		
	}
	
	public static int flee( Char ch, int cur, int from, boolean pass[], boolean[] visible ) {
		
		if (ch.flying) {
			BArray.or( pass, level.avoid, passable );
		} else {
			System.arraycopy( pass, 0, passable, 0, level.getLength() );
		}
		
		if(visible != null){
			markActorsAsUnpassable(visible);
		} else {
			markActorsAsUnpassableIgnoreFov();
		}
		
		passable[cur] = true;
		
		return PathFinder.getStepBack( cur, from, passable );
	}
	
	public static void challengeAllMobs(Char ch, String sound) {

		if (Dungeon.level == null) {
			return;
		}

		for (Mob mob : Dungeon.level.mobs) {
			mob.beckon(ch.pos);
		}

		for (Heap heap : Dungeon.level.allHeaps()) {
			if (heap.type == Heap.Type.MIMIC) {
				Mimic m = Mimic.spawnAt(heap.pos, heap.items);
				if (m != null) {
					m.beckon(ch.pos);
					heap.destroy();
				}
			}
		}

		if (ch.getSprite() != null) {
			ch.getSprite().centerEmitter()
					.start(Speck.factory(Speck.SCREAM), 0.3f, 3);
		}
		Sample.INSTANCE.play(sound);
		if (ch instanceof Hero) {
			Invisibility.dispel((Hero) ch);
		}
	}
}
