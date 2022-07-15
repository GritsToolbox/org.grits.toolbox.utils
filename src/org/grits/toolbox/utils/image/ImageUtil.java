package org.grits.toolbox.utils.image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageUtil
{

    public static BufferedImage fromFile(String a_sourceName) throws IOException
    {
        return javax.imageio.ImageIO.read(new File(a_sourceName) );
    }

}
