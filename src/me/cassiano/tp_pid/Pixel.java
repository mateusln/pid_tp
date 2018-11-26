package me.cassiano.tp_pid;

public class Pixel {

    public int alpha;
    public int red;
    public int green;
    public int blue;
    public int gray;

    public Pixel(){
        alpha = red = green = blue = gray = 0;
    }

    public Pixel(int pixel){
        this.alpha = (pixel>>24)&0xff;
        this.red = (pixel>>16)&0xff;
        this.green = (pixel>>8)&0xff;
        this.blue = pixel&0xff;
        this.gray = (red+green+blue) / 3;
    }

    public void setRGB(int r, int g, int b) {
        this.red = r;
        this.green = g;
        this.blue = b;
        this. gray = (red+green+blue) / 3;
    }

    int getComposedPixel() {
        return (alpha<<24) | (red<<16) | (green<<8) | blue;
    }

    int getLimiarizedPixel(int limiar, int color1, int color2) {
        if(gray > limiar){
            red = green = blue = gray = color1;
        } else {
            red = green = blue = gray = color2;
        }
        return getComposedPixel();
    }


}
