package visual;

import javax.media.j3d.*;
import javax.vecmath.Point3d;
import com.sun.j3d.utils.behaviors.mouse.*;

public class MouseInteraction {

    // Set mouth behavior (rotate, zoom, translate).
    // returns  transform group.
    public static TransformGroup setMouseBehavior() {

        BoundingSphere bounds =  new BoundingSphere(
            new Point3d(0.,0.,0.), 16*SurfaceGeometry.sizeMax);

        TransformGroup tg = new TransformGroup();
        tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        tg.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);

        // Create the rotate behavior node
        MouseRotate behavior1 = new MouseRotate();
        behavior1.setSchedulingBounds(bounds);
        behavior1.setTransformGroup(tg);
        tg.addChild(behavior1);

        // Create the zoom behavior node
        MouseZoom behavior2 = new MouseZoom();
        behavior2.setSchedulingBounds(bounds);
        behavior2.setTransformGroup(tg);
        tg.addChild(behavior2);

        // Create the translate behavior node
        MouseTranslate behavior3 = new MouseTranslate();
        behavior3.setSchedulingBounds(bounds);
        behavior3.setTransformGroup(tg);
        tg.addChild(behavior3);

        return tg;
    }

}
