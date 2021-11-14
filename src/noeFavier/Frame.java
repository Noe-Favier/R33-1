package noeFavier;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.util.ArrayList;

public class Frame extends JFrame{
    private JPanel ContentPane;
    private JButton GOButton;
    private JButton nextButton;
    private JTextField tagField;
    private JPanel imgPanel;
    private JLabel imgLabel;
    public JLabel bufferSizeLabel;
    private JButton clearButton;
    private JCheckBox autoPlayCheckBox;
    private JSlider autoplayDelaySlider;
    private JLabel autoplayDelayDisplayerLabel;

    private static int indexBuffer = 0;
    private Timer refresher = null;
    private Timer autoplay = null;
    private Thread loader = null;
    public Frame(){
        initComponent();
        GOButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String tags = tagField.getText();
                loader = new Thread(new ImgLoader(getImgUrl(tags)));
                loader.start();
                try {
                    updatePlayer();
                }catch(Exception u){
                    //
                }

                if(refresher == null){
                    refresher = new Timer(500, LabelUpdater);
                    refresher.start();
                }

                GOButton.setEnabled(false);
                clearButton.setEnabled(true);
                autoplayDelaySlider.setEnabled(true);
                autoPlayCheckBox.setEnabled(true);
                autoplayDelayDisplayerLabel.setEnabled(true);
            }
        });

        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    updatePlayer();
                }catch(Exception u){
                }
            }
        });
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GOButton.setEnabled(true);
                clearButton.setEnabled(false);
                ImgLoader.STOP();

                autoplayDelaySlider.setEnabled(false);
                autoPlayCheckBox.setEnabled(false);
                autoplayDelayDisplayerLabel.setEnabled(false);
            }
        });
        autoPlayCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(autoPlayCheckBox.isSelected()){
                    //autoplay on
                    if(autoplay == null){
                        autoplay = new Timer(autoplayDelaySlider.getValue(), autoplayTrigger);
                    }
                    autoplay.start();
                }else{
                    //autoplay off
                    autoplay.stop();
                }
            }
        });
        autoplayDelaySlider.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentMoved(ComponentEvent e) {
                super.componentMoved(e);
                if(autoplay!=null) autoplay.setDelay(autoplayDelaySlider.getValue());
            }
        });
    }

    private ActionListener autoplayTrigger = new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
            try {
                updatePlayer();
            } catch (Exception e) {
                //
            }
        }
    };

    private void initComponent(){

    }

    private void updatePlayer() throws IOException {
        Image image = null;
        try {
            image = ImgBuffer.imgBuffer.get(indexBuffer);
            ImgBuffer.imgBuffer.remove(0);
        }catch(Exception e){
            System.out.println("erreur recuperation img depuis Buffer\n\t> "+e.getMessage());

        }
        double imageW = image.getWidth(null);
        double imageH = image.getHeight(null);
        double panelW = imgPanel.getWidth();
        double panelH = imgPanel.getWidth();
        double ratio = 1;

        if(imageW>=imageH && imageW >= panelW){
            ratio = panelW/imageW;
        }else if(imageH>=imageW && imageH >= panelH){
            ratio = panelH/imageH;
        }

        if(imageW*ratio > panelW){
            ratio*=panelW/imageW;
        }
        if(imageH*ratio > panelH){
            ratio*=panelH/imageH;
        }
        //System.out.println("\t> on a ratio = "+ratio + "\t> on a width = "+panelH+" | height = "+panelW +"\t> on a imgW = "+imageW+" | imgH = "+ imageH +"\t> au final, on fait : "+(int)((imageW)*ratio)+";"+(int)((imageH)*ratio));

        imgLabel.setIcon(new ImageIcon(image.getScaledInstance((int)(imageW*ratio)-1,(int)(imageH*ratio)-1,Image.SCALE_SMOOTH)));
    }

    private static ArrayList<String> getImgUrl(String tag){
        ArrayList<String> url = new ArrayList<String>();
        String fullUrl = "https://api.rule34.xxx/index.php?page=dapi&s=post&q=index&tags="+tag+"&limit=1000";
        System.out.println(fullUrl);
        try {
            Document doc = Jsoup
                    .connect(fullUrl)
                    .userAgent(
                            "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/27.0.1453.110 Safari/537.36")
                    .timeout(0).followRedirects(true).execute().parse();
            Elements titles = doc.select(".entrytitle");

            // print all available links on page
            Elements links = doc.select("post[file_url]");
            for (Element l : links) {
                url.add(l.attr("abs:file_url"));
            }
        }catch (Exception e){
            System.out.println("ERREUR"+e.getMessage());
        }

        return url;
    }

    private ActionListener LabelUpdater = new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
            bufferSizeLabel.setText(""+ImgBuffer.imgBuffer.size());
            autoplayDelayDisplayerLabel.setText("Delay = "+autoplayDelaySlider.getValue()/1000.0+" seconds");
        }
    };

    public static void main(String[] args) {
        JFrame frame = new JFrame("R33+1");
        frame.setContentPane(new Frame().ContentPane);
        frame.setResizable(false);
        frame.setSize(900,600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setSize(1000,1000);
        frame.setVisible(true);
    }

}
