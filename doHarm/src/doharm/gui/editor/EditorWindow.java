package doharm.gui.editor;

import java.awt.BorderLayout;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

public class EditorWindow extends JFrame {
    private EditorCanvas canvas;
    private MouseListener listen;

    /**
     * @param args
     */
    public static void main(String[] args) {
	new EditorWindow().setVisible(true);
    }

    public EditorWindow(){
		setBounds(0, 0, 800, 600);
		setLayout(new BorderLayout());
		canvas = new EditorCanvas();
		add(canvas, BorderLayout.CENTER);
		canvas.setWorld(EditorLogic.loadWorld("world1"));
		JPanel editor = new JPanel(new MigLayout());
		add(editor, BorderLayout.EAST);
		canvas.addMouseListener(listen = new EditorMouseListener(this));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

    EditorCanvas getCanvas() {
	return canvas;
    }

}
