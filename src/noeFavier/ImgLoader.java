package noeFavier;

import javax.imageio.ImageIO;
import java.net.URL;
import java.util.ArrayList;

public class ImgLoader implements Runnable{
    private ArrayList<String> urls;
    public static boolean exec = true;
    public ImgLoader(ArrayList<String> us){
        this.urls = us;
    }
    private int CAP = 40;
    public static void STOP(){
        exec = false;
    }

    @Override
    public void run() {
        String trhInfo = " : Thread > ";
        //System.out.println(trhInfo+"démarrage récup");
        String url;
        for(int i = 0; i< urls.size()-1 ; i++){
            url = urls.get(i);
            if(ImgBuffer.imgBuffer.size()<40) {
                if (!exec) {
                    System.out.println("STOP !");
                    ImgBuffer.imgBuffer.removeAll(ImgBuffer.imgBuffer);
                    exec = true;
                    break;
                }

                try {
                    ImgBuffer.imgBuffer.add(ImageIO.read(new URL(url)));
                    //System.out.println(trhInfo+"+Img Processed");
                } catch (Exception e) {
                    System.out.println(trhInfo + "+Error Processing Img\n\t> " + e.getMessage());
                }
            }else{
                i--;
            }
        }
    }
}
