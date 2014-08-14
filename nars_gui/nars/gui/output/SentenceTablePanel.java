package nars.gui.output;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import nars.core.NAR;
import nars.entity.Concept;
import nars.entity.Sentence;
import nars.entity.TruthValue;
import nars.gui.NPanel;
import nars.io.Output;
import nars.language.CompoundTerm;
import nars.language.Term;
import nars.util.NARGraph;

/**
 *
 * @author me
 */
public class SentenceTablePanel extends NPanel implements Output {

    private final NAR nar;

    DefaultTableModel data;
    private final JButton graphButton;
    private final JTable t;

    public SentenceTablePanel(NAR nar) {
        super();
        this.nar = nar;

        setLayout(new BorderLayout());

        data = newModel();

        t = new JTable(data);
        t.setAutoCreateRowSorter(true);
        t.validate();
        t.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                graphButton.setEnabled(t.getSelectedRowCount() > 0);
            }
        });

        add(new JScrollPane(t), BorderLayout.CENTER);

        JPanel menu = new JPanel(new FlowLayout(FlowLayout.LEFT));
        {
            graphButton = new JButton("Graph");
            graphButton.setEnabled(false);
            graphButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    newSelectedGraphPanel();
                }
            });
            menu.add(graphButton);

            JButton clearButton = new JButton("Clear");
            clearButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    data = newModel();
                    t.setModel(data);
                }
            });
            menu.add(clearButton);

        }
        add(menu, BorderLayout.SOUTH);
    }

    public DefaultTableModel newModel() {
        DefaultTableModel data = new DefaultTableModel();
        data.addColumn("Sentence");
        data.addColumn("Time");
        data.addColumn("Punctuation");
        data.addColumn("Frequency");
        data.addColumn("Confidence");
        data.addColumn("Complexity");
        data.fireTableStructureChanged();
        return data;
    }

    public void newSelectedGraphPanel() {

        new ProcessingGraphPanel(nar) {

//            public List<Sentence> getSentences() {
//                List<Sentence> displayed;
//                if (sentenceIndex == -1) {
//                    displayed = sentences;
//                }
//                else {
//                    displayed = new ArrayList(1);
//                    displayed.add(sentences.get(sentenceIndex));
//                }
//                return displayed;
//            }
            private NARGraph.DefaultGraphizer graphizer;
            private List<Sentence> sentences = getSelectedRows();

            @Override
            public NARGraph getGraph(ProcessingGraphPanel p) {

                graphizer = new NARGraph.DefaultGraphizer(true, true, true, true, false) {

                    @Override
                    public void onTime(NARGraph g, long time) {
                        super.onTime(g, time);

                        for (Sentence s : sentences) {
                            g.add(s);

                            Term t = s.content;
                            addTerm(g, t);
                            g.addEdge(new NARGraph.SentenceContent(s, s.content), false);

                            if (t instanceof CompoundTerm) {
                                CompoundTerm ct = ((CompoundTerm) t);
                                Set<Term> contained = ct.getContainedTerms();

                                for (Term x : contained) {
                                    addTerm(g, x);
                                    if (ct.containsTerm(x)) {
                                        g.addEdge(new NARGraph.TermContent(x, t), false);
                                    }

                                    for (Term y : contained) {
                                        addTerm(g, y);

                                        if (x != y) {
                                            if (x.containsTerm(y)) {
                                                g.addEdge(new NARGraph.TermContent(y, x), false);
                                            }
                                        }
                                    }

                                }
                            }

                        }
                        //add sentences
                    }

                };

//        if (sentences.size() > 1) {
//            final JTextField ssl = new JTextField();
//            final JSlider indexSlider = new JSlider(-1, sentences.size()-1, -1);        
//            indexSlider.setSnapToTicks(true);
//            indexSlider.setMajorTickSpacing(1);
//            indexSlider.setMinorTickSpacing(1);
//            indexSlider.addChangeListener(new ChangeListener() {
//                @Override
//                public void stateChanged(ChangeEvent e) {
//                    int i = indexSlider.getValue();
//                    sentenceIndex = i;
//                    if (i == -1) {
//                        update();
//                        ssl.setText("All Sentences");
//                    }
//                    else {
//                        update();
//                        ssl.setText(ProcessingGraphPanel.this.sentences.get(i).toString());
//                    }
//                }            
//            });
//            menu.add(indexSlider);
//            menu.add(ssl);        
//        }
//
                app.updating = true;

                graphizer.setShowSyntax(true /*showSyntax*/);

                NARGraph g = new NARGraph();
                g.add(nar, newSelectedGraphFilter(), graphizer);

                return g;
            }

            public NARGraph.Filter newSelectedGraphFilter() {

                final List<Sentence> selected = sentences;

                final Set<Term> include = new HashSet();
                for (final Sentence s : selected) {
                    Term t = s.content;
                    include.add(t);
                    if (t instanceof CompoundTerm) {
                        CompoundTerm ct = (CompoundTerm) t;
                        include.addAll(ct.getContainedTerms());
                    }
                }

                return new NARGraph.Filter() {

                    @Override
                    public boolean includePriority(float l) {
                        return true;
                    }

                    @Override
                    public boolean includeConcept(final Concept c) {

                        final Term t = c.term;

                        return include.contains(t);
                    }

                };
            }

        };
    }

    @Override
    public void output(Class channel, Object o) {
        if (o instanceof Sentence) {
            Sentence s = (Sentence) o;

            float freq = -1;
            float conf = -1;
            TruthValue truth = s.truth;
            if (truth != null) {
                freq = truth.getFrequency();
                conf = truth.getConfidence();
            }

            //TODO use table sort instead of formatting numbers with leading '0's
            data.addRow(new Object[]{
                s,
                String.format("%08d", nar.getTime()), s.punctuation,
                freq == -1 ? "" : freq,
                conf == -1 ? "" : conf,
                String.format("%03d", s.content.getComplexity())
            });
        }
    }

    @Override
    protected void onShowing(boolean showing) {
        if (showing) {
            nar.addOutput(this);
        } else {
            nar.removeOutput(this);
        }
    }

    private List<Sentence> getSelectedRows() {
        int[] selectedRows = t.getSelectedRows();
        List<Sentence> l = new ArrayList(selectedRows.length);
        for (int i : selectedRows) {
            int selectedRow = t.convertRowIndexToModel(i);
            l.add((Sentence) data.getValueAt(selectedRow, 0));
        }
        return l;
    }

}
