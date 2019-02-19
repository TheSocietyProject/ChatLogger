

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

    private ArrayList<ChatMessage> data = new ArrayList<>();

    private LimitedQueue<ChatMessage> testForSpam = new LimitedQueue<>(CFG.var_howOldIsSpam);

    private ArrayList<ChatMessage> spam = new ArrayList<>();


    public ILogger logger = LoggerBuilder.buildProperLogger("ChatLoggerLog");

    private ScheduledExecutorService executor;

    @Override
    public void onPluginInit() {

        save("start: " + System.currentTimeMillis());


        this.getReMinecraft().EVENT_BUS.registerListener(this);


        Runtime.getRuntime().addShutdownHook(new Thread(() -> {


            save("shutdown: " + System.currentTimeMillis());
        }));

    }

    @Override
    public void onPluginEnable() {
        executor = Executors.newScheduledThreadPool(2);
        executor.scheduleAtFixedRate(() -> {
            if (ReClient.ReClientCache.INSTANCE.playerListEntries.size() != 0) {
                handleData();
            }
        }, 1L, 3L, TimeUnit.MINUTES);
    }

    public synchronized void handleData(){

        if(CFG.var_saveAsFile)
            writeToFile();

        if(CFG.var_saveInDatabase)
            saveInDatabase();

        if(CFG.var_outputInDiscord)
            outputInDiscord();

    }

    public void saveInDatabase(){
        // TODO
    }

    public void outputInDiscord(){
        // TODO

    }

    @Override
    public void onPluginDisable() {
        executor.shutdownNow();
    }

    @Override
    public void onPluginShutdown() {
        this.getReMinecraft().EVENT_BUS.deregisterListener(this);
    }

    @Override
    public void registerCommands() {

    }

    @SimpleEventHandler
    public void onEvent(ChatReceivedEvent e){

        ChatMessage data = prepareIfSpam(new ChatMessage(e));

        // if it is spam and should be left out its null so return if null
        if(data == null)
            return;


        this.data.add(data);

        this.testForSpam.add(data);

    }

    private ChatMessage prepareIfSpam(ChatMessage msg) {

        if(CFG.var_whatToDoWithSpam == 0)
            return msg;

        ChatMessage spam = isSpam(msg);

        if(spam == null) // only values < 0 r no spam
            return msg;

        if(CFG.var_whatToDoWithSpam == 1)
            return null;

        if(CFG.var_whatToDoWithSpam == 2) // P for pointer so u know what orig msg it was
            return msg.setLink(spam.time + "");


        return msg;
    }

    /**
     *
     * @param msg
     * @return where the repeated msg is found
     */
    private ChatMessage isSpam(ChatMessage msg) {

        for(int i = 0; i < spam.size(); i ++) {
            if(msg.equals(spam.get(i)))
                return spam.get(i);
        }

        for(int i = 0; i < testForSpam.size(); i ++) {
            if(msg.equals(testForSpam.get(i))){
                spam.add(testForSpam.get(i));
                return testForSpam.get(i);
            }
        }

        return null;
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



    public void save(String data){
        try{
            FileWriter fw = new FileWriter(getFilepath(), true);
            BufferedWriter bw = new BufferedWriter(fw);

            bw.newLine();
            bw.write(data);

            bw.flush();
            fw.flush();
            bw.close();
            fw.close();
        } catch (IOException e) {

            e.printStackTrace();
        }
    }


    /**
     *  Method to be called at a fixed rate,
     *  writes everything to a file and switches to a new file if needed
     */
    private synchronized void writeToFile() {

        try{
            logger.log("[ChatLogger]: flushing data: " + data.size());
            FileWriter fw = new FileWriter(getFilepath(), true);
            BufferedWriter bw = new BufferedWriter(fw);
            for (ChatMessage line : data) {
                bw.newLine();
                bw.write(line.toString());
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

    @ConfigSetting
    public boolean var_saveAsFile;


    // v these are var configs for michu`s commit
    @ConfigSetting
    public boolean var_saveInDatabase;

    @ConfigSetting
    public boolean var_outputInDiscord;



    public Config() {
        super("ChatLogger");

        this.var_whatToDoWithSpam = 2;
        this.var_howOldIsSpam = Integer.MAX_VALUE;

        this.var_saveAsFile = true;
        this.var_saveInDatabase = true;
        this.var_outputInDiscord = false;
    }



}

