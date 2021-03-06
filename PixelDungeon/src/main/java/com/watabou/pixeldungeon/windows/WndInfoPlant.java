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
package com.watabou.pixeldungeon.windows;

import com.nyrds.android.util.GuiProperties;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.plants.Plant;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.sprites.PlantSprite;
import com.watabou.pixeldungeon.ui.Window;

public class WndInfoPlant extends Window {

	private static final int WIDTH = 120;
	
	public WndInfoPlant( Plant plant ) {
		
		super();
		
		IconTitle titlebar = new IconTitle();
		titlebar.icon( new PlantSprite( plant.image ) );
		titlebar.label( plant.plantName );
		titlebar.setRect( 0, 0, WIDTH, 0 );
		add( titlebar );
		
		Text info = PixelScene.createMultiline(GuiProperties.regularFontSize());
		add( info );
		
		info.text( plant.desc() );
		info.maxWidth(WIDTH);
		info.x = titlebar.left();
		info.y = titlebar.bottom() + GAP;
		
		resize( WIDTH, (int)(info.y + info.height()) );
	}
}
