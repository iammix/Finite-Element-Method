package visual;

import javax.media.j3d.*;
import java.awt.*;
import java.awt.image.MemoryImageSource;
import com.sun.j3d.utils.image.TextureLoader;

// Create 2D texture VisData.textureSize by 1 pixels.
// Texture contains VisData.nContours color intervals.
class ColorScale extends Component {

    // Returns textture with color gradation
    Texture2D getTexture() {

        int pix[] = new int[VisData.textureSize];
        int n2 = 0;
        double delta =
                (double) VisData.textureSize/VisData.nContours;
        for (int i = 0; i < VisData.nContours; i++) {
            int n1 = n2;
            n2 = (int) ((i + 1)*delta + 0.5);
            int color =
                getScaleColor(1.0/VisData.nContours*(i+0.5));
            for (int j = n1; j < n2; j++) pix[j] = color;
        }

        Image img = createImage(
                new MemoryImageSource(VisData.textureSize, 1,
                        pix, 0, VisData.textureSize));
        TextureLoader loader = new TextureLoader(img, null);
        Texture2D texture = new Texture2D(Texture.BASE_LEVEL,
                Texture.RGBA, VisData.textureSize, 1);
        texture = (Texture2D) loader.getTexture();

        return texture;
    }

    // Compute color for color scale texture.
    // Texture (v=RGB): 0.0=101; 0.2=001; 0.4=011;
    //                  0.6=010; 0.8=110; 1.0=100.
    // v - texture coordinate (0..1).
    // returns  color in RGBA int format.
    private static int getScaleColor(double v) {

        double R, G, B;
        if (v < 0.2) {       // magenta - blue
            R = 1 - 5*v;
            G = 0;
            B = 1;
        }
        else if (v < 0.4) {  // blue - cyan
            R = 0;
            G = 5*(v - 0.2);
            B = 1;
        }
        else if (v < 0.6) {  // cyan - green
            R = 0;
            G = 1;
            B = 1 - 5*(v - 0.4);
        }
        else if (v < 0.8) {  // green - yellow
            R = 5*(v - 0.6);
            G = 1;
            B = 0;
        }
        else {               // yellow - red
            R = 1;
            G = 1 - 5*(v - 0.8);
            B = 0;
        }
        int iR = (int) (R*255 + 0.5);
        int iG = (int) (G*255 + 0.5);
        int iB = (int) (B*255 + 0.5);
        return (255 << 24) | (iR << 16) | (iG << 8) | iB;
    }

}
