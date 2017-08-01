package simple;

import org.apache.lucene.analysis.*;
import org.apache.lucene.util.Version;

import java.io.Reader;
import java.util.Set;

/**
 * 自定义分析器-转成小写+
 * Created by zwguo on 2017/8/1.
 */
public class KiteAnalyzer1 extends Analyzer {

    private Set stopWords;

    public KiteAnalyzer1() {
        stopWords = StopAnalyzer.ENGLISH_STOP_WORDS_SET;
    }

    public KiteAnalyzer1(String[] sw) {
        stopWords = StopFilter.makeStopSet(sw);
    }

    /**
     * 真正的分词方法
     * @param s
     * @param reader
     * @return
     */
    @Override
    public TokenStream tokenStream(String s, Reader reader) {
        return new StopFilter(true, new LowerCaseFilter(new LetterTokenizer(reader)), stopWords);
        //return new LetterTokenizer(Version.LUCENE_34, reader);
    }
}
