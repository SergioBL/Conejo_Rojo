
package practica_3_dba;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

/**
 * 
 * @author Alex
 */
public class DibujarMapa extends javax.swing.JPanel {
    
    BufferedImage mapaDibujado = new BufferedImage(560, 560, BufferedImage.TYPE_INT_RGB);

    /**
    *
    * @author Alex
    */
    public DibujarMapa(int [][] mapa) {
        initComponents();
        
	for(int y = 0; y < mapa.length; ++y)
            for(int x = 0; x < mapa.length; ++x){
                    DibujarBit(mapa[y][x], x, y);
            }
    }
    
    /**
    *
    * @author Alex
    */
    public void Actualizar(int [][] mapa) {	
	for(int y = 0; y < mapa.length; ++y)
            for(int x = 0; x < mapa.length; ++x)
                    DibujarBit(mapa[y][x], x, y);
    }
    
    /**
     * 
     * @author Alex
     */
    private void DibujarBit(int valor, int x, int y) {
            Color color;
            switch(valor) {
                case 0:
                    color = Color.WHITE;
                    mapaDibujado.setRGB(x, y, color.getRGB());
                break;
                case -1:
                    color = Color.RED;
                    mapaDibujado.setRGB(x, y, color.getRGB());
                    break;
                default:
                    color = Color.GRAY;
                    mapaDibujado.setRGB(x, y, color.getRGB());
                    break;
            }
    }
    
	/**
	 * 
	 * @author Alex
	 */
	@Override
        public void paint(Graphics g) {
            g.drawImage(mapaDibujado, 0, 0,mapaDibujado.getHeight(),mapaDibujado.getWidth(), null);
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
