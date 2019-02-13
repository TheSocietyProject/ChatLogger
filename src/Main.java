

import com.sasha.eventsys.SimpleEventHandler;
import com.sasha.eventsys.SimpleListener;
import com.sasha.reminecraft.Configuration;
import com.sasha.reminecraft.api.RePlugin;
import com.sasha.reminecraft.api.event.ChatReceivedEvent;
import com.sasha.reminecraft.client.ReClient;
import com.sasha.reminecraft.logging.ILogger;
import com.sasha.reminecraft.logging.LoggerBuilder;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main extends RePlugin implements SimpleListener {


    private Config CFG = new Config();

    private ArrayList<String> data = new ArrayList<>();

    public ILogger logger = LoggerBuilder.buildProperLogger("ChatLoggerPlugin");

    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);

    @Override
    public void onPluginInit() {
        this.getReMinecraft().EVENT_BUS.registerListener(this);
    }

    @Override
    public void onPluginEnable() {
        executor.scheduleAtFixedRate(() -> {
            if (ReClient.ReClientCache.INSTANCE.playerListEntries.size() != 0) {
                writeToFile();
            }
        }, 5L, 60L, TimeUnit.SECONDS);
    }

    @Override
    public void onPluginDisable() {

    }

    @Override
    public void onPluginShutdown() {

    }

    @Override
    public void registerCommands() {

    }

    @SimpleEventHandler
    public void onEvent(ChatReceivedEvent e){
        String msg = e.getMessageText();
        long time = e.getTimeRecieved();

        String data = "";

        data += time;
        data += ": ";
        data += msg;

        if(CFG.var_whatToDoWithSpam != 0)
            data = prepareIfSpam(data, CFG.var_whatToDoWithSpam);

        // if it is spam and should be left out its null so return if null
        if(data == null)
            return;


        this.data.add(data);

    }

    private String prepareIfSpam(String rV, int whatToDoWithSpam) {
        int spam = isSpam(rV);

        if(spam < 0) // only values < 0 r no spam
            return rV;

        if(whatToDoWithSpam == 1)
            return null;

        if(whatToDoWithSpam == 2) // P for pointer so u know what orig msg it was
            return "P(" + spam + "): " + rV.split(": ")[0];

        return rV;
    }

    /**
     *
     * @param rV
     * @return where the repeated msg is found
     */
    private int isSpam(String rV) {
        try {
            FileReader fr = new FileReader(getFilepath());
            BufferedReader br = new BufferedReader(fr);

            int length = getLength(fr);
            int index = CFG.var_howOldIsSpam;

            int toSkip = length - index;
            if(toSkip < 0){
                toSkip = 0;
            }
            br.skip(toSkip);

            while(index < length) {
                if(rV.equals(br.readLine().split(": ")[1])){

                    return index;
                }

            }



        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int getLength(FileReader input){
        int rV = 0;
        try {
            LineNumberReader count = new LineNumberReader(input);
            while (count.skip(Long.MAX_VALUE) > 0) {
                // Loop just in case the file is > Long.MAX_VALUE or skip() decides to not read the entire file
            }

            rV = count.getLineNumber() + 1;                                    // +1 because line index starts at 0
        } catch(Exception e){}
        return rV;
    }

    public String getFilepath(){

        Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString();

        String filename = s + "/data/ChatLog" + LocalDateTime.now().getYear() + "-" + LocalDateTime.now().getMonthValue() + "-" + LocalDateTime.now().getDayOfMonth() + ".txt";

        File f = new File(filename);
        if (!f.exists()) {
            new File(s + "\\data").mkdir();//.createNewFile();
        }

        return filename;
    }


    /**
     *  Method to be called at a fixed rate,
     *  writes everything to a file and switches to a new file if needed
     */
    private void writeToFile() {

        try{
            logger.log("[ChatLogger]: flushing data: " + data.size());
            FileWriter fw = new FileWriter(getFilepath(), true);
            BufferedWriter bw = new BufferedWriter(fw);
            for (String line : data) {
                bw.newLine();
                bw.write(line);
                bw.flush();
            }
            data = new ArrayList<>();

            fw.flush();
            bw.close();
            fw.close();
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    @Override
    public void registerConfig() {
        this.getReMinecraft().configurations.add(CFG);
    }
}


class Config extends Configuration {
    @ConfigSetting
    public int var_whatToDoWithSpam; // 0 does nothing, 1 removes it completely, 2 points to the msg that is repeated

    @ConfigSetting
    public int var_howOldIsSpam; // how long to go back to compare whether msg is repeated

    public Config() {
        super("ReMinecraft");

        this.var_whatToDoWithSpam = 2;
        this.var_howOldIsSpam = Integer.MAX_VALUE;
    }



}

