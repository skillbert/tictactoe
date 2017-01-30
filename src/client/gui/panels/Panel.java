package client.gui.panels;

import javax.swing.JPanel;

import client.gui.Gui;
import net.miginfocom.swing.MigLayout;

public abstract class Panel extends JPanel {
	private MigLayout layout = new MigLayout("wrap 2");
	protected Gui gui;
	protected JPanel panel = new JPanel(layout);

	public JPanel getPanel() {
		panel.revalidate();
		panel.repaint();
		return this.panel;
	}
}
