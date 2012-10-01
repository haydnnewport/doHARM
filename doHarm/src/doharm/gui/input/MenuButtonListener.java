package doharm.gui.input;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import doharm.gui.view.MainWindow;
import doharm.gui.view.PictureButton;

/**
 * The transient button listener is a listener which always returns focus to the
 * component given in it's constructor upon completion of the actionPerformed
 * method
 * 
 * @author Haydn Newport
 * 
 */
public class MenuButtonListener implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent e) {
		String action = "";
		if (e.getSource() instanceof PictureButton){
			action = ((PictureButton)e.getSource()).getName();
		}
		System.out.println(action);
		if (action.equals("runserver")){
			//TODO:Start a server
		} else if (action.equals("join")){
			//TODO:Join the given server
		} else if (action.equals("resume")){
			//TODO:remove the menu screen
		}  else if (action.equals("quit")){
			//TODO:Graceful exit
			System.exit(0);
		}
	}
	

}
