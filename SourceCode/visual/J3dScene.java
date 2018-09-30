package visual;

import javax.media.j3d.*;
import javax.vecmath.*;
import java.awt.*;
import java.applet.Applet;
import com.sun.j3d.utils.universe.SimpleUniverse;

// Scene graph for visualization.
public class J3dScene {

    private SurfaceSubGeometry subGeometry;

    // Construct Java3D scene for visualization.
    public J3dScene(Applet c) {

        GraphicsConfiguration config =
                SimpleUniverse.getPreferredConfiguration();

        Canvas3D canvas = new Canvas3D(config);
        c.setLayout(new BorderLayout());
        c.add("Center", canvas);

        // Element subfaces, subedges and nodes
        subGeometry = new SurfaceSubGeometry();
        
        BranchGroup root = new BranchGroup();
        Lights.setLights(root);
        TransformGroup tg =
                MouseInteraction.setMouseBehavior();

        // Add finite element model shape
        tg = addModelShape(tg);

        root.addChild(tg);
        root.compile();

        System.out.println(" Number of polygons = " +
                subGeometry.nVertices/3);
        if (VisData.showDeformShape) System.out.printf(
                " Deformed shape: max displacement ="+
                " %4.2f max size\n", VisData.deformScale);
        if (VisData.drawContours) {
            System.out.printf(" Contours: %d colors" +
                    " (Magenta-Blue-Cyan-Green-Yellow-Red)\n",
                    VisData.nContours);
            System.out.printf(" %s: Fmin = %10.4e, " +
                    "Fmax = %10.4e\n", VisData.parm,
                    subGeometry.fmin, subGeometry.fmax);
        }

        SimpleUniverse u = new SimpleUniverse(canvas);
        u.getViewingPlatform().setNominalViewingTransform();
        u.addBranchGraph(root);
    }

    // Add model shape to the Java 3D scene graph.
    // tg - transform group of the scene graph.
    // returns  transform group of the scene graph
    TransformGroup addModelShape(TransformGroup tg) {

        Transform3D t3d = new Transform3D();
        t3d.setScale(subGeometry.getScale());
        tg.setTransform(t3d);

        // Element faces composed of triangular subfaces
        tg.addChild(facesShape());

        // Edges composed of line segments
        if (VisData.showEdges) tg.addChild(edgesShape());

        // Nodes located at the model surface
        if (VisData.showNodes) tg.addChild(nodesShape());

        return tg;
    }

    // Shape object for element faces
    private Shape3D facesShape() {

        TriangleArray faces = subGeometry.getModelTriangles();

        Appearance facesApp = new Appearance();

        // Polygon Attributes
        PolygonAttributes pa = new PolygonAttributes();
        pa.setCullFace(PolygonAttributes.CULL_BACK);
        pa.setPolygonOffset(VisData.offset);
        pa.setPolygonOffsetFactor(VisData.offsetFactor);
        facesApp.setPolygonAttributes(pa);

        // Material
        Color3f darkColor = new Color3f(0.0f, 0.0f, 0.0f);
        Color3f brightColor = new Color3f(0.9f, 0.9f, 0.9f);
        Color3f surfaceColor = VisData.modelColor;
        if (VisData.drawContours)
            surfaceColor = VisData.surTexColor;
        Material facesMat = new Material(surfaceColor,
                darkColor, surfaceColor, brightColor, 16.0f);
        facesMat.setLightingEnable(true);
        facesApp.setMaterial(facesMat);

        if (VisData.drawContours) {
            // Texture for creating contours
            ColorScale scale = new ColorScale();
            Texture2D texture = scale.getTexture();
            facesApp.setTexture(texture);
            TextureAttributes ta = new TextureAttributes();
            ta.setTextureMode(TextureAttributes.MODULATE);
            facesApp.setTextureAttributes(ta);
        }

        // Create Shape using Geometry and Appearance
        return new Shape3D(faces, facesApp);
    }

    // Shape object for element edges
    private Shape3D edgesShape() {

        LineArray edges = subGeometry.getModelLines();

        Appearance edgesApp = new Appearance();

        LineAttributes la = new LineAttributes();
        la.setLineAntialiasingEnable(true);
        edgesApp.setLineAttributes(la);

        ColoringAttributes ca = new ColoringAttributes();
        ca.setColor(VisData.edgeColor);
        edgesApp.setColoringAttributes(ca);

        return new Shape3D(edges, edgesApp);
    }

    // Shape object for nodes
    private Shape3D nodesShape() {

        PointArray nodes = subGeometry.getModelPoints();

        Appearance nodesApp = new Appearance();

        PointAttributes pa = new PointAttributes();
        pa.setPointAntialiasingEnable(true);
        pa.setPointSize(3.0f);
        nodesApp.setPointAttributes(pa);

        ColoringAttributes ca = new ColoringAttributes();
        ca.setColor(VisData.nodeColor);
        nodesApp.setColoringAttributes(ca);

        return new Shape3D(nodes, nodesApp);
    }
    //pa.setPolygonMode(PolygonAttributes.POLYGON_LINE); //==
}