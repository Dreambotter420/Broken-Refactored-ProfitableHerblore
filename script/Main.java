package script;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.input.mouse.MouseSettings;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.utilities.Timer;
import script.framework.Tree;
import script.paint.CustomPaint;
import script.paint.PaintInfo;
import script.profitableherblore.ProfitableHerblore;
import script.settings.GUI;
import script.settings.Startup;
import script.utilities.API;
import script.utilities.Sleepz;

import java.awt.*;

@ScriptManifest(
        category = Category.HERBLORE,
        name = "Profitable Herblore",
        description = "Processes Grimy herbs -> clean -> unf pot, sells unf pot and buys more grimy herbs." +
                " In-game price checking, profit margins, and undercutting! Druidic Ritual!",
        author = "420x69x420",
        version = 1.69
)
public class Main extends AbstractScript implements PaintInfo {
    @Override
    public void onStart(String[] params) {
        Startup.quickStart(params);
    }

    @Override
    public void onStart() {
        Startup.GUIStart();
    }

    public static final Tree tree = new Tree();
    @Override
    public int onLoop() {
        if (!GUI.closedGUI) {
            return 100;
        }
        if (!Startup.initialized) {
            Startup.sharedOnStart();
        }

        return tree.onLoop();
    }

    @Override
    public void onExit() {
        Logger.log("~~~~Exiting~~~~");
        Logger.log("Total runtime: " + Timer.formatTime(API.runTimer.elapsed()));
        Logger.log("Final cleans/hr " + API.cleansPerHour);
        Logger.log("Total cleans " + API.cleans);
        Logger.log("Final profit/hr " + API.profitPerHour);
        Logger.log("Net profit: " + API.profit);
        Logger.log("~~~~~~~~~~~~~~~~");
        MouseSettings.resetMouseTimings();
    }

    // Our paint info
    // Add new lines to the paint here
    public String[] getPaintInfo()
    {
        return new String[] {
                getManifest().name() + " V" + getManifest().version() + " by Dreambotter420 ^_^",
                (API.runTimer == null ? "Runtime: none" : "Runtime: " + Timer.formatTime(API.runTimer.elapsed())),
                "Current task: " + currentTask,
                (ProfitableHerblore.selectedHerbPrice == null ? "Herb selected: None" : "Herb selected: " + ProfitableHerblore.selectedHerbPrice.herb.toString()),
                "Cleans per hour (total cleans): " + API.cleansPerHour+" herbs/hr ("+ API.cleans+")",
                "Profit per hour (net profit): " +API.profitPerHour/1000 + " k/hr ("+API.profit/1000+"k)"
        };
    }
    public static String currentTask = "N/A";

    // Instantiate the paint object. This can be customized to your liking.
    private final CustomPaint CUSTOM_PAINT = new CustomPaint(this,
            CustomPaint.PaintLocations.BOTTOM_LEFT_PLAY_SCREEN,
            new Color[] {new Color(255, 251, 255)},
            "Trebuchet MS",
            new Color[] {new Color(50, 50, 50, 175)},
            new Color[] {new Color(28, 28, 29)},
            1, false, 5, 3, 0);
    private final RenderingHints aa = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


    @Override
    public void onPaint(Graphics2D graphics2D)
    {
        // Set the rendering hints
        graphics2D.setRenderingHints(aa);
        // Draw the custom paint
        CUSTOM_PAINT.paint(graphics2D);
    }
}
