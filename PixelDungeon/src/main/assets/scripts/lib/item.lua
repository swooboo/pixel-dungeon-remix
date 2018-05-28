--
-- User: mike
-- Date: 28.05.2018
-- Time: 22:35
-- This file is part of Remixed Pixel Dungeon.
--

local item = {}

item.__index = item

function item.actions(self, item, hero)
    return {}
end

function item.execute(self, item, hero, action)
end

function item.burn(self, item, cell)
    return item
end

function item.freeze(self, item, cell)
    return item
end

function item.poison(self, item, cell)
    return item
end

function item.onThrow(self, item, cell)
    item:dropTo(cell)
end

function item.desc(self, item)
    return {
        image     = 14,
        imageFile = "items/food.png",
        name      = "smth",
        info      = "smth",
        stackable = false,
        upgradable = false,
        identifieyd = false
    }
end

item.init = function(desc)
    setmetatable(desc, item)

    return desc
end

return item