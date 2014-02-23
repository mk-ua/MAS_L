package examples.bookTrading;

import jade.core.AID;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class BookBuyerGui extends JFrame{
	private BookBuyerAgent myAgent;
	private JTextField titleField;
	
	BookBuyerGui(BookBuyerAgent a) {
		super(a.getLocalName());
		
		myAgent = a;
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(2, 2));
		p.add(new JLabel("Book title:"));
		titleField = new JTextField(15);
		p.add(titleField);
		getContentPane().add(p, BorderLayout.CENTER);
		JButton SetButton = new JButton("Set");
		SetButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent eve) {
				// TODO Auto-generated method stub
				try {
					String title = titleField.getText().trim();
					myAgent.setBook(title);
					titleField.setText("");
					System.out.println("adding "+title+" to wishlist");
				}
				catch (Exception e) {
					JOptionPane.showMessageDialog(BookBuyerGui.this, "Invalid values. "+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); 
				}
			}
		});
		p = new JPanel();
		p.add(SetButton);
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
	
	
	
	public void showGui() {
		pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int centerX = (int)screenSize.getWidth() / 2;
		int centerY = (int)screenSize.getHeight() / 2;
		setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
		super.setVisible(true);
	}	
}
