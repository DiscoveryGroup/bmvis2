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

package biomine.bmvis2;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import biomine.bmvis2.graphics.ZoomController;
import biomine.bmvis2.layout.SpringLayoutManager;
import biomine.bmvis2.pipeline.Pipeline;
import biomine.bmvis2.pipeline.sources.GraphSource;
import biomine.bmvis2.ui.GraphVisualizer;
import biomine.bmvis2.ui.Menus;

/**
 * Main visual component. Handles drawing of graph and user interaction.
 *
 * @author alhartik
 * @author ahinkka
 */
public class GraphArea extends GraphVisualizer implements GraphObserver {
    /**
     * Tiny object to store settings to.
     *
     * @author alhartik
     */
    public static class GraphAreaSettings extends JComponent {
        public boolean antiAlias = false;
        public boolean antiAliasText = false;
        public boolean vectorEdges = true;
    }

    private int zoomHelpShownTimes = 0;

    public static final int LAYOUT_DELAY = 15; // delay between two graphLayout updates in millisecs

    private Rectangle2D.Double areaSelect;
    public VisualGraph visualGraph;
    private SpringLayoutManager graphLayout;

    // Default settings for drawing
    private boolean antiAlias = false;
    private boolean antiAliasText = false;
    private boolean vectorEdges = true;

    private Timer layoutTimer;
    private ZoomController zoomController;

    private boolean initialZoomDone = false;
    private Pipeline pipeline;

    private ArrayList<JComponent> leftReachingComponents = new ArrayList<JComponent>();

    // Left upper corner graphLayout timing data
    // averageLayoutTime is the average time used by the graphLayout algorithm.
    private double averageLayoutTime = 13.37;
    private long totalLayoutTime = 0;
    private int layoutCount = 0;
    private double maxLayoutTime;

