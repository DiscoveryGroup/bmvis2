/*
 * Copyright 2012 University of Helsinki.
 *
 * This file is part of BMVis².
 *
 * BMVis² is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * BMVis² is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with BMVis².  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package biomine.bmvis2.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.FocusTraversalPolicy;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedWriter;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.DefaultEditorKit;

import biomine.bmvis2.VisualGraph;

public class JavaScriptConsole extends JPanel {
	private ScriptEngineManager scriptEngineManager;
	private JTextArea output;
	// private JTextField input;
	private JTextArea input;
	private List<String> inputHistory;
	private int historyItem = 0;
	public ScriptingLanguage scriptingLanguage;
    public static String LANGUAGE_NAME = "JavaScript";

	public JavaScriptConsole(VisualGraph graph,
			ScriptEngineManager scriptEngineManager) {
		this.scriptEngineManager = scriptEngineManager;
		this.scriptingLanguage = new ScriptingLanguage(graph, "ECMAScript");
		this.inputHistory = new ArrayList<String>();
		inputHistory
				.add("for (var i in all_nodes()) {\n  var edge = all_nodes()[i];\n  pln(\"  \" + edge + \" [\" + i + \"]\");\n  pln(dir(edge));\n}");
		inputHistory
				.add("for (var i in all_edges()) {\n  var edge = all_edges()[i];\n  pln(\"  \" + edge + \" [\" + i + \"]\");\n  pln(dir(edge));\n}");
		inputHistory
				.add("for (var i in nodes()) {\n  var edge = nodes()[i];\n  pln(\"  \" + edge + \" [\" + i + \"]\");\n  pln(dir(edge));\n}");
		inputHistory
				.add("for (var i in edges()) {\n  var edge = edges()[i];\n  pln(\"  \" + edge + \" [\" + i + \"]\");\n  pln(dir(edge));\n}");
		inputHistory
				.add("for (var i in nodes()) {\n  var edge = nodes()[i];\n  edge.setSelected(false);\n  if (edge.getType() == \"Gene\") edge.setSelected(true)\n}");
		inputHistory
				.add("for (var i in nodes()) {\n  var edge = nodes()[i];\n  if (edge.getType() != \"Group\") continue;\n  var d = edge.getDescendants();\n  pln(edge + \" \" + d + \" \" + d.size());\n  if (d.size() < 5) edge.setOpen(true);\n}");
		inputHistory.add("");

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		this.output = new JTextArea();
		this.input = new JTextArea();
		this.addEmacsKeybindings(this.input);

        JScrollPane outputScroller = new JScrollPane(this.output);
		outputScroller
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		outputScroller
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		output.setLineWrap(true);
		output.setWrapStyleWord(false);
		output.setEditable(false);

		JScrollPane inputScroller = new JScrollPane(this.input);
		inputScroller
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		inputScroller
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		input.setLineWrap(true);
		input.setWrapStyleWord(false);

		JSplitPane hSplitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		hSplitter.setTopComponent(outputScroller);
		hSplitter.setBottomComponent(inputScroller);
		hSplitter.setAlignmentX(CENTER_ALIGNMENT);
		hSplitter.setResizeWeight(0.85);

		this.add(hSplitter);
		input.setMaximumSize(new Dimension(Short.MAX_VALUE, input
				.getPreferredSize().height));

		Box tb = new Box(BoxLayout.X_AXIS);
		JButton evalButton = new JButton("EVAL");
		evalButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				eval(output);
			}
		});
		tb.add(evalButton);

		JButton prevButton = new JButton("<<");
		prevButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				previousHistory();
			}
		});
		tb.add(prevButton);

		JButton nextButton = new JButton(">>");
		nextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				nextHistory();
			}
		});
		tb.add(nextButton);
		this.add(tb);

		input.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {
			}

			public void keyReleased(KeyEvent arg0) {
			}

			public void keyPressed(KeyEvent e) {
				// http://java.sun.com/javase/6/docs/api/java/awt/event/InputEvent.html#getModifiersEx()
				int onmask = InputEvent.CTRL_DOWN_MASK;
				int offmask = InputEvent.CTRL_DOWN_MASK
						| InputEvent.SHIFT_DOWN_MASK;

				if ((e.getModifiersEx() & (onmask | offmask)) == onmask) {
					if (e.getKeyCode() == KeyEvent.VK_UP) {
						previousHistory();
					} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
						nextHistory();
					} else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						eval(output);
					}
				}
			}
		});
		input.requestFocusInWindow();
	}

	private void eval(JTextArea textArea) {
		if (!inputHistory.get(0).equals(input.getText()))
			inputHistory.add(0, input.getText());
		output.append(scriptingLanguage.eval(textArea, input.getText()));
		input.setText("");
		historyItem = -1;
		input.requestFocusInWindow();

		output.setCaretPosition(output.getDocument().getLength());
	}

	private void previousHistory() {
		if (historyItem < inputHistory.size() - 2)
			historyItem++;
		if (historyItem >= inputHistory.size())
			historyItem = inputHistory.size() - 1;
		input.setText(inputHistory.get(historyItem));
	}

	private void nextHistory() {
		if (historyItem != 0)
			historyItem--;
		input.setText(inputHistory.get(historyItem));
	}

	private void addEmacsKeybindings(JComponent comp) {
		InputMap inputMap = comp.getInputMap();
		KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_B, Event.CTRL_MASK);
		inputMap.put(key, DefaultEditorKit.backwardAction);

		key = KeyStroke.getKeyStroke(KeyEvent.VK_F, Event.CTRL_MASK);
		inputMap.put(key, DefaultEditorKit.forwardAction);

		key = KeyStroke.getKeyStroke(KeyEvent.VK_P, Event.CTRL_MASK);
		inputMap.put(key, DefaultEditorKit.upAction);

		key = KeyStroke.getKeyStroke(KeyEvent.VK_N, Event.CTRL_MASK);
		inputMap.put(key, DefaultEditorKit.downAction);

		key = KeyStroke.getKeyStroke(KeyEvent.VK_M, Event.CTRL_MASK);
		inputMap.put(key, DefaultEditorKit.insertBreakAction);

		key = KeyStroke.getKeyStroke(KeyEvent.VK_H, Event.CTRL_MASK);
		inputMap.put(key, DefaultEditorKit.deletePrevCharAction);

		key = KeyStroke.getKeyStroke(KeyEvent.VK_D, Event.CTRL_MASK);
		inputMap.put(key, DefaultEditorKit.deleteNextCharAction);

		key = KeyStroke.getKeyStroke(KeyEvent.VK_W, Event.CTRL_MASK);
		inputMap.put(key, DefaultEditorKit.deletePrevWordAction);

		key = KeyStroke.getKeyStroke(KeyEvent.VK_E, Event.CTRL_MASK);
		inputMap.put(key, DefaultEditorKit.endLineAction);

		key = KeyStroke.getKeyStroke(KeyEvent.VK_W, Event.CTRL_MASK);
		inputMap.put(key, DefaultEditorKit.deletePrevWordAction);

		input.setInputMap(WHEN_FOCUSED, inputMap);
	}

	public class ScriptingLanguage {
		ScriptEngine engine;
		int evaluatedExpressions = 0;

		private void e(String s) {
			System.err.println(new String(s + ": " + eval(output, s))
					.replaceFirst("\n", ""));
		}

		public ScriptingLanguage(VisualGraph graph, String lang) {
			try {
				engine = scriptEngineManager.getEngineByName(lang);
				engine.put("graph", graph);
				engine
						.eval("function apply (collection, func) { var iter=collection.iterator(); while(iter.hasNext()) { func(iter.next()) }}");
				engine
						.eval("function map (collection, func) { var items=[]; var iter=collection.iterator(); while(iter.hasNext()) { var item = iter.next(); items.push(func(item)); } return items; }");
				engine
						.eval("function filter (collection, func) { var items=[]; var iter=collection.iterator(); while(iter.hasNext()) { var item = iter.next(); if (func(item)) items.push(item) }; return items; }");
				engine.eval("function p (s) {textArea.append(s)}");
				engine.eval("function pln (s) {textArea.append(s + \"\\n\")}");
				engine
						.eval("function edges () {return graph.getEdges().toArray()}");
				engine
						.eval("function nodes () {return graph.getNodes().toArray()}");
				engine
						.eval("function alledges() {return graph.getAllEdges().toArray()}");
				engine
						.eval("function allnodes() {return graph.getAllNodes().toArray()}");
				engine
						.eval("function all_edges() {return graph.getAllEdges().toArray()}");
				engine
						.eval("function all_nodes() {return graph.getAllNodes().toArray()}");
				engine
						.eval("function dir (obj) { var items=[]; for (var prop in obj) {items.push(prop)}; return items }");
				engine
						.eval("function sleep(time) {java.lang.Thread.sleep(time)}");

				// e("importClass(java.lang.System);");
				// e("importClass(java.util.Collection);");
				// e("importClass(java.util.Iterator);");
				// String[] classes = {"VisualGraph", "VisualNode",
				// "VisualGroupNode"};
				// for (String s: classes) {
				// engine.put(s,
				// this.getClass().getClassLoader().loadClass("biomine.bmvis2."
				// + s));
				// e("importClass(Packages.biomine.bmvis2." + s + ");" + ": ");
				// }
				// e("importPackage(Packages.biomine.bmvis2)");
				// e("(function () { function iterate (collection, func) { var iter=collection.iterator(); while(iter.hasNext()) { func(iter.next()) }} })()");
				// e("function iterate (collection, func) { var iter=collection.iterator(); while(iter.hasNext()) { func(iter.next()) }}");
				evaluatedExpressions = 0;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// private class StringBufferOutputStream extends OutputStream {
		// StringBuffer buf;
		//
		// StringBufferOutputStream() {
		// buf = new StringBuffer();
		// }
		//
		// public void write(int arg0) {
		// buf.append(arg0);
		// }
		//
		// public String toString() {
		// return buf.toString();
		// }
		// }

		public String eval(JTextArea textArea, String input) {
			PrintStream ps = System.out;
			String ret = null;
			try {
				engine.put("textArea", textArea);
				// StringBufferOutputStream sbos = new
				// StringBufferOutputStream();
				// PrintStream tmpStream = new PrintStream(sbos);
				// System.setOut(tmpStream);
				// textArea.append(sbos.toString());

				Object evaled = engine.eval(input);
				// Special case for single string printing
				if (evaled instanceof String)
					ret = "\"" + evaled.toString() + "\"";
				// Array printing (Java array printing is poor :()
				else if (evaled instanceof Object[]) {
					ret = "[";
					for (Object o : (Object[]) evaled) {
						ret = ret + o.toString() + ",";
					}
					ret = ret.substring(0, ret.length() - 2);
					ret = ret + "]";
				} else
					ret = evaled.toString();
			} catch (ScriptException e) {
				ret = e.getLineNumber() + ":" + e.getColumnNumber() + " "
						+ e.getMessage();
			} catch (Exception e) {
				ret = e.getMessage();
			} finally {
				System.setOut(ps);
			}
			evaluatedExpressions++;
			return evaluatedExpressions + "> " + ret + "\n";
		}
	}

	public static boolean hasJavaScript() {
		try {
			ScriptEngineManager m = new ScriptEngineManager();
			ScriptEngine e = m.getEngineByName("ECMAScript");
			return true;
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
		return false;
	}

}
