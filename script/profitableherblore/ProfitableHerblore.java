package script.profitableherblore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankMode;
import org.dreambot.api.methods.container.impl.bank.BankQuantitySelection;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.filter.Filter;
import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.api.methods.grandexchange.GrandExchangeItem;
import org.dreambot.api.methods.grandexchange.LivePrices;
import org.dreambot.api.methods.grandexchange.Status;
import org.dreambot.api.methods.input.Keyboard;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.methods.widget.helpers.ItemProcessing;
import org.dreambot.api.script.ScriptManager;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.utilities.Timer;
import org.dreambot.api.utilities.impl.Condition;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.api.wrappers.widgets.WidgetChild;

import script.framework.Leaf;
import script.settings.Settings;
import script.utilities.*;


public class ProfitableHerblore extends Leaf
{
	public static HerbPrice selectedHerbPrice = null;
	public static LiveHerbPrice selectedHerbLivePrice = null;
	public static boolean needSelectedHerbPriceCheck = false;
	public static Timer lastUnfSellTimer;

	@Override
	public boolean isValid() {
		return true;
	}
	public static boolean invyContainsAnythingNoted() {
		for(Item i: Inventory.all())
		{
			if(i == null || i.getID() <= 0 || i.getName() == null || i.getName().equalsIgnoreCase("null")) continue;
			if(i.isNoted()) return true;
		}
		return false;
	}

	public static void checkLivePrices() {
		List<LiveHerbPrice> herbPriceList = new ArrayList<>();
		for (Herb approvedHerb : Settings.acceptableHerbs) {
			if (Skills.getRealLevel(Skill.HERBLORE) >= approvedHerb.lvl) {
				herbPriceList.add(new LiveHerbPrice(approvedHerb));
			}
		}
		//if XP mode, sort by lvl
		//also sort by lvl if not both buying and selling (no profit margin)
		if (Settings.xpMode ||
				!Settings.sellUnf ||
				(!Settings.buyGrimy && !Settings.buyClean)) {
			Logger.log("Sorting herb list according to highest lvl");
			herbPriceList.sort((o1, o2) -> o2.herb.lvl - o1.herb.lvl);
		} else {
			Logger.log("Sorting herb list according to profit");
			herbPriceList.sort((o1, o2) -> o2.profitMargin - o1.profitMargin);
		}
		selectedHerbLivePrice = herbPriceList.get(0);
	}

	@Override
	public int onLoop() {
		if(GrandExchange.isOpen() && isGEActuallyReadyToCollect()) {
			Logger.log("Have availability to collect GE!");
			collect();
			return Sleepz.calculate(111, 420);
		}

		if (Settings.useLivePrices) {
			//LivePrices logic
			if (selectedHerbLivePrice == null) {
				if (checkForAvailableExistingHerbs()) {
					return Sleepz.calculate(420,696);
				}
				checkLivePrices();
				return Sleepz.calculate(420, 696);
			}

		} else {
			//In-game price-checking logic
			if(selectedHerbPrice == null) {
				//choose random existing one
				if(checkForAvailableExistingHerbs()) {
					return Sleepz.calculate(420,696);
				}
				//or sell all herbs and then price check all available
				priceCheck();
				return Sleepz.calculate(420, 696);
			}
		}
		final int grimy = selectedHerbLivePrice.herb.grimy;
		final int clean = selectedHerbLivePrice.herb.clean;
		final int notedGrimy = Inventory.count(new Item(grimy,1).getNotedItemID());
		final int notedClean = Inventory.count(new Item(clean,1).getNotedItemID());
		final int invyGrimy = Inventory.count(grimy);
		final int invyClean = Inventory.count(clean);
		final int bankCleans = Bank.count(clean);
		final int bankGrimys = Bank.count(grimy);
		final int bankVials = Bank.count(id.vial);
		final int totalHerbCount = invyGrimy + notedGrimy + invyClean + notedClean + bankCleans + bankGrimys;
		if(bankVials <= 0) {
			buyMoreVials();
			return Sleepz.calculate(420, 696);
		}
		//check if nothing left to process of selectedHerb
		if(totalHerbCount <= 0) {
			if (checkForAvailableExistingHerbs()) {
				return Sleepz.calculate(420,696);
			}
			//check settings if should buy more
			if (Settings.sellUnf && (Settings.buyClean || Settings.buyGrimy)) {
				if(needSelectedHerbPriceCheck) {
					if (Settings.useLivePrices) {
						checkLivePrices();
					} else {
						priceCheck();
					}
					return Sleepz.calculate(420, 696);
				}
				buyABunchOfHerbs();
				return Sleepz.calculate(420, 696);
			}
		}
		return processTheStuff();
	}
	public static int processTheStuff() {
		final int grimy = selectedHerbPrice.herb.grimy;
		final int clean = selectedHerbPrice.herb.clean;
		final int unf = selectedHerbPrice.herb.unf;
		final int bankCleans = Bank.count(clean);
		final int bankGrimys = Bank.count(grimy);
		final int bankVials = Bank.count(id.vial);
		if(!Bank.isOpen() && invyContainsAnythingNoted()) {
			Filter<GameObject> bankFilter = g-> g!=null &&
					g.getName().equals("Grand Exchange booth") &&
					g.hasAction("Bank");
			GameObject bank = GameObjects.closest(bankFilter);
			if(bank != null && bank.interact("Bank")) Sleep.sleepUntil(Bank::isOpen, Sleepz.calculate(2222, 2222));
			return Sleepz.calculate(111, 420);
		}
		API.cleansPerHour = (int) ((double) API.cleans / ((double) API.runTimer.elapsed() / 3600000));

		if(Bank.isOpen()) {
			if(!Inventory.isFull() && selectedHerbPrice.herb.clean == id.guam && Inventory.count(selectedHerbPrice.herb.unf) > 0 && Bank.count(id.eyeOfNewt) > 0) {
				API.currentTask = "Withdrawing 14 eyes of newt";
				if(Bank.withdraw(id.eyeOfNewt,14) && Sleep.sleepUntil(() -> Inventory.contains(id.eyeOfNewt), Sleepz.calculate(3333, 3333))) {
					Sleepz.sleep(69, 111);
					Bank.close();
					Sleep.sleepUntil(() -> Inventory.contains(id.eyeOfNewt), Sleepz.calculate(3333, 3333));
				}
				return Sleepz.calculate(69, 696);
			}
			API.currentTask = "Depositing all items";
			Logger.log("Depositing all items");
			if(!Bank.depositAllItems()) {
				return Sleepz.calculate(69,111);
			}
			Sleep.sleepUntil(Inventory::isEmpty, Sleepz.calculate(3333, 3333));
			Sleepz.sleep(69, 111);
			if(Bank.getDefaultQuantity() != BankQuantitySelection.X) {
				if(Bank.setDefaultQuantity(BankQuantitySelection.X)) {
					Sleep.sleepTick();
				}
				return Sleepz.calculate(69, 696);
			}
			if(bankGrimys > 0) {
				API.currentTask = "Withdrawing grimy/vial 14/14";
				if(Bank.withdraw(grimy,14)) {
					Sleepz.sleep(69, 111);
					if (Bank.withdraw(id.vial,14)) {
						Sleepz.sleep(69, 111);
						Bank.close();
						Sleep.sleepUntil(() -> Inventory.contains(grimy) && Inventory.contains(id.vial), Sleepz.calculate(3333, 3333));
					}
				}
				return Sleepz.calculate(69, 696);
			}
			if(bankCleans > 0) {
				if(Bank.withdraw(clean,14)) {
					Sleepz.sleep(69, 111);
					if(Bank.withdraw(id.vial,14)) {
						Sleepz.sleep(69, 111);
						Sleep.sleepUntil(() -> Inventory.contains(clean) && Inventory.contains(id.vial), Sleepz.calculate(3333, 3333));
						Bank.close();
					}
				}
				return Sleepz.calculate(69, 696);
			}
			Sleep.sleepUntil(Inventory::isEmpty, Sleepz.calculate(8888, 3333));
			Sleepz.sleep(696, 1111);
			if(Inventory.isEmpty()) {
				if(Bank.count(id.vial) != bankVials ||
						Bank.count(clean) != bankCleans ||
						Bank.count(grimy) != bankGrimys) return Sleepz.calculate(111,111);
				return -1;
			}
			return Sleepz.calculate(111, 696);
		}

		if(Inventory.contains(grimy)) {
			API.currentTask = "Cleaning grimys";
			if(!Tabs.isOpen(Tab.INVENTORY)) {
				Tabs.open(Tab.INVENTORY);
				return Sleepz.calculate(111, 696);
			}
			Filter<Item> filter = i -> i != null && i.getID() == grimy;
			for(Item i : Inventory.all(filter)) {
				if(i == null) continue;
				if(i.interact("Clean")) {
					API.cleans++;
					Sleepz.sleep(42,69);
				}
			}
			if(Inventory.contains(id.vial)) {
				Sleep.sleepUntil(() -> Inventory.contains(clean), Sleepz.calculate(2222, 2222));
			}
		}

		if(ItemProcessing.isOpen()) {
			Condition c;
			if(Inventory.contains(id.vial) && Inventory.contains(clean)) c = () -> !Players.getLocal().exists() || !Inventory.contains(clean) || Dialogues.canContinue();
			else c = () -> !Players.getLocal().exists() || !Inventory.contains(id.eyeOfNewt) || Dialogues.canContinue();
			Keyboard.typeSpecialKey(32);
			Sleep.sleepUntil(c,() -> Players.getLocal().isAnimating(), Sleepz.calculate(2222, 2222),69);
			return Sleepz.calculate(111, 1111);
		}
		Condition itemProcessing = ItemProcessing::isOpen;
		int sleepTimeout = Sleepz.calculate(3333, 3333);
		if(Inventory.contains(id.vial) && Inventory.contains(clean)) {
			API.currentTask = "Using vial -> clean";
			if(Inventory.get(id.vial).useOn(clean)) {
				Keyboard.holdSpace(itemProcessing, sleepTimeout);
				Sleep.sleepUntil(itemProcessing, sleepTimeout);
			}
			return Sleepz.calculate(111, 1111);
		}
		if(Inventory.contains(id.eyeOfNewt) && Inventory.contains(unf)) {
			API.currentTask = "Using unf -> eye of newt";
			if(Inventory.get(unf).useOn(id.eyeOfNewt)) {
				Keyboard.holdSpace(itemProcessing, sleepTimeout);
				Sleep.sleepUntil(itemProcessing, sleepTimeout);
			}
			return Sleepz.calculate(111, 1111);
		}
		clickBank();
		return Sleepz.calculate(111, 1111);
	}