    public GraphArea(VisualGraph graph, Pipeline pipe) {
        FocusListener focusListener = new FocusListener() {
            public void focusGained(FocusEvent focusEvent) {
                Logging.debug("focus", GraphArea.this.getClass() + " gained focus!");
            }

            public void focusLost(FocusEvent focusEvent) {
                Logging.debug("focus", GraphArea.this.getClass() + " lost focus!");
            }
        };
        this.addFocusListener(focusListener);
        this.setLayout(new SpringLayout());

        this.visualGraph = graph;
        this.pipeline = pipe;
        this.addComponentListener(new BMVisComponentListener());

        BMVisMouseListener tml = new BMVisMouseListener();
        this.addMouseListener(tml);
        this.addMouseMotionListener(tml);
        this.addMouseWheelListener(tml);
        this.addKeyListener(new GraphAreaKeyListener());

        this.setGraph(this.visualGraph, true);

        this.zoomController = new ZoomController(this);

        // We want as much space we can get
        this.setPreferredSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));

        this.initializeZoomButtons();

        JButton settingsButton = new JButton("Settings");
        settingsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                Logging.debug("ui", "settingsButton pressed: " + actionEvent);
                JOptionPane.showMessageDialog(GraphArea.this.pipeline.getVis(), GraphArea.this.getSettingsDialogComponent());
            }
        });

        SpringLayout layout = (SpringLayout) this.getLayout();
        layout.putConstraint(SpringLayout.SOUTH, settingsButton, -10, SpringLayout.SOUTH, this);
        layout.putConstraint(SpringLayout.WEST, settingsButton, 10, SpringLayout.WEST, this);
        this.add(settingsButton);
    }


    private void initializeZoomButtons() {
        JButton zoomInButton = new JButton("+");
        zoomInButton.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
        JButton zoomOutButton = new JButton("-");
        zoomOutButton.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));

        FocusListener fl = new FocusListener() {
            public void focusGained(FocusEvent focusEvent) {
                GraphArea.this.zoomHelpShownTimes++;
            }

            public void focusLost(FocusEvent focusEvent) {
                GraphArea.this.zoomHelpShownTimes++;
            }
        };

        zoomInButton.addFocusListener(fl);
        zoomOutButton.addFocusListener(fl);

        zoomInButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                Logging.debug("ui", "zoomInButton: " + actionEvent);

                double scale = 1.1;
                if (GraphArea.this.transform.getScaleX() * scale > 10)
                    return;

                int x = new Double(GraphArea.this.getBounds().getCenterX()).intValue();
                int y = new Double(GraphArea.this.getBounds().getCenterY()).intValue();
                Vec2 tP = GraphArea.this.inverseTransform(x, y);

                GraphArea.this.transform.translate(tP.x, tP.y);
                GraphArea.this.transform.scale(scale, scale);
                GraphArea.this.transform.translate(-tP.x, -tP.y);

                repaint();
            }
        });


        zoomOutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                Logging.debug("ui", "zoomOutButton: " + actionEvent);

                double scale = 0.9;
                if (GraphArea.this.transform.getScaleX() * scale > 10)
                    return;

                int x = new Double(GraphArea.this.getBounds().getCenterX()).intValue();
                int y = new Double(GraphArea.this.getBounds().getCenterY()).intValue();
                Vec2 tP = GraphArea.this.inverseTransform(x, y);

                GraphArea.this.transform.translate(tP.x, tP.y);
                GraphArea.this.transform.scale(scale, scale);
                GraphArea.this.transform.translate(-tP.x, -tP.y);

                repaint();
            }
        });

        /**
         * Add springs:
         *  - zoomInButton: to top of the screen, distance 35; to left, distance 15
         *  - zoomOutButton: to zoomInButton, distance 25; to left, distance 15
         */
        // Point zoomInButtonLoc = new Point(leftUpper.x + 15, leftUpper.y + 35);
        // Point zoomOutButtonLoc = new Point(zoomInButtonLoc.x, zoomInButtonLoc.y + 25);

        SpringLayout layout = (SpringLayout) this.getLayout();

        layout.putConstraint(SpringLayout.NORTH, zoomInButton, 35, SpringLayout.NORTH, this);
        layout.putConstraint(SpringLayout.WEST, zoomInButton, 10, SpringLayout.WEST, this);

        layout.putConstraint(SpringLayout.NORTH, zoomOutButton, 0, SpringLayout.SOUTH, zoomInButton);
        layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, zoomOutButton, 0, SpringLayout.HORIZONTAL_CENTER, zoomInButton);

        this.add(zoomInButton);
        this.add(zoomOutButton);
    }

    // These two methods are called by Pipeline when GraphVisualizer is set.
    private void initLayout() {
        this.graphLayout = new SpringLayoutManager(this.visualGraph);

        LayoutUpdater layoutUpdater = new LayoutUpdater();
        this.layoutTimer = new Timer(LAYOUT_DELAY, layoutUpdater);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                GraphArea.this.graphLayout.setActive(true);
            }
        });
    }

    public void setPipeline(Pipeline pipeline) {
        Logging.debug("ui", "setPipeline() called!");
        this.pipeline = pipeline;
        this.setGraph(pipeline.getCurrentGraph(), true);
        this.pipeline.getControls().initElements(this);
    }


    /**
     * Resumes graphLayout thread if its not running
     */
    public void activateLayoutManager() {
        if (this.graphLayout == null)
            this.initLayout();

        this.graphLayout.setActive(true);
        if (!layoutTimer.isRunning()) {
            Logging.info("layout", "Activating graphLayout manager.");
            layoutTimer.restart();
        }
    }

    /**
     * Maps points from screen coordinates to graph coordinates.
     *
     * @param x x-coordinate
     * @param y y-coordinate
     * @return Vec2
     */
    public Vec2 inverseTransform(int x, int y) {
        Point2D.Double pd = new Point2D.Double(x, y);
        Point2D.Double result = new Point2D.Double();

        try {
            transform.inverseTransform(pd, result);
        } catch (NoninvertibleTransformException e) {
            e.printStackTrace();
            Logging.error("ui", "BUG");
            System.exit(1);

        }

        return new Vec2(result.x, result.y);
    }

    private final EdgeRenderer bezierEdgeRenderer = new BezierEdgeRenderer();
    private final EdgeRenderer vectorEdgeRenderer = new VectorBezierEdgeRenderer();

    private void updateEdgeRenderer() {
        if (vectorEdges) {
            VisualEdge.setEdgeRenderer(vectorEdgeRenderer);
        } else {
            VisualEdge.setEdgeRenderer(bezierEdgeRenderer);
        }
    }

    private void prepareGraphics(Graphics2D g) {
        this.updateEdgeRenderer();
        Object aaHint = antiAlias ? RenderingHints.VALUE_ANTIALIAS_ON
                : RenderingHints.VALUE_ANTIALIAS_OFF;

        Object aaTextHint = antiAliasText ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON
                : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF;

        if (g.getRenderingHint(RenderingHints.KEY_ANTIALIASING) != aaHint) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, aaHint);
        }
        if (g.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING) != aaTextHint) {
            g
                    .setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            aaTextHint);
        }
    }

    public void paintComponent(Graphics pGraphics) {
        // Call super class paintComponent() to get everything going...
        super.paintComponent(pGraphics);

        Graphics2D graphics = (Graphics2D) pGraphics;
        this.prepareGraphics(graphics);
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, this.getWidth(), getHeight());

        AffineTransform graphTransform = graphics.getTransform();
        AffineTransform originalTransform = graphics.getTransform();
        graphTransform.concatenate(this.transform);
        graphics.setTransform(graphTransform);

        // Draw the actual graph
        this.visualGraph.paint(graphics);

        // Draw selection box
        this.paintSelectionRectangle(graphics);

        graphics.setTransform(originalTransform);

        // Draw left upper corner statistics
        graphics.setColor(Color.BLACK);
        graphics.drawString("" + averageLayoutTime + " ms, " + maxLayoutTime, 1, 10);
        graphics.drawString(visualGraph.getNodes().size() + "/"
                + (visualGraph.getAllNodes().size() - 1)
                + " nodes (visible/total)", 1, 20);

        if (this.zoomHelpShownTimes < 5) {
            graphics.drawString("You can also use mouse wheel to zoom and double-click to autozoom.", 160, 20);
            graphics.drawString("Try double-clicking on objects as well as the background!", 160, 30);
        }
    }

    public void addLeftReachingComponent(JComponent jComponent) {
        SpringLayout layout = (SpringLayout) this.getLayout();

        if (this.leftReachingComponents.size() == 0) {
            this.add(jComponent);
            layout.putConstraint(SpringLayout.NORTH, jComponent, 10, SpringLayout.NORTH, this);
            layout.putConstraint(SpringLayout.EAST, jComponent, -10, SpringLayout.EAST, this);
        } else if (this.leftReachingComponents.size() > 0) {
            JComponent previous = this.leftReachingComponents.get(this.leftReachingComponents.size() - 1);
            this.add(jComponent);
            layout.putConstraint(SpringLayout.VERTICAL_CENTER, jComponent, 0, SpringLayout.VERTICAL_CENTER, previous);
            layout.putConstraint(SpringLayout.EAST, jComponent, -5, SpringLayout.WEST, previous);
        }
        this.leftReachingComponents.add(jComponent);
    }

    public Collection<JComponent> getLeftReachingComponents() {
        return this.leftReachingComponents;
    }

    /**
     * Draw node selection box
     *
     * @param graphics Graphics object to draw on.
     */
    private void paintSelectionRectangle(Graphics2D graphics) {
        if (GraphArea.this.areaSelect != null) {
            int type = AlphaComposite.SRC_OVER;
            graphics.setComposite(AlphaComposite.getInstance(type, 0.33f));
            graphics.setColor(Color.BLUE);
            graphics.setStroke(new BasicStroke());
            graphics.fill(GraphArea.this.areaSelect);
            graphics.setComposite(AlphaComposite.getInstance(type, 1));

            graphics.draw(GraphArea.this.areaSelect);
        }
    }


    public AffineTransform transform = new AffineTransform();

    public AffineTransform getTransform() {
        return transform;
    }

    public void setTransform(AffineTransform transform) {
        this.transform = transform;
        zoomController.stop();
        repaint();
    }

    /**
     * Repeatedly updates graphLayout using LayoutManager
     * (SpringLayout)
     *
     * @author alhartik
     */
    private class LayoutUpdater implements ActionListener {
        public void actionPerformed(ActionEvent arg0) {
            long start = System.currentTimeMillis();
            GraphArea.this.repaint();
            GraphArea.this.graphLayout.update();
            GraphArea.this.repaint();
            long elapsed = System.currentTimeMillis() - start;

            totalLayoutTime += elapsed;
            layoutCount++;
            if (elapsed > maxLayoutTime) {
                maxLayoutTime = elapsed;
            }
            if (layoutCount == 25) {
                averageLayoutTime = totalLayoutTime / (double) layoutCount;
                maxLayoutTime = 0;
                layoutCount = 0;
                totalLayoutTime = 0;
            }

            if (graphLayout.isActive())
                layoutTimer.restart();
            else
                layoutTimer.stop();
        }
    }

    /**
     * Glue to enable initial zooming, and ensure repainting.
     *
     * @author alhartik
     */
    class BMVisComponentListener implements ComponentListener {

        public void componentHidden(ComponentEvent e) {
        }

        public void componentMoved(ComponentEvent e) {
            repaint();
        }

        public void componentResized(ComponentEvent e) {
            if (!GraphArea.this.initialZoomDone) {
                GraphArea.this.zoomTo(visualGraph.getNodes());
                GraphArea.this.initialZoomDone = true;
                Logging.info("ui", "Initial zoom done.");
            }
            GraphArea.this.repaint();
        }

        public void componentShown(ComponentEvent e) {
            repaint();
        }
    }


    /**
     * Mouse stuff goes here.
     */
    public static String MOUSE_CLICK_MODE_SELECT = "select";
    public static String MOUSE_CLICK_MODE_TOGGLE_GROUP = "open or close group";
    public static String MOUSE_CLICK_MODE_NODE_EXPAND = "expand node neighborhood";

    private String mouseClickMode = MOUSE_CLICK_MODE_SELECT;

    public void setMouseClickMode(String mouseClickMode) {
        this.mouseClickMode = mouseClickMode;
    }

    class BMVisMouseListener implements MouseListener, MouseMotionListener,
            MouseWheelListener {
        Vec2 areaStart = null;
        int lastX;
        int lastY;
        LayoutItem selected;
        int button;
        long lastClick = 0;
        boolean dragged = false;

        private LayoutItem getItem(int x, int y) {
            Point2D p = new Point2D.Double(x, y);
            Point2D.Double tP = new Point2D.Double();

            try {
                transform.inverseTransform(p, tP);
            } catch (NoninvertibleTransformException e1) {
                e1.printStackTrace();
                Logging.error("ui", "Transform was invalid.");
                System.exit(1);
            }

            Vec2 v = new Vec2(tP.x, tP.y);

            ArrayList<LayoutItem> items = visualGraph.getZOrderItems();

            for (int j = items.size() - 1; j >= 0; j--) {
                LayoutItem n = items.get(j);
                if (n.containsPoint(v)) {
                    return n;
                }
            }
            return null;
        }

        public void mouseClicked(MouseEvent e) {
            long time = System.currentTimeMillis();
            LayoutItem item = getItem(e.getX(), e.getY());

            // Double click
            if (time - lastClick < 200) {
                GraphArea.this.zoomHelpShownTimes++;

                // Automatic zoom
                Collection<LayoutItem> neighbors;
                if (item != null) {
                    neighbors = visualGraph.getNeighbors(item);
                    neighbors.add(item);
                } else
                    neighbors = visualGraph.getZOrderItems();

                GraphArea.this.zoomTo(neighbors);
                GraphArea.this.repaint();
            } else if (e.getButton() == MouseEvent.BUTTON1) {
                // Handle selections & deselections
                if (e.isControlDown()) {
                    if (item != null && item instanceof VisualNode) {
                        VisualNode vn = (VisualNode) item;
                        if (vn.isSelected())
                            visualGraph.removeSelected(vn);
                        else
                            visualGraph.addSelected(vn);
                        visualGraph.selectionChanged();
                        GraphArea.this.repaint();
                    }
                } else {
                    if (item != null && item instanceof VisualNode) {
                        if (GraphArea.this.mouseClickMode.equals(MOUSE_CLICK_MODE_TOGGLE_GROUP)) {
                            if (item instanceof VisualGroupNode) {
                                VisualGroupNode vgn = (VisualGroupNode) item;
                                vgn.setOpen(true);
                                GraphArea.this.pipeline.settingsChanged(false);
                            } else {
                                VisualNode node = (VisualNode) item;
                                if (node.getParent().getParent() == null)
                                    return;
                                node.getParent().setOpen(false);
                                GraphArea.this.pipeline.settingsChanged(false);
                            }
                        } else if (GraphArea.this.mouseClickMode.equals(MOUSE_CLICK_MODE_NODE_EXPAND)) {
                            if (item instanceof VisualGroupNode)
                                return;
                            VisualNode node = (VisualNode) item;
                            try {
                                GraphSource tmp = pipeline.expand(node);
                                if (tmp != null)
                                    pipeline.addSource(tmp);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        } else {
                            if (!GraphArea.this.mouseClickMode.equals(MOUSE_CLICK_MODE_SELECT))
                                Logging.error("ui", "Unknown mouse click mode!");
                            visualGraph.clearSelected();
                            visualGraph.addSelected((VisualNode) item);
                            visualGraph.selectionChanged();
                            GraphArea.this.repaint();
                        }
                    } else {
                        // Hackish, this is to prevent repaint because of a single stray click.
                        boolean changed = visualGraph.clearSelected();
                        if (changed) {
                            visualGraph.selectionChanged();
                            GraphArea.this.repaint();
                        }
                    }
                }
            }
            lastClick = System.currentTimeMillis();
        }

        public void mouseEntered(MouseEvent arg0) {
        }

        public void mouseExited(MouseEvent arg0) {
        }

        public void mousePressed(MouseEvent e) {
            button = e.getButton();
            lastX = e.getX();
            lastY = e.getY();
            dragged = false;
            selected = getItem(lastX, lastY);
            Vec2 mp = inverseTransform(lastX, lastY);
            if (selected != null)
                visualGraph.pullUp(selected);

            if (e.isShiftDown()) {
                GraphArea.this.areaSelect = new Rectangle2D.Double(mp.x, mp.y, 0, 0);
                areaStart = mp;
                GraphArea.this.repaint();
            }
            if (e.getButton() == MouseEvent.BUTTON3 && selected != null) {
                GraphArea.this.openContextMenu(selected, lastX, lastY);
            } else if (button == MouseEvent.BUTTON3) {
                GraphArea.this.openMenu(lastX, lastY);
            }

        }

        public void mouseReleased(MouseEvent e) {
            selected = null;
            if (GraphArea.this.areaSelect != null) {
                if (!e.isControlDown())
                    visualGraph.clearSelected();
                for (VisualNode n : visualGraph.getNodes()) {
                    if (GraphArea.this.areaSelect.contains(n.getPos().toPoint())) {
                        visualGraph.addSelected(n);
                    }
                }
                visualGraph.selectionChanged();
            }
            GraphArea.this.areaSelect = null;
            GraphArea.this.repaint();
        }

        public void mouseDragged(MouseEvent e) {
            if (button != MouseEvent.BUTTON1)
                return;
            if (zoomController.scaling)
                zoomController.stop();

            dragged = true;

            int x = e.getX();
            int y = e.getY();

            // Area selection
            if (e.isShiftDown() || GraphArea.this.areaSelect != null) {
                Rectangle2D.Double as = GraphArea.this.areaSelect;
                Vec2 ep = inverseTransform(x, y);
                if (GraphArea.this.areaSelect == null) {
                    this.areaStart = ep;
                    GraphArea.this.areaSelect = new Rectangle2D.Double(ep.x, ep.y, 0, 0);
                }
                as.x = this.areaStart.x;
                as.y = this.areaStart.y;
                as.width = ep.x - this.areaStart.x;
                as.height = ep.y - this.areaStart.y;
                if (as.width < 0) {
                    as.x += as.width;
                    as.width *= -1;
                }
                if (as.height < 0) {
                    as.y += as.height;
                    as.height *= -1;
                }
                return;
            }

            /*
             * Panning
             */
            Point2D.Double tP = new Point2D.Double();
            Point2D.Double tL = new Point2D.Double();
            try {
                Point2D p = new Point2D.Double(x, y);

                transform.inverseTransform(p, tP);
                Point2D l = new Point2D.Double(lastX, lastY);
                transform.inverseTransform(l, tL);
            } catch (NoninvertibleTransformException e1) {
                e1.printStackTrace();
                Logging.error("ui", e1.getLocalizedMessage());
                System.exit(1);
            }

            if (selected != null) {
                Vec2 transform = new Vec2(tP.x - tL.x, tP.y - tL.y);
                if ((selected instanceof VisualNode)) {
                    VisualNode vn = (VisualNode) selected;
                    if (vn.isSelected()) {
                        for (VisualNode n : visualGraph.getSelected()) {
                            n.setPos(n.getPos().plus(transform));
                            n.setPositionFixed(true);
                            GraphArea.this.activateLayoutManager();
                        }
                    } else {
                        selected.setPos(selected.getPos().plus(transform));
                        GraphArea.this.activateLayoutManager();
                        selected.setPositionFixed(true);
                    }
                    GraphArea.this.activateLayoutManager();
                }
                GraphArea.this.repaint();
            } else {
                transform.translate(tP.getX() - tL.getX(), tP.getY()
                        - tL.getY());
                GraphArea.this.repaint();
            }

            lastX = x;

            lastY = y;

            /*
                * for (LayoutItem n : visualGraph.getZOrderItems()) { boolean wasHL
                * = n.isHighlighted(); n.setHighlight(false); if (wasHL) repaint();
                * }
                */

            // System.out.println(transform.getTranslateX()+" "+transform.getTranslateY());

        }

        /**
         * Implements the mouseMoved method of the corresponding interface.
         * <p/>
         * This method NO LONGER activates the graph layout because otherwise layout changes would not happen after releasing
         * LayoutItems (from pinning) which are highlighted during context-menu click and also un-highlighted when
         * the actual setFixed(false) is called on them.  So either unhighlight the items or let this method
         * reactivate the graph layout.  This change might be desirable if there are performance problems.
         *
         * @param e
         */
        public void mouseMoved(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();

            ArrayList<LayoutItem> items = visualGraph.getZOrderItems();

            LayoutItem hlItem = getItem(x, y);
            if (hlItem != null && hlItem.isHighlighted()) {
                return;
            }

            boolean unHighlighted = false;
            for (LayoutItem n : items) {
                if (n.isHighlighted())
                    unHighlighted = true;
                n.setHighlight(false);
            }

            if (hlItem != null) {
                visualGraph.setHighlighted(hlItem);
                // GraphArea.this.activateLayoutManager();
            } else {
                /* if (unHighlighted)
                    GraphArea.this.activateLayoutManager(); */
                visualGraph.setHighlighted(null);
            }
        }

        /*
           * Zoom.
           */
        public void mouseWheelMoved(MouseWheelEvent e) {
            int x = e.getX();
            int y = e.getY();
            int a = e.getWheelRotation();
            double scale = 0.8;

            scale = Math.pow(scale, a);
            if (
                // (transform.getScaleX() * scale < 0.1 && a > 0)||
                    (transform.getScaleX() * scale > 10 && a < 0)) {
                return;
            }
            Vec2 tP = inverseTransform(x, y);
            transform.translate(tP.x, tP.y);
            transform.scale(scale, scale);
            transform.translate(-tP.x, -tP.y);
            /*
                * try { Point2D p = new Point2D.Double(x, y); Point2D tP = new
                * Point2D.Double(); transform.inverseTransform(p, tP);
                *
                * transform.translate(tP.getX(), tP.getY()); transform.scale(scale,
                * scale); transform.translate(-tP.getX(), -tP.getY());
                *
                * } catch (NoninvertibleTransformException e1) {
                *
                * e1.printStackTrace(); // System.out.println(""); System.exit(1);
                * }
                */
            repaint();
        }
    }


    /**
     * This is to enable C-p, C-r et al on the graph canvas.
     *
     * @author ahinkka
     */
    class GraphAreaKeyListener implements KeyListener {
        public void keyPressed(KeyEvent e) {
            Logging.debug("ui", e.toString());
            // http://java.sun.com/javase/6/docs/api/java/awt/event/InputEvent.html#getModifiersEx()
            int onMask = InputEvent.CTRL_DOWN_MASK;
            int offMask = InputEvent.CTRL_DOWN_MASK
                    | InputEvent.SHIFT_DOWN_MASK;

            if ((e.getModifiersEx() & (onMask | offMask)) == onMask) {
                if (e.getKeyCode() == KeyEvent.VK_P)
                    visualGraph.pinAll();
                else if (e.getKeyCode() == KeyEvent.VK_R)
                    visualGraph.releaseAll();
            }
        }

        public void keyReleased(KeyEvent e) {
        }

        public void keyTyped(KeyEvent arg0) {
        }
    }

    /*
      * Right click menu
      */
    private void openMenu(int x, int y) {
        Point pt = SwingUtilities.convertPoint(this, new Point(x, y), this);

        JPopupMenu m = Menus.getInstance(pipeline).getCanvasMenu(this);
        m.show(this, pt.x, pt.y);
    }

    private void openContextMenu(final LayoutItem item, int x, int y) {
        if (pipeline == null)
            return;
        JPopupMenu menu = Menus.getInstance(pipeline).get2ndButtonMenu(item, this);

        Point pt = SwingUtilities.convertPoint(this, new Point(x, y), this);
        menu.show(this, pt.x, pt.y);
    }

    private static final long serialVersionUID = 1L;


    // These methods implement the GraphObserver behavior
    public void graphStructureChanged(VisualGraph g) {
        Logging.debug("graph_drawing", "GraphArea.graphStructureChanged() called¡");
        this.repaint();

        if (this.graphLayout != null)
            this.graphLayout = new SpringLayoutManager(this.visualGraph, this.graphLayout);
        else
            this.graphLayout = new SpringLayoutManager(this.visualGraph);
        this.activateLayoutManager();
    }

    public void visibleNodesChanged(VisualGraph g) {
        this.repaint();
        this.activateLayoutManager();
    }

    public void selectionChanged(VisualGraph g) {
        this.repaint();
    }

    public void colorsChanged(VisualGraph g) {
        this.repaint();
    }

    public void zoomRequested(VisualGraph g, Collection<LayoutItem> items) {
        this.zoomTo(items);
    }

    public JComponent getSettingsDialogComponent() {
        JComponent ret = new JTabbedPane();

        ret.add("Graphics", this.getGraphicsSettingsComponent());
        ret.add("Layout", this.getLayoutSettingsComponent());

        return ret;
    }


    public JComponent getGraphicsSettingsComponent() {
        final JPanel ret = new JPanel();
        GridLayout gl = new GridLayout(4, 1);
        ret.setLayout(gl);

        final JCheckBox antiAliasLinesCB = new JCheckBox("Anti-alias lines");
        final JCheckBox antiAliasTextCB = new JCheckBox("Anti-alias text");
        final JCheckBox vectorCB = new JCheckBox("Vector edges");
        antiAliasLinesCB.setSelected(this.antiAlias);
        antiAliasTextCB.setSelected(this.antiAliasText);
        vectorCB.setSelected(this.vectorEdges);

        ret.add(antiAliasLinesCB);
        ret.add(antiAliasTextCB);
        ret.add(vectorCB);

        final JPanel edgeRendererSettingsHolder = new JPanel();
        edgeRendererSettingsHolder.setBorder(new EmptyBorder(5, 5, 5, 5));

        edgeRendererSettingsHolder.add(VisualEdge.getEdgeRenderer().getSettingsComponent(this));

        ret.add(edgeRendererSettingsHolder);

        ChangeListener cl = new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                GraphArea.this.antiAlias = antiAliasLinesCB.isSelected();
                GraphArea.this.antiAliasText = antiAliasTextCB.isSelected();
                GraphArea.this.vectorEdges = vectorCB.isSelected();
                GraphArea.this.repaint();
                edgeRendererSettingsHolder.removeAll();
                updateEdgeRenderer();
                edgeRendererSettingsHolder.add(VisualEdge.getEdgeRenderer().getSettingsComponent(GraphArea.this));
                edgeRendererSettingsHolder.revalidate();
            }
        };

        antiAliasLinesCB.addChangeListener(cl);
        antiAliasTextCB.addChangeListener(cl);
        vectorCB.addChangeListener(cl);

        return ret;
    }

    public JComponent getLayoutSettingsComponent() {
        JPanel ret = new JPanel();

        GridLayout gl = new GridLayout(4, 2);
        ret.setLayout(gl);

        final JSlider damping = new JSlider(0, 100);
        damping.setPaintLabels(true);
        damping.setMajorTickSpacing(25);
        damping.setValue((int) (100 * graphLayout.getDamping()));
        damping.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent arg0) {
                GraphArea.this.graphLayout.setDamping(damping.getValue() * 0.01);
                GraphArea.this.activateLayoutManager();
            }
        });

        final JSlider repulsion = new JSlider(0, 100);
        repulsion.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent arg0) {
                GraphArea.this.graphLayout.setRepulsive_k(repulsion.getValue() * 1000.0);
                GraphArea.this.activateLayoutManager();
            }
        });
        repulsion.setPaintLabels(true);
        repulsion.setMajorTickSpacing(25);
        repulsion.setValue((int) Math.round(graphLayout.getRepulsive_k() / 1000.0));

        final JSlider freezingThreshold = new JSlider(0, 25);
        freezingThreshold.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent arg0) {
                GraphArea.this.graphLayout.setFreezingThreshold(freezingThreshold.getValue());
                GraphArea.this.activateLayoutManager();
            }
        });
        freezingThreshold.setPaintLabels(true);
        freezingThreshold.setMajorTickSpacing(5);
        freezingThreshold.setValue((int) Math.round(graphLayout.getFreezingThreshold()));

        JLabel dampingLabel = new JLabel("Damping");
        JLabel repulsionLabel = new JLabel("Repulsion");
        JLabel freezingLabel = new JLabel("Freezing");
        ret.add(dampingLabel);
        ret.add(damping);
        ret.add(repulsionLabel);
        ret.add(repulsion);
        ret.add(freezingLabel);
        ret.add(freezingThreshold);

        return ret;
    }

    public GraphAreaSettings getSettings() {
        GraphAreaSettings ret = new GraphAreaSettings();
        ret.antiAlias = this.antiAlias;
        ret.antiAliasText = this.antiAliasText;
        ret.vectorEdges = this.vectorEdges;
        return ret;
    }

    public void setSettings(GraphAreaSettings sets) {
        antiAlias = sets.antiAlias;
        antiAliasText = sets.antiAliasText;
        vectorEdges = sets.vectorEdges;
    }

    /**
     * Draws current view as png to a file.
     *
     * @param out
     * @throws IOException
     */
    @SuppressWarnings({"JavaDoc"})
    public void exportToPNG(File out) throws IOException {
        BufferedImage output = new BufferedImage(getWidth(), getHeight(),
                BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g = output.createGraphics();
        // g.setTransform(t)
        prepareGraphics(g);

        // Composite com =AlphaComposite.Clear;
        // g.setComposite(com);
        // g.fillRect(0, 0, getWidth(),getHeight());
        // g.setComposite(AlphaComposite.Src);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());

        AffineTransform tr = g.getTransform();
        tr.concatenate(transform);
        g.setTransform(tr);

        visualGraph.setPrintMode(true);
        visualGraph.paint(g);
        visualGraph.setPrintMode(false);

        ImageIO.write(output, "PNG", out);
    }

    public void pointsOfInterestsChanged(VisualGraph g) {
    }

    public GraphArea getGraphArea() {
        return this;
    }

    public void setGraph(VisualGraph graph, boolean initialZoom) {
        if (graph == VisualGraph.EMPTY)
            return;

        Logging.info("ui", "setGraph(" + graph + ") called!");

        this.visualGraph = graph;
        this.activateLayoutManager();
        this.visualGraph.addObserver(this);
    }


    public void zoomTo(Collection<? extends LayoutItem> itemsToZoom) {
        this.zoomController.zoomTo(itemsToZoom);
    }
}