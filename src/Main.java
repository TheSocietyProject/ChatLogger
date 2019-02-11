

import com.sasha.eventsys.SimpleEventHandler;
import com.sasha.eventsys.SimpleListener;
import com.sasha.reminecraft.ReMinecraft;
import com.sasha.reminecraft.api.RePlugin;
import com.sasha.reminecraft.api.event.ChatReceivedEvent;
import com.sasha.reminecraft.client.ReClient;
import com.sasha.reminecraft.logging.ILogger;
import com.sasha.reminecraft.logging.LoggerBuilder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main extends RePlugin implements SimpleListener {


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


        this.data.add(data);

    }



    /**
     *  Method to be called at a fixed rate,
     *  writes everything to a file and switches to a new file if needed
     */
    private void writeToFile() {


        Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString();

        String filename = s + "/data/ChatLog" + LocalDateTime.now().getYear() + "-" + LocalDateTime.now().getMonthValue() + "-" + LocalDateTime.now().getDayOfMonth() + ".txt";
        logger.log("[ChatLogger]: flushing data: " + data.size() + " in " + filename);
        try {

            File f = new File(filename);
            if(!f.exists()){
                new File(s + "\\data").mkdir();//.createNewFile();
            }
            FileWriter fw = new FileWriter(filename, true);
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


    private void testTabFile(){





    }

    @Override
    public void registerConfig() {

    }
}

