package simple;

import learn.KiteAnalyzer2;
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.*;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.io.StringReader;

/**
 * 展示token
 * Created by zwguo on 2017/8/1.
 */
public class TokenLearn {
    private static final String[] examples = {
            "The quick brown fox jumped over the lazy dog",
            "XY&Z Coporation - xyz@example.com"
    };

    private static final Analyzer[] analyzers = new Analyzer[]{
//            new WhitespaceAnalyzer(),
//            new SimpleAnalyzer(),
//            new StopAnalyzer(Version.LUCENE_34),
//            new StandardAnalyzer(Version.LUCENE_34),
            new KiteAnalyzer1()
//            new KiteAnalyzer2()
    };

    public static void main(String[] args) {
        String[] strings = examples;
        for (int i = 0; i < strings.length; i++) {
            analyze(strings[i]);
        }
    }

    /**
     * 分析
     *
     * @param text
     */
    public static void analyze(String text) {
        System.out.println("Analyzing \"" + text + "\"");
        for (Analyzer analyzer : analyzers) {
            String name = analyzer.getClass().getSimpleName();
            System.out.print(" " + name + ":");
            //displayTokens(analyzer, text);
            displayTokensWithFullDetails(analyzer, text);
            System.out.println();
        }
    }

    /**
     * 展示token
     *
     * @param analyzer
     * @param text
     */
    public static void displayTokens(Analyzer analyzer, String text) {
        displayTokens(analyzer.tokenStream("contents", new StringReader(text)));
    }

    /**
     * 展示token流的每个term
     *
     * @param stream
     */
    private static void displayTokens(TokenStream stream) {
        try {
            TermAttribute term = stream.addAttribute(TermAttribute.class);
            while (stream.incrementToken()) {
                System.out.print("[" + term.term() + "]");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 展示全部的细节
     *
     * @param analyzer
     * @param text
     */
    public static void displayTokensWithFullDetails(Analyzer analyzer, String text) {
        TokenStream stream = analyzer.tokenStream("contents", new StringReader(text));
        TermAttribute term = stream.addAttribute(TermAttribute.class);
        PositionIncrementAttribute positionIncrementAttribute = stream.addAttribute(PositionIncrementAttribute.class);
        OffsetAttribute offsetAttribute = stream.addAttribute(OffsetAttribute.class);
        TypeAttribute type = stream.addAttribute(TypeAttribute.class);

        int position = 0;
        try {
            System.out.println();
            while (stream.incrementToken()) {
                int increment = positionIncrementAttribute.getPositionIncrement();
                if (increment > 0) {
                    position = position + increment;
                    System.out.print(position + ": ");
                }
                System.out.println("[" + term.term() + ":" + offsetAttribute.startOffset() + "->" + offsetAttribute.endOffset() + ":" + type.type() + "]");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
