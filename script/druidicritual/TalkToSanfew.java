package script.druidicritual;

import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.utilities.Sleep;
import script.framework.Leaf;
import script.utilities.*;

public class TalkToSanfew extends Leaf {

    @Override
    public boolean isValid() {
        return (DruidicRitual.getProgressStep() == 1 && PrepareMeats.fulfilledStep0()) ||
                (DruidicRitual.getProgressStep() == 2 && PrepareMeats.preparedMeats());
    }

    @Override
    public int onLoop() {
        if(Locations.druidicRitual_sanfewAbove.contains(Players.getLocal()))
        {
            API.currentTask = "[Druidic Ritual] Talk-to -> Sanfew";
            API.walkTalkToNPC("Sanfew", "Talk-to", true,Locations.druidicRitual_sanfewAbove);
            return Sleepz.calculate(420,696);
        }
        if(Locations.allBurthropeTaverly.contains(Players.getLocal()))
        {
            API.currentTask = "[Druidic Ritual] Walking -> Climb-down -> Staircase";
            API.walkInteractWithGameObject("Staircase", "Climb-up", Locations.druidicRitual_sanfewbelow, () -> !Players.getLocal().exists() || Locations.druidicRitual_sanfewAbove.contains(Players.getLocal()));
            return Sleepz.calculate(420,696);
        }
        if(Locations.druidicRitual_cauldron.contains(Players.getLocal()))
        {
            API.currentTask = "[Druidic Ritual] Getting out of this musty witch's kitchen";
            API.walkInteractWithGameObject("Prison door","Open", Locations.druidicRitual_cauldron, () -> !Players.getLocal().exists() || Locations.druidicRitual_skeletonsHallway.contains(Players.getLocal()));
            return Sleepz.calculate(420,696);
        }
        if(Locations.druidicRitual_skeletonsHallway.contains(Players.getLocal()))
        {
            API.currentTask = "[Druidic Ritual] Twerking on they skelly asses";
            if(Combatz.shouldEatFood(9)) Combatz.eatFood();
            API.walkInteractWithGameObject("Ladder", "Climb-up", Locations.druidicRitual_undergroundLadder, () -> !Players.getLocal().exists() || Locations.druidicRitual_abovegroundLadder.contains(Players.getLocal()));
            return Sleepz.calculate(420,696);
        }

        API.currentTask = "[Druidic Ritual] Games necklace -> Burthrope";
        Walkz.useJewelry(InvEquip.games, "Burthorpe");
        return Sleepz.calculate(420,696);
    }
}
