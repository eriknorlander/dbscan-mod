package Structure;

import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

public class SessionWritable implements WritableComparable<SessionWritable>, Serializable {
    private ArrayList<String> value;

    public SessionWritable() {}

    public SessionWritable(ArrayList<String> value) { set(value); }

    public void set(ArrayList<String> value) { this.value = value; }

    public String[] get() {
        return value.toArray(new String[value.size()]);
    }

    public boolean equals(Object o) {
        if (!(o instanceof SessionWritable))
            return false;
        SessionWritable other = (SessionWritable)o;
        return this.value == other.value;
    }

    public int compareTo(SessionWritable o) {
        int thisLength = value.size();
        int thatLength = o.value.size();
        return (thisLength<thatLength ? -1 : (thisLength==thatLength ? 0 : 1));
    }

    public static class Comparator extends WritableComparator {
        public Comparator() {
            super(SessionWritable.class);
        }

        public int compare(byte[] b1, int s1, int l1,
                           byte[] b2, int s2, int l2) {
            long thisValue = readLong(b1, s1);
            long thatValue = readLong(b2, s2);
            return (thisValue<thatValue ? -1 : (thisValue==thatValue ? 0 : 1));
        }
    }

    public static class DecreasingComparator extends Comparator {
        public int compare(WritableComparable a, WritableComparable b) {
            return -super.compare(a, b);
        }

        public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
            return -super.compare(b1, s1, l1, b2, s2, l2);
        }
    }

    static {
        WritableComparator.define(SessionWritable.class, new Comparator());
    }

    public void readFields(DataInput in) throws IOException {
        value = new ArrayList<>();
        for (String row : value) {
           value.add(in.readUTF());
        }
    }
    public void write(DataOutput out) throws IOException {
        Iterator vl = value.iterator();
        while (vl.hasNext()) {
            out.writeUTF((String) vl.next());
        }
    }
}