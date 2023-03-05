package script.profitableherblore;


import com.sun.xml.internal.ws.api.config.management.policy.ManagementAssertion;
import org.dreambot.api.methods.grandexchange.LivePrices;
import org.dreambot.api.utilities.Logger;

import script.profitableherblore.Herb;
import script.settings.Settings;

public class HerbPrice {
	public int grimyLow;
	public int grimyHigh;
	public int cleanLow;
	public int cleanHigh;
	public int unfLow;
	public int unfHigh;
	public int profitMargin;
	public Herb herb;
	
	public HerbPrice (Herb herb, int grimyLow, int grimyHigh, int cleanLow, int cleanHigh, int unfLow, int unfHigh)
	{
		this.herb = herb;
		this.grimyLow = grimyLow;
		this.grimyHigh = grimyHigh;
		this.cleanLow = cleanLow;
		this.cleanHigh = cleanHigh;
		this.unfLow = unfLow;
		this.unfHigh = unfHigh;
		this.profitMargin = unfLow - grimyHigh;
	}
	public void printHerbPrices() {
		StringBuilder logLine = new StringBuilder();
		logLine.append("Herb ["+this.herb.toString()+"] ");
		if (Settings.useLivePrices) {
			if (Settings.buyGrimy) {
				logLine.append("Grimy Low ["+grimyLow+"] Grimy High ["+grimyHigh+"] ");
			}
			if (Settings.buyClean) {
				logLine.append("Clean Low ["+cleanLow+"] Clean High ["+cleanHigh+"] ");
			}
			if (Settings.sellUnf) {
				logLine.append("(unf) Low ["+unfLow+"] (unf) High ["+unfHigh+"] ");
			}
		}
		logLine.append("Profit Margin ["+profitMargin+"]");
		Logger.log(logLine.toString());
	}

	public int getBestBuyHerbID() {
		if (Settings.buyGrimy && Settings.buyClean) {
			if(grimyHigh <= cleanHigh) {
				return herb.grimy;
			}
			else {
				return herb.clean;
			}
		}
		if (Settings.buyGrimy) {
			return herb.grimy;
		}
		if (Settings.buyClean) {
			return herb.clean;
		}
		return -1;
	}
}