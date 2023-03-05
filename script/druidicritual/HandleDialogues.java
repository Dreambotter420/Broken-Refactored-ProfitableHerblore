package script.druidicritual;

import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.input.Keyboard;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.utilities.impl.Condition;
import org.dreambot.api.wrappers.widgets.WidgetChild;
import script.framework.Leaf;
import script.utilities.Sleepz;

public class HandleDialogues extends Leaf {
    public static String dialog = "";

    @Override
    public boolean isValid() {
        return Dialogues.isProcessing() ||
                Dialogues.canContinue() ||
                Dialogues.areOptionsAvailable();
    }

    @Override
    public int onLoop() {
        if (Dialogues.isProcessing()) {
            Sleep.sleepTick();
            return Sleepz.calculate(69,333);
        }

        if (Dialogues.canContinue()) {
            final int timeout = Sleepz.calculate(3333,3333);
            final Condition condition = () -> {
                updateLastNPCDialog();
                return Dialogues.areOptionsAvailable() ||
                        !Dialogues.inDialogue();
            };
            Keyboard.holdSpace(condition,timeout);
            Sleep.sleepUntil(condition, timeout);
            return Sleepz.calculate(69,333);
        }

        if (Dialogues.areOptionsAvailable()) {
            if (Dialogues.chooseOption("I\'m in search of a quest.") ||
                    Dialogues.chooseOption("Okay, I will try and help.") ||
                    Dialogues.chooseOption("Yes.") ||
                    Dialogues.chooseOption("I\'ve been sent to help purify the Varrock stone circle.") ||
                    Dialogues.chooseOption("Ok, I\'ll do that then.")) {
                Sleep.sleepTick();
            }
        }
        return Sleepz.calculate(69,333);
    }
    public static void updateLastNPCDialog()
    {
        String txt = "";
        //first check resizable and fixed chat widgets for text (player / npc)
        WidgetChild dialogWidget = Widgets.getWidgetChild(193, 2);
        if(dialogWidget == null || !dialogWidget.isVisible()) {
            dialogWidget = Widgets.getWidgetChild(231,6);
        }
        if(dialogWidget != null && dialogWidget.isVisible()) {
            txt = dialogWidget.getText();
            if(txt != null && !txt.isEmpty() && !txt.equalsIgnoreCase("null")) {
                if (!dialog.equalsIgnoreCase(txt)) {
                    Logger.log("NPC Dialogue: " + txt);
                    dialog = txt;
                }
            }
            return;
        }
        //if nothing, check for NPC Dialogue from DB API
        txt = Dialogues.getNPCDialogue();
        if(txt != null && !txt.isEmpty() && !txt.equalsIgnoreCase("null")) {
            if (!dialog.equalsIgnoreCase(txt)) {
                Logger.log("NPC Dialogue: " + txt);
                dialog = txt;
            }
        }
    }
}
