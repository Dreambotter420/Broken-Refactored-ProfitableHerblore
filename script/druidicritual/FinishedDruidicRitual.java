package script.druidicritual;

import org.dreambot.api.methods.interactive.Players;
import script.framework.Leaf;
import script.utilities.*;

public class FinishedDruidicRitual extends Leaf {
    @Override
    public boolean isValid() {
        return DruidicRitual.getProgressStep() == 4 && !Locations.GE1.contains(Players.getLocal());
    }
    @Override
    public int onLoop() {
        API.currentTask = "Walking to GE";
        Walkz.goToGE(180000);
        return Sleepz.calculate(420,666);
    }
}
