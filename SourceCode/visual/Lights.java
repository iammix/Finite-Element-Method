package visual;

import javax.media.j3d.*;
import javax.vecmath.*;

public class Lights {

    // Set lights and background.
    // root - branch group.
    public static void setLights(BranchGroup root) {

        BoundingSphere bounds = new BoundingSphere(
            new Point3d(0.,0.,0.), 16*SurfaceGeometry.sizeMax);

        // Set up the background
        Background bgNode = new Background(VisData.bgColor);
        bgNode.setApplicationBounds(bounds);
        root.addChild(bgNode);

        // Set up the ambient light
        Color3f ambientColor = new Color3f(1.f, 1.f, 1.f);
        AmbientLight light0 = new AmbientLight(ambientColor);
        light0.setInfluencingBounds(bounds);
        root.addChild(light0);

        // Set up the directional lights
        Color3f color1 = new Color3f(0.6f, 0.6f, 0.6f);
        Vector3f direction1 = new Vector3f(4.f, -7.f, -12.f);
        DirectionalLight light1
                = new DirectionalLight(color1, direction1);
        light1.setInfluencingBounds(bounds);
        root.addChild(light1);

        Color3f color2 = new Color3f(0.4f, 0.4f, 0.4f);
        Vector3f direction2 = new Vector3f(-5.f, -3.f, -1.f);
        DirectionalLight light2
                = new DirectionalLight(color2, direction2);
        light2.setInfluencingBounds(bounds);
        root.addChild(light2);
    }

}
