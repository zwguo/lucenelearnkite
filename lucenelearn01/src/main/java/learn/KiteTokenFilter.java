package learn;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;

import java.io.IOException;

/**
 * 过滤器-仿照LowerCaseFilter
 * Created by zwguo on 2017/8/1.
 */
public class KiteTokenFilter extends TokenFilter {
    private final TermAttribute termAtt;

    public KiteTokenFilter(TokenStream input) {
        super(input);
        termAtt = this.addAttribute(TermAttribute.class);
    }

    @Override
    public boolean incrementToken() throws IOException {
        if (!this.input.incrementToken()) {
            return false;
        }
        char[] buffer = termAtt.termBuffer();
        if (buffer.length > 0 && buffer[0] == '2') {
            return incrementToken();
        }
        if (buffer.length > 3) {
            buffer[3] = 'T';
        }
        return true;
    }
}
