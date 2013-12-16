package readerAdvisor.file;

import readerAdvisor.speech.SpeechManager;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Drop down for the highlight combo box
 */
@Deprecated
public class HighlightComboBox extends JComboBox {
    private int lastIndex;

    public HighlightComboBox(ComboBoxModel model) {
        super(model);
        lastIndex = -1;
        addActionListener(new ComboBoxActionListener());
    }

    class ComboBoxActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            if (getSelectedIndex() != lastIndex) {
                Thread t = new Thread() {
                    public void run() {
                        SpeechManager.getInstance().setHighlightItem((HighlightItem) getSelectedItem());
                        lastIndex = getSelectedIndex();
                    }
                };
                t.start();
            }
        }
    }
}
