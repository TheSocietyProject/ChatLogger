import java.util.LinkedList;

public class LimitedQueue<E> extends LinkedList<E> {

    protected int max;

    public LimitedQueue(int max){
        super();
        this.max = max;
    }


    @Override
    public boolean add(E o) {
        boolean added = super.add(o);
        while (added && size() > max) {
            super.remove();
        }
        return added;
    }


    @Override
    public String toString() {
        String rV = "[\n";
        for(E s : this){
            rV += s.toString() + "\n";

        }

        return rV + "]";
    }
}



