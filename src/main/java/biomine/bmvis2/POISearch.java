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

// package biomine.bmvis2;
//
//import java.awt.Dimension;
//import java.util.ArrayList;
//
//import javax.swing.BoxLayout;
//import javax.swing.JPanel;
//import javax.swing.JScrollPane;
//import javax.swing.JTable;
//import javax.swing.JTextField;
//import javax.swing.event.CaretEvent;
//import javax.swing.event.CaretListener;
//import javax.swing.table.AbstractTableModel;
//
//public class POISearch extends JPanel implements GraphObserver {
//
//	VisualGraph visualGraph;
//	JTable table;
//	JTextField filter;
//	NodeTableModel tableModel;
//
//	public POISearch(VisualGraph g) {
//		visualGraph = g;
//		visualGraph.addObserver(this);
//		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
//
//		table = new JTable();
//		filter = new JTextField();
//		filter.setMaximumSize(new Dimension(Short.MAX_VALUE,filter.getPreferredSize().height));
//		this.add(filter);
//		JScrollPane scroll = new JScrollPane(table);
//
//		this.add(scroll);
//		
//
//		tableModel = new NodeTableModel();
//		table.setModel(tableModel);
//		table.getColumnModel().getColumn(2).setCellEditor(
//				new DoubleEditor(-1, 1));
//		filter.addCaretListener(new CaretListener() {
//			
//			public void caretUpdate(CaretEvent arg0) {
//				tableModel.filterData(filter.getText());
//			}
//		});
//		tableModel.filterData("");
//	}
//
//	class NodeTableModel extends AbstractTableModel {
//		VisualNode[] nodes;
//		Object[][] tableData = new Object[0][3];
//
//		public NodeTableModel() {
//			filterData("");
//		}
//
//		String[] columnNames = { "Name", "Type", "Interest" };
//
//		public String getColumnName(int col) {
//			return columnNames[col].toString();
//		}
//
//		public int getRowCount() {
//			return tableData.length;
//		}
//
//		public int getColumnCount() {
//			return columnNames.length;
//		}
//
//		public Object getValueAt(int row, int col) {
//			return tableData[row][col];
//		}
//
//		public boolean isCellEditable(int row, int col) {
//			return col == 2;
//		}
//
//		public void setValueAt(Object value, int row, int col) {
//			tableData[row][col] = value;
//			if (value instanceof String) {
//				value = new Double(Double.parseDouble((String) value));
//			}
//			fireTableCellUpdated(row, col);
//
//			visualGraph.addPointOfInterest(nodes[row], (Double) value);
//		}
//
//		public void filterData(String filter) {
//			System.out.println("filter");
//			ArrayList<Object[]> newData = new ArrayList<Object[]>();
//			ArrayList<VisualNode> newNodes = new ArrayList<VisualNode>();
//			for (VisualNode n : visualGraph.getAllNodes()) {
//				if (!(n instanceof VisualGroupNode)) {
//					if(n.getName().toLowerCase().contains(filter.toLowerCase())==false)continue;
//					newData.add(new Object[] { n.getName(), n.getType(),
//							new Double(visualGraph.getInterestingness(n)) });
//					newNodes.add(n);
//				}
//			}
//			tableData = new Object[newData.size()][3];
//
//			newData.toArray(tableData);
//			nodes = new VisualNode[newNodes.size()];
//			newNodes.toArray(nodes);
//			this.fireTableDataChanged();
//		}
//	}
//
//	@Override
//	public void graphChanged(VisualGraph g) {
//		tableModel.filterData(filter.getText());
//	}
//
//	@Override
//	public void highlightChanged(VisualGraph g) {
//	}
//
//	@Override
//	public void selectionChanged(VisualGraph g) {
//	}
//}
