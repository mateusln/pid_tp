package me.cassiano.tp_pid;

import ij.ImagePlus;
import ij.gui.ShapeRoi;
import ij.io.Opener;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.FileChooser;

import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.PriorityQueue;
import java.util.ResourceBundle;

//import java.awt.Color;

public class ImageProcessingController implements Initializable {


    @FXML
    private Slider zoomSlider;

    @FXML
    private Button inSeed;

    @FXML
    private Button outSeed;

    @FXML
    private Button runRegionGrowingButton;

    @FXML
    private Group rootGroup;

    @FXML
    TextArea textArea;

    @FXML
    private Group rootGroupOriginal;

    @FXML
    private Button clearSeedsButton;

    @FXML
    private Slider minSlider;

    @FXML
    private Slider maxSlider;

    @FXML
    private BarChart<String, Number> histogramChart;

    private Group zoomGroup;

    private ImagePlus originalImage;

    private Canvas canvas;

    private DoubleProperty sliderZoomProperty = new SimpleDoubleProperty(100);

    private boolean pickingSeed = false;

    private Seed internalSeed;
    private Seed externalSeed;

    private Path tempPath;
    private Seed.Type seedBeingPicked;
    private Seed.Shape seedShape;

    private Shape shape;

    private BufferedImage img_convertida;



    public void initialize(URL location, ResourceBundle resources) {

        seedShape = Seed.Shape.Circle;
        zoomGroup = new Group();
        zoomGroup.setMouseTransparent(false);
        rootGroup.getChildren().add(zoomGroup);
        this.imagemPadrao(); // já abre o programa com uma img carregada p/ acelerar o teste

    }

    public void imagemPadrao(){


        if (originalImage == null) {
            registerScrollListener();
            registerListenerForZoomSlider();
            registerListenerForWindowingSliders();
        }

        originalImage = new Opener().openImage("/home/mateus/Imagens/lena.jpeg");

        Image currentImage = SwingFXUtils.
                toFXImage(originalImage.getBufferedImage(), null);
        ImageView originalImageView = new ImageView();
        originalImageView.setImage(currentImage);
        rootGroupOriginal.getChildren().clear();
        rootGroupOriginal.getChildren().add(originalImageView);

        ImageConverter  converter = new ImageConverter(originalImage);
        img_convertida = originalImage.getBufferedImage();
        converter.convertToGray8();

        if (canvas == null) {
            canvas = new Canvas(originalImage.getWidth(), originalImage.getHeight());
            zoomGroup.getChildren().add(canvas);
        }

        clearPoints();

        resetWindowingSliders();

        redrawCanvas();

        showHistogramChart();

        enableSeedButtons();
    }

    public void openImageClicked(ActionEvent actionEvent) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar imagem");
        File file = fileChooser.showOpenDialog(inSeed.getScene().getWindow());


