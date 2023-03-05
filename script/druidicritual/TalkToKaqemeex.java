package script.druidicritual;

import org.dreambot.api.methods.interactive.Players;
import script.framework.Leaf;
import script.utilities.*;

public class TalkToKaqemeex extends Leaf {

    @Override
    public boolean isValid() {
        return (DruidicRitual.getProgressStep() == 0 && PrepareMeats.fulfilledStep0()) ||
                DruidicRitual.getProgressStep() == 3;
    }

    @Override
    public int onLoop() {
        if(Locations.allBurthropeTaverly.contains(Players.getLocal()))
        {
            API.currentTask = "[Druidic Ritual] Walking -> Talk-to -> Kaqemeex";
            API.walkTalkToNPC("Kaqemeex", "Talk-to", Locations.kaqemeex);
            return Sleepz.calculate(420,696);
        }
        if(Locations.druidicRitual_sanfewAbove.contains(Players.getLocal()))
        {
            API.currentTask = "[Druidic Ritual] Climb-down -> Staircase";
            API.interactWithGameObject("Staircase", "Climb-down", () -> !Players.getLocal().exists() || Locations.druidicRitual_sanfewbelow.contains(Players.getLocal()));
            return Sleepz.calculate(420,696);
        }
        API.currentTask = "[Druidic Ritual] Games necklace -> Burthrope";
        Walkz.useJewelry(InvEquip.games, "Burthorpe");
        return Sleepz.calculate(420,696);
    }
}
