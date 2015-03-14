package ca.nengo.ui.model.plot;

import ca.nengo.model.SimulationException;
import ca.nengo.model.impl.AbstractNode;
import ca.nengo.ui.lib.object.model.ModelObject;
import ca.nengo.ui.lib.world.PaintContext;
import ca.nengo.ui.lib.world.piccolo.object.BoundsHandle;
import ca.nengo.ui.model.UIBuilder;
import ca.nengo.ui.model.UINeoNode;
import ca.nengo.ui.model.icon.EmptyIcon;
import ca.nengo.ui.model.icon.ModelIcon;
import org.piccolo2d.util.PBounds;

/**
 * Created by me on 3/3/15.
 */
public abstract class AbstractWidget extends AbstractNode implements UIBuilder {

    public final AbstractWidgetUI ui;

    public AbstractWidget(String name) {
        this(name, 64, 64);
    }

    public AbstractWidget(String name, double width, double height) {
        super(name);

        ui = newUI(width, height);
    }

    public boolean isResizable() {
        return true;
    }

    public ModelIcon newIcon(ModelObject UI) {
        EmptyIcon ei = new EmptyIcon(UI);
        ei.setLabelVisible(false);
        return ei;
    }


    public PBounds getBounds() { return ui.getBounds(); }
    public PBounds setBounds(PBounds p) { ui.setBounds(p); return p; }


    protected abstract void paint(PaintContext paintContext, double width, double height);

    public String getTypeName() {
        return getClass().getSimpleName();
    }

    @Override
    final public AbstractWidgetUI newUI(double width, double height) {
        return new AbstractWidgetUI(width, height);
    }

    @Override
    public abstract void run(float startTime, float endTime) throws SimulationException;

    @Override
    public void reset(boolean randomize) {



    }

    public void draggedTo(double x, double y) {

    }


    /** called before destruction */
    protected void beforeDestroy() {

    }

    public class AbstractWidgetUI extends UINeoNode<AbstractWidget> {

        public AbstractWidgetUI(double width, double height) {
            super(AbstractWidget.this);


            if (isResizable())
                BoundsHandle.addBoundsHandlesTo(this);



            setIcon(newIcon(this));


            setSize(getIcon().getWidth(), getIcon().getHeight());


            repaint();
        }


        @Override
        protected void prepareForDestroy() {
            super.prepareForDestroy();

            if (!isDestroyed())
                beforeDestroy();
        }

        @Override
        public void dragOffset(double dx, double dy) {
            super.dragOffset(dx, dy);
            draggedTo(getOffset().getX(), getOffset().getY());
        }

        @Override
        public void dragTo(double dx, double dy) {
            super.dragTo(dx, dy);
        }
        @Override
        public void dragTo(double dx, double dy, double speed /* 1 - LERP momentum */) {
            super.dragTo(dx, dy, speed);
        }

        @Override
        public String getTypeName() {
            return AbstractWidget.this.getTypeName();
        }


        @Override
        public void paint(PaintContext paintContext) {
            super.paint(paintContext);

            AbstractWidget.this.paint(paintContext, getWidth(), getHeight());
        }

        @Override
        public ModelIcon getIcon() {
            return (ModelIcon) super.getIcon();
        }
    }

    public void move(double x, double y) {
        ui.dragTo(x, y);
    }
}