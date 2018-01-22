 
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.*;
import javax.swing.event.*;
import java.util.*;
import java.util.Hashtable;
 
/*
 * FileChooserDemo.java uses these files:
 *   images/Open16.gif
 *   images/Save16.gif
 */
public class HomeOffsetSoftFix extends JPanel
                             implements ActionListener, ChangeListener {
    static private final String newline = "\n";
    JButton openButton, saveButton;
    JTextArea log;
    JFileChooser fc;
    JSlider slider;
    JLabel sliderValue;
    File instanceFile, fileHandler;
    public int layer=10;
     
    public HomeOffsetSoftFix() {
        super(new BorderLayout());
 
        //Create the log first, because the action listeners
        //need to refer to it.
        log = new JTextArea(5,20);
        log.setMargin(new Insets(5,5,5,5));
        log.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(log);
 
        //Create a file chooser
        fc = new JFileChooser();
 
        //Uncomment one of the following lines to try a different
        //file selection mode.  The first allows just directories
        //to be selected (and, at least in the Java look and feel,
        //shown).  The second allows both files and directories
        //to be selected.  If you leave these lines commented out,
        //then the default mode (FILES_ONLY) will be used.
        //
        //fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        //fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
 
        //Create the open button.  We use the image from the JLF
        //Graphics Repository (but we extracted it from the jar).
        openButton = new JButton("Select a File...",
                                 createImageIcon("Open16.gif"));
        openButton.addActionListener(this);
 
        //Create the save button.  We use the image from the JLF
        //Graphics Repository (but we extracted it from the jar).
        saveButton = new JButton("Run and Save...",
                                createImageIcon("Save16.gif"));
        saveButton.addActionListener(this);
        saveButton.setEnabled(false);
        
        //jslider in separate panel
        slider = new JSlider(JSlider.HORIZONTAL,1,20,10);
        slider.addChangeListener(this);
        
        sliderValue = new JLabel(Integer.toString(layer));
        //sliderValue.setEditable(false);
        
        JPanel sliderPanel = new JPanel();
        sliderPanel.add(slider);
        sliderPanel.add(sliderValue);
        

 
        //For layout purposes, put the buttons in a separate panel
        JPanel buttonPanel = new JPanel(); //use FlowLayout
        buttonPanel.add(openButton);
        buttonPanel.add(saveButton);
         
        //Add the buttons and the log to this panel.
        add(buttonPanel, BorderLayout.PAGE_START);
        add(sliderPanel, BorderLayout.CENTER);
        add(logScrollPane, BorderLayout.SOUTH);
    }
    
    public int getLayerValue(){
      return layer;
    }
    public void writeLayerValue(int i){
      layer = i;
    }
    
    public void stateChanged(ChangeEvent ec) {
      JSlider source = (JSlider)ec.getSource();
      if (!source.getValueIsAdjusting()) {
        writeLayerValue((int)source.getValue());
        sliderValue.setText(Integer.toString(layer));
      }    
    }
    
    public void actionPerformed(ActionEvent e){
 
        //Handle open button action.
        if (e.getSource() == openButton) {
            int returnVal = fc.showOpenDialog(HomeOffsetSoftFix.this);
            
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                if(file.canRead()== false){
                  log.append("Cannot read file. Select another.");
                }
                instanceFile = file;
                saveButton.setEnabled(true);
                //This is where a real application would open the file.
                log.append("Opening: " + file.getName() + "." + newline);
            } else {
                log.append("Open command cancelled by user." + newline);
            }
            log.setCaretPosition(log.getDocument().getLength());
            

        //Handle save button action.
        } else if (e.getSource() == saveButton) {
            try{
            int returnVal = fc.showSaveDialog(HomeOffsetSoftFix.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File filen = fc.getSelectedFile();
                
                //This is where a real application would save the file.
                runProgram(instanceFile, filen, layer);
                log.append("Saving: " + filen.getName() + "." + newline);
            } else {
                log.append("Save command cancelled by user." + newline);
            }
            log.setCaretPosition(log.getDocument().getLength());
            }catch(FileNotFoundException ex){
               log.append("FileNotFoundException. Try again.");
            }
        }
    }
 
    // Returns an ImageIcon, or null if the path was invalid. 
    protected static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = HomeOffsetSoftFix.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("Home Gcode editor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
        //Add content to the window.
        frame.add(new HomeOffsetSoftFix());
 
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
    
    public void runProgram(File input, File ps, int wLayers)throws FileNotFoundException{
      int lineCount = 0;
      String LAYER = ";LAYER:";
      Scanner psc = new Scanner(input); // scanner for running through from the beginning while printing new file
      Scanner handlersc;    //scanner to break apart tokens from the handler
      String[] modLines={""};    //array holding each layer that we will be modifying 
      String handler = "";  //string that holds each line
      String handlerH = ""; //string that handles each token of the line
      String count = "count:";        
      double x=0,y=0;
      PrintStream output = new PrintStream(ps);
      
       // run through file and print output file while  modifying where needed
       int p = 0;
       boolean b = false;
       while(psc.hasNext()){
         handler = psc.nextLine();
         handlersc = new Scanner(handler);
         if(b){
            if (handler.equals(modLines[p])){
               output.println(modLines[p]);
               output.println("G28 X0 Y0");
               output.println("G0 X" + x + " Y" + y);
               p++;
            }else{
               while(handlersc.hasNext()){
                  handlerH = handlersc.next();
                  if(handlerH.charAt(0)=='X'){
                     x = Double.parseDouble(handlerH.substring(1));
                  }else if(handlerH.charAt(0) == 'Y'){
                     y = Double.parseDouble(handlerH.substring(1));
                  }
               }
               output.println(handler);
            }
         }else{
            while(handlersc.hasNext()){
                  handlerH = handlersc.next();
               if(handlerH.equals(count)){
                  lineCount = handlersc.nextInt();
                  //create array of strings to search for
                  modLines = new String[lineCount/wLayers];
                  int modLayers = wLayers;
                  for(int i = 0; i < modLines.length; i++){
                     modLines[i] = LAYER + modLayers;
                     modLayers += wLayers;
                  }
                  b=true;
               }
            }
            output.println(handler);
         }
      }
      output.close();
    }
    
 
    public static void main(String[] args) {
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                //Turn off metal's use of bold fonts
                UIManager.put("swing.boldMetal", Boolean.FALSE); 
                createAndShowGUI();
            }
        });
    }
}