	public static void priceCheck() {
		if(selectedHerbPrice == null) {
			priceCheckAvailableHerbs();
			return;
		}
		priceCheckSelectedHerb();
	}
	/**
 	 * Returns true if no unf in invy or bank, otherwise sets 3 min timer to try to sell.
	 * Empties inventory, withdraws all unf pots (all of them).
	 * If the price of unf pot is pre-recorded, we sell it for unfLow price.
	 * If the price is not recorded of existing unf, 
	 * do another pricecheck regardless of lvl so we can know how much to sell it,
	 * then sell it for that much.
	 */
	public static void sellAllUnf() {
		if (Settings.processAllThenEnd) {
			Logger.log("Have no more supplies to process and script set to end after no more supplies, so script is stopping");
			ScriptManager.getScriptManager().stop();
			Sleep.sleep(200);
			return;
		}
		API.currentTask = "Selling all unf potions";
		Logger.log("[sellAllUnf] Start");
		needSelectedHerbPriceCheck = true;
		if(!InvEquip.checkedBank()) return;
		Timer timer = new Timer((int) Calculations.nextGaussianRandom(80000,5000));
		int randUnfID = -1;
		int randUnfOfferCount = -1;
		HerbPrice tempHerbPrice = null;
		boolean putOffer = false;
		while(!timer.finished() && !ScriptManager.getScriptManager().isPaused() && ScriptManager.getScriptManager().isRunning()) {
			Sleepz.sleep(420, 696);
			if(putOffer) {
				if(lastUnfSellTimer == null) {
					lastUnfSellTimer = new Timer((int) Calculations.nextGaussianRandom(300000,20000));
				}
				if(isGEActuallyReadyToCollect()) {
					collect();
					continue;
				}
				if(!isGEEmpty()) {
					if(!havePendingSellOfferOfAnyUnf()) {
						tempHerbPrice = null;
						putOffer = false;
					}
					//if inventory contains any grimy herbs, break
					int herbLvl = Skills.getRealLevel(Skill.HERBLORE);
					List<Integer> acceptableIDs = new ArrayList<>();
					Settings.acceptableHerbs.stream().filter(h -> h.lvl <= herbLvl).forEach(h -> {
						acceptableIDs.add(h.grimy);
					});
					if(Inventory.contains(i -> i != null && acceptableIDs.contains(i.getID()))) {
						Logger.log("Found grimy herbs to clean in invy! Stopping waiting for unf to sell");
						return;
					}
					if(!completedGEOfferWithQty(randUnfID,randUnfOfferCount,false)) {
						Sleepz.sleep(111, 1111);
					}
					continue;
				}
				tempHerbPrice = null;
				putOffer = false;
			}
			List<Integer> okUnf = new ArrayList<Integer>();
			List<Integer> unfFound = new ArrayList<Integer>();
			for(Herb herb : Herb.values()) {
				if(Bank.contains(herb.unf)) unfFound.add(herb.unf);
				okUnf.add(herb.unf);
				okUnf.add(new Item(herb.unf,1).getNotedItemID());
			}
			if(unfFound.isEmpty()) {
				Logger.log("Bank empty of unf!");
				if(Bank.isOpen()) {
					if(Bank.contains(id.coins)) {
						if(Bank.withdrawAll(id.coins)) {
							Sleep.sleepUntil(() -> !Bank.contains(id.coins), Sleepz.calculate(3333, 3333));
						}
						continue;
					}
					if(Bank.close()) Sleep.sleepUntil(() -> !Bank.isOpen(), Sleepz.calculate(2222, 2222));
					continue;
				}
				//check for all inventory unf
				for(Herb herb : Herb.values()) {
					if(Inventory.contains(herb.unf) || Inventory.contains(new Item(herb.unf,1).getNotedItemID())) unfFound.add(herb.unf);
				}
				//no unf in bank or invy
				if(unfFound.isEmpty())
				{
					if(isGEActuallyReadyToCollect()) 
					{
						collect();
						continue;
					}
					Logger.log("[sellAllUnf] End");
					return;
				}
				//check unf for existing prices, otherwise sell for 1 gp to check price
				if(tempHerbPrice == null) {
					Collections.shuffle(unfFound);
					for(int i : unfFound)
					{
						tempHerbPrice = priceCheck1Unf(getHerbFromID(i));
						break;
					}
				}
				
				if(!GrandExchange.isOpen())
				{
					openGE();
					continue;
				}
				if(GrandExchange.getFirstOpenSlot() == -1)
				{
					GrandExchange.cancelAll();
					Sleep.sleepUntil(GrandExchange::isReadyToCollect, Sleepz.calculate(2222, 2222));
					continue;
				}
				if(isGEActuallyReadyToCollect())
				{
					collect();
					continue;
				}
				randUnfID = tempHerbPrice.herb.unf;
				int sellPrice = tempHerbPrice.unfLow - Settings.undercuttingSellUnf;
				if(sellPrice <= 1) 
				{
					Logger.log("Not selling unf with unfLow price: "+ sellPrice);
				}
				randUnfOfferCount = Inventory.count(randUnfID) + Inventory.count(new Item(randUnfID,1).getNotedItemID());
				if(GrandExchange.sellItem(new Item(randUnfID,1).getName(), randUnfOfferCount, sellPrice))
				{
					Logger.log("Put sell offer for item: " + new Item(randUnfID,1).getName() + " in qty: " + randUnfOfferCount + " at pricePer: " +sellPrice);
					putOffer = true;
					Sleep.sleepUntil(ProfitableHerblore::isGEActuallyReadyToCollect, Sleepz.calculate(2222, 2222));
				}
				continue;
			}
			Filter<Item> unfCoinsFilter = i -> i!=null && (okUnf.contains(i.getID()) || i.getID() == id.coins);
			//have some unf pots to withdraw
			if(Bank.contains(unfCoinsFilter)) {
				if(!Bank.isOpen())
				{
					if(Inventory.isItemSelected()) {
						Inventory.deselect();
						return;
					}
					Bank.open();
					continue;
				}
				if(!Inventory.isEmpty() && !Inventory.onlyContains(unfCoinsFilter))
				{
					Bank.depositAllExcept(unfCoinsFilter);
					continue;
				}
				if(Bank.getWithdrawMode() == BankMode.NOTE)
				{
					if(Bank.withdrawAll(unfCoinsFilter))
					{
						Sleep.sleepUntil(() -> !Bank.contains(unfCoinsFilter), Sleepz.calculate(3333, 3333));
					}
					continue;
				}
				Bank.setWithdrawMode(BankMode.NOTE);
			}
		}
		Logger.log("[sellAllUnf] Timeout!");
	}
	/**
	 * Sells 1 unf pot that should already be in invy by time of this method call.
	 */
	public static HerbPrice priceCheck1Unf(Herb herb)
	{
		API.currentTask = "Checking sell price of 1 "+new Item(herb.unf,1).getName();
		Logger.log("[priceCheck1UnfHerb] Starting check price: " + new Item(herb.unf,1).getName());

		//check bank, get coins, deposit everything else
		boolean putOffer = false;
		boolean soldUnf = false;
		Timer timeout = new Timer(180000);
		while(!timeout.finished() && ScriptManager.getScriptManager().isRunning() && 
				!ScriptManager.getScriptManager().isPaused()) {
			//need to buy grimy, then sell grimy, then buy unf, sell unf,
			//then check history and search from top down for item id and qty = 1 of sold
			Sleepz.sleep(69, 420);
			if(soldUnf) {
				if(isGEHistoryOpen()) {
					final int soldUnfFor = getLatestGEHistoryPrice(herb.unf, false);
					if(soldUnfFor == -1) 
					{
						Logger.log("Failed to observe existing sold amount for unf potion! Trying again...");
						soldUnf = false;
						continue;
					}
					Logger.log("Found unf " + herb.toString() + " with sell price: " + soldUnfFor);
					return new HerbPrice(herb,0,0,0,0,soldUnfFor,0);
				}
				if(!GrandExchange.isOpen()) {
					NPC GETeller = NPCs.closest("Grand Exchange Clerk");
					if(GETeller!=null)
					{
						if(GETeller.interact("History"))
						{
							Sleep.sleepUntil(() -> !Players.getLocal().exists() || isGEHistoryOpen(),
									()-> Players.getLocal().isMoving(), Sleepz.calculate(2222,2222),69);
						}
					}
					continue;
				}
				if(isGEActuallyReadyToCollect()) {
					collect();
					continue;
				}
				WidgetChild historyButton = Widgets.getWidgetChild(465, 3);
				if(historyButton != null && historyButton.isVisible()) {
					if(historyButton.interact("History")) {
						Sleep.sleepUntil(ProfitableHerblore::isGEHistoryOpen, Sleepz.calculate(2222, 2222));
					}
					continue;
				}
				continue;
			}
			if(!GrandExchange.isOpen()) {
				openGE();
				continue;
			}
			if(putOffer) {
				if(completedGEOfferWithQty(herb.unf,1,false)) {
					soldUnf = true;
					putOffer = false;
				}
				continue;
			}
			if(isGEActuallyReadyToCollect()) {
				collect();
				continue;
			}
			if(GrandExchange.sellItem(new Item(herb.unf,1).getName(), 1, 1))
			{
				putOffer = true;
				Sleep.sleepUntil(ProfitableHerblore::isGEActuallyReadyToCollect, Sleepz.calculate(2222, 2222));
			}
		}
		Logger.log("Timeout after 3 minutes of unf sold pricecheck!");
		return null;
	}
	/**
	 * Check price of our selected herb, sets it to selectedHerbPrice.
	 */
	public static void priceCheckSelectedHerb() {
		API.currentTask = "Checking prices of selected herb: "+selectedHerbPrice.herb.toString();

		//check bank, get coins, deposit everything else
		if(!InvEquip.checkedBank()) {
			return;
		}

		Logger.log("[priceCheckSelectedHerb] Starting check price of selected herb: " + selectedHerbPrice.herb.toString());

		int grimyOfferPrice = (int) Calculations.nextGaussianRandom((LivePrices.getHigh(selectedHerbPrice.herb.grimy) * 5), 50);
		if(grimyOfferPrice >= 20000) grimyOfferPrice = Calculations.random(18000,22000);
		int cleanOfferPrice = (int) Calculations.nextGaussianRandom((LivePrices.getHigh(selectedHerbPrice.herb.grimy) * 5), 50);
		if(cleanOfferPrice >= 20000) cleanOfferPrice = Calculations.random(18000,22000);
		int unfOfferPrice = (int) Calculations.nextGaussianRandom((LivePrices.getHigh(selectedHerbPrice.herb.unf) * 5), 50);
		if(unfOfferPrice >= 20000) unfOfferPrice = Calculations.random(18000,22000);

		boolean boughtGrimy = false;
		boolean soldGrimy = false;
		boolean boughtClean = false;
		boolean soldClean = false;
		boolean boughtUnf = false;
		boolean soldUnf = false;
		boolean putOffer = false;
		Timer timeout = new Timer(180000);
		while(!timeout.finished() && ScriptManager.getScriptManager().isRunning() && 
				!ScriptManager.getScriptManager().isPaused())
		{
			//need to buy grimy, then sell grimy, then buy unf, sell unf,
			//then check history and search from top down for item id and qty = 1 of sold
			Sleepz.sleep(69, 420);
			
			if((!Settings.buyGrimy || (boughtGrimy && soldGrimy)) &&
					(!Settings.buyClean || (boughtClean && soldClean)) &&
					(!Settings.sellUnf || (boughtUnf && soldUnf))) {
				if(isGEHistoryOpen()) {
					final int boughtGrimyFor = getLatestGEHistoryPrice(selectedHerbPrice.herb.grimy, true);
					final int soldGrimyFor = getLatestGEHistoryPrice(selectedHerbPrice.herb.grimy, false);
					final int boughtCleanFor = getLatestGEHistoryPrice(selectedHerbPrice.herb.clean, true);
					final int soldCleanFor = getLatestGEHistoryPrice(selectedHerbPrice.herb.clean, false);
					final int boughtUnfFor = getLatestGEHistoryPrice(selectedHerbPrice.herb.unf, true);
					final int soldUnfFor = getLatestGEHistoryPrice(selectedHerbPrice.herb.unf, false);
					if((Settings.buyGrimy && (boughtGrimyFor == -1 || soldGrimyFor == -1)) ||
							(Settings.buyClean && (boughtCleanFor == -1 || soldCleanFor == -1)) ||
							(Settings.sellUnf && (boughtUnfFor == -1 || soldUnfFor == -1))) {
						Logger.log("Failed to observe existing bought/sold qtys for grimy/unf herb! Trying again...");
						boughtGrimy = soldGrimy = boughtUnf = soldUnf = false;
						continue;
					}
					HerbPrice foundHerbPrice = new HerbPrice(selectedHerbPrice.herb, soldGrimyFor, boughtGrimyFor, boughtCleanFor, soldCleanFor, soldUnfFor, boughtUnfFor);
					foundHerbPrice.printHerbPrices();
					if(foundHerbPrice.profitMargin < Settings.minProfitMargin) {
						Logger.log("Profit margin of less than "+ Settings.minProfitMargin+"gp! Price-checking all herbs again...");
						selectedHerbPrice = null;
						return;
					}
					selectedHerbPrice = foundHerbPrice;
					Logger.log("Setting herb to process: " + selectedHerbPrice.herb.toString());
					needSelectedHerbPriceCheck = false;
					return;
				}
				if(!GrandExchange.isOpen()) {
					NPC GETeller = NPCs.closest("Grand Exchange Clerk");
					if(GETeller!=null) {
						if(GETeller.interact("History")) {
							Sleep.sleepUntil(ProfitableHerblore::isGEHistoryOpen, ()-> Players.getLocal().isMoving(), Sleepz.calculate(2222,2222),69);
						}
					}
					continue;
				}
				if(isGEActuallyReadyToCollect()) {
					collect();
					continue;
				}
				WidgetChild historyButton = Widgets.getWidgetChild(465, 3);
				if(historyButton != null && historyButton.isVisible()) {
					if(historyButton.interact("History")) {
						Sleep.sleepUntil(ProfitableHerblore::isGEHistoryOpen, Sleepz.calculate(2222, 2222));
					}
					continue;
				}
				continue;
			}
			//need to buy/sell all items starting with grimy then unf
			if (Settings.buyGrimy) {
				if(!boughtGrimy) {
					if(Inventory.count(id.coins) <= 20000) {
						if(Bank.count(id.coins) >= 1) {
							if(!Bank.isOpen()) {

								if(Inventory.isItemSelected()) {
									Inventory.deselect();
									return;
								}
								Bank.open();
								continue;
							}
							if(Bank.withdrawAll(id.coins)) {
								Sleep.sleepUntil(() -> !Bank.contains(id.coins), Sleepz.calculate(2222, 2222));
							}
							continue;
						}
						if(!GrandExchange.isOpen()) {
							openGE();
							continue;
						}
						if(isGEActuallyReadyToCollect()) {
							collect();
							continue;
						}
						if(!isGEEmpty()) continue;
						Logger.log("No more coins! Need more than 20k...");
						continue;
					}
					if(!GrandExchange.isOpen()) {
						openGE();
						continue;
					}
					if(putOffer) {
						if(completedGEOfferWithQty(selectedHerbPrice.herb.grimy,1,true)) {
							boughtGrimy = true;
							putOffer = false;
						}
						continue;
					}
					if(GrandExchange.getFirstOpenSlot() == -1) {
						GrandExchange.cancelAll();
						Sleep.sleepUntil(GrandExchange::isReadyToCollect, Sleepz.calculate(2222, 2222));
						continue;
					}
					if(isGEActuallyReadyToCollect()) {
						collect();
						continue;
					}
					if(GrandExchange.buyItem(selectedHerbPrice.herb.grimy, 1, grimyOfferPrice)) {
						putOffer = true;
						Sleep.sleepUntil(ProfitableHerblore::isGEActuallyReadyToCollect, Sleepz.calculate(2222, 2222));
					}
					continue;
				}
				if(!soldGrimy) {
					if(!GrandExchange.isOpen()) {
						openGE();
						continue;
					}
					if(putOffer) {
						if(completedGEOfferWithQty(selectedHerbPrice.herb.grimy,1,false)) {
							soldGrimy = true;
							putOffer = false;
						}
						continue;
					}
					if(isGEActuallyReadyToCollect()) {
						collect();
						continue;
					}
					if(GrandExchange.sellItem(new Item(selectedHerbPrice.herb.grimy,1).getName(), 1, 1)) {
						putOffer = true;
						Sleep.sleepUntil(ProfitableHerblore::isGEActuallyReadyToCollect, Sleepz.calculate(2222, 2222));
					}
					continue;
				}
			}
			if (Settings.buyClean) {
				if(!boughtClean) {
					if(Inventory.count(id.coins) <= 20000) {
						if(Bank.count(id.coins) >= 1) {
							if(!Bank.isOpen()) {
								if(Inventory.isItemSelected()) {
									Inventory.deselect();
									return;
								}
								Bank.open();
								continue;
							}
							if(Bank.withdrawAll(id.coins)) {
								Sleep.sleepUntil(() -> !Bank.contains(id.coins), Sleepz.calculate(2222, 2222));
							}
							continue;
						}
						if(!GrandExchange.isOpen()) {
							openGE();
							continue;
						}
						if(isGEActuallyReadyToCollect()) {
							collect();
							continue;
						}
						if(!isGEEmpty()) continue;
						Logger.log("No more coins! Need more than 20k...");
						continue;
					}
					if(!GrandExchange.isOpen()) {
						openGE();
						continue;
					}
					if(putOffer) {
						if(completedGEOfferWithQty(selectedHerbPrice.herb.clean,1,true)) {
							boughtClean = true;
							putOffer = false;
						}
						continue;
					}
					if(GrandExchange.getFirstOpenSlot() == -1) {
						GrandExchange.cancelAll();
						Sleep.sleepUntil(GrandExchange::isReadyToCollect, Sleepz.calculate(2222, 2222));
						continue;
					}
					if(isGEActuallyReadyToCollect()) {
						collect();
						continue;
					}
					if(GrandExchange.buyItem(selectedHerbPrice.herb.clean, 1, cleanOfferPrice)) {
						putOffer = true;
						Sleep.sleepUntil(ProfitableHerblore::isGEActuallyReadyToCollect, Sleepz.calculate(2222, 2222));
					}
					continue;
				}
				if(!soldClean) {
					if(!GrandExchange.isOpen()) {
						openGE();
						continue;
					}
					if(putOffer) {
						if(completedGEOfferWithQty(selectedHerbPrice.herb.clean,1,false)) {
							soldClean = true;
							putOffer = false;
						}
						continue;
					}
					if(isGEActuallyReadyToCollect()) {
						collect();
						continue;
					}
					if(GrandExchange.sellItem(new Item(selectedHerbPrice.herb.clean,1).getName(), 1, 1)) {
						putOffer = true;
						Sleep.sleepUntil(ProfitableHerblore::isGEActuallyReadyToCollect, Sleepz.calculate(2222, 2222));
					}
					continue;
				}
			}
			if (Settings.sellUnf) {
				if(!boughtUnf) {
					if(!GrandExchange.isOpen()) {
						openGE();
						continue;
					}
					if(putOffer) {
						if(completedGEOfferWithQty(selectedHerbPrice.herb.unf,1,true)) {
							boughtUnf = true;
							putOffer = false;
						}
						continue;
					}
					if(isGEActuallyReadyToCollect()) {
						collect();
						continue;
					}
					if(GrandExchange.buyItem(selectedHerbPrice.herb.unf, 1, unfOfferPrice)) {
						putOffer = true;
						Sleep.sleepUntil(ProfitableHerblore::isGEActuallyReadyToCollect, Sleepz.calculate(2222, 2222));
					}
					continue;
				}
				if (!soldUnf) {
					if(!GrandExchange.isOpen()) {
						openGE();
						continue;
					}
					if(putOffer) {
						if(completedGEOfferWithQty(selectedHerbPrice.herb.unf,1,false)) {
							soldUnf = true;
							putOffer = false;
						}
						continue;
					}
					if(isGEActuallyReadyToCollect()) {
						collect();
						continue;
					}
					if(GrandExchange.sellItem(new Item(selectedHerbPrice.herb.unf,1).getName(), 1, 1)) {
						putOffer = true;
						Sleep.sleepUntil(ProfitableHerblore::isGEActuallyReadyToCollect, Sleepz.calculate(2222, 2222));
					}
				}
			}

		}
		Logger.log("Timeout after 3 minutes of pricecheck selected herb! Resetting selected herb...");
		selectedHerbPrice = null;
	}
	public static void openGE()
	{
		if(isGEHistoryOpen())
		{
			WidgetChild exchangeButton = Widgets.getChildWidget(383, 2);
			if(exchangeButton != null && exchangeButton.isVisible())
			{
				if(exchangeButton.interact("Exchange"))
				{
					Sleep.sleepUntil(GrandExchange::isOpen, Sleepz.calculate(3333, 3333));
				}
			}
			return;
		}
		if(Inventory.isItemSelected()) {
			Inventory.deselect();
			return;
		}
		GrandExchange.open();
	}
	public static void buyMoreVials()
	{
		API.currentTask = "Buying 13k vials";
		int buyPrice = (int) Calculations.nextGaussianRandom(9, 2);
		int totalQty = 13000; //Assume max buy limit unless fewer coins
		if(Bank.contains(id.coins) || (Inventory.contains(id.coins) && !Inventory.onlyContains(id.coins))) {
			Logger.log("[buyMoreVials] Init - Withdrawing coins / depositing everything else");
			if(!Bank.isOpen()) {
				clickBank();
				return;
			}
			if((Inventory.contains(id.coins) && !Inventory.onlyContains(id.coins))) {
				if(Bank.depositAllExcept(id.coins)) {
					Sleep.sleepUntil(() -> Inventory.onlyContains(id.coins), Sleepz.calculate(3333, 3333));
				}
				return;
			}
			if(Bank.withdrawAll(id.coins)) {
				Sleep.sleepUntil(() -> !Bank.contains(id.coins), Sleepz.calculate(2222, 2222));
			}
			return;
		}
		
		if(Bank.isOpen()) {
			if(Bank.close()) Sleep.sleepUntil(() -> !Bank.isOpen(), Sleepz.calculate(2222,2222));
			return;
		}
		
		Logger.log("[buyMoreVials] Start");
		
		Timer timer = new Timer(180000);
		boolean putOffer = false;
		while(!timer.finished() && !ScriptManager.getScriptManager().isPaused() && ScriptManager.getScriptManager().isRunning()) {
			Sleepz.sleep(420, 696);
			if(!GrandExchange.isOpen()) {
				openGE();
				continue;
			}
			if(putOffer) {
				if(isGEEmpty()) break;
				if(isGEActuallyReadyToCollect()) {
					collect();
					continue;
				}
				Logger.log("Waiting for offer to complete...");
				Sleepz.sleep(2222, 2222);
				continue;
			}
			if(GrandExchange.getFirstOpenSlot() == -1) {
				GrandExchange.cancelAll();
				Sleep.sleepUntil(GrandExchange::isReadyToCollect, Sleepz.calculate(2222, 2222));
				continue;
			}
			if(isGEActuallyReadyToCollect()) {
				collect();
				continue;
			}
			if(Inventory.count(id.coins) < (totalQty * buyPrice)) {
				totalQty = (id.coins / buyPrice);
				if(totalQty <= 0) {
					Logger.log("Not enough coins to buy vials! Stopping script");
					return;
				}
			}
			if(GrandExchange.buyItem(id.vial, totalQty, buyPrice)) {
				putOffer = true;
				Sleep.sleepUntil(ProfitableHerblore::isGEActuallyReadyToCollect, Sleepz.calculate(2222, 2222));
			}
		}
	}
	public static void collect() {
		WidgetChild collect = Widgets.getWidgetChild(465,6,0);
		if(collect != null && collect.isVisible() && collect.interact()) Sleep.sleepUntil(() -> !isGEActuallyReadyToCollect(), Sleepz.calculate(2222, 2222));
	}
	
