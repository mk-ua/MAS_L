package lab2.agpackage;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

/*
 * 	Shtogryn Oleg modifications
 *  This class represent a BookBuyerAgentGui 
 */
class BookBuyerGui extends JFrame {	
	private BookBuyerAgent myAgent;
	
	private JTextField titleField;
	
	BookBuyerGui(BookBuyerAgent a) {
		super(a.getLocalName());
		
		myAgent = a;
		
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(3, 10));
		p.add(new JLabel("You can buy books here!"));
		p.add(new JLabel("What book do you want to buy:"));
		titleField = new JTextField(15);
		p.add(titleField);
		getContentPane().add(p, BorderLayout.CENTER);
		
		JButton addButton = new JButton("Find book");
		addButton.addActionListener( new ActionListener() {
		public void actionPerformed(ActionEvent ev) {
				try {
					myAgent.tryFindBook(titleField.getText());
				}
				catch (Exception e) {
					JOptionPane.showMessageDialog(BookBuyerGui.this, "Invalid values. "+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); 
				}
			}
		} );
		p = new JPanel();
		p.add(addButton);
		getContentPane().add(p, BorderLayout.SOUTH);
		
		// Make the agent terminate when the user closes 
		// the GUI using the button on the upper right corner	
		addWindowListener(new	WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				myAgent.doDelete();
			}
		} );
		
		setResizable(false);
	}
	
	public void showFinalText(String message) {
		JOptionPane.showMessageDialog(BookBuyerGui.this, message, "BookFinder", JOptionPane.INFORMATION_MESSAGE); 
	}
	public void showGui() {
		pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int centerX = (int)screenSize.getWidth() / 2;
		int centerY = (int)screenSize.getHeight() / 2;
		setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
		super.setVisible(true);
	}	
}

