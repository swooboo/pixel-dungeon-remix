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
package com.watabou.pixeldungeon.items.rings;

import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Dungeon;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.hero.Hero;

public class RingOfDetection extends Ring {
	
	@Override
	public boolean doEquip( Hero hero ) {
		if (super.doEquip( hero )) {
			Dungeon.hero.search( false );
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	protected Buff buff( ) {
		return new Detection();
	}
	
	@Override
	public String desc() {
		return isKnown() ? Game.getVar(R.string.RingOfDetection_Info) : super.desc();
	}
	
	public class Detection extends RingBuff {
	}
}
