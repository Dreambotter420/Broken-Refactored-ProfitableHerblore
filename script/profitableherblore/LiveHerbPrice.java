package script.profitableherblore;


import org.dreambot.api.methods.grandexchange.LivePrices;
import org.dreambot.api.utilities.Logger;
import script.settings.Settings;

public class LiveHerbPrice {
	public int grimyLiveBuy;
	public int cleanLiveBuy;
	public int unfLiveSell;
	public int profitMargin;
	public Herb herb;

	public LiveHerbPrice(Herb herb)
	{
		this.herb = herb;
		updatePrices();
	}

	public void updatePrices() {
		this.grimyLiveBuy = (int) (((double) LivePrices.getLow(this.herb.grimy)) * (1 + ((double) Settings.livePricesBuy) / 100));
		this.cleanLiveBuy = (int) (((double) LivePrices.getLow(this.herb.clean)) * (1 + ((double) Settings.livePricesBuy) / 100));
		this.unfLiveSell = (int) (((double) LivePrices.getHigh(this.herb.unf)) * (1 - ((double) Settings.livePricesSell) / 100));
		if (Settings.sellUnf) {
			if (Settings.buyGrimy && Settings.buyClean) {
				this.profitMargin = this.unfLiveSell - Math.max(this.grimyLiveBuy, this.cleanLiveBuy);
			}
			else if (!Settings.buyGrimy && Settings.buyClean) {
				this.profitMargin = this.unfLiveSell - this.cleanLiveBuy;
			}
			else if (Settings.buyGrimy && !Settings.buyClean) {
				this.profitMargin = this.unfLiveSell - this.grimyLiveBuy;
			}
		}
	}
	public int getBestBuyHerbID(){
		updatePrices();
		if (Settings.buyGrimy && Settings.buyClean) {
			if(grimyLiveBuy <= cleanLiveBuy) {
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
	public void printHerbPrices() {
		updatePrices();
		StringBuilder logLine = new StringBuilder();
		logLine.append("Herb ["+this.herb.toString()+"] ");
		logLine.append("LivePrices: ");
		if (Settings.buyGrimy) {
			logLine.append("Grimy (Low + "+Settings.livePricesBuy+"%) ["+this.grimyLiveBuy +"] ");
		}
		if (Settings.buyClean) {
			logLine.append("Clean (Low + "+Settings.livePricesBuy+"%) ["+this.cleanLiveBuy +"] ");
		}
		if (Settings.sellUnf) {
			logLine.append("(unf) (High - "+Settings.livePricesSell+"%) ["+this.unfLiveSell +"] ");
		}
		Logger.log(logLine.toString());
	}
}