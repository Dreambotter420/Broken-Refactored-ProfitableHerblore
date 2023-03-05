package script.profitableherblore;

import script.utilities.id;

public enum Herb
{
    GUAM(id.grimyGuam, id.guam, id.guamUnf,3),
    RANARR(id.grimyRanarr, id.ranarr, id.ranarrUnf,30),
    AVANTOE(id.grimyAvantoe, id.avantoe, id.avantoeUnf,50),
    IRIT(id.grimyIrit, id.irit, id.iritUnf,45),
    TOADFLAX(id.toadflaxGrimy, id.toadflax, id.toadflaxUnf,34),
    KWUARM(id.kwuarmGrimy, id.kwuarm, id.kwuarmUnf,55),
    SNAPDRAGON(id.snapdragonGrimy, id.snapdragon, id.snapdragonUnf,63),
    DWARF_WEED(id.dwarfweedGrimy, id.dwarfweed, id.dwarfweedUnf,72),
    CADANTINE(id.cadantineGrimy, id.cadantine, id.cadantineUnf,66),
    LANTADYME(id.lantadymeGrimy, id.lantadyme, id.lantadymeUnf,69),
    HARRALANDER(id.grimyHarra, id.harra, id.harraUnf,22),
    TORSTOL(id.torstolGrimy, id.torstol, id.torstolUnf,78);
    public final int grimy;
    public final int clean;
    public final int unf;
    public final int lvl;
    Herb(int grimy, int clean, int unf, int lvl) {
        this.grimy = grimy;
        this.clean = clean;
        this.unf = unf;
        this.lvl = lvl;
    }
}