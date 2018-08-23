package Structure;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.Seekable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.CodecPool;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.SplitCompressionInputStream;
import org.apache.hadoop.io.compress.SplittableCompressionCodec;
import org.apache.hadoop.io.compress.CompressionCodecFactory;
import org.apache.hadoop.io.compress.Decompressor;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.util.LineReader;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

public class SessionRecordReader extends RecordReader<SessionWritable, SessionWritable> {
    private static final Log LOG = LogFactory.getLog(SessionRecordReader.class);
    public static final String MAX_LINE_LENGTH =
            "mapreduce.input.linerecordreader.line.maxlength";

    private long start;
    private long pos;
    private long end;
    private LineReader in;
    private FSDataInputStream fileIn;
    private Seekable filePosition;
    private int maxLineLength;
    private SessionWritable value = new SessionWritable();
    private SessionWritable key;
    private boolean isCompressedInput;
    private Decompressor decompressor;
    private byte[] recordDelimiterBytes;
    private TaskAttemptContext context;

    private Path file = null;
    private Configuration jc;

    public SessionRecordReader() {
    }

    public SessionRecordReader(byte[] recordDelimiter, TaskAttemptContext context) {
        this.recordDelimiterBytes = recordDelimiter;
        this.context = context;
    }

    public void initialize(InputSplit genericSplit,
                           TaskAttemptContext context) throws IOException {
        FileSplit split = (FileSplit) genericSplit;
        Configuration job = context.getConfiguration();

        file = split.getPath();
        jc = job;

        this.maxLineLength = job.getInt(MAX_LINE_LENGTH, Integer.MAX_VALUE);
        start = split.getStart();
        end = start + split.getLength();
        final Path file = split.getPath();

        // open the file and seek to the start of the split
        final FileSystem fs = file.getFileSystem(job);
        fileIn = fs.open(file);

        CompressionCodec codec = new CompressionCodecFactory(job).getCodec(file);
        if (null!=codec) {
            isCompressedInput = true;
            decompressor = CodecPool.getDecompressor(codec);
            if (codec instanceof SplittableCompressionCodec) {
                final SplitCompressionInputStream cIn =
                        ((SplittableCompressionCodec)codec).createInputStream(
                                fileIn, decompressor, start, end,
                                SplittableCompressionCodec.READ_MODE.BYBLOCK);
                if (null == this.recordDelimiterBytes){
                    in = new LineReader(cIn, job);
                } else {
                    in = new LineReader(cIn, job, this.recordDelimiterBytes);
                }

                start = cIn.getAdjustedStart();
                end = cIn.getAdjustedEnd();
                filePosition = cIn;
            } else {
                if (null == this.recordDelimiterBytes) {
                    in = new LineReader(codec.createInputStream(fileIn, decompressor),
                            job);
                } else {
                    in = new LineReader(codec.createInputStream(fileIn,
                            decompressor), job, this.recordDelimiterBytes);
                }
                filePosition = fileIn;
            }
        } else {
            fileIn.seek(start);
            if (null == this.recordDelimiterBytes){
                in = new LineReader(fileIn, job);
            } else {
                in = new LineReader(fileIn, job, this.recordDelimiterBytes);
            }

            filePosition = fileIn;
        }
        // If this is not the first split, we always throw away first record
        // because we always (except the last split) read one extra line in
        // next() method.
        if (start != 0) {
            start += in.readLine(new Text(), 0, maxBytesToConsume(start));
        }
        this.pos = start;
    }


    private int maxBytesToConsume(long pos) {
        return isCompressedInput
                ? Integer.MAX_VALUE
                : (int) Math.min(Integer.MAX_VALUE, end - pos);
    }

    private long getFilePosition() throws IOException {
        long retVal;
        if (isCompressedInput && null != filePosition) {
            retVal = filePosition.getPos();
        } else {
            retVal = pos;
        }
        return retVal;
    }

    private boolean toggle = false;
    private boolean read = false;

    public boolean nextKeyValue() throws IOException {
        if (!read) {
            // FileSystem fs = FileSystem.get(jc); // <- this works locally on windows and on hdfs
            FileSystem fs = file.getFileSystem(context.getConfiguration()); // <- this is to make it work with S3
            BufferedReader br = new BufferedReader(new InputStreamReader((fs.open(file))));

            try {
                br.skip(start);
                ArrayList<String> trackContents = new ArrayList<>();
                String line;
                line = br.readLine();
                long idx = start;
                while (line != null /*&& idx <= end*/) {
                    idx += line.length();
                    trackContents.add(line);
                    line = br.readLine();
                }

                key = new SessionWritable(trackContents);
            } catch(Exception e) {
                e.printStackTrace();
            } finally {
                br.close();
            }
            read = true;
        }

        toggle = !toggle;
        return toggle;
    }

    @Override
    public SessionWritable getCurrentKey() {
        return key;
    }

    @Override
    public SessionWritable getCurrentValue() {
        return value;
    }

    /**
     * Get the progress within the split
     */
    public float getProgress() throws IOException {
        if (start == end) {
            return 0.0f;
        } else {
            return Math.min(1.0f, (getFilePosition() - start) / (float)(end - start));
        }
    }

    public synchronized void close() throws IOException {
        try {
            if (in != null) {
                in.close();
            }
        } finally {
            if (decompressor != null) {
                CodecPool.returnDecompressor(decompressor);
            }
        }
    }
}