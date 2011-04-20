package com.belfrygames.sloth;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;

/**
 *
 * @author tomas
 */
public class Main {

	public static void main(String[] args) {
		new Main().run();
	}
	final ArrayList<Example> examples = new ArrayList<Example>();

	public Main() {
		examples.add(new Example("1", "Block", "Basic example", "Press SpaceBar to show each step", ExampleStatus.PERFECT, "com.belfrygames.sloth.chapter01.Block"));

		examples.add(new Example("2", "Bounce", "Bouncing box", "None", ExampleStatus.PERFECT, "com.belfrygames.sloth.chapter02.Bounce"));
		examples.add(new Example("2", "Move", "Moving rectangle", "Use arrow keys", ExampleStatus.PERFECT, "com.belfrygames.sloth.chapter02.Move"));
		examples.add(new Example("2", "Triangle", "Show Red Triangle in the middle of the screen", "None", ExampleStatus.PERFECT, "com.belfrygames.sloth.chapter02.Triangle"));

		examples.add(new Example("3", "Blending", "", "", ExampleStatus.PERFECT, "com.belfrygames.sloth.chapter03.Blending"));
		examples.add(new Example("3", "GeoTest", "", "Use arrow keys to move, F1-F5 for different effects. Should use GLUT to display menu.", ExampleStatus.ALMOST_PERFECT, "com.belfrygames.sloth.chapter03.GeoTest"));
		examples.add(new Example("3", "Primitives", "", "", ExampleStatus.PERFECT, "com.belfrygames.sloth.chapter03.Primitives"));
		examples.add(new Example("3", "Scissor", "", "", ExampleStatus.PERFECT, "com.belfrygames.sloth.chapter03.Scissor"));
		examples.add(new Example("3", "Smoother", "Shows antialiasing", "F1, F2 to change modes. Should use GLUT to display menu.", ExampleStatus.ALMOST_PERFECT, "com.belfrygames.sloth.chapter03.Smoother"));

		examples.add(new Example("4", "Orthographic", "Squared tube in Orthographic projection", "Move with arrow keys", ExampleStatus.PERFECT, "com.belfrygames.sloth.chapter04.Orthographic"));
		examples.add(new Example("4", "Perspective", "Squared tube in Perspective projection", "Move with arrow keys", ExampleStatus.PERFECT, "com.belfrygames.sloth.chapter04.Perspective"));
		examples.add(new Example("4", "ModelViewProjection", "", "", ExampleStatus.PERFECT, "com.belfrygames.sloth.chapter04.ModelViewProjection"));
		examples.add(new Example("4", "Move", "Moving square with ProjectModel matrix", "Move with arrow keys.", ExampleStatus.PERFECT, "com.belfrygames.sloth.chapter04.Move"));
		examples.add(new Example("4", "Objects", "Show basic shapes", "Arrow Keys to rotate, press SpaceBar to change object.", ExampleStatus.PERFECT, "com.belfrygames.sloth.chapter04.Objects"));
		examples.add(new Example("4", "SphereWorld", "Rotating wire frame torus", "None", ExampleStatus.PERFECT, "com.belfrygames.sloth.chapter04.SphereWorld"));
		examples.add(new Example("4", "SphereWorld2", "Rotating wire frame torus and sphere with camera", "Move with arrow keys", ExampleStatus.PERFECT, "com.belfrygames.sloth.chapter04.SphereWorld2"));
		examples.add(new Example("4", "SphereWorld3", "Rotating wire frame torus and sphere and spheres with camera", "Move with arrow keys", ExampleStatus.PERFECT, "com.belfrygames.sloth.chapter04.SphereWorld3"));
		examples.add(new Example("4", "SphereWorld4", "Rotating torus and sphere and spheres with camera and point light", "Move with arrow keys", ExampleStatus.PERFECT, "com.belfrygames.sloth.chapter04.SphereWorld4"));

		examples.add(new Example("5", "Pyramid", "Basic Texturing Example", "Move with arrow keys.", ExampleStatus.PERFECT, "com.belfrygames.sloth.chapter05.Pyramid"));
		examples.add(new Example("5", "Tunnel", "Texture Filtering Example", "F1 to F6 to change filtering modes. Move with arrow keys.", ExampleStatus.ALMOST_PERFECT, "com.belfrygames.sloth.chapter05.Tunnel"));
		examples.add(new Example("5", "Anisotropic", "Texture Filtering Example with Anisotropic filtering", "F1 to F6 to change filtering modes. F7/F8 on/off anisotropic filtering. Move with arrow keys.", ExampleStatus.ALMOST_PERFECT, "com.belfrygames.sloth.chapter05.Anisotropic"));
		examples.add(new Example("5", "SphereWorld", "General texturing example", "Move with arrow keys.", ExampleStatus.PERFECT, "com.belfrygames.sloth.chapter05.SphereWorld"));

		examples.add(new Example("6", "Triangle", "Shows a Red Triangle in the middle of the screen BUT it's done with custom shaders", "None", ExampleStatus.PERFECT, "com.belfrygames.sloth.chapter06.Triangle"));
		examples.add(new Example("6", "ShadedTriangle", "Shows a cool Triangle in the middle of the screen", "None", ExampleStatus.PERFECT, "com.belfrygames.sloth.chapter06.ShadedTriangle"));
		examples.add(new Example("6", "ProvokingVertex", "Shows a Triangle in the middle of the screen that changes color via proxking vertex feature", "Press SpaceBar", ExampleStatus.PERFECT, "com.belfrygames.sloth.chapter06.ProvokingVertex"));
	}

