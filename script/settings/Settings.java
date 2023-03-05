package script.settings;

import script.profitableherblore.Herb;
import script.profitableherblore.ProfitableHerblore;

import java.util.ArrayList;
import java.util.List;

public class Settings {
    public static boolean botMode = true;
    public static boolean buyGrimy = true;
    public static boolean buyClean = true;
    public static boolean sellUnf = true;
    public static boolean processAllThenEnd = true;
    public static boolean useLivePrices = false;
    public static boolean xpMode = false;
    public static boolean useGuam = true;
    public static boolean useHarralander = true;
    public static boolean useRanarr = true;
    public static boolean useToadflax = true;
    public static boolean useIrit = true;
    public static boolean useAvantoe = true;
    public static boolean useKwuarm = true;
    public static boolean useSnapdragon = true;
    public static boolean useCadantine = true;
    public static boolean useLantadyme = true;
    public static boolean useDwarf = true;
    public static boolean useTorstol = true;
    public static int minProfitMargin = 50;
    public static int livePricesBuy = 2;
    public static int livePricesSell = 2;
    public static int undercuttingBuyGrimy = 2;
    public static int undercuttingSellUnf = 2;
    public static int maxHerbBuyQty = 5000;
    public static List<Herb> acceptableHerbs = new ArrayList<Herb>();
}
