import com.sasha.reminecraft.api.event.ChatReceivedEvent;

public class Msg{

    public static final String separation = ": ";

    public Msg(ChatReceivedEvent e){
        this.msg = e.getMessageText();
        this.time = e.getTimeRecieved();
        this.extra0 = "";
        this.extra1 = "";
    }


    public String extra0, extra1;

    public String msg;
    public long time;

    @Override
    public String toString() {
        return extra0 + time + extra1 + Msg.separation + msg;
    }


    public Msg setExtra(String e0, String e1){
        this.extra0 = e0;
        this.extra1 = e1;
        return this;
    }

    public Msg setMsg(String nevMsg){
        this.msg = nevMsg;
        return this;
    }


    public boolean equals(Msg toComp) {
        return this.msg.equals(toComp.msg);
    }
}
