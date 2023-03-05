package script.druidicritual;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.items.Item;
import script.framework.Leaf;
import script.utilities.*;

public class PrepareMeats extends Leaf {
    @Override
    public boolean isValid() {
        return DruidicRitual.getProgressStep() == 2 &&
                !preparedMeats();
    }

    public static boolean preparedMeats() {
        return Inventory.containsAll(id.enchantedBear,id.enchantedBeef,id.enchantedChikken,id.enchantedRat);
    }

    @Override
    public int onLoop() {
        if(!Inventory.contains(id.enchantedBear,id.rawBear) ||
                !Inventory.contains(id.enchantedBeef,id.rawBeef) ||
                !Inventory.contains(id.enchantedChikken,id.rawChikken) ||
                !Inventory.contains(id.enchantedRat,id.rawRat))
        {
            fulfilledStep0();
            return Sleepz.calculate(420,696);
        }
        if(Locations.druidicRitual_cauldron.contains(Players.getLocal()))
        {
            API.currentTask = "[Druidic Ritual] Using meats -> Cauldron";
            Item i = Inventory.get(id.rawBeef,id.rawChikken,id.rawRat,id.rawBear);
            GameObject cauldron = GameObjects.closest("Cauldron of Thunder");
            if(i != null && cauldron != null)
            {
                if(i.useOn(cauldron))
                {
                    Sleep.sleepUntil(() -> !Players.getLocal().exists() || !Inventory.contains(i),
                            () -> Players.getLocal().isMoving(),
                            Sleepz.calculate(3333, 3333),69);
                }
            }
            return Sleepz.calculate(420,696);
        }
        if(Locations.druidicRitual_skeletonsHallway.contains(Players.getLocal()))
        {
            API.currentTask = "[Druidic Ritual] Skrrting on they skellie asses";
            if(!Combatz.toggleAutoRetaliate(false)) return Sleepz.calculate(420,696);
            if(Combatz.shouldEatFood(9)) Combatz.eatFood();
            API.interactWithGameObject("Prison door","Open", Locations.druidicRitual_cauldron);

            return Sleepz.calculate(420,696);
        }
        if(Locations.allBurthropeTaverly.contains(Players.getLocal()))
        {
            API.currentTask = "[Druidic Ritual] Walking -> Climb-down -> Ladder";
            API.walkInteractWithGameObject("Ladder", "Climb-down", Locations.druidicRitual_abovegroundLadder, () -> !Players.getLocal().exists() || Locations.druidicRitual_undergroundLadder.contains(Players.getLocal()));

            return Sleepz.calculate(420,696);
        }
        if(Locations.druidicRitual_sanfewAbove.contains(Players.getLocal()))
        {
            API.currentTask = "[Druidic Ritual] Walking -> Climb-down -> Staircase";
            API.walkInteractWithGameObject("Staircase", "Climb-down", Locations.druidicRitual_sanfewAbove, () -> !Players.getLocal().exists() || Locations.druidicRitual_sanfewbelow.contains(Players.getLocal()));

            return Sleepz.calculate(420,696);
        }
        Walkz.useJewelry(InvEquip.games, "Burthorpe");
        return Sleepz.calculate(420,696);
    }

    public static boolean fulfilledStep0()
    {
        API.currentTask = "[Druidic Ritual] Fulfilling inventory / equipment for quest start";
        if(Inventory.containsAll(id.rawBear,id.rawBeef,id.rawChikken,id.rawRat) &&
                InvEquip.equipmentContains(InvEquip.wearableWealth) &&
                InvEquip.equipmentContains(InvEquip.wearableGames)) return true;
        if(!InvEquip.checkedBank()) return false;
        InvEquip.clearAll();
        InvEquip.addInvyItem(Combatz.lowFood, 5, (int) Calculations.nextGaussianRandom(10, 3), false, (int) Calculations.nextGaussianRandom(20, 5));
        int herbXPNeeded = Skills.getExperienceForLevel(30) - Skills.getExperience(Skill.HERBLORE);
        if (herbXPNeeded > 0) {
            int attPotsNeeded = (herbXPNeeded / 25) + Timing.getRandomDelay(true, 1, 75, 20, 20);
            InvEquip.addInvyItem(id.eyeOfNewt, attPotsNeeded, attPotsNeeded, true, attPotsNeeded);
            InvEquip.addInvyItem(id.grimyGuam, attPotsNeeded, attPotsNeeded, true, attPotsNeeded);
        }
        InvEquip.addInvyItem(id.rawBear, 1, 1, false, 1);
        InvEquip.addInvyItem(id.rawRat, 1, 1, false, 1);
        InvEquip.addInvyItem(id.rawBeef, 1, 1, false, 1);
        InvEquip.addInvyItem(id.rawChikken, 1, 1, false, 1);
        int staminaID = id.stamina4;
        if(InvEquip.bankContains(id.staminas)) staminaID = InvEquip.getBankItem(id.staminas);
        else if(InvEquip.invyContains(id.staminas)) staminaID = InvEquip.getInvyItem(id.staminas);
        InvEquip.addInvyItem(staminaID, 1, 1, false, 1);
        InvEquip.setEquipItem(EquipmentSlot.RING, InvEquip.wealth);
        InvEquip.setEquipItem(EquipmentSlot.AMULET, InvEquip.games);
        InvEquip.shuffleFulfillOrder();
        InvEquip.fulfillSetup(true, 240000);
        return Inventory.containsAll(id.rawBear,id.rawBeef,id.rawChikken,id.rawRat) &&
                InvEquip.equipmentContains(InvEquip.wearableWealth) &&
                InvEquip.equipmentContains(InvEquip.wearableGames);
    }
}
