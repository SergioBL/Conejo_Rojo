
package practica_3_dba;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

/**
 * 
 * @author Alex
 */
public class MyDrawPanel extends javax.swing.JPanel {
    
    BufferedImage image = new BufferedImage(530, 530, BufferedImage.TYPE_INT_RGB);
    boolean radar;

    /**
    *
    * @author Alex
    */
    public MyDrawPanel(int [][] mapWorld, boolean radar) {
        initComponents();
        
        this.radar = radar;
	for(int y = 0; y < mapWorld.length; ++y)
            for(int x = 0; x < mapWorld.length; ++x){
                if(radar)
                    paintCoord(mapWorld[y][x], x, y);
                else
                    paintCoordScanner(mapWorld[y][x], x, y);
            }
    }
    
    /**
    *
    * @author Alex
    */
    public void Update(int [][] mapWorld) {	
	for(int y = 0; y < mapWorld.length; ++y)
            for(int x = 0; x < mapWorld.length; ++x)
                if(radar)
                    paintCoord(mapWorld[y][x], x, y);
                else
                    paintCoordScanner(mapWorld[y][x], x, y);
        //repaint();
    }
    
    /**
     * 
     * @author Alex
     */
    private void paintCoord(int mapWorld, int x, int y) {
            Color color;
            switch(mapWorld) {
                case 0:
                    color = Color.WHITE;
                    image.setRGB(x, y, color.getRGB());
                break;
                case 50000:
                    color = Color.BLACK;
                    image.setRGB(x, y, color.getRGB());
                break;
                case -1:
                    color = Color.RED;
                    image.setRGB(x, y, color.getRGB());
                    break;
                default:
                    color = Color.GRAY;
                    image.setRGB(x, y, color.getRGB());
                    break;
            }
    }
    
    /**
     * 
     * @author Alex
     */
    private void paintCoordScanner(int mapWorld, int x, int y) {
            Color color;
            if(mapWorld > 255)
                mapWorld = 255;
            color = new Color(mapWorld,mapWorld,mapWorld);
            image.setRGB(x, y, color.getRGB());
    }
    
	/**
	 * 
	 * @author Alex
	 */
	@Override
        public void paint(Graphics g) {
            g.drawImage(image, 0, 0,image.getHeight(),image.getWidth(), null);
        }

    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
