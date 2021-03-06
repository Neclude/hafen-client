package haven;

import java.awt.*;

import static haven.Equipory.*;
import static haven.Inventory.*;

public class EquipProxy extends DraggableWidget implements DTarget2 {
    public static final Color BG_COLOR = new Color(91, 128, 51, 202);
    private int[] slots;
    
    public EquipProxy(int[] slots) {
	super("EquipProxy");
	setSlots(slots);
    }
    
    public void setSlots(int[] slots) {
	this.slots = slots;
	sz = invsz(new Coord(slots.length, 1));
    }
    
    private int slot(Coord c) {
	int slot = sqroff(c).x;
	if(slot < 0) {slot = 0;}
	if(slot >= slots.length) {slot = slots.length - 1;}
	return slots[slot];
    }
    
    @Override
    public boolean mousedown(Coord c, int button) {
	Equipory e = ui.gui.equipory;
	if(e != null) {
	    WItem w = e.slots[slot(c)];
	    if(w != null) {
		w.mousedown(Coord.z, button);
		return true;
	    }
	}
	return super.mousedown(c, button);
    }
    
    @Override
    public void draw(GOut g) {
	Equipory equipory = ui.gui.equipory;
	if(equipory != null) {
	    int k = 0;
	    g.chcolor(BG_COLOR);
	    g.frect(Coord.z, sz);
	    g.chcolor();
	    Coord c0 = new Coord(0, 0);
	    for (int slot : slots) {
		c0.x = k;
		Coord c1 = sqoff(c0);
		g.image(invsq, c1);
		WItem w = equipory.slots[slot];
		if(w != null) {
		    w.draw(g.reclipl(c1, g.sz));
		} else if(ebgs[slot] != null) {
		    g.image(ebgs[slot], c1);
		}
		k++;
	    }
	}
    }
    
    @Override
    public Object tooltip(Coord c, Widget prev) {
	Equipory e = ui.gui.equipory;
	if(e != null) {
	    int slot = slot(c);
	    WItem w = e.slots[slot];
	    if(w != null) {
		return w.tooltip(c, (prev == this) ? w : prev);
	    } else {
		return etts[slot];
	    }
	}
	return super.tooltip(c, prev);
    }
    
    @Override
    public boolean drop(WItem target, Coord cc, Coord ul) {
	Equipory e = ui.gui.equipory;
	if(e != null) {
	    e.wdgmsg("drop", slot(cc));
	    return true;
	}
	return false;
    }
    
    @Override
    public boolean iteminteract(WItem target, Coord cc, Coord ul) {
	Equipory e = ui.gui.equipory;
	if(e != null) {
	    WItem w = e.slots[slot(cc)];
	    if(w != null) {
		return w.iteminteract(target, cc, ul);
	    }
	}
	return false;
    }
    
    public void activate(int i) {
	ui.modctrl = false;
	Coord c = sqoff(new Coord(i, 0)).add(rootpos());
	ui.mousedown(c, 1);
	ui.modctrl = true;
    }
}