        if (file != null) {

            if (originalImage == null) {
                registerScrollListener();
                registerListenerForZoomSlider();
                registerListenerForWindowingSliders();
            }

            originalImage = new Opener().openImage(file.getPath());

            Image currentImage = SwingFXUtils.
                    toFXImage(originalImage.getBufferedImage(), null);
            ImageView originalImageView = new ImageView();
            originalImageView.setImage(currentImage);
            rootGroupOriginal.getChildren().clear();
            rootGroupOriginal.getChildren().add(originalImageView);

            ImageConverter converter = new ImageConverter(originalImage);
            img_convertida = originalImage.getBufferedImage();
            converter.convertToGray8();

            if (canvas == null) {
                canvas = new Canvas(originalImage.getWidth(), originalImage.getHeight());
                zoomGroup.getChildren().add(canvas);
            }

            clearPoints();

            resetWindowingSliders();

            redrawCanvas();

            showHistogramChart();

            enableSeedButtons();
        }

    }

    private void showHistogramChart() {

        ImagePlus iplus = new ImagePlus();

        iplus.setImage(img_convertida);

        ImageProcessor imageProcessor = iplus.getChannelProcessor();


        int[] histogram = imageProcessor.getHistogram();

        XYChart.Series<String, Number> series1 = new XYChart.Series<String, Number>();


        for (Integer i = 0; i < histogram.length; i++) {
            XYChart.Data<String, Number> point =
                    new XYChart.Data<String, Number>(i.toString(), histogram[i]);

            series1.getData().add(point);
        }

        histogramChart.getData().clear();
        histogramChart.getData().add(series1);


    }

    private void clearPoints() {

        if (internalSeed != null) {
            internalSeed.getView().getChildren().
                    removeAll(internalSeed.getView().getChildren());
            internalSeed = null;
        }

        if (externalSeed != null) {
            externalSeed.getView().getChildren().
                    removeAll(externalSeed.getView().getChildren());
            externalSeed = null;
        }


    }

    private void registerCanvasForMouseEvents() {

        canvas.setOnMousePressed(new EventHandler<MouseEvent>() {

            public void handle(MouseEvent event) {

                Seed seed;
                Color strokeColor;

                if (seedBeingPicked == Seed.Type.Internal) {

                    if (internalSeed == null) {
                        internalSeed = new Seed();
                        internalSeed.setType(Seed.Type.Internal);
                        internalSeed.setView(new Group());
                        zoomGroup.getChildren().add(internalSeed.getView());
                    }

                    internalSeed.setShape(seedShape);
                    seed = internalSeed;
                    strokeColor = Color.GREEN;
                }

                else {

                    if (externalSeed == null) {
                        externalSeed = new Seed();
                        externalSeed.setType(Seed.Type.Internal);
                        externalSeed.setView(new Group());
                        zoomGroup.getChildren().add(externalSeed.getView());
                    }

                    externalSeed.setShape(seedShape);
                    seed = externalSeed;
                    strokeColor = Color.BLUE;
                }

                seed.getView().getChildren().
                        removeAll(seed.getView().getChildren());


                if (seedShape == Seed.Shape.Circle) {
                    shape = new Circle();
                    ((Circle) shape).setCenterX(event.getX());
                    ((Circle) shape).setCenterY(event.getY());
                }

                else {
                    shape = new Rectangle();
                    ((Rectangle) shape).setX(event.getX());
                    ((Rectangle) shape).setY(event.getY());
                }

                shape.setFill(Color.TRANSPARENT);
                shape.setStroke(strokeColor);
                shape.setStrokeWidth(3.0);

                seed.getView().setMouseTransparent(true);
                seed.getView().getChildren().add(shape);

                tempPath = new Path();

                tempPath.setMouseTransparent(true);
                tempPath.setStrokeWidth(0.0);
                tempPath.setStroke(strokeColor);

                tempPath.getElements().add(
                        new MoveTo(event.getX(), event.getY()));

            }
        });

        canvas.setOnMouseDragged(new EventHandler<MouseEvent>() {

            public void handle(MouseEvent event) {

                if (canvas.getBoundsInLocal().contains(
                        event.getX(), event.getY())) {

                    tempPath.getElements().add(
                            new LineTo(event.getX(), event.getY()));

                    MoveTo p = (MoveTo) tempPath.getElements().get(0);

                    Double xIn = p.getX();
                    Double yIn = p.getY();

                    if (seedShape == Seed.Shape.Circle) {

                        Circle circle = (Circle) shape;
                        circle.setCenterX(event.getX());
                        circle.setCenterY(event.getY());

                        Double deltaX = xIn - event.getX();
                        Double deltaY = yIn - event.getY();
                        Double diameter = Math.sqrt(deltaX*deltaX + deltaY*deltaY);

                        circle.setRadius(diameter/2.0);
                    }

                    else if (seedShape == Seed.Shape.Square) {

                        Rectangle rectangle = (Rectangle) shape;
                        rectangle.setWidth(Math.abs(event.getX() - xIn));
                        rectangle.setHeight(Math.abs(event.getY() - yIn));

                    }

                }
            }
        });

        canvas.setOnMouseReleased(new EventHandler<MouseEvent>() {

            public void handle(MouseEvent event) {

                canvas.setOnMousePressed(null);
                canvas.setOnMouseDragged(null);

                tempPath = null;

                enableSeedButtons();

            }
        });

    }

    private void disableSeedButtons() {
        pickingSeed = true;

        zoomSlider.setDisable(true);
        inSeed.setDisable(true);
        outSeed.setDisable(true);
        clearSeedsButton.setDisable(true);
    }

    private void enableSeedButtons() {
        pickingSeed = false;

        zoomSlider.setDisable(false);
        inSeed.setDisable(false);
        outSeed.setDisable(false);
        clearSeedsButton.setDisable(false);
        runRegionGrowingButton.setDisable(false);

    }

    public void inSeedClicked(ActionEvent actionEvent) {

        seedBeingPicked = Seed.Type.Internal;

        disableSeedButtons();
        registerCanvasForMouseEvents();

    }

    public void outSeedClicked(ActionEvent actionEvent) {

        seedBeingPicked = Seed.Type.External;
        disableSeedButtons();
        registerCanvasForMouseEvents();
    }


    private void registerListenerForZoomSlider() {

        zoomSlider.setDisable(false);

        zoomSlider.valueProperty().addListener(new ChangeListener<Number>() {

            public void changed(ObservableValue<? extends Number> observable,
                                Number oldValue, Number newValue) {
                sliderZoomProperty.set(newValue.doubleValue());
            }
        });

        sliderZoomProperty.addListener(new InvalidationListener() {

            public void invalidated(Observable observable) {

                if (pickingSeed)
                    return;

                zoomGroup.setScaleX(sliderZoomProperty.get()/100);
                zoomGroup.setScaleY(sliderZoomProperty.get()/100);


            }
        });

    }

    private void registerScrollListener() {

        zoomGroup.addEventFilter(ScrollEvent.ANY, new EventHandler<ScrollEvent>() {

            public void handle(ScrollEvent event) {

                if (pickingSeed)
                    return;

                Double  blockIncrement = zoomSlider.getBlockIncrement();

                if (event.getDeltaY() > 0 &&
                        zoomSlider.getValue() + blockIncrement <= zoomSlider.getMax()) {

                    sliderZoomProperty.set(sliderZoomProperty.get() + blockIncrement);
                    zoomSlider.setValue(zoomSlider.getValue() + blockIncrement);
                }

                else if (event.getDeltaY() < 0 &&
                        zoomSlider.getValue() - blockIncrement >= zoomSlider.getMin()) {

                    sliderZoomProperty.set(sliderZoomProperty.get() - blockIncrement);
                    zoomSlider.setValue(zoomSlider.getValue() - blockIncrement);
                }

            }
        });

    }

    private void registerListenerForWindowingSliders() {

        minSlider.setDisable(false);
        maxSlider.setDisable(false);

        minSlider.valueProperty().addListener(new ChangeListener<Number>() {

            public void changed(ObservableValue<? extends Number> observable,
                                Number oldValue, Number newValue) {

                ImageProcessor ip = originalImage.getChannelProcessor();
                ip.setMinAndMax(newValue.doubleValue(), maxSlider.getValue());
                redrawCanvas();

            }
        });

        maxSlider.valueProperty().addListener(new ChangeListener<Number>() {

            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {

                ImageProcessor ip = originalImage.getChannelProcessor();
                ip.setMinAndMax(minSlider.getValue(), newValue.doubleValue());
                redrawCanvas();

            }
        });

    }

    private void resetWindowingSliders() {

        minSlider.adjustValue(0);
        maxSlider.adjustValue(255);

    }

    private void redrawCanvas() {

        Image currentImage = SwingFXUtils.
                toFXImage(originalImage.getBufferedImage(), null);

        canvas.setWidth(currentImage.getWidth());
        canvas.setHeight(currentImage.getHeight());

        canvas.getGraphicsContext2D().
                clearRect(0, 0, currentImage.getWidth(), currentImage.getHeight());

        canvas.getGraphicsContext2D().drawImage(currentImage, 0, 0);

    }

    private void redrawCanvas(BufferedImage imbuffered) {

        Image currentImage = SwingFXUtils.
                toFXImage(imbuffered, null);

        canvas.setWidth(currentImage.getWidth());
        canvas.setHeight(currentImage.getHeight());

        canvas.getGraphicsContext2D().
                clearRect(0, 0, currentImage.getWidth(), currentImage.getHeight());

        canvas.getGraphicsContext2D().drawImage(currentImage, 0, 0);

    }


    public void clearSeeds(ActionEvent actionEvent) {
        if (internalSeed != null) {
            getSeedImage(internalSeed).show();
        }
        clearPoints();

    }

    private void extractFeatures() {

        GLCMtexture internalSeedGLCM = new GLCMtexture();
        internalSeedGLCM.calcGLCM(
                getSeedImage(internalSeed).getChannelProcessor());

        GLCMtexture externalSeedGLCM = new GLCMtexture();
        externalSeedGLCM.calcGLCM(
                getSeedImage(internalSeed).getChannelProcessor());


        System.out.println("Semente interna");
        System.out.println("Entropia : " + internalSeedGLCM.getEntropy());
        System.out.println("Homogeniedade : " + internalSeedGLCM.getHomogeneity());
        System.out.println("Energia: " + internalSeedGLCM.getEnergy());
        System.out.println("Contraste: " + internalSeedGLCM.getContrast());
        System.out.println("Correlação: " + internalSeedGLCM.getCorrelation());

        System.out.println("");

        System.out.println("Semente interna");
        System.out.println("Entropia : " + externalSeedGLCM.getEntropy());
        System.out.println("Homogeniedade : " + externalSeedGLCM.getHomogeneity());
        System.out.println("Energia: " + externalSeedGLCM.getEnergy());
        System.out.println("Contraste: " + externalSeedGLCM.getContrast());
        System.out.println("Correlação: " + externalSeedGLCM.getCorrelation());
    }

    public void runRegionGrowing(ActionEvent actionEvent) {

        // run this on a new thread
        // ImagePlus internal = getSeedImage(internal_seed);
        // ImagePlus external = getSeedImage(external_seed);
        // call_to_method(originalImage, internal, external);

    }

    private void onRegionGrowingComplete(ImagePlus imagePlus) {

        // originalImage = imagePlus;
        // redraw_canvas();

    }


    private ImagePlus getSeedImage(Seed seed) {


        if (seed.getShape() == Seed.Shape.Square) {

            ImageProcessor ip = originalImage.getChannelProcessor();

            Rectangle rec = (Rectangle) seed.getView().getChildren().get(0);

            int x = (int) rec.getX();
            int y = (int) rec.getY();
            int w = (int) rec.getWidth();
            int h = (int) rec.getHeight();

            ip.setRoi(x, y, w, h);
            ImageProcessor ip2 = ip.crop();

            return new ImagePlus("", ip2);
        }

        else if (seed.getShape() == Seed.Shape.Circle) {

            ImageProcessor ip = originalImage.getChannelProcessor();

            Circle circ = (Circle) seed.getView().getChildren().get(0);

            int x = (int) (circ.getCenterX() - circ.getRadius());
            int y = (int) (circ.getCenterY() - circ.getRadius());
            int w = (int) (2*circ.getRadius());

            Ellipse2D.Double shapeRoi = new Ellipse2D.Double(x, y, w, w);

            ip.setRoi(new ShapeRoi(shapeRoi));
            ImageProcessor ip2 = ip.crop();

            return new ImagePlus("", ip2);

        }

        return null;

    }


    public void selectSeedShape(ActionEvent actionEvent) {

        Button button = (Button) actionEvent.getSource();

        if (button.getId().equals("circle"))
            seedShape = Seed.Shape.Circle;

        else if (button.getId().equals("square"))
            seedShape = Seed.Shape.Square;

    }

    public void teste(ActionEvent actionEvent) {

        minSlider.setDisable(false);
        maxSlider.setDisable(false);

//        BufferedImage img_convertida;
        this.SimpleGlobalTresholding();
    //        originalImage.getp


//                ImageProcessor ip = originalImage.getChannelProcessor();
////                ip.filter(MAX);
//                ip.setColor(java.awt.Color.DARK_GRAY);
//
//                this.showHistogramChart();
//                originalImage.setImage(img_convertida);
//
//                redrawCanvas(img_convertida);


    }



    public void min() {


        int maskWidth = Integer.parseInt("3");

        int edge = maskWidth / 2;
        int[][] newImage = new int[img_convertida.getWidth()][img_convertida.getHeight()];

        for(int i=edge; i<img_convertida.getWidth()-edge; i++){
            for(int j=edge; j<img_convertida.getHeight()-edge; j++){

                PriorityQueue<Integer> values = new PriorityQueue<Integer>();
                int v = 0;
                for(int x=i-edge; x<=i+edge; x++){
                    for(int y=j-edge; y<=j+edge; y++){

                        Pixel p = new Pixel(img_convertida.getRGB(x, y));
                        v = p.gray;
                        values.add(v);

                    }
                }

                v = values.element();

                newImage[i][j] = v;


            }
        }
        for(int i=edge; i<img_convertida.getWidth()-edge; i++){
            for(int j=edge; j<img_convertida.getHeight()-edge; j++){
                Pixel n = new Pixel(img_convertida.getRGB(i, j));
                int v = newImage[i][j];
                n.setRGB(v, v, v);
                img_convertida.setRGB(i, j, n.getComposedPixel());
            }
        }
        redrawCanvas(img_convertida);

        textArea.appendText("Min\n");
    }

    /*
     * O filtro de máxima realça regiões claras da imagem. Ela aumenta o tamanho de
     * objetos mais claros que o fundo e diminui o tamanho de objetos mais escuros
     * que o fundo em que estão inseridos.
     */
    public  void max() {

        int maskWidth = 3;

        int edge = maskWidth / 2;
        int[][] newImage = new int[img_convertida.getWidth()][img_convertida.getHeight()];

        for(int i=edge; i<img_convertida.getWidth()-edge; i++){
            for(int j=edge; j<img_convertida.getHeight()-edge; j++){

                PriorityQueue<Integer> values = new PriorityQueue<Integer>();
                int v = 0;
                for(int x=i-edge; x<=i+edge; x++){
                    for(int y=j-edge; y<=j+edge; y++){

                        Pixel p = new Pixel(img_convertida.getRGB(x, y));
                        v = p.gray;
                        values.add(v);

                    }
                }

                while(values.size()>1){
                    values.remove();
                }

                v = values.element();
                newImage[i][j] = v;


            }
        }

        for(int i=edge; i<img_convertida.getWidth()-edge; i++){
            for(int j=edge; j<img_convertida.getHeight()-edge; j++){
                Pixel n = new Pixel(img_convertida.getRGB(i, j));
                int v = newImage[i][j];
                n.setRGB(v, v, v);
                img_convertida.setRGB(i, j, n.getComposedPixel());
            }
        }

        this.showHistogramChart();
        redrawCanvas(img_convertida);

        textArea.appendText("Max\n");
    }



    /*
     * A equalização de histograma é utilizada para extender o contraste da imagem.
     * Basicamente ela redistribui as intensidades da imagem de maneira que ocupem
     * todo o espectro de intensidades.
     */
    public void HistogramEqualization() {

        int NUM_COLORS = 256;



        int[] histogram = new int[NUM_COLORS];
        int[] equalized = new int[NUM_COLORS];
        double[] probabilities = new double[NUM_COLORS];
        int numPixels = img_convertida.getWidth()*img_convertida.getHeight();

        for(int x=0; x<NUM_COLORS; x++){
            histogram[x] = 0;
            equalized[x] = 0;
            probabilities[x] = 0.0;
        }

        for(int i=0; i<img_convertida.getWidth(); i++){
            for(int j=0; j< img_convertida.getHeight(); j++){
                int p = img_convertida.getRGB(i, j);
                Pixel pixel = new Pixel(p);
                histogram[pixel.gray]++;
            }
        }

        probabilities[0] = (double)histogram[0] / (double)numPixels;
        for(int x=1; x<NUM_COLORS; x++){
            probabilities[x] = ((double)histogram[x] / (double)numPixels) + probabilities[x-1];
            //System.out.println(probabilities[x]);
        }

        double vMin = probabilities[0];
        double vMax = 1.0 - probabilities[0];

        for(int x=0; x<NUM_COLORS; x++){
            equalized[x] = (int)((((probabilities[x] - vMin) / vMax) * (double)(NUM_COLORS-1)) + 0.5);
            //System.out.println(equalized[x]);
        }

        for(int i=0; i<img_convertida.getWidth(); i++){
            for(int j=0; j< img_convertida.getHeight(); j++){
                int p = img_convertida.getRGB(i, j);
                Pixel pixel = new Pixel(p);
                int index = pixel.gray;
                int eq = equalized[index];
                pixel.setRGB(eq, eq, eq);
                img_convertida.setRGB(i, j, pixel.getComposedPixel());
            }
        }
        this.showHistogramChart();
        redrawCanvas(img_convertida);

        textArea.appendText("HistogramEqualization\n");
    }

    public void potency() {



        int c = 25;
        double gama = 0.4;

        for(int x=0; x<img_convertida.getWidth(); x++){
            for(int y=0; y<img_convertida.getHeight(); y++){
                int pixel = img_convertida.getRGB(x, y);
                Pixel p = new Pixel(pixel);
                double k = (double) p.gray;
                int value = (int) (c * Math.pow(k, gama));
                value = Math.min(255, Math.max(0, value));
                p.setRGB(value, value, value);
                img_convertida.setRGB(x, y, p.getComposedPixel());
            }
        }

        this.showHistogramChart();
        redrawCanvas(img_convertida);

        textArea.appendText("Potency\n");
    }


    /*
     * A limiarização global simples binariza a imagem escolhendo automaticamente
     * um limiar que irá dividir os pixels em dois grupos. Inicialmente o limiar
     * é a intensidade média da imagem.
     */

    public void SimpleGlobalTresholding(){

        try {
//            this.setFilteredImageName("limiarização_global");

            ImageProcessor imageProcessor = originalImage.getChannelProcessor();

            int[] h = imageProcessor.getHistogram();
            int treshold = Filter.getMean(h);
            int[] groups = new int[h.length];

            boolean done = false;
            while(!done){
                int m1 = 0;
                int g1 = 0;
                int m2 = 0;
                int g2 = 0;
                for(int i=0; i<h.length; i++){
                    if(i < treshold){
                        m1 += h[i]*i;
                        g1 += h[i];
                    } else{
                        m2 += h[i]*i;
                        g2 += h[i];
                    }
                }
                if(g1 != 0)
                    m1 = m1 / g1;
                if(g2 != 0)
                    m2 = m2 / g2;

                int newTreshold = (m1 + m2) / 2;
                int diff = Math.abs(newTreshold - treshold);
                if(diff < (newTreshold / 10)){
                    done = true;
                } else{
                    treshold = newTreshold;
                }
            }

            for(int i=0; i<img_convertida.getWidth(); i++){
                for(int j=0; j<img_convertida.getHeight(); j++){
                    Pixel n = new Pixel(img_convertida.getRGB(i, j));
                    int v = (n.gray > treshold) ? 255 : 0;
                    n.setRGB(v, v, v);
                    img_convertida.setRGB(i, j, n.getComposedPixel());
                }
            }

        } catch (Exception ex) {
            System.out.println( ex.getMessage());
        }

        this.showHistogramChart();
        redrawCanvas(img_convertida);

        textArea.appendText("SimpleGlobalTresholding\n");
    }


    public void Negativo() {
        img_convertida = new BufferedImage(img_convertida.getColorModel(), img_convertida.copyData(null), img_convertida.getColorModel().isAlphaPremultiplied(), null);


        //pegar coluna e linha da img_convertida
        int coluna = img_convertida.getWidth();
        int linha = img_convertida.getHeight();
        //laço para varrer a matriz de pixels da img_convertida
        for (int i = 0; i < coluna; i++) {
            for (int j = 0; j < linha; j++) {
                //rgb recebe o valor RGB do pixel em questão
                int rgb = img_convertida.getRGB(i, j);
                //a cor inversa é dado por 255 menos o valor da cor
                int r = 255 - (int) ((rgb & 0x00FF0000) >>> 16);
                int g = 255 - (int) ((rgb & 0x0000FF00) >>> 8);
                int b = 255 - (int) (rgb & 0x000000FF);
                java.awt.Color color = new java.awt.Color(r, g, b);

                img_convertida.setRGB(i, j, color.getRGB());

            }
        }

        redrawCanvas(img_convertida);

        textArea.appendText("Negativo\n");
    }

    public void reset(ActionEvent actionEvent) {

        if (originalImage == null) {
            registerScrollListener();
            registerListenerForZoomSlider();
            registerListenerForWindowingSliders();
        }

        ImageConverter  converter = new ImageConverter(originalImage);
        converter.convertToGray8();

        if (canvas == null) {
            canvas = new Canvas(originalImage.getWidth(), originalImage.getHeight());
            zoomGroup.getChildren().add(canvas);
        }

        clearPoints();

        resetWindowingSliders();

        redrawCanvas();

        showHistogramChart();

        enableSeedButtons();

        textArea.clear();
    }

    public void median(){

        int maskWidth = 3;
        int maskHeight = 3;

        int edge = maskWidth / 2;
        int[][] newImage = new int[img_convertida.getWidth()][img_convertida.getHeight()];

        for(int i=edge; i<img_convertida.getWidth()-edge; i++){
            for(int j=edge; j<img_convertida.getHeight()-edge; j++){

                PriorityQueue<Integer> values = new PriorityQueue<Integer>();
                int v = 0;
                for(int x=i-edge; x<=i+edge; x++){
                    for(int y=j-edge; y<=j+edge; y++){

                        Pixel p = new Pixel(img_convertida.getRGB(x, y));
                        v = p.gray;
                        values.add(v);

                    }
                }
                Integer[] valAux = new Integer[maskWidth * maskHeight];
                valAux = values.toArray(valAux);
                for(int k=0; k<((valAux.length+1) / 2)-1; k++){
                    values.remove();
                }

                v = values.element();
                newImage[i][j] = v;

            }
        }

        for(int i=edge; i<img_convertida.getWidth()-edge; i++){
            for(int j=edge; j<img_convertida.getHeight()-edge; j++){
                Pixel n = new Pixel(img_convertida.getRGB(i, j));
                int v = newImage[i][j];
                n.setRGB(v, v, v);
                img_convertida.setRGB(i, j, n.getComposedPixel());
            }
        }

        redrawCanvas(img_convertida);

        textArea.appendText("Median\n");
    }

    public void Sobel() {
        //imagem resultante
        BufferedImage ResultImage = new BufferedImage(img_convertida.getColorModel(), img_convertida.copyData(null), img_convertida.getColorModel().isAlphaPremultiplied(), null);
        //mascaras
        int[][] mascaraS1 = {{-1, -2, -1},
                {0, 0, 0},
                {1, 2, 1}};
        int[][] mascaraS2 = {{-1, 0, 1},
                {-2, 0, 2},
                {-1, 0, 1}};

        int r = 0, g = 0, b = 0;
        //tamanho da imagem
        int coluna = img_convertida.getWidth();
        int linha = img_convertida.getHeight();

        //percorre imagem
        for (int i = 1; i + 1 < linha; i++) {
            for (int j = 1; j + 1 < coluna; j++) {
                //percorre mascara
                for (int l = -1; l <= 1; l++) {
                    for (int k = -1; k <= 1; k++) {
                        //rgb
                        int rgb = img_convertida.getRGB(j + k, i + l);
                        //pegando os valores das cores primarias de cada pixel após a convolucao com a máscara
                        r += (mascaraS1[1 + l][1 + k] * (int) ((rgb & 0x00FF0000) >>> 16));
                        g += (mascaraS1[1 + l][1 + k] * (int) ((rgb & 0x0000FF00) >>> 8));
                        b += (mascaraS1[1 + l][1 + k] * (int) ((rgb & 0x000000FF)));
                    }

                }
                //arredondamento de valores
                java.awt.Color tempColor = new java.awt.Color(Math.min(255, Math.max(0, r)), Math.min(255, Math.max(0, g)), Math.min(255, Math.max(0, b)));
                ResultImage.setRGB(j, i, tempColor.getRGB());
                r = g = b = 0;
            }
        }
        //percorrer imagem
        for (int i = 1; i + 1 < img_convertida.getHeight(); i++) {
            for (int j = 1; j + 1 < img_convertida.getWidth(); j++) {
                //Percorrer máscara
                for (int l = -1; l <= 1; l++) {
                    for (int k = -1; k <= 1; k++) {
                        //RGB
                        int rgb = img_convertida.getRGB(j + k, i + l);
                        //pegando os valores das cores primarias de cada pixel após a convolucao com a máscara
                        r += (mascaraS2[1 + l][1 + k] * (int) ((rgb & 0x00FF0000) >>> 16));
                        g += (mascaraS2[1 + l][1 + k] * (int) ((rgb & 0x0000FF00) >>> 8));
                        b += (mascaraS2[1 + l][1 + k] * (int) ((rgb & 0x000000FF)));
                    }

                }
                //Arredondamento dos valores
                java.awt.Color tempColor = new java.awt.Color(Math.min(255, Math.max(0, r)), Math.min(255, Math.max(0, g)), Math.min(255, Math.max(0, b)));
                ResultImage.setRGB(j, i, tempColor.getRGB());
                r = g = b = 0;
            }
        }
        ResultImage.getSubimage(1, 1, coluna - 1, linha - 1);
        redrawCanvas(ResultImage);

        textArea.appendText("Sobel\n");
    }
}
