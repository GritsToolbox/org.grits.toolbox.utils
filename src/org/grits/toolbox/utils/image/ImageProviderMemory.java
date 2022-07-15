package org.grits.toolbox.utils.image;

import java.awt.image.BufferedImage;
import java.util.HashMap;

public class ImageProviderMemory implements ImageProvider
{
    private HashMap<String, BufferedImage> m_images = new HashMap<String, BufferedImage>();
    
    public boolean add(String a_imageId, BufferedImage a_image) throws Exception
    {
        if ( this.m_images.get(a_imageId) != null )
        {
            return false;
        }
        this.m_images.put(a_imageId, a_image);
        return true;
    }

    public BufferedImage get(String a_imageId) throws Exception
    {
        return this.m_images.get(a_imageId);
    }

}
