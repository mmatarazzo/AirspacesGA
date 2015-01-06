/**
 * 
 */
package gov.nasa.worldwindx.applications.airvis;

import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;

import javax.swing.*;

/**
 * @author Mike
 *
 */
public class SimpleTest extends JFrame {
	
	public SimpleTest() {
		WorldWindowGLCanvas wwd = new WorldWindowGLCanvas();
		wwd.setPreferredSize(new java.awt.Dimension(1440, 900));
		this.getContentPane().add(wwd, java.awt.BorderLayout.CENTER);
		wwd.setModel(new BasicModel());
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				JFrame frame = new SimpleTest();
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.pack();
				frame.setVisible(true);
			}
		});
	}

}
