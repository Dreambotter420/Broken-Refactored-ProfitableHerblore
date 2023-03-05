package script.settings;

import org.dreambot.api.utilities.Logger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class GUI {

    public static boolean closedGUI = false;

    public static void createGUI() {
        JFrame frame = new JFrame();
        frame.setResizable(false);
        frame.setTitle("Profitable Herblore Settings");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setBounds(100, 100, 474, 449);
        JPanel contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        frame.setContentPane(contentPane);
        contentPane.setLayout(null);

        JLabel lblNewLabel_1 = new JLabel("Approved Herbs List");
        lblNewLabel_1.setBounds(160, 217, 126, 17);
        contentPane.add(lblNewLabel_1);
        lblNewLabel_1.setFont(new Font("Times New Roman", Font.BOLD, 14));
        lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);

        JTextField minProfitMarginTextField = new JTextField();
        minProfitMarginTextField.setBounds(395, 11, 53, 20);
        contentPane.add(minProfitMarginTextField);
        minProfitMarginTextField.setColumns(10);

        JLabel lblNewLabel = new JLabel("Minimum profit margin per grimy -> unf herb (default 50 gp):");
        lblNewLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel.setBounds(52, 11, 333, 20);
        contentPane.add(lblNewLabel);

        JCheckBox XPModeBox = new JCheckBox("XP Mode?");
        XPModeBox.setBounds(10, 31, 93, 23);
        XPModeBox.addActionListener(l -> {
            Settings.xpMode = XPModeBox.isSelected();
        });
        contentPane.add(XPModeBox);

        JCheckBox botModeBox = new JCheckBox("Bot Mode?");
        botModeBox.setBounds(10, 57, 93, 23);
        botModeBox.addActionListener(l -> {
            Settings.botMode = botModeBox.isSelected();
        });
        contentPane.add(botModeBox);

        JLabel lblMaximumBuyQuantity = new JLabel("Maximum Grimy herb buy quantity:");
        lblMaximumBuyQuantity.setHorizontalAlignment(SwingConstants.RIGHT);
        lblMaximumBuyQuantity.setBounds(189, 34, 196, 20);
        contentPane.add(lblMaximumBuyQuantity);

        JTextField maxGrimyTextField = new JTextField();
        maxGrimyTextField.setColumns(10);
        maxGrimyTextField.setBounds(395, 34, 53, 20);
        contentPane.add(maxGrimyTextField);

        JPanel panel = new JPanel();
        panel.setBounds(20, 231, 428, 92);
        contentPane.add(panel);

        JLabel livePricesSellLabel = new JLabel("[LivePrices] Sell at % less than LivePrices High:");
        livePricesSellLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        livePricesSellLabel.setBounds(134, 126, 251, 20);
        contentPane.add(livePricesSellLabel);
        JTextField livePricesSellTextField = new JTextField();
        livePricesSellTextField.setColumns(10);
        livePricesSellTextField.setBounds(395, 126, 53, 20);
        contentPane.add(livePricesSellTextField);

        JLabel livePricesBuyLabel = new JLabel("[LivePrices] Buy at % more than LivePrices Low:");
        livePricesBuyLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        livePricesBuyLabel.setBounds(134, 103, 251, 20);
        contentPane.add(livePricesBuyLabel);
        JTextField livePricesBuyTextField = new JTextField();
        livePricesBuyTextField.setColumns(10);
        livePricesBuyTextField.setBounds(395, 103, 53, 20);
        contentPane.add(livePricesBuyTextField);

        // adding 4 more checkboxes
        JCheckBox buyGrimyCheckBox = new JCheckBox("Buy grimy?");
        buyGrimyCheckBox.setSelected(true);
        buyGrimyCheckBox.setBounds(10, 83, 105, 23);
        buyGrimyCheckBox.addActionListener(l -> {
            Settings.buyGrimy = buyGrimyCheckBox.isSelected();
        });
        contentPane.add(buyGrimyCheckBox);

        JCheckBox buyCleanCheckBox = new JCheckBox("Buy clean?");
        buyCleanCheckBox.setBounds(10, 109, 105, 23);
        buyCleanCheckBox.addActionListener(l -> {
            Settings.buyClean = buyCleanCheckBox.isSelected();
        });
        contentPane.add(buyCleanCheckBox);

        JCheckBox sellUnfCheckBox = new JCheckBox("Sell (unf)?");
        sellUnfCheckBox.setSelected(true);
        sellUnfCheckBox.setBounds(10, 135, 105, 23);
        sellUnfCheckBox.addActionListener(l -> {
            Settings.sellUnf = sellUnfCheckBox.isSelected();
        });
        contentPane.add(sellUnfCheckBox);

        JCheckBox processAllCheckBox = new JCheckBox("Process all owned items then stop script?");
        processAllCheckBox.setBounds(10, 187, 359, 23);
        processAllCheckBox.addActionListener(l -> {
            Settings.processAllThenEnd = processAllCheckBox.isSelected();
        });
        contentPane.add(processAllCheckBox);

        JCheckBox useLivePricesBox = new JCheckBox("Use DreamBot API LivePrices? (default is in-game price checking)");
        useLivePricesBox.setBounds(10, 161, 438, 23);
        useLivePricesBox.addActionListener(l -> {
            Settings.useLivePrices = useLivePricesBox.isSelected();
        });
        contentPane.add(useLivePricesBox);

        JCheckBox Guam = new JCheckBox("Guam");
        Guam.setSelected(true);
        Guam.addActionListener(l -> {
            Settings.useGuam = Guam.isSelected();
        });
        panel.add(Guam);

        JCheckBox Harralander = new JCheckBox("Harralander");
        Harralander.setSelected(true);
        Harralander.addActionListener(l -> {
            Settings.useHarralander = Harralander.isSelected();
        });
        panel.add(Harralander);

        JCheckBox Ranarr = new JCheckBox("Ranarr");
        Ranarr.setSelected(true);
        Ranarr.addActionListener(l -> {
            Settings.useRanarr = Ranarr.isSelected();
        });
        panel.add(Ranarr);

        JCheckBox Toadflax = new JCheckBox("Toadflax");
        Toadflax.setSelected(true);
        Toadflax.addActionListener(l -> {
            Settings.useToadflax = Toadflax.isSelected();
        });
        panel.add(Toadflax);

        JCheckBox Irit = new JCheckBox("Irit");
        Irit.setSelected(true);
        Irit.addActionListener(l -> {
            Settings.useIrit = Irit.isSelected();
        });
        panel.add(Irit);

        JCheckBox Avantoe = new JCheckBox("Avantoe");
        Avantoe.setSelected(true);
        Avantoe.addActionListener(l -> {
            Settings.useAvantoe = Avantoe.isSelected();
        });
        panel.add(Avantoe);

        JCheckBox Kwuarm = new JCheckBox("Kwuarm");
        Kwuarm.setSelected(true);
        Kwuarm.addActionListener(l -> {
            Settings.useKwuarm = Kwuarm.isSelected();
        });
        panel.add(Kwuarm);

        JCheckBox Snapdragon = new JCheckBox("Snapdragon");
        Snapdragon.setSelected(true);
        Snapdragon.addActionListener(l -> {
            Settings.useSnapdragon = Snapdragon.isSelected();
        });
        panel.add(Snapdragon);

        JCheckBox Cadantine = new JCheckBox("Cadantine");
        Cadantine.setSelected(true);
        Cadantine.addActionListener(l -> {
            Settings.useCadantine = Cadantine.isSelected();
        });
        panel.add(Cadantine);

        JCheckBox Lantadyme = new JCheckBox("Lantadyme");
        Lantadyme.setSelected(true);
        Lantadyme.addActionListener(l -> {
            Settings.useLantadyme = Lantadyme.isSelected();
        });
        panel.add(Lantadyme);

        JCheckBox Dwarfweed = new JCheckBox("Dwarf weed");
        Dwarfweed.setSelected(true);
        Dwarfweed.addActionListener(l -> {
            Settings.useDwarf = Dwarfweed.isSelected();
        });
        panel.add(Dwarfweed);

        JCheckBox Torstol = new JCheckBox("Torstol");
        Torstol.setSelected(true);
        Torstol.addActionListener(l -> {
            Settings.useTorstol = Torstol.isSelected();
        });
        panel.add(Torstol);



        JLabel lblundercuttingSellUnf = new JLabel("[Undercutting] Sell unf more than min sell:");
        lblundercuttingSellUnf.setHorizontalAlignment(SwingConstants.RIGHT);
        lblundercuttingSellUnf.setBounds(150, 57, 235, 20);
        contentPane.add(lblundercuttingSellUnf);

        JTextField undercuttingMinSellUnfTextField = new JTextField();
        undercuttingMinSellUnfTextField.setColumns(10);
        undercuttingMinSellUnfTextField.setBounds(395, 57, 53, 20);
        contentPane.add(undercuttingMinSellUnfTextField);

        JLabel lblundercuttingBuyGrimy = new JLabel("[Undercutting] Buy grimy more than max buy:");
        lblundercuttingBuyGrimy.setHorizontalAlignment(SwingConstants.RIGHT);
        lblundercuttingBuyGrimy.setBounds(121, 80, 264, 20);
        contentPane.add(lblundercuttingBuyGrimy);

        JTextField undercuttingMaxGrimyBuyTextField = new JTextField();
        undercuttingMaxGrimyBuyTextField.setColumns(10);
        undercuttingMaxGrimyBuyTextField.setBounds(395, 80, 53, 20);
        contentPane.add(undercuttingMaxGrimyBuyTextField);

        JLabel lblNewLabel_2 = new JLabel("NOTE: Script will not start if you put anything other than numbers in text fields!");
        lblNewLabel_2.setHorizontalAlignment(SwingConstants.CENTER);
        lblNewLabel_2.setFont(new Font("Verdana", Font.PLAIN, 9));
        lblNewLabel_2.setBounds(10, 385, 438, 14);
        contentPane.add(lblNewLabel_2);

        JButton StartButton = new JButton("~~ (>^_^)>             START             <(^_^<) ~~");
        StartButton.addActionListener(l -> {
            //set boolean to not start and change all applicable text fields containing non-numbers
            boolean start = true;

            //minimum profit margin
            int val = extractInteger(minProfitMarginTextField.getText());
            if (val == -1) {
                minProfitMarginTextField.setText("number");
                start = false;
            } else {
                Settings.minProfitMargin = val;
            }

            //undercutting buy grimy
            val = extractInteger(undercuttingMaxGrimyBuyTextField.getText());
            if (val == -1) {
                undercuttingMaxGrimyBuyTextField.setText("number");
                start = false;
            } else {
                Settings.undercuttingBuyGrimy = val;
            }

            //undercutting sell unf
            val = extractInteger(undercuttingMaxGrimyBuyTextField.getText());
            if (!isTextClean(undercuttingMinSellUnfTextField.getText())) {
                undercuttingMinSellUnfTextField.setText("number");
                start = false;
            } else {
                Settings.undercuttingSellUnf = val;
            }

            //LivePrices buy
            val = extractInteger(livePricesBuyTextField.getText());
            if (!isTextClean(livePricesBuyTextField.getText())) {
                livePricesBuyTextField.setText("number");
                start = false;
            } else {
                Settings.livePricesBuy = val;
            }

            //LivePrices sell unf
            val = extractInteger(livePricesSellTextField.getText());
            if (!isTextClean(livePricesSellTextField.getText())) {
                livePricesSellTextField.setText("number");
                start = false;
            } else {
                Settings.livePricesSell = val;
            }

            //maximum grimy herb qty to buy
            val = extractInteger(maxGrimyTextField.getText());
            if (!isTextClean(maxGrimyTextField.getText())) {
                maxGrimyTextField.setText("number");
                start = false;
            } else {
                Settings.maxHerbBuyQty = val;
            }

            if(!start) {
                Logger.log("Clicked \"Start\" with some text other than numbers in the text fields!");
                return;
            }
            closedGUI = true;
            Logger.log("Starting after closing GUI!");
            frame.dispose();
        });
        StartButton.setForeground(Color.BLACK);
        StartButton.setBackground(Color.GRAY);
        StartButton.setFont(new Font("Tahoma", Font.BOLD, 15));
        StartButton.setBounds(10, 334, 438, 42);
        contentPane.add(StartButton);
        frame.setVisible(true);
    }
    private static int extractInteger(String fieldText) {
        try {
            return Integer.parseInt(fieldText.trim());
        } catch (NumberFormatException e) {
            Logger.log("Invalid integer value: " + fieldText);
        } catch (NullPointerException e) {
            Logger.log("Text field is null");
        } catch (IllegalArgumentException e) {
            Logger.log("Invalid input: " + fieldText);
        } catch (SecurityException e) {
            Logger.log("Security exception: " + e.getMessage());
        }
        return -1;
    }
    private static boolean isTextClean(String fieldText) {
        if(!fieldText.isEmpty())
        {
            if(!fieldText.matches("[0-9]+") || fieldText.contains(" ")) {
                return false;
            }
            int tmp = Integer.parseInt(fieldText);
            if(tmp <= 0) {
                return false;
            }
        }
        return true;
    }
}
