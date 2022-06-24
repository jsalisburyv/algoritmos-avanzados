/*
    Algoritmes Avançats - Capitulo 7
    Autors:
        Jonathan Salisbury Vega
        Joan Sansó Pericàs
        Joan Vilella Candia
*/
package model;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * Class that contains the methods that simulate the various complexities.
 */
public class Model {
    
    /* Constants */
    private static final File BD_FILE = new File("data/bd.dat");
    private static final FlagColor[] COLORS = FlagColor.values();
    
    /* Variables */
    private PropertyChangeSupport propertyChangeSupport;
    private BufferedImage flag;
    private String country;
    private HashMap<String, double[]> database;
    
    public Model(){
        propertyChangeSupport = new PropertyChangeSupport(this);
    }
    
    /* Methods */
    public void loadDatabase(File dir, final int N_TESTS){
        if(database != null) return;
        if(BD_FILE.exists()){
            // leer
            firePropertyChange("creating DB", null, false);
            System.out.println("Reading DB...");
             try(ObjectInputStream reader = new ObjectInputStream(new FileInputStream(BD_FILE))){
                 database = (HashMap<String, double[]>) reader.readObject();
             } catch(IOException | ClassNotFoundException ex) {
                 System.err.println(ex.getMessage());
             }
       } else {
            // crear
            System.out.println("Creating DB...");
            firePropertyChange("creating DB", null, true);
            database = new HashMap<>();
            File[] files = dir.listFiles();
            for(File file: files){
                System.out.println(file.getName());
                loadFlag(file);
                double[] perc = getRealPercentages();
                Locale loc = new Locale("en", file.getName().replace(".png", ""));
                country = loc.getDisplayCountry();
                database.put(country, perc);
            }
            // guardar
            System.out.println("Writing DB...");
            try(ObjectOutputStream writer = new ObjectOutputStream(new FileOutputStream(BD_FILE))){
                writer.writeObject(database);
            } catch(IOException ex) {
                System.err.println(ex.getMessage());
            }
        }
    }
    
    public void loadFlag(File flagFile){
        try {
            flag = ImageIO.read(flagFile);
            Locale loc = new Locale("en", flagFile.getName().replace(".png", ""));
            country = loc.getDisplayCountry();
        } catch (IOException ex) {
            Logger.getLogger(Model.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public double[] getColorPercentages(final int N_TESTS){
        if(flag == null || database == null) return null;
        double x, y;
        int[] colorCount = new int[COLORS.length];
        for (int i = 0; i < N_TESTS; i++) {
            x = Math.random() * flag.getWidth();
            y = Math.random() * flag.getHeight();
            Color color = new Color(flag.getRGB((int) x, (int) y));
            int pos = getClosestColorIndex(color);
            colorCount[pos]++;
        }
        double[] percentages = new double[COLORS.length];
        for (int i = 0; i < colorCount.length; i++) {
            percentages[i] = (double) colorCount[i]/N_TESTS;
        }
        return percentages;
    }
    
    public String findCountry(double[] percentages){
        country = null;
        double min = Double.MAX_VALUE, dist;
        for (Map.Entry<String, double[]> entry : database.entrySet()) {
            double[] countryPerc = entry.getValue();
            dist = distanceBetweenColorPercentages(percentages, countryPerc);
            if(dist < min){
                 min = dist;
                 country = entry.getKey();
            }
        }
        return country;
    }
    
    public File getRandomFlag(File dir){
        File[] files = dir.listFiles();
        Random rand = new Random();
        File flagFile = files[rand.nextInt(files.length)];
        return flagFile;
    }
    
    public BufferedImage getFlagImage(){
        return this.flag;
    }
    
    public String getCountryName(){
        return this.country;
    }
    
    public BufferedImage getFlagImage(String countryName){
        Map<String, String> countries = new HashMap<>();
        for (String iso : Locale.getISOCountries()) {
            Locale l = new Locale("en", iso);
            countries.put(l.getDisplayCountry(), iso);
        }
        BufferedImage img = null;
        try {
            String name = countries.get(countryName);
            String fileName = "resources/flags/" + name + ".png";
            img = ImageIO.read(new File(fileName));
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
        return img;
    }
    
    private int getClosestColorIndex(java.awt.Color color){
        double min = Double.MAX_VALUE, dist;
        int pos = -1;
        for(int i = 0; i < COLORS.length; i++){
            dist = COLORS[i].distanceToColor(color);
            if(dist < min){
                min = dist;
                pos = i;
            }
        }
        return pos;
    }
    
    private Double distanceBetweenColorPercentages(double[] flag, double[] country){
        if(flag.length != country.length) return null;
        double[] diff = new double[flag.length];
        double sum = 0;
        for (int i = 0; i < diff.length; i++) {
            diff[i] = country[i] - flag[i];
            diff[i] *= diff[i];
            sum += diff[i];
        }
        return Math.sqrt(sum);
    }
    
    private double[] getRealPercentages() {
        int[] colorCount = new int[COLORS.length];
        for (int i = 0; i < flag.getWidth(); i++) {
            for (int j = 0; j < flag.getHeight(); j++) {
                Color color = new Color(flag.getRGB(i, j));
                int pos = getClosestColorIndex(color);
                colorCount[pos]++;
            }
        }
        double[] percentages = new double[COLORS.length];
        for (int i = 0; i < colorCount.length; i++) {
            percentages[i] = (double) colorCount[i]/(flag.getWidth()*flag.getHeight());
        }
        return percentages;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }
}