	public static Timer lastOfferTimer;

	public static void buyABunchOfHerbs()
	{
		int bestHerbToBuy;
		if (Settings.useLivePrices) {
			bestHerbToBuy = selectedHerbLivePrice.getBestBuyHerbID();
		} else {
			bestHerbToBuy = selectedHerbPrice.getBestBuyHerbID();
		}
		API.currentTask = "Buying a bunch of "+new Item(bestHerbToBuy,1).getName();
		if(Bank.contains(id.coins) || (Inventory.contains(id.coins) && !Inventory.onlyContains(id.coins))) {
			Logger.log("[buyABunchOfHerbs] Init - Withdrawing coins / depositing everything else");
			if(!Bank.isOpen()) {
				clickBank();
				return;
			}
			if((Inventory.contains(id.coins) && !Inventory.onlyContains(id.coins))) {
				if(Bank.depositAllExcept(id.coins))
				{
					Sleep.sleepUntil(() -> Inventory.onlyContains(id.coins), Sleepz.calculate(3333, 3333));
				}
				return;
			}
			if(Bank.withdrawAll(id.coins)) {
				Sleep.sleepUntil(() -> !Bank.contains(id.coins), Sleepz.calculate(2222, 2222));
			}
			return;
		}
		if(Bank.isOpen()) {
			if(Bank.close()) Sleep.sleepUntil(() -> !Bank.isOpen(), Sleepz.calculate(2222,2222));
			return;
		}
		Logger.log("[buyABunchOfHerbs] Starting to buy selected herb: " + selectedHerbPrice.herb.toString()+" after seeing out of all grimy/clean/unf! Updating profit/hr...");
		if(isGEEmpty()) {
			if(initCoins == -1) {
				initCoins = Bank.count(id.coins) + Inventory.count(id.coins);
			}
			if(API.profitTimer == null) {
				API.profitTimer = new Timer(2000000000);
			}
			API.profit = Bank.count(id.coins) + Inventory.count(id.coins) - initCoins;
			API.profitPerHour = (int) ((double) API.profit / ((double) API.profitTimer.elapsed() / 3600000));
		}
		Timer timer = new Timer((int) Calculations.nextGaussianRandom(80000, 20000));
		boolean putOffer = false;
		while(!timer.finished() && !ScriptManager.getScriptManager().isPaused() && ScriptManager.getScriptManager().isRunning()) {
			Sleepz.sleep(420, 696);
			if(!GrandExchange.isOpen()) {
				openGE();
				continue;
			}
			if(havePendingBuyOfferOfAnyGrimyOfLvl() || havePendingBuyOfferOfAnyCleanOfLvl()) {
				putOffer = true;
			}
			if(putOffer) {
				if(lastOfferTimer == null) {
					lastOfferTimer = new Timer((int) Calculations.nextGaussianRandom(450000, 222222));
				}
				if(isGEActuallyReadyToCollect()) {
					if(!GrandExchange.isOpen()) {
						openGE();
						continue;
					}
					collect();
					continue;
				}
				if(isGEEmpty()) break;
				//if inventory contains any grimy herbs
				int herbLvl = Skills.getRealLevel(Skill.HERBLORE);
				List<Integer> acceptableIDs = new ArrayList<>();
				Settings.acceptableHerbs.stream().filter(h -> h.lvl <= herbLvl).forEach(h -> {
					acceptableIDs.add(h.grimy);
					acceptableIDs.add(h.clean);
				});
				Item grimy = Inventory.get(i -> i != null && acceptableIDs.contains(i.getID()));
				if(grimy != null) {
					Logger.log("Found herbs: "+ grimy.getName()+" to process in invy! Stopping waiting for selected herb to buy");
					return;
				}
			}
			if(GrandExchange.getFirstOpenSlot() == -1) {
				GrandExchange.cancelAll();
				Sleep.sleepUntil(GrandExchange::isReadyToCollect, Sleepz.calculate(2222, 2222));
				continue;
			}
			if(isGEActuallyReadyToCollect()) {
				collect();
				continue;
			}
			int buyPrice;
			if (Settings.useLivePrices) {
				if (selectedHerbLivePrice.getBestBuyHerbID() == selectedHerbLivePrice.herb.grimy) {
					buyPrice = selectedHerbLivePrice.grimyLiveBuy;
				} else {
					buyPrice = selectedHerbLivePrice.cleanLiveBuy;
				}
			} else {
				if (selectedHerbPrice.getBestBuyHerbID() == selectedHerbPrice.herb.grimy) {
					buyPrice = selectedHerbPrice.grimyHigh - Settings.undercuttingBuyGrimy;
				} else {
					buyPrice = selectedHerbPrice.cleanHigh - Settings.undercuttingBuyGrimy;
				}
			}
			int maxAffordableQty = (int) Math.floor((double) Inventory.count(id.coins) / buyPrice); //buy up as many as can afford
			if(maxAffordableQty > Settings.maxHerbBuyQty) {
				int tmp = 0;
				while(randBuyQty < 1 || randBuyQty > Settings.maxHerbBuyQty ) {
					randBuyQty = (int) Calculations.nextGaussianRandom(Settings.maxHerbBuyQty, 5);
					tmp++;
					if(tmp > 1000) {
						Logger.log("Tried 1000 times to get a random gaussian mean: "+ Settings.maxHerbBuyQty+" sigma:"+5+
								", but all returned less than 1 or more than maxHerbBuyQty: "+ Settings.maxHerbBuyQty+" :-(");
						return;
					}
				}
			} else {
				int tmp = 0;
				while(randBuyQty < 1 || randBuyQty > maxAffordableQty) {
					randBuyQty = (int) Calculations.nextGaussianRandom(maxAffordableQty, 5);
					tmp++;
					if(tmp > 1000) {
						Logger.log("Tried 1000 times to get a random gaussian mean: "+ Settings.maxHerbBuyQty+" sigma:"+5+", but all returned less than 1 :-(");
						return;
					}
				}
			}
			//here we have a valid qty to buy set to variable
			int totalPrice = buyPrice * randBuyQty;
			if(Inventory.count(id.coins) < totalPrice) {
				Logger.log("Not enough coins to buy a bunch of herbs!");
				return;
			}
			if(GrandExchange.buyItem(bestHerbToBuy, randBuyQty, buyPrice)) {
				putOffer = true;
				randBuyQty = -1;
				Sleep.sleepUntil(ProfitableHerblore::isGEActuallyReadyToCollect, Sleepz.calculate(2222, 2222));
			}
		}
	}
	public static int randBuyQty = -1;
	public static void clickBank() {
		Filter<GameObject> bankFilter = g-> g!=null && 
				g.getName().equals("Grand Exchange booth") && 
				g.hasAction("Bank");
		GameObject bank = GameObjects.closest(bankFilter);
		if(bank != null && bank.interact("Bank")) Sleep.sleepUntil(Bank::isOpen,
				() -> Players.getLocal().isMoving(),
				Sleepz.calculate(3333, 3333),69);
	}
	/**
	 * checks bank for existing herbs, looks for all existing that have lvl for, randomize, set herb.
	 * Set herb == create new HerbPrice object and populate the selectedHerbPrice field with it.
	 * Returns true if not done checking, false if out of everything.
	 */
	public static boolean checkForAvailableExistingHerbs() {
		Logger.log("Checking for available existing herbs");
		if(!InvEquip.checkedBank()) {
			return true;
		}
		final int herbLvl = Skills.getRealLevel(Skill.HERBLORE);
		List<Herb> existingHerbs = new ArrayList<Herb>();
		List<Integer> existingUnf = new ArrayList<Integer>();
		for(Herb herb : Settings.acceptableHerbs) {
			if(herb.lvl > herbLvl) {
				continue;
			}
			if(Bank.contains(herb.clean) || Bank.contains(herb.grimy) ||  
					Inventory.contains(herb.clean) || Inventory.contains(herb.grimy) || Inventory.contains(new Item(herb.grimy,1).getNotedItemID())) existingHerbs.add(herb);
		}
		if(existingHerbs.isEmpty()) {
			for(Herb herb : Herb.values()) {
				if(Bank.contains(herb.unf) || Inventory.contains(herb.unf)  || Inventory.contains(new Item(herb.unf,1).getName())) existingUnf.add(herb.unf);
			}
			if(!Settings.sellUnf || existingUnf.isEmpty()) {
				if(isGEEmpty()) {
					return false;
				}
				if(havePendingBuyOfferOfAnyGrimyOfLvl() || havePendingBuyOfferOfAnyCleanOfLvl() || havePendingSellOfferOfAnyUnf()) {
					Logger.log("Have pending offers");
					if(!GrandExchange.isOpen()) {
						openGE();
						return true;
					}
					if(havePendingBuyOfferOfAnyGrimyOfLvl() || havePendingBuyOfferOfAnyCleanOfLvl()) {
						if(lastOfferTimer == null) {
							if(GrandExchange.cancelAll()) {
								lastUnfSellTimer = null;
								lastOfferTimer = null;
							}
						}
						else if(lastOfferTimer.finished()) {
							if(GrandExchange.cancelAll()) {
								needSelectedHerbPriceCheck = true;
								lastUnfSellTimer = null;
								lastOfferTimer = null;
							}
						}
					}
					if(havePendingSellOfferOfAnyUnf()) {
						if(lastUnfSellTimer == null) {
							if(GrandExchange.cancelAll()) {
								lastUnfSellTimer = null;
								lastOfferTimer = null;
							}
						}
						else if(lastUnfSellTimer.finished()) {
							if(GrandExchange.cancelAll()) {
								lastUnfSellTimer = null;
								lastOfferTimer = null;
							}
						}
					}
					return true;
				}
				return false;
			}
			if (Settings.sellUnf) {
				//sell all existing unf if no more clean or grimy of our lvl
				sellAllUnf();
				return true;
			}
			return false;
		}
		Collections.shuffle(existingHerbs);
		if (Settings.useLivePrices) {
			selectedHerbLivePrice = new LiveHerbPrice(existingHerbs.get(0));
			Logger.log("Found random existing herb to process: " + selectedHerbLivePrice.herb.toString());
		} else {
			selectedHerbPrice = new HerbPrice(existingHerbs.get(0),0,0,0,0,0,0);
			Logger.log("Found random existing herb to process: " + selectedHerbPrice.herb.toString());
		}
		return true;
	}
	public static boolean havePendingBuyOfferOfAnyGrimyOfLvl() {
		final int herbLvl = Skills.getRealLevel(Skill.HERBLORE);
		for(GrandExchangeItem i : GrandExchange.getItems()) {
			boolean found = false;
			for(Herb herb : Settings.acceptableHerbs) {
				if(herbLvl < herb.lvl) continue;
				if(herb.grimy == i.getID()) {
					found = true;
					break;
				}
			}
			if(!found) {
				continue;
			}
			//have offer- check status
			if(i.getStatus() == Status.BUY || i.getStatus() == Status.BUY_COLLECT) {
				return true;
			}
		}
		return false;
	}
	public static boolean havePendingBuyOfferOfAnyCleanOfLvl() {
		final int herbLvl = Skills.getRealLevel(Skill.HERBLORE);
		for(GrandExchangeItem i : GrandExchange.getItems()) {
			boolean found = false;
			for(Herb herb : Settings.acceptableHerbs) {
				if(herbLvl < herb.lvl) continue;
				if(herb.clean == i.getID()) {
					found = true;
					break;
				}
			}
			if(!found) {
				continue;
			}
			//have offer- check status
			if(i.getStatus() == Status.BUY || i.getStatus() == Status.BUY_COLLECT) {
				return true;
			}
		}
		return false;
	}
	public static boolean havePendingSellOfferOfAnyUnf()
	{
		for(GrandExchangeItem i : GrandExchange.getItems())
		{
			boolean found = false;
			for(Herb herb : Herb.values())
			{
				if(herb.unf == i.getID()) 
				{
					found = true;
					break;
				}
			}
			if(!found) continue;
			//have offer- check status
			if(i.getStatus() == Status.SELL || i.getStatus() == Status.SELL_COLLECT) return true;
		}
		return false;
	}
	/**
	 * sets a timer for 3 minutes for each herb to buy/sell 1 grimy and 1 unf pot of lvl high enough to process.
	 * after recording successfully it will set the highest profit margin herb to selectedHerbPrice to start buying via buyABunchOfHerbs().	
	 */
	public static void priceCheckAvailableHerbs()
	{
		API.currentTask = "Price checking herbs";
		//check bank, get coins, deposit everything else
		if(!InvEquip.checkedBank()) return;
		List<HerbPrice> validHerbPrices = new ArrayList<HerbPrice>();
		if(Bank.contains(id.coins) || (Inventory.contains(id.coins) && !Inventory.onlyContains(id.coins))) {
			Logger.log("[priceCheckAvailableHerbs] Init - withdrawing all coins / depositing everything else");
			
			if(!Bank.isOpen()) {
				clickBank();
				return;
			}
			if((Inventory.contains(id.coins) && !Inventory.onlyContains(id.coins))) {
				if(Bank.depositAllExcept(id.coins)) {
					Sleep.sleepUntil(() -> Inventory.onlyContains(id.coins), Sleepz.calculate(3333, 3333));
				}
				return;
			}
			if(Bank.withdrawAll(id.coins)) {
				Sleep.sleepUntil(() -> !Bank.contains(id.coins), Sleepz.calculate(2222, 2222));
			}
			return;
		}
		if(Bank.isOpen()) {
			if(Bank.close()) Sleep.sleepUntil(() -> !Bank.isOpen(), Sleepz.calculate(2222,2222));
			return;
		}
		Logger.log("[priceCheckAvailableHerbs] Starting");
		
		final int herblvl = Skills.getRealLevel(Skill.HERBLORE);
		//invy only contains coins now
		for(Herb herb : Settings.acceptableHerbs) {
			if(herblvl < herb.lvl) continue;
			Logger.log("Price checking herb: " + herb.toString());
			int grimyOfferPrice = (int) Calculations.nextGaussianRandom((LivePrices.getHigh(herb.grimy) * 5), 50);
			if(grimyOfferPrice >= 20000) grimyOfferPrice = Calculations.random(18000,22000);
			int cleanOfferPrice = (int) Calculations.nextGaussianRandom((LivePrices.getHigh(herb.grimy) * 5), 50);
			if(cleanOfferPrice >= 20000) grimyOfferPrice = Calculations.random(18000,22000);
			int unfOfferPrice = (int) Calculations.nextGaussianRandom((LivePrices.getHigh(herb.unf) * 5), 50);
			if(unfOfferPrice >= 20000) unfOfferPrice = Calculations.random(18000,22000);

			boolean boughtGrimy = false;
			boolean soldGrimy = false;
			boolean boughtClean = false;
			boolean soldClean = false;
			boolean boughtUnf = false;
			boolean soldUnf = false;
			boolean putOffer = false;
			Timer timeout = new Timer(180000);
			boolean notTimeout = false;
			while(!timeout.finished() && ScriptManager.getScriptManager().isRunning() && 
					!ScriptManager.getScriptManager().isPaused()) {
				//need to buy grimy, then sell grimy, then buy unf, sell unf,
				//then check history and search from top down for item id and qty = 1 of sold
				Sleepz.sleep(69, 420);
				if((!Settings.buyGrimy || (boughtGrimy && soldGrimy)) &&
						(!Settings.buyClean || (boughtClean && soldClean)) &&
						(!Settings.sellUnf || (boughtUnf && soldUnf))) {
					if(isGEHistoryOpen()) {
						final int boughtGrimyFor = getLatestGEHistoryPrice(herb.grimy, true);
						final int soldGrimyFor = getLatestGEHistoryPrice(herb.grimy, false);
						final int boughtCleanFor = getLatestGEHistoryPrice(herb.clean, true);
						final int soldCleanFor = getLatestGEHistoryPrice(herb.clean, false);
						final int boughtUnfFor = getLatestGEHistoryPrice(herb.unf, true);
						final int soldUnfFor = getLatestGEHistoryPrice(herb.unf, false);
						if((Settings.buyGrimy && (boughtGrimyFor == -1 || soldGrimyFor == -1)) ||
								(Settings.buyClean && (boughtCleanFor == -1 || soldCleanFor == -1)) ||
								(Settings.sellUnf && (boughtUnfFor == -1 || soldUnfFor == -1))) {
							Logger.log("Failed to observe existing bought/sold qtys for grimy/unf herb! Trying again...");
							boughtGrimy = soldGrimy = boughtUnf = soldUnf = false;
							continue;
						}
						HerbPrice foundHerbPrice = new HerbPrice(herb,soldGrimyFor,boughtGrimyFor,soldCleanFor,boughtCleanFor,soldUnfFor,boughtUnfFor);
						foundHerbPrice.printHerbPrices();
						if(foundHerbPrice.profitMargin < Settings.minProfitMargin) {
							Logger.log("Profit margin of less than "+ Settings.minProfitMargin+"gp! Skipping this herb...");
							notTimeout = true;
							break;
						}
						validHerbPrices.add(foundHerbPrice);
						notTimeout = true;
						break;
					}
					if(!GrandExchange.isOpen()) {
						NPC GETeller = NPCs.closest("Grand Exchange Clerk");
						if(GETeller!=null) {
							if(GETeller.interact("History")) {
								Sleep.sleepUntil(ProfitableHerblore::isGEHistoryOpen,
										()-> Players.getLocal().isMoving(), Sleepz.calculate(2222,2222),69);
							}
						}
						continue;
					}
					if(isGEActuallyReadyToCollect()) {
						collect();
						continue;
					}
					WidgetChild historyButton = Widgets.getWidgetChild(465, 3);
					if(historyButton != null && historyButton.isVisible()) {
						if(historyButton.interact("History")) {
							Sleep.sleepUntil(ProfitableHerblore::isGEHistoryOpen, Sleepz.calculate(2222, 2222));
						}
						continue;
					}
					continue;
				}
				//need to buy/sell all items starting with grimy then unf
				if (Settings.buyGrimy) {
					if(!boughtGrimy) {
						if(!GrandExchange.isOpen()) {
							openGE();
							continue;
						}
						if(putOffer) {
							if(completedGEOfferWithQty(herb.grimy,1,true)) {
								boughtGrimy = true;
								putOffer = false;
							}
							continue;
						}
						if(GrandExchange.getFirstOpenSlot() == -1) {
							GrandExchange.cancelAll();
							Sleep.sleepUntil(GrandExchange::isReadyToCollect, Sleepz.calculate(2222, 2222));
							continue;
						}
						if(isGEActuallyReadyToCollect()) {
							collect();
							continue;
						}
						if(GrandExchange.buyItem(herb.grimy, 1, grimyOfferPrice)) {
							putOffer = true;
							Sleep.sleepUntil(ProfitableHerblore::isGEActuallyReadyToCollect, Sleepz.calculate(2222, 2222));
						}
						continue;
					}
					if(!soldGrimy) {
						if(!GrandExchange.isOpen()) {
							openGE();
							continue;
						}
						if(putOffer) {
							if(completedGEOfferWithQty(herb.grimy,1,false)) {
								soldGrimy = true;
								putOffer = false;
							}
							continue;
						}
						if(isGEActuallyReadyToCollect()) {
							collect();
							continue;
						}
						if(GrandExchange.sellItem(new Item(herb.grimy,1).getName(), 1, 1)) {
							putOffer = true;
							Sleep.sleepUntil(ProfitableHerblore::isGEActuallyReadyToCollect, Sleepz.calculate(2222, 2222));
						}
						continue;
					}
				}
				if (Settings.buyClean) {
					if(!boughtClean) {
						if(!GrandExchange.isOpen()) {
							openGE();
							continue;
						}
						if(putOffer) {
							if(completedGEOfferWithQty(herb.clean,1,true)) {
								boughtClean = true;
								putOffer = false;
							}
							continue;
						}
						if(GrandExchange.getFirstOpenSlot() == -1) {
							GrandExchange.cancelAll();
							Sleep.sleepUntil(GrandExchange::isReadyToCollect, Sleepz.calculate(2222, 2222));
							continue;
						}
						if(isGEActuallyReadyToCollect()) {
							collect();
							continue;
						}
						if(GrandExchange.buyItem(herb.clean, 1, cleanOfferPrice)) {
							putOffer = true;
							Sleep.sleepUntil(ProfitableHerblore::isGEActuallyReadyToCollect, Sleepz.calculate(2222, 2222));
						}
						continue;
					}
					if(!soldClean) {
						if(!GrandExchange.isOpen()) {
							openGE();
							continue;
						}
						if(putOffer) {
							if(completedGEOfferWithQty(herb.clean,1,false)) {
								soldClean = true;
								putOffer = false;
							}
							continue;
						}
						if(isGEActuallyReadyToCollect()) {
							collect();
							continue;
						}
						if(GrandExchange.sellItem(new Item(herb.clean,1).getName(), 1, 1)) {
							putOffer = true;
							Sleep.sleepUntil(ProfitableHerblore::isGEActuallyReadyToCollect, Sleepz.calculate(2222, 2222));
						}
						continue;
					}
				}
				if (Settings.sellUnf) {
					if(!boughtUnf) {
						if(!GrandExchange.isOpen()) {
							openGE();
							continue;
						}
						if(putOffer) {
							if(completedGEOfferWithQty(herb.unf,1,true)) {
								boughtUnf = true;
								putOffer = false;
							}
							continue;
						}
						if(isGEActuallyReadyToCollect()) {
							collect();
							continue;
						}
						if(GrandExchange.buyItem(herb.unf, 1, unfOfferPrice)) {
							putOffer = true;
							Sleep.sleepUntil(ProfitableHerblore::isGEActuallyReadyToCollect, Sleepz.calculate(2222, 2222));
						}
						continue;
					}
					if (!soldUnf) {
						if(!GrandExchange.isOpen()) {
							openGE();
							continue;
						}
						if(putOffer) {
							if(completedGEOfferWithQty(herb.unf,1,false)) {
								soldUnf = true;
								putOffer = false;
							}
							continue;
						}
						if(isGEActuallyReadyToCollect()) {
							collect();
							continue;
						}
						if(GrandExchange.sellItem(new Item(herb.unf,1).getName(), 1, 1)) {
							putOffer = true;
							Sleep.sleepUntil(ProfitableHerblore::isGEActuallyReadyToCollect, Sleepz.calculate(2222, 2222));
						}
					}
				}
			}
			if(!notTimeout)	Logger.log("Timeout after 3 minutes of herb pricecheck!");
		}
		if(validHerbPrices.isEmpty()) {
			Logger.log("Failed to obtain any acceptable herb prices after checking all available herbs for lvl!");
			return;
		}
		if(Settings.xpMode) {
			Logger.log("Sorting herb list according to highest lvl");
			validHerbPrices.sort((o1, o2) -> o2.herb.lvl - o1.herb.lvl);
		} else {
			Logger.log("Sorting herb list according to highest profit margin");
			validHerbPrices.sort((o1, o2) -> o2.profitMargin - o1.profitMargin);
		}
		for(HerbPrice herbPrice : validHerbPrices) {
			herbPrice.printHerbPrices();
		}
		selectedHerbPrice = validHerbPrices.get(0);
		Logger.log("~Choosing herb: "+selectedHerbPrice.herb.toString()+"~");
		needSelectedHerbPriceCheck = false;
	}
	public static boolean isGEHistoryOpen()
	{
		WidgetChild historyHeader = Widgets.getWidgetChild(383, 1 , 1);
		return historyHeader != null && historyHeader.isVisible() && historyHeader.getText().contains("Grand Exchange Trade History");
	}
	/**
	 * Call after GE History open pls. 
	 * Scans top down for the price that the latest offer was fulfilled at, returns -1 if not found.
	 * Gets price per item, not total offer price.
	 * When searching sell offers, accounts for the stupid GE tax to return the price that the buyer paid for it.
	 */
	private final static String yellowColour = "<col=ffb83f>";
	private final static String greyColour = "<col=9f9f9f>";
	//orange colour is lack of yellow
	public static int getLatestGEHistoryPrice(int itemID, boolean bought)
	{
		for(int i = 0; i < 240; i = i+6)
		{
			WidgetChild itemIDStackSize = Widgets.getWidgetChild(383,3,(i+4));
			final int ID = itemIDStackSize.getItemId();
			if(ID != itemID) continue;
			WidgetChild boughtOrSold = Widgets.getWidgetChild(383,3,(i+2));
			WidgetChild pricingWidget = Widgets.getWidgetChild(383,3,(i+5));
			final int stackSize = itemIDStackSize.getItemStack();
			final boolean buy = boughtOrSold.getText().contains("Bought:");
			if(buy != bought) continue;
			String priceText = pricingWidget.getText();
			if(bought)
			{
				if(stackSize > 1)
				{
					return Integer.parseInt(priceText.split("</col><br>= ")[1].split(" each")[0].replace(",", ""));
				}
				else return Integer.parseInt(priceText.replace(yellowColour, "").split(" coins")[0].replace(",", ""));
			}
			//sold with GE tax
			if(priceText.contains(greyColour))
			{
				//must find the total offer price paid in grey, divide by stacksize, round up
				return (int) Math.ceil((((double)Integer.parseInt(priceText.split("</col><br><col=9f9f9f>\\(")[1].split(" - ")[0].replace(",", ""))) / stackSize));
			}
			//sold no GE tax
			if(stackSize > 1)
			{
				return Integer.parseInt(priceText.split("</col><br>= ")[1].split(" each")[0].replace(",", ""));
			}
			else return Integer.parseInt(priceText.replace(yellowColour, "").split(" ")[0]);
		}
		return -1;
	}
	/**
	 * Checks GE offers to see if an offer slot has bought at least the qty of item, if so returns true, else false.
	 */
	public static boolean completedGEOfferWithQty(int itemID,int minQty, boolean buy)
	{
		for(GrandExchangeItem i : GrandExchange.getItems())
		{
			if(i.getID() <=  0 || (buy ? !i.isBuyOffer() : !i.isSellOffer()))  continue;
			if(i.getID() == itemID && i.getTransferredAmount() >= minQty)
			{
				Logger.log("Found offer fulfilled for "+(buy ? "buy" : "sell")+" item: " + new Item(itemID,1).getName() + " in minQty: " + minQty);
				return true;
			}
		}
		//return as completed offer if none exist
		return true;
	}
	
	public static boolean isGEEmpty()
	{
		for(GrandExchangeItem i : GrandExchange.getItems())
		{
			if(i.getID() ==  0)  continue;
			return false;
		}
		return true;
	}
	public static boolean isGEActuallyReadyToCollect()
	{
		if(!GrandExchange.isOpen()) return false;
		WidgetChild collect = Widgets.getWidgetChild(465,6,0);
		if(collect != null)
		{
			return !collect.isHidden();
		}
		return false;
	}
	public static int initCoins = -1;



	public static Herb getHerbFromID(int anyCleanGrimyUnfID) {
		Item anyItem = new Item(anyCleanGrimyUnfID, 1);
		if (anyItem == null || !anyItem.isValid()) {
			return null;
		}
		//extract unnoted ID to compare if noted value is being checked
		if (anyItem.isNoted()) {
			anyCleanGrimyUnfID = anyItem.getUnnotedItemID();
		}

		for(Herb herb : Herb.values()) {
			if(herb.grimy == anyCleanGrimyUnfID || 
					herb.clean == anyCleanGrimyUnfID || 
					herb.unf == anyCleanGrimyUnfID) return herb;
		}
		return null;
	}
}