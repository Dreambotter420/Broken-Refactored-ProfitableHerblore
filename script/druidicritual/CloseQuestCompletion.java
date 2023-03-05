package script.druidicritual;

import org.dreambot.api.ClientSettings;
import org.dreambot.api.methods.input.Keyboard;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.wrappers.widgets.WidgetChild;
import script.framework.Leaf;
import script.utilities.API;
import script.utilities.Sleepz;

public class CloseQuestCompletion extends Leaf {

    @Override
    public boolean isValid() {
        WidgetChild questCompletionClose = Widgets.getWidgetChild(153,16);
        return questCompletionClose != null && questCompletionClose.isVisible();
    }

    @Override
    public int onLoop() {
        API.currentTask = "Closing quest completion interface";
        if(ClientSettings.isEscInterfaceClosingEnabled()) {
            Keyboard.closeInterfaceWithESC();
        }
        else if (!Widgets.getWidgetChild(153,16).interact("Close")) {
            return Sleepz.calculate(111,222);
        }

        return Sleepz.calculate(420, 2222);
    }
}
