
package practica_3_dba;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

/**
 * 
 * @author Alex
 */
public class MyDrawPanel extends javax.swing.JPanel {
    
    BufferedImage image = new BufferedImage(510, 510, BufferedImage.TYPE_INT_RGB);

    /**
    *
    * @author Alex
    */
    public MyDrawPanel(int [][] mapWorld) {
        initComponents();
		
	for(int y = 0; y < mapWorld.length; ++y)
            for(int x = 0; x < mapWorld.length; ++x)
		paintCoord(mapWorld[y][x], x, y);
    }
    
    /**
    *
    * @author Alex
    */
    public void Update(int [][] mapWorld) {	
	for(int y = 0; y < mapWorld.length; ++y)
            for(int x = 0; x < mapWorld.length; ++x)
		paintCoord(mapWorld[y][x], x, y);
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
