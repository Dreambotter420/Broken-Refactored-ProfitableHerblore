package script.paint;


import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.JCheckBox;
import java.awt.Font;
import javax.swing.JButton;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class GUITest extends JFrame {

	private JPanel contentPane;
	private JTextField minProfitMarginTextField;
	private JTextField maxGrimyTextField;
	private JTextField undercuttingMinSellUnfTextField;
	private JTextField undercuttingMaxGrimyBuyTextField;
	private JTextField textField;
	private JTextField textField_1;

	/**
	 * Create the frame.
	 */
	public GUITest() {
		setResizable(false);
		setTitle("Profitable Herblore Settings");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 474, 449);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblNewLabel_1 = new JLabel("Approved Herbs List");
		lblNewLabel_1.setBounds(160, 217, 126, 17);
		contentPane.add(lblNewLabel_1);
		lblNewLabel_1.setFont(new Font("Times New Roman", Font.BOLD, 14));
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
		
		minProfitMarginTextField = new JTextField();
		minProfitMarginTextField.setBounds(395, 11, 53, 20);
		contentPane.add(minProfitMarginTextField);
		minProfitMarginTextField.setColumns(10);
		
		JLabel lblNewLabel = new JLabel("Minimum profit margin per grimy -> unf herb (default 50 gp):");
		lblNewLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		lblNewLabel.setBounds(52, 11, 333, 20);
		contentPane.add(lblNewLabel);
		
		JCheckBox XPModeBox = new JCheckBox("XP Mode?");
		XPModeBox.setBounds(10, 31, 93, 23);
		contentPane.add(XPModeBox);
		
		JCheckBox botModeBox = new JCheckBox("Bot Mode?");
		botModeBox.setBounds(10, 57, 93, 23);
		contentPane.add(botModeBox);
		
		JLabel lblMaximumBuyQuantity = new JLabel("Maximum Grimy herb buy quantity:");
		lblMaximumBuyQuantity.setHorizontalAlignment(SwingConstants.RIGHT);
		lblMaximumBuyQuantity.setBounds(189, 34, 196, 20);
		contentPane.add(lblMaximumBuyQuantity);
		
		maxGrimyTextField = new JTextField();
		maxGrimyTextField.setColumns(10);
		maxGrimyTextField.setBounds(395, 34, 53, 20);
		contentPane.add(maxGrimyTextField);
		
		JPanel panel = new JPanel();
		panel.setBounds(20, 231, 428, 92);
		contentPane.add(panel);
		
		JCheckBox Guam = new JCheckBox("Guam");
		Guam.setSelected(true);
		panel.add(Guam);
		
		JCheckBox Harralander = new JCheckBox("Harralander");
		Harralander.setSelected(true);
		panel.add(Harralander);
		
		JCheckBox Ranarr = new JCheckBox("Ranarr");
		Ranarr.setSelected(true);
		panel.add(Ranarr);
		
		JCheckBox Toadflax = new JCheckBox("Toadflax");
		Toadflax.setSelected(true);
		panel.add(Toadflax);
		
		JCheckBox Irit = new JCheckBox("Irit");
		Irit.setSelected(true);
		panel.add(Irit);
		
		JCheckBox Avantoe = new JCheckBox("Avantoe");
		Avantoe.setSelected(true);
		panel.add(Avantoe);
		
		JCheckBox Kwuarm = new JCheckBox("Kwuarm");
		Kwuarm.setSelected(true);
		panel.add(Kwuarm);
		
		JCheckBox Snapdragon = new JCheckBox("Snapdragon");
		Snapdragon.setSelected(true);
		panel.add(Snapdragon);
		
		JCheckBox Cadantine = new JCheckBox("Cadantine");
		Cadantine.setSelected(true);
		panel.add(Cadantine);
		
		JCheckBox Lantadyme = new JCheckBox("Lantadyme");
		Lantadyme.setSelected(true);
		panel.add(Lantadyme);
		
		JCheckBox Dwarfweed = new JCheckBox("Dwarf weed");
		Dwarfweed.setSelected(true);
		panel.add(Dwarfweed);
		
		JCheckBox Torstol = new JCheckBox("Torstol");
		Torstol.setSelected(true);
		panel.add(Torstol);
		
		JButton StartButton = new JButton("~~ (>^_^)>             START             <(^_^<) ~~");
		StartButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
			}
		});
		StartButton.setForeground(Color.BLACK);
		StartButton.setBackground(Color.GRAY);
		StartButton.setFont(new Font("Tahoma", Font.BOLD, 15));
		StartButton.setBounds(10, 334, 438, 42);
		contentPane.add(StartButton);
		
		JLabel lblundercuttingSellUnf = new JLabel("[Undercutting] Sell unf more than min sell:");
		lblundercuttingSellUnf.setHorizontalAlignment(SwingConstants.RIGHT);
		lblundercuttingSellUnf.setBounds(150, 57, 235, 20);
		contentPane.add(lblundercuttingSellUnf);
		
		undercuttingMinSellUnfTextField = new JTextField();
		undercuttingMinSellUnfTextField.setColumns(10);
		undercuttingMinSellUnfTextField.setBounds(395, 57, 53, 20);
		contentPane.add(undercuttingMinSellUnfTextField);
		
		JLabel lblundercuttingBuyGrimy = new JLabel("[Undercutting] Buy grimy more than max buy:");
		lblundercuttingBuyGrimy.setHorizontalAlignment(SwingConstants.RIGHT);
		lblundercuttingBuyGrimy.setBounds(121, 80, 264, 20);
		contentPane.add(lblundercuttingBuyGrimy);
		
		undercuttingMaxGrimyBuyTextField = new JTextField();
		undercuttingMaxGrimyBuyTextField.setColumns(10);
		undercuttingMaxGrimyBuyTextField.setBounds(395, 80, 53, 20);
		contentPane.add(undercuttingMaxGrimyBuyTextField);
		
		JLabel lblNewLabel_2 = new JLabel("NOTE: Script will not start if you put anything other than numbers in text fields!");
		lblNewLabel_2.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_2.setFont(new Font("Verdana", Font.PLAIN, 9));
		lblNewLabel_2.setBounds(10, 385, 438, 14);
		contentPane.add(lblNewLabel_2);
		
		JCheckBox chckbxBuyGrimy = new JCheckBox("Buy Grimy?");
		chckbxBuyGrimy.setBounds(10, 83, 105, 23);
		contentPane.add(chckbxBuyGrimy);
		
		JCheckBox chckbxBuyClean = new JCheckBox("Buy Clean?");
		chckbxBuyClean.setBounds(10, 109, 105, 23);
		contentPane.add(chckbxBuyClean);
		
		JCheckBox chckbxSellunf = new JCheckBox("Sell (unf)?");
		chckbxSellunf.setBounds(10, 135, 105, 23);
		contentPane.add(chckbxSellunf);
		
		JCheckBox chckbxUseLiveprices = new JCheckBox("Use DreamBot API LivePrices? (default is in-game price checking)");
		chckbxUseLiveprices.setBounds(10, 161, 438, 23);
		contentPane.add(chckbxUseLiveprices);
		
		JLabel lbllivepricesBuyAt = new JLabel("[LivePrices] Buy at % more than LivePrices Low:");
		lbllivepricesBuyAt.setHorizontalAlignment(SwingConstants.RIGHT);
		lbllivepricesBuyAt.setBounds(134, 103, 251, 20);
		contentPane.add(lbllivepricesBuyAt);
		
		textField = new JTextField();
		textField.setColumns(10);
		textField.setBounds(395, 103, 53, 20);
		contentPane.add(textField);
		
		JLabel lbllivepricesSellAt = new JLabel("[LivePrices] Sell at % less than LivePrices High:");
		lbllivepricesSellAt.setHorizontalAlignment(SwingConstants.RIGHT);
		lbllivepricesSellAt.setBounds(110, 126, 275, 20);
		contentPane.add(lbllivepricesSellAt);
		
		textField_1 = new JTextField();
		textField_1.setColumns(10);
		textField_1.setBounds(395, 126, 53, 20);
		contentPane.add(textField_1);
		
		JCheckBox chckbxProcessAllOwned = new JCheckBox("Process all owned items then stop script?");
		chckbxProcessAllOwned.setBounds(10, 187, 359, 23);
		contentPane.add(chckbxProcessAllOwned);
	}
}
