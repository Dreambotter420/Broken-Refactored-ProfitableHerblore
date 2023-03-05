package script.druidicritual;


import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.settings.PlayerSettings;
import script.framework.Branch;
import script.utilities.InvEquip;
import script.utilities.Locations;
import script.utilities.Walkz;

public class DruidicRitual extends Branch {
    public static int getProgressStep()
    {
        return PlayerSettings.getConfig(80);
    }

    @Override
    public boolean isValid() {
        return getProgressStep() != 4 || !Locations.GE1.contains(Players.getLocal());
    }
}
