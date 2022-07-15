package org.grits.toolbox.utils.image;

import java.awt.image.BufferedImage;

public interface ImageProvider
{

    public boolean add(String a_imageId, BufferedImage a_image) throws Exception;

    public BufferedImage get(String a_image) throws Exception;

}