	public void run() {
		// create some tabular data
		String[] headings = new String[]{"Chapter", "Name", "Description", "Instructions", "Status"};

		Object[][] data = new Object[examples.size()][headings.length];
		for (int i = 0; i < data.length; i++) {
			Example e = examples.get(i);
			data[i] = new Object[]{e.getChapter(), e.getName(), e.getDescription(), e.getInstructions(), e.getStatus()};
		}

		// create the data model and the JTable
		final JTable table = new JTable(data, headings);

		final JFrame frame = new JFrame("Sloth Examples v0.1");
		frame.setLayout(new BorderLayout());
		frame.getContentPane().add(new JScrollPane(table), BorderLayout.NORTH);
		JButton button = new JButton("RUN!");
		frame.getContentPane().add(button, BorderLayout.SOUTH);

		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent ae) {
				int row = table.getSelectedRow();
				if (row >= 0 && examples.get(row).getStatus() != ExampleStatus.PENDING) {
					frame.setVisible(false);
					new Thread(examples.get(row)).start();
				}
			}
		});

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(200, 200);
		frame.pack();
		frame.setVisible(true);
	}

	private enum ExampleStatus {

		UNKOWN,
		PENDING,
		STARTED,
		CONTAINS_ERRORS,
		ALMOST_PERFECT,
		PERFECT
	}

	private class Example implements Runnable {

		protected String chapter;
		protected String name;
		protected String description;
		protected String instructions;
		protected ExampleStatus status;
		protected String mainPath;
		protected String[] args = new String[0];

		public Example(String chapter, String name, String description, String instructions, ExampleStatus status, String mainPath) {
			this.chapter = chapter;
			this.name = name;
			this.description = description;
			this.instructions = instructions;
			this.status = status;
			this.mainPath = mainPath;
		}

		@Override
		public void run() {
			if (status == ExampleStatus.PENDING) {
				return;
			}

			try {
				Method m = Class.forName(mainPath).getMethod("main", new Class[]{args.getClass()});
				m.invoke(null, new Object[]{args});
			} catch (NoSuchMethodException ex) {
				Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
			} catch (ClassNotFoundException ex) {
				Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
			} catch (IllegalAccessException ex) {
				Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
			} catch (InvocationTargetException ex) {
				Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex.getCause());
			}
		}

		public String[] getArgs() {
			return args;
		}

		public void setArgs(String[] args) {
			this.args = args;
		}

		public String getChapter() {
			return chapter;
		}

		public void setChapter(String chapter) {
			this.chapter = chapter;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String getInstructions() {
			return instructions;
		}

		public void setInstructions(String instructions) {
			this.instructions = instructions;
		}

		public String getMainPath() {
			return mainPath;
		}

		public void setMainPath(String mainPath) {
			this.mainPath = mainPath;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public ExampleStatus getStatus() {
			return status;
		}

		public void setStatus(ExampleStatus status) {
			this.status = status;
		}
	}
}
