package learn;

import org.apache.lucene.analysis.ReusableAnalyzerBase;
import org.apache.lucene.analysis.Tokenizer;
import simple.TokenLearn;

import java.io.Reader;

/**
 * 实验2-模拟StopAnalyzer
 * Created by zwguo on 2017/8/1.
 */
public class KiteAnalyzer2 extends ReusableAnalyzerBase {

    @Override
    protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
        //return new TokenStreamComponents(new KiteTokenizer(reader));
        KiteTokenizer source = new KiteTokenizer(reader);
        return new TokenStreamComponents(source, new KiteTokenFilter(source));
    }

    public static void main(String[] args) {
        TokenLearn.displayTokensWithFullDetails(new KiteAnalyzer2(), "1text\r\n2text\r\ntext\r\n4text");
        System.out.println("end...");
    }
}
