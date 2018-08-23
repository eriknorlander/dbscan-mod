package Structure;

import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionCodecFactory;
import org.apache.hadoop.io.compress.SplittableCompressionCodec;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import com.google.common.base.Charsets;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SessionInputFormat extends FileInputFormat<SessionWritable, SessionWritable> {

    public RecordReader<SessionWritable, SessionWritable>
    createRecordReader(InputSplit split,
                       TaskAttemptContext context) {
        String delimiter = context.getConfiguration().get(
                "textinputformat.record.delimiter");
        byte[] recordDelimiterBytes = null;
        if (null != delimiter)
            recordDelimiterBytes = delimiter.getBytes(Charsets.UTF_8);
        return new SessionRecordReader(recordDelimiterBytes, context);
    }

    protected boolean isSplitable(JobContext context, Path file) {
        final CompressionCodec codec =
                new CompressionCodecFactory(context.getConfiguration()).getCodec(file);
        if (null == codec) {
            return true;
        }
        return codec instanceof SplittableCompressionCodec;
    }

    public List<InputSplit> getSplits(JobContext job) throws IOException {
        long minSize = Math.max(getFormatMinSplitSize(), getMinSplitSize(job));
        long maxSize = getMaxSplitSize(job);

        // generate splits
        List<InputSplit> splits = new ArrayList<InputSplit>();
        List<FileStatus> files = listStatus(job);
        for (FileStatus file: files) {
            Path path = file.getPath();
            long length = file.getLen();
            if (length != 0) {
                BlockLocation[] blkLocations;
                if (file instanceof LocatedFileStatus) {
                    blkLocations = ((LocatedFileStatus) file).getBlockLocations();
                } else {
                    FileSystem fs = path.getFileSystem(job.getConfiguration());
                    blkLocations = fs.getFileBlockLocations(file, 0, length);
                }
                if (isSplitable(job, path)) {
                    long blockSize = file.getBlockSize();
                    long splitSize = computeSplitSize(blockSize, minSize, maxSize);

                    long bytesRemaining = length;
                    while (((double) bytesRemaining)/splitSize > 1.1) {
                        int blkIndex = getBlockIndex(blkLocations, length-bytesRemaining);
                        splits.add(makeSplit(path, length-bytesRemaining, splitSize,
                                blkLocations[blkIndex].getHosts()));
                        bytesRemaining -= splitSize;
                    }

                    if (bytesRemaining != 0) {
                        int blkIndex = getBlockIndex(blkLocations, length-bytesRemaining);
                        splits.add(makeSplit(path, length-bytesRemaining, bytesRemaining,
                                blkLocations[blkIndex].getHosts()));
                    }
                } else { // not splitable
                    splits.add(makeSplit(path, 0, length, blkLocations[0].getHosts()));
                }
            } else {
                //Create empty hosts array for zero length files
                splits.add(makeSplit(path, 0, length, new String[0]));
            }
        }
        // Save the number of input files for metrics/loadgen
        job.getConfiguration().setLong(NUM_INPUT_FILES, files.size());
        return splits;
    }
}