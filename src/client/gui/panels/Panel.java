package client.gui.panels;

import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

public abstract class Panel extends JPanel {
	private MigLayout layout = new MigLayout("wrap 2");
	protected JPanel panel = new JPanel(layout);

	public JPanel getPanel() {
		return this.panel;
	}
}
