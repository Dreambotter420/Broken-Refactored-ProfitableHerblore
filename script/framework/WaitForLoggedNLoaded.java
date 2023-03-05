package script.framework;

import org.dreambot.api.Client;
import org.dreambot.api.data.GameState;
import org.dreambot.api.utilities.Sleep;
import script.utilities.Sleepz;

public class WaitForLoggedNLoaded extends Leaf {

    @Override
    public boolean isValid() {
        return !loggedNLoaded();
    }

    @Override
    public int onLoop() {
        return Sleepz.calculate(420,420);
    }

    private boolean loggedNLoaded()
    {
        if(Client.getGameState() == GameState.LOADING ||
                Client.getGameState() == GameState.GAME_LOADING)
        {
            Sleep.sleepUntil(() -> (Client.getGameState() != GameState.LOADING &&
                    Client.getGameState() != GameState.GAME_LOADING), 10000);
            Sleepz.sleep(1111, 1111);
            return false;
        }
        else if(Client.getGameState() != GameState.LOGGED_IN)
        {
            Sleepz.sleep(1111, 1111);
            return false;
        }
        return true;
    }

}
