package nars.io.nlp;

import java.awt.GridLayout;
import javax.swing.*;

public class NlpGui extends JFrame {
    public NlpGui() {
        super("NLP");
        
        JPanel panel = new JPanel();
        add(panel);
        
        GridLayout gridLayout = new GridLayout(3, 5);
        panel.setLayout(gridLayout);
        
        setSize(300,300);
        setLocation(300,300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(gridLayout);
        
        pureTextArea = new JTextArea();
        
        JButton parseButton = new JButton("parse");
        
        panel.add(pureTextArea);
        panel.add(parseButton);
        
        
        
        pack();
        setVisible(true);
    }
 
    public static void main(String[] args) {
        NlpGui g = new NlpGui();
    }
    
    private JTextArea pureTextArea;
}
