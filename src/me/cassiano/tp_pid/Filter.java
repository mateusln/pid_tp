/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.cassiano.tp_pid;


import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLOutput;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
//import org.jfree.chart.ChartFactory;
//import org.jfree.chart.ChartUtilities;
//import org.jfree.chart.JFreeChart;
//import org.jfree.chart.plot.PlotOrientation;
//import org.jfree.data.category.DefaultCategoryDataset;

/**
 * @author Herbert
 */
public class Filter {

    protected String file, filteredImage;
    protected BufferedImage image, grayImage;

    /**
     * Open a stored image
     * @param file the name of the stored image
     * @return
     */
    public BufferedImage openImage(String file) {
        this.file = file;
        try {
            File file2 = new File(file);
            System.out.println(file2.getAbsolutePath());
            this.image = ImageIO.read(file2);
//            this.image = ImageIO.read(getClass().getResourceAsStream(file));
            //this.grayImage = this.toGray();
        } catch (IOException ex) {
//            Logger.getLogger(this.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println(ex.getMessage());
        }
        return image;
    }

    /**
     * Convert the image to 8 bits grayscale
     * @return the converted image
     */
    public BufferedImage toGray() {
        for(int x=0; x<image.getWidth(); x++){
            for(int y=0; y<image.getHeight(); y++){
                int pixel = image.getRGB(x, y);
                int alpha = (pixel>>24)&0xff;
                int red = (pixel>>16)&0xff;
                int green = (pixel>>8)&0xff;
                int blue = pixel&0xff;

                int gray = (red+green+blue) / 3;
                pixel = (alpha<<24) | (gray<<16) | (gray<<8) | gray;
                image.setRGB(x, y, pixel);
            }
        }
        //this.storeImage("grayscale_"+file, image);
        return image;
    }

    public static void toGray(String file){
        BufferedImage img = new Filter().openImage(file);
        for(int x=0; x<img.getWidth(); x++){
            for(int y=0; y<img.getHeight(); y++){
                int pixel = img.getRGB(x, y);
                int alpha = (pixel>>24)&0xff;
                int red = (pixel>>16)&0xff;
                int green = (pixel>>8)&0xff;
                int blue = pixel&0xff;

                int gray = (red+green+blue) / 3;
                pixel = (alpha<<24) | (gray<<16) | (gray<<8) | gray;
                img.setRGB(x, y, pixel);
            }
        }
        new Filter().storeImage(file + "_grayscale", img);
    }

