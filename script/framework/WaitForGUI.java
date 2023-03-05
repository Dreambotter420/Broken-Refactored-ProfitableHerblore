package script.framework;

import org.dreambot.api.script.frameworks.treebranch.Leaf;
import script.settings.GUI;
import script.utilities.Sleepz;

public class WaitForGUI extends Leaf {

    @Override
    public boolean isValid() {
        return !GUI.closedGUI;
    }

    @Override
    public int onLoop() {
        return Sleepz.calculate(420,420);
    }
}
