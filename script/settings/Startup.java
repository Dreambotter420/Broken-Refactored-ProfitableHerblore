package script.settings;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.input.Keyboard;
import org.dreambot.api.methods.input.mouse.MouseSettings;
import org.dreambot.api.script.ScriptManager;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Timer;
import script.Main;
import script.framework.BankOnceLeaf;
import script.profitableherblore.Herb;
import script.profitableherblore.ProfitableHerblore;
import script.framework.WaitForLoggedNLoaded;
import script.druidicritual.*;
import script.utilities.*;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.prefs.Preferences;

public class Startup {
    public static boolean initialized = false;

    public static void GUIStart() {
        Logger.log("Starting Script with GUI!");
        try {
            SwingUtilities.invokeAndWait(() -> {
                GUI.createGUI();
            });
        } catch (InvocationTargetException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void setAllHerbsTrue()
    {
        Settings.useGuam = true;
        Settings.useHarralander = true;
        Settings.useRanarr = true;
        Settings.useToadflax = true;
        Settings.useIrit = true;
        Settings.useAvantoe = true;
        Settings.useKwuarm = true;
        Settings.useSnapdragon = true;
        Settings.useCadantine = true;
        Settings.useLantadyme = true;
        Settings.useDwarf = true;
        Settings.useTorstol = true;
    }
    public static void setAllHerbsFalse()
    {
        Settings.useGuam = false;
        Settings.useHarralander = false;
        Settings.useRanarr = false;
        Settings.useToadflax = false;
        Settings.useIrit = false;
        Settings.useAvantoe = false;
        Settings.useKwuarm = false;
        Settings.useSnapdragon = false;
        Settings.useCadantine = false;
        Settings.useLantadyme = false;
        Settings.useDwarf = false;
        Settings.useTorstol = false;
    }
    public static void setHerbQuickstart(String herbName)
    {
        if(herbName.contains("ranarr")) Settings.useRanarr = true;
        else if(herbName.contains("guam")) Settings.useGuam = true;
        else if(herbName.contains("harra")) Settings.useHarralander = true;
        else if(herbName.contains("toadflax")) Settings.useToadflax = true;
        else if(herbName.contains("dwarf")) Settings.useDwarf = true;
        else if(herbName.contains("irit")) Settings.useIrit = true;
        else if(herbName.contains("avantoe")) Settings.useAvantoe = true;
        else if(herbName.contains("kwuarm")) Settings.useKwuarm = true;
        else if(herbName.contains("snapdragon")) Settings.useSnapdragon = true;
        else if(herbName.contains("cadantine")) Settings.useCadantine = true;
        else if(herbName.contains("lantadyme")) Settings.useLantadyme = true;
        else if(herbName.contains("torstol")) Settings.useTorstol = true;
        else
        {
            Logger.log("Ignoring quickstart param herb name: " + herbName);
            return;
        }
        Logger.log("OK to use " + herbName);
    }

    public static void sharedOnStart()
    {
        Sleepz.dt = LocalDateTime.now();
        API.runTimer = new Timer(2000000000);
        Keyboard.setWordsPerMinute((int) Calculations.nextGaussianRandom(150, 30));
        InvEquip.initializeIntLists();
        id.initializeIDLists();
        Combatz.initializeFoods();

        Main.tree.addBranches(
                new WaitForLoggedNLoaded(),
                new BankOnceLeaf(),
                new DruidicRitual().addLeafs(
                        new CloseQuestCompletion(),
                        new HandleDialogues(),
                        new FinishedDruidicRitual(),
                        new TalkToKaqemeex(),
                        new TalkToSanfew(),
                        new PrepareMeats()),
                new ProfitableHerblore());

        if(Settings.useGuam) Settings.acceptableHerbs.add(Herb.GUAM);
        if(Settings.useHarralander) Settings.acceptableHerbs.add(Herb.HARRALANDER);
        if(Settings.useRanarr) Settings.acceptableHerbs.add(Herb.RANARR);
        if(Settings.useToadflax) Settings.acceptableHerbs.add(Herb.TOADFLAX);
        if(Settings.useIrit) Settings.acceptableHerbs.add(Herb.IRIT);
        if(Settings.useAvantoe) Settings.acceptableHerbs.add(Herb.AVANTOE);
        if(Settings.useKwuarm) Settings.acceptableHerbs.add(Herb.KWUARM);
        if(Settings.useSnapdragon) Settings.acceptableHerbs.add(Herb.SNAPDRAGON);
        if(Settings.useCadantine) Settings.acceptableHerbs.add(Herb.CADANTINE);
        if(Settings.useLantadyme) Settings.acceptableHerbs.add(Herb.LANTADYME);
        if(Settings.useDwarf) Settings.acceptableHerbs.add(Herb.DWARF_WEED);
        if(Settings.useTorstol) Settings.acceptableHerbs.add(Herb.TORSTOL);
        if(Settings.acceptableHerbs.isEmpty()) {
            Logger.log("No acceptable herbs set via GUI or quickstart to process :-( Script stop...");
            ScriptManager.getScriptManager().stop();
            return;
        }
        for(Herb herb : Settings.acceptableHerbs) {
            Logger.log("Approved to use " + herb.toString());
        }
        Logger.log("XP Mode: " + Settings.xpMode);
        Logger.log("Bot Mode: " + Settings.botMode);
        Logger.log("Buy grimy: " + Settings.buyGrimy);
        Logger.log("Buy clean: " + Settings.buyClean);
        Logger.log("Sell (unf): " + Settings.sellUnf);
        Logger.log("Use LivePrices: " + Settings.useLivePrices);
        Logger.log("Process all owned then stop script: " + Settings.processAllThenEnd);
        if (Settings.botMode) {
            MouseSettings.setMouseTiming(() -> Timing.getBotModeClickTiming());
        }
        if (Settings.useLivePrices) {
            Logger.log("LivePrice buy % above LivePrice Low: " + Settings.livePricesBuy);
            Logger.log("LivePrice sell % below LivePrice High: " + Settings.livePricesSell);
        }
        else {
            Logger.log("Minimum profit margin: " + Settings.minProfitMargin);
            Logger.log("Maximum quantity of grimy herbs to buy at once: " + Settings.maxHerbBuyQty);
            Logger.log("Undercut sell unf potions by: " + Settings.undercuttingSellUnf+"gp");
            Logger.log("Undercut buy grimy herbs by: " + Settings.undercuttingBuyGrimy+"gp");
        }
        initialized = true;
    }
    public static void quickStart(String[] params) {
        GUI.closedGUI = true;
        Logger.log("OnStart Script quickstart parameters!");
        for(String param : params)
        {
            String para = param.toLowerCase().replace(" ","");
            if(para.contains("maxbuy="))
            {
                String number = para.split("=")[1];
                if(!number.matches("[0-9]+"))
                {
                    Logger.log("Ignoring found -param due to not number: " + para);
                    continue;
                }
                int tmp = Integer.parseInt(number);
                if(tmp <= 0)
                {
                    Logger.log("Ignoring found -param due to number less than or equal to zero: " + para);
                    continue;
                }
                Settings.maxHerbBuyQty = tmp;
                Logger.log("Setting maximum herb buy quantity: "+Settings.maxHerbBuyQty);
            }
            if(para.contains("profitmargin="))
            {
                String number = para.split("=")[1];
                if(!number.matches("[0-9]+"))
                {
                    Logger.log("Ignoring found -param due to not number: " + para);
                    continue;
                }
                int tmp = Integer.parseInt(number);
                if(tmp <= 0)
                {
                    Logger.log("Ignoring found -param due to number less than or equal to zero: " + para);
                    continue;
                }
                Settings.minProfitMargin = tmp;
                Logger.log("Minimum profit margin per grimy -> unf herb: "+Settings.minProfitMargin+"gp");
            }
            if(para.contains("undercutsell="))
            {
                String number = para.split("=")[1];
                if(!number.matches("[0-9]+"))
                {
                    Logger.log("Ignoring found -param due to not number: " + para);
                    continue;
                }
                int tmp = Integer.parseInt(number);
                if(tmp <= 0)
                {
                    Logger.log("Ignoring found -param due to number less than or equal to zero: " + para);
                    continue;
                }
                Settings.undercuttingSellUnf = tmp;
                Logger.log("Undercutting sell unf by: "+Settings.undercuttingSellUnf+"gp");
            }
            if(para.contains("undercutbuy="))
            {
                String number = para.split("=")[1];
                if(!number.matches("[0-9]+"))
                {
                    Logger.log("Ignoring found -param due to not number: " + para);
                    continue;
                }
                int tmp = Integer.parseInt(number);
                if(tmp <= 0)
                {
                    Logger.log("Ignoring found -param due to number less than or equal to zero: " + para);
                    continue;
                }
                Settings.undercuttingBuyGrimy = tmp;
                Logger.log("Undercutting sell unf by: "+Settings.undercuttingBuyGrimy+"gp");
            }
            if(para.contains("xpmode="))
            {
                String bool = para.split("=")[1];
                if(bool.contains("true") || bool.contains("on"))
                {
                    Logger.log("Setting Bot Mode: ON");
                    Settings.xpMode = true;
                }
                else if(bool.contains("false") || bool.contains("off"))
                {
                    Logger.log("Setting Bot Mode: OFF");
                    Settings.xpMode = true;
                }
                else Logger.log("Ignoring found -param due to not true/false: " + para);
            }
            if(para.contains("botmode="))
            {
                String bool = para.split("=")[1];
                if(bool.contains("true") || bool.contains("on"))
                {
                    Logger.log("Setting Bot Mode: ON");
                    Settings.botMode = true;
                }
                else if(bool.contains("false") || bool.contains("off"))
                {
                    Logger.log("Setting Bot Mode: OFF");
                    Settings.botMode = true;
                }
                else Logger.log("Ignoring found -param due to not true/false: " + para);
            }

            if(para.contains("herbs{"))
            {
                setAllHerbsFalse();
                para = para.replace("herbs{","").replace("}","");
                if(para.contains(","))
                {
                    for(String herb : para.split(","))
                    {
                        setHerbQuickstart(herb);
                    }
                } else setHerbQuickstart(para);
            }
        }
    }


}