    /**
     * Store the image
     */
    public void storeImage(){
        try {
            ImageIO.write(image, "JPG", new File(filteredImage));
        } catch (IOException ex) {
            Logger.getLogger(Filter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void storeImage(String name, BufferedImage i){
        try {
            ImageIO.write(i, "JPG", new File(name));
        } catch (IOException ex) {
            Logger.getLogger(Filter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setFilteredImageName(String type){
        filteredImage = file+"_"+type;
    }

    /**
     *
     * @param file
     * @return
     * @throws IOException
     */
//    public static int[] getHistogram(String file) throws IOException{
//
//        BufferedImage img = new Filter().openImage(file);
//        int[] histogram = new int[256];
//
//        for(int k=0; k<256; k++){
//            histogram[k] = 0;
//        }
//
//        for(int i=0; i<img.getWidth(); i++){
//            for(int j=0; j<img.getHeight(); j++){
//                int rgb = img.getRGB(i, j);
//                Pixel p = new Pixel(rgb);
//                histogram[p.gray]++;
//            }
//        }
//        return histogram;
//    }

    /**
     * Creates an image file with the graph of a histogram
     * @param histogram the histogram to be ploted
     * @param file a name to indentify the image file in the disk
     * @throws FileNotFoundException
     * @throws IOException
     */
//    public static void plotHistogram(int[] histogram, String file) throws FileNotFoundException, IOException {
//
//        // cria o conjunto de dados
//        DefaultCategoryDataset ds = new DefaultCategoryDataset();
//
//        for(int i=0; i<histogram.length; i++){
//            ds.addValue(histogram[i], file, ""+i);
//        }
//
//        // cria o gráfico
//        JFreeChart grafico = ChartFactory.createBarChart("Histograma", "Intensidade",
//                "Quantidade", ds, PlotOrientation.VERTICAL, false, false, false);
//
//        OutputStream fos = new FileOutputStream(file+"_histogram");
//        ChartUtilities.writeChartAsJPEG(fos, grafico, 550, 400);
//        fos.close();
//    }


    /**
     * Creates an image file with the graph of the Probability Distribution Function
     * @param h the histogram from wich will be calculated the probabilities
     * @param file a name to indentify the file in the disk
     * @throws FileNotFoundException
     * @throws IOException
     */
//    public static void plotFDP(int[] h, String file) throws FileNotFoundException, IOException{
//
//        DefaultCategoryDataset ds = new DefaultCategoryDataset();
//        int numPixels = 0;
//        double density = 0.0;
//
//        for(int i=0; i<h.length; i++){
//            numPixels += h[i];
//        }
//
//        for(int i=0; i<h.length; i++){
//            density += (double)h[i] / numPixels;
//            ds.addValue(density, file, ""+i);
//        }
//
//        // cria o gráfico
//        JFreeChart grafico = ChartFactory.createLineChart("Função de Distribuição de Probabilidades", "Intensidade",
//                "Probabilidade", ds, PlotOrientation.VERTICAL, false, false, false);
//
//        OutputStream fos = new FileOutputStream(file + "_fdistp");
//        ChartUtilities.writeChartAsJPEG(fos, grafico, 550, 400);
//        fos.close();
//    }

    /**
     * Creates an image file with the graph of the Probability Density Function
     * @param h the histogram from wich will be calculated the probabilities
     * @param file a name to indentify the file in the disk
     * @throws FileNotFoundException
     * @throws IOException
     */
//    public static void plotfdp(int[] h, String file) throws FileNotFoundException, IOException{
//
//        DefaultCategoryDataset ds = new DefaultCategoryDataset();
//        int numPixels = 0;
//        double density = 0.0;
//
//        for(int i=0; i<h.length; i++){
//            numPixels += h[i];
//        }
//
//        for(int i=0; i<h.length; i++){
//            density = (double)h[i] / numPixels;
//            ds.addValue(density, file, ""+i);
//        }
//
//        // cria o gráfico
//        JFreeChart grafico = ChartFactory.createLineChart("Função de Densidade de Probabilidades", "Intensidade",
//                "Probabilidade", ds, PlotOrientation.VERTICAL, false, false, false);
//
//        OutputStream fos = new FileOutputStream(file + "_fdensp");
//        ChartUtilities.writeChartAsJPEG(fos, grafico, 550, 400);
//        fos.close();
//    }

    /**
     * Calculates the Mean Squared Error between two images
     * @param file1 the path of image 1
     * @param file2 the path of image 2
     * @return the Mean Squared Error (MSE)
     * @throws IOException
     */
//    public static double MSE(String file1, String file2) throws IOException{
//
//        BufferedImage img1 = new Filter().openImage(file1);
//        BufferedImage img2 = new Filter().openImage(file2);
//
//        int numPixels1 = img1.getWidth()*img1.getHeight();
//        int numPixels2 = img2.getWidth()*img2.getHeight();
//
//        double mse = 0.0;
//
//        if (numPixels1 != numPixels2){
//            System.out.println("As imagens têm tamanhos diferentes!");
//            return -1.0;
//        } else {
//
//            for(int i=0; i<img1.getWidth(); i++){
//                for(int j=0; j<img1.getHeight(); j++){
//                    int rgb1 = img1.getRGB(i, j);
//                    Pixel p1 = new Pixel(rgb1);
//
//                    int rgb2 = img2.getRGB(i, j);
//                    Pixel p2 = new Pixel(rgb2);
//
//                    mse += Math.pow((double)(Math.abs(p1.gray - p2.gray)), 2.0);
//                }
//            }
//            mse = mse / (double)numPixels1;
//            return mse;
//        }
//    }

    /**
     * Calculates the Mean Squared Error between two histograms
     * @param h1 the first histogram
     * @param h2 the second histogram
     * @return the Mean Squared Error (MSE)
     */
    public static double MSE(int[]h1, int[] h2){

        int numPixels1 = 0;
        int numPixels2 = 0;

        for(int i=0; i<h1.length; i++){
            numPixels1 += h1[i];
        }
        for(int i=0; i<h2.length; i++){
            numPixels2 += h2[i];
        }

        double mse = 0.0;

        if (numPixels1 != numPixels2){
            System.out.println("As imagens têm tamanhos diferentes!");
            return -1.0;
        } else {

            for(int i=0; i<h1.length; i++){

                double e1 = (double)h1[i];
                double e2 = (double)h2[i];

                double m = e1 - e2;

                mse += Math.pow((double)(Math.abs(m)), 2.0);

            }

            mse = mse / (double)h1.length;
            return mse;
        }
    }

//    public static double PSNR(String file1, String file2) throws IOException{
//        int LMAX = 255;
//        //double MSE = Filter.MSE(Filter.getHistogram(file1), Filter.getHistogram(file2));
//        double MSE = Filter.MSE(file1, file2);
//        return 10.0 * Math.log10(Math.pow((double)LMAX, 2.0) / MSE);
//    }

    public static int getMean(int[] h) {

        int mean = 0;
        int numPixels = 0;
        for(int i=0; i<h.length; i++){
            numPixels += h[i];
            mean += h[i] * i;
        }

        mean = mean / numPixels;
        return mean;
    }

}