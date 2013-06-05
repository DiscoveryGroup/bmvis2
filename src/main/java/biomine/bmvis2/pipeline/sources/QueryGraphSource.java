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

package biomine.bmvis2.pipeline.sources;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import biomine.bmvis2.pipeline.SettingsChangeCallback;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import biomine.bmgraph.BMGraph;
import biomine.bmgraph.BMNode;
import biomine.bmvis2.VisualGraph;
import biomine.bmvis2.VisualNode;
import biomine.bmvis2.crawling.CrawlQuery;
import biomine.bmvis2.crawling.CrawlSuggestionList;
import biomine.bmvis2.crawling.CrawlerFetch;

public class QueryGraphSource extends GraphSource {
    CrawlQuery query;
    CrawlerFetch fetch;
    boolean neighborhood = false;
    String database = null;
    BMGraph ret;

    public QueryGraphSource(CrawlQuery q) {
        query = q;
    }

    public QueryGraphSource(CrawlQuery q, String db) {
        query = q;
        database = db;
    }

    @Override
    public BMGraph getBMGraph() throws GraphOperationException {
        try {
            if (fetch == null) {
                fetch = new CrawlerFetch(query, neighborhood, database);
            }
            if (ret == null) {
                final JDialog dial = new JDialog((JFrame) null);

                dial.setTitle("BMVIS II - Query to database");
                dial.setSize(400, 200);
                dial.setMinimumSize(new Dimension(400, 200));
                dial.setResizable(false);
                final JTextArea text = new JTextArea();
                text.setEditable(false);

                dial.add(text);
                text.setText("...");

                class Z {
                    Exception runExc = null;
                }

                final Z z = new Z();
                Runnable fetchThread = new Runnable() {
                    public void run() {
                        try {
                            long startTime = System.currentTimeMillis();
                            while (!fetch.isDone()) {
                                fetch.update();
                                Thread.sleep(500);
                                long time = System.currentTimeMillis();
                                long elapsed = time - startTime;

                                if (elapsed > 30000) {
                                    throw new GraphOperationException(
                                            "Timeout while querying " + query);
                                }
                                final String newText = fetch.getState() + ":\n" + fetch.getMessages();
                                SwingUtilities.invokeLater(new Runnable() {
                                    public void run() {
                                        text.setText(newText);
                                    }
                                });
                            }

                            SwingUtilities.invokeAndWait(new Runnable() {
                                public void run() {
                                    dial.setVisible(false);
                                }
                            });

                        } catch (Exception e) {
                            z.runExc = e;
                        }

                    }

                };

                new Thread(fetchThread).start();
                dial.setModalityType(ModalityType.APPLICATION_MODAL);
                dial.setVisible(true);
                ret = fetch.getBMGraph();
                if (ret == null)
                    throw new GraphOperationException(fetch.getMessages());
                if (z.runExc != null) {
                    if (z.runExc instanceof GraphOperationException) throw (GraphOperationException) z.runExc;
                    else throw new GraphOperationException(z.runExc);
                }
            }

        } catch (IOException e) {
            throw new GraphOperationException(e);
        }
        updateInfo();
        return ret;
    }

    @Override
    public String getTitle() {
        return "Query: " + query.toString();
    }

    @Override
    public String getToolTip() {
        return null;
    }

    private String getNodeName(String id) {
        if (ret != null) {
            BMNode n = null;
            for (BMNode nod : ret.getNodes()) {
                if (nod.getId().equals(id))
                    n = nod;
            }
            if (n != null) {
                String name = n.get("ShortName");
                if (name != null)
                    return name;
                return id;
            }
        }

        return id;
    }

    private JTextArea infoText;

    private void updateInfo() {
        if (infoText == null) return;
        infoText.setEditable(false);

        StringBuilder buf = new StringBuilder();
        buf.append("Query nodes:");
        for (String str : query) {
            buf.append("\n" + getNodeName(str));
        }
        infoText.setText(buf.toString());
        infoText.revalidate();
        infoText.repaint();
    }

    public JComponent getSettingsComponent(SettingsChangeCallback v, VisualGraph graph) {
        infoText = new JTextArea();
        updateInfo();
        return infoText;
    }

    @Override
    public void fromJSON(JSONObject o) throws Exception {
        fetch = null;
        ret = null;
        query = new CrawlQuery();
        JSONArray qc = (JSONArray) o.get("query");
        for (Object queryName : qc) {
            query.add(queryName.toString());
        }
        Boolean nh = (Boolean) o.get("neighborhood");
        if (nh != null) {
            neighborhood = nh;
        } else {
            neighborhood = false;
        }
    }

    @Override
    public JSONObject toJSON() {
        JSONObject ret = new JSONObject();
        JSONArray queryArr = new JSONArray();
        queryArr.addAll(query);
        ret.put("query", queryArr);
        ret.put("neighborhood", new Boolean(neighborhood));
        return ret;
    }

    public static QueryGraphSource createFromDialog(Collection<VisualNode> start, String database) {
        final CrawlSuggestionList sel = new CrawlSuggestionList();
        final JDialog dial = new JDialog();
        if (database != null)
            sel.setDatabase(database);
        for (VisualNode vn : start) {
            if (vn.getBMNode() != null)
                sel.addNode(vn);
        }
        dial.setModalityType(ModalityType.APPLICATION_MODAL);
        final CrawlQuery q = new CrawlQuery();

        // Helper class to pass back data from anonymous inner classes to this method
        class Z {
            boolean okPressed = false;
        }
        final Z z = new Z();
        JButton okButton = new JButton(new AbstractAction("OK") {
            public void actionPerformed(ActionEvent arg0) {
                dial.setVisible(false);
                q.addAll(sel.getQueryTerms());
                z.okPressed = true;
            }
        });

        JPanel pane = new JPanel();
        pane.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1;
        c.weighty = 1;
        c.fill = c.BOTH;
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 0;
        pane.add(sel, c);

        c.weighty = 0;
        c.gridy++;
        c.fill = c.HORIZONTAL;
        c.gridwidth = 1;

        pane.add(okButton, c);
        dial.setContentPane(pane);
        dial.setSize(600, 500);
        dial.setVisible(true);//this will hopefully block

        if (z.okPressed) {
            if (q.size() == 0) {
                JOptionPane.showMessageDialog(dial, "At least 1 edge must be selected: no queries added");
                return null;
            }
            return new QueryGraphSource(q, sel.getDatabase());
        }

        return null;
    }

    public void setNeighborhood(boolean neighborhood) {
        this.neighborhood = neighborhood;
        ret = null;
    }
}
