package learn;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/**
 * 实验2
 * Created by zwguo on 2017/8/1.
 */
public class KiteTokenizer extends Tokenizer {

    private BufferedReader br;

    public KiteTokenizer(Reader input) {
        this.input = input;
        br = new BufferedReader(input);
    }

    /**
     * 必须以数字开头
     *
     * @return
     * @throws IOException
     */
    @Override
    public boolean incrementToken() throws IOException {
        TermAttribute term = this.addAttribute(TermAttribute.class);
        String line = br.readLine();
        while ((line == null && !isEnd(br)) || (line != null && !line.matches("\\d\\S*"))) {
            line = br.readLine();
        }
        if (line == null) {
            return false;
        }
        term.setTermBuffer(line);
        term.setTermLength(line.length());
        return true;
    }

    /**
     * 是否结束
     *
     * @param br
     * @return
     */
    private boolean isEnd(BufferedReader br) throws IOException {
        return br.read() < 0;
    }
}
