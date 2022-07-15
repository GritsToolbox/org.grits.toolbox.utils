package org.grits.toolbox.utils.image;

import java.awt.image.BufferedImage;
import java.io.FileOutputStream;

public class ImageProviderFolder implements ImageProvider
{
    private String m_folder = null;
    
    public ImageProviderFolder(String a_folder)
    {
        this.m_folder = a_folder;
    }

    public boolean add(String a_imageId, BufferedImage a_image) throws Exception
    {
        FileOutputStream t_fileWriter = new FileOutputStream( this.m_folder + "/" + a_imageId );
        javax.imageio.ImageIO.write(a_image,"png",t_fileWriter);
        t_fileWriter.close();
        return true;
    }

    public BufferedImage get(String a_image) throws Exception
    {
        return ImageUtil.fromFile(this.m_folder + "/" + a_image);
    }
}
