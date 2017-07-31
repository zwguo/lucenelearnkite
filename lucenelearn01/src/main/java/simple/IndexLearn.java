package simple;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import util.PathUtil;

import java.io.*;
import java.util.*;

/**
 * index简单的创建等
 * Created by zwguo on 2017/7/31.
 */
public class IndexLearn {
    //第一次是true，如果改了结构，可以是true，否则是false
    private static final boolean CREATEINDEX = false;
    private static final Random RANDOM = new Random(System.currentTimeMillis());
    private static final String DATA_PATH;
    private static final String FIELD_ID = "id";
    private static final String FIELD_TITLE = "title";
    private static final String FIELD_CONTENT = "content";
    private static final String FIELD_COMMENTSCORE = "commentscore";
    private static final String FIELD_CATEGORY = "category";
    private static String[] CATEGORYNAMES = {"book", "car", "history", "computer", "cook", "bar", "beer"};

    static {
        String currentDir = PathUtil.getInstance().getCurrentSourceCodePath(); //F:\kitesource\gitsource\lucenelearnkite
        currentDir += "\\lucenelearn01\\src\\main\\resources\\data_IndexLearn";
        File file = new File(currentDir);
        if (!file.exists()) {
            file.mkdir();
        }
        DATA_PATH = currentDir;
    }

    public static void main(String[] args) {
        createIndex();
        //add sort
        Sort commentScoreSort = new Sort(new SortField(FIELD_COMMENTSCORE, SortField.LONG, true));
        Sort categorySort = new Sort(new SortField(FIELD_CATEGORY, SortField.STRING, true));
        Sort customSortWithCommentScore = new Sort(SortField.FIELD_SCORE, new SortField(FIELD_CATEGORY, getComparator(), false));

        boolean isExplain = false;
        //simpleSearch(FIELD_TITLE, "股价", 10, null, isExplain);
        //simpleSearch(FIELD_CATEGORY, "book", 5, commentScoreSort, isExplain);
        //simpleSearch(FIELD_CONTENT, "乐视", 5, categorySort, isExplain);
        //simpleSearch(FIELD_CONTENT, "乐视", 5, new Sort(SortField.FIELD_DOC), isExplain); //order by doc number
        //simpleSearch(FIELD_CONTENT, "乐视", 5, Sort.INDEXORDER, isExplain); //order by doc number
        //simpleSearch(FIELD_CONTENT, "乐视", 5, new Sort(SortField.FIELD_SCORE), isExplain); //order by relevance
        //simpleSearch(FIELD_CONTENT, "乐视", 5, Sort.RELEVANCE, isExplain); //order by relevance
        simpleSearch(FIELD_CONTENT, "乐视", 15, customSortWithCommentScore, isExplain);


    }

    /**
     * 获取自定义排序-按照CATEGORYNAMES顺序排序
     */
    private static FieldComparatorSource getComparator() {
        FieldComparatorSource customComparatorWithCommentScore = new FieldComparatorSource() {
            /**
             * 比较器，参考StringValComparator
             * @param fieldName 字段名
             * @param numHits
             * @param sortPos 排序维度排名，比如第一排序RELEVANCE-相关度，第二排序catgory
             * @param reversed 是否倒序
             * @return
             * @throws IOException
             */
            @Override
            public FieldComparator<String> newComparator(String fieldName, int numHits, int sortPos, boolean reversed) throws IOException {
                return new FieldComparator<String>() {
                    private String[] values = new String[numHits];
                    private String[] currentReaderValues;
                    private String bottom;
                    private Map<String, Integer> categoryAndSortMap = new HashMap<>();

                    {
                        for (int i = 0; i < CATEGORYNAMES.length; i++) {
                            categoryAndSortMap.put(CATEGORYNAMES[i], i);
                        }
                    }

                    public int compare(int slot1, int slot2) {
                        String val1 = this.values[slot1];
                        String val2 = this.values[slot2];
                        return val1 == null ? (val2 == null ? 0 : -1) : (val2 == null ? 1 : customCompare(val1, val2));
                    }

                    public int compareBottom(int doc) {
                        String val2 = this.currentReaderValues[doc];
                        return this.bottom == null ? (val2 == null ? 0 : -1) : (val2 == null ? 1 : customCompare(bottom, val2));
                    }

                    public void copy(int slot, int doc) {
                        this.values[slot] = this.currentReaderValues[doc];
                    }

                    public void setNextReader(IndexReader reader, int docBase) throws IOException {
                        this.currentReaderValues = FieldCache.DEFAULT.getStrings(reader, fieldName);
                    }

                    public void setBottom(int bottom) {
                        this.bottom = this.values[bottom];
                    }

                    public String value(int slot) {
                        return this.values[slot];
                    }

                    public int compareValues(String val1, String val2) {
                        return val1 == null ? (val2 == null ? 0 : -1) : (val2 == null ? 1 : customCompare(val1, val2));
                    }

                    /**
                     * 比较逻辑
                     * @param a
                     * @param b
                     * @return
                     */
                    private int customCompare(String a, String b) {
                        int asort = categoryAndSortMap.get(a);
                        int bsort = categoryAndSortMap.get(b);
                        return Integer.compare(asort, bsort);
                    }
                };
            }
        };
        return customComparatorWithCommentScore;
    }

    /**
     * 创建索引
     */
    public static void createIndex() {
        try {
            FSDirectory directory = FSDirectory.open(new File(DATA_PATH));
            IndexWriterConfig writeConfig = new IndexWriterConfig(Version.LUCENE_34, new StandardAnalyzer(Version.LUCENE_34));
            IndexWriter writer = new IndexWriter(directory, writeConfig);
            if (CREATEINDEX || writer.numDocs() == 0) {
                writer.deleteAll();
                System.out.println("创建索引...");
                List<Document> docs = generateDocs(100);
                for (int i = 0; i < docs.size(); i++) {
                    writer.addDocument(docs.get(i));
                }
                writer.commit();
                System.out.println("创建索引结束");
            }
            System.out.println("numDocs:" + writer.numDocs() + "\tmaxDoc:" + writer.maxDoc() + "\tnumRamDocs:" + writer.numRamDocs() + "\thasDeletions:" + writer.hasDeletions());
            writer.close();
            directory.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 搜索标题
     */
    public static void simpleSearch(String field, String searchText, int needCount, Sort sort, boolean isExplain) {
        try {
            FSDirectory directory = FSDirectory.open(new File(DATA_PATH));
            IndexReader reader = IndexReader.open(directory);
            IndexSearcher searcher = new IndexSearcher(reader);
            //add filter
            FieldSelector needFieldSelector = new FieldSelector() {
                @Override
                public FieldSelectorResult accept(String s) {
                    if (field.equalsIgnoreCase(s) || FIELD_ID.equalsIgnoreCase(s) || FIELD_COMMENTSCORE.equalsIgnoreCase(s) || FIELD_CATEGORY.equalsIgnoreCase(s)) {
                        return FieldSelectorResult.LOAD;
                    }
                    return FieldSelectorResult.NO_LOAD;
                }
            };
            //自定义排序时，是否要启用评分和最高分
            searcher.setDefaultFieldSortScoring(false, true);
            Query query = new QueryParser(Version.LUCENE_34, field, new StandardAnalyzer(Version.LUCENE_34)).parse(searchText);
            TopDocs topDocs = sort == null ? searcher.search(query, needCount) : searcher.search(query, needCount, sort);
            System.out.println("simpleSearch " + field + ":" + searchText + " needCount:" + needCount);
            System.out.println("\ttotal:" + topDocs.totalHits);
            String[] needFields = {FIELD_ID, field, FIELD_COMMENTSCORE, FIELD_CATEGORY};
            for (int i = 0; i < topDocs.scoreDocs.length; i++) {
                ScoreDoc sdc = topDocs.scoreDocs[i];
                Document doc = searcher.doc(sdc.doc, needFieldSelector);
                System.out.println("\tdocid:" + sdc.doc + "\tscore:" + sdc.score + "\t" + getDocString(doc, needFields));
            }
            //explain
            if (isExplain && topDocs.scoreDocs != null && topDocs.scoreDocs.length > 0) {
                Explanation explanation = searcher.explain(query, topDocs.scoreDocs[0].doc);
                System.out.println("Explain for 0 position:" + explanation);
            }
            searcher.close();
            reader.close();
            directory.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //SortField.FIELD_SCORE
    }

    /**
     * 描述一个doc
     *
     * @param doc
     * @param needFields
     * @return
     */
    private static String getDocString(Document doc, String[] needFields) {
        Set<String> fieldNames = new HashSet(Arrays.asList(needFields));
        List<Fieldable> fieldables = doc.getFields();
        StringBuilder bld = new StringBuilder(100);
        for (int i = 0; i < fieldables.size(); i++) {
            Fieldable field = fieldables.get(i);
            String fieldName = field.name();
            if (!fieldNames.contains(fieldName)) {
                continue;
            }
            String value = field.stringValue();
            bld.append(fieldName).append(":").append(value).append("|");
        }
        if (bld.length() <= 0) {
            return "";
        }
        return bld.toString();
    }

    /**
     * 生成随机的docs
     *
     * @param size
     * @return
     */
    public static List<Document> generateDocs(int size) {
        char[] characters = getTxtChars();
        List<Document> list = new ArrayList<Document>();

        Document doc = new Document();
        doc.add(new Field(FIELD_ID, String.valueOf(0), Field.Store.YES, Field.Index.NOT_ANALYZED));
        doc.add(new Field(FIELD_TITLE, "7月28日科技早间新闻：乐视系酷派被银行起诉要求偿还借款", Field.Store.YES, Field.Index.ANALYZED));
        doc.add(new Field(FIELD_CONTENT, "阿里巴巴市值一度突破4000亿美元，" +
                "据凤凰网报道，美国当地时间7月27日上午，阿里巴巴股价涨破155美元，总市值达到了4050亿美元。阿里巴巴正在提倡的新零售概念、它的全球化举动，以及从电商向数字娱乐、本地服务、线下实体的生态布局，都让投资者对它抱有期待。而阿里巴巴CFO武卫此前曾告诉投资者，集团在2018财年的收入指引增幅为“45%到49%”。不过截至当地时间27日收盘，阿里巴巴的股价又小幅回落，令其市值重回4000亿美元以内。\n" +
                "百度第二季度净利润6.51亿美元 同比增长82.9%",
                Field.Store.YES, Field.Index.ANALYZED));
        doc.add(new NumericField(FIELD_COMMENTSCORE, 4, Field.Store.YES, true).setLongValue(1 * 100));
        doc.add(new Field(FIELD_CATEGORY, CATEGORYNAMES[0], Field.Store.YES, Field.Index.NOT_ANALYZED));
        list.add(doc);

        for (int i = 1; i < size + 1; i++) {
            doc = new Document();
            doc.add(new Field(FIELD_ID, String.valueOf(i), Field.Store.YES, Field.Index.NOT_ANALYZED));
            doc.add(new Field(FIELD_TITLE, getRandomString(characters, 5 + RANDOM.nextInt(10)), Field.Store.YES, Field.Index.ANALYZED));
            doc.add(new Field(FIELD_CONTENT, getRandomString(characters, 10 + RANDOM.nextInt(30)), Field.Store.YES, Field.Index.ANALYZED));
            doc.add(new NumericField(FIELD_COMMENTSCORE, 4, Field.Store.YES, true).setLongValue(RANDOM.nextInt(100)));
            doc.add(new Field(FIELD_CATEGORY, CATEGORYNAMES[RANDOM.nextInt(CATEGORYNAMES.length)], Field.Store.YES, Field.Index.NOT_ANALYZED));
            list.add(doc);
        }
        return list;
    }

    /**
     * 获取随机的文本
     *
     * @param characters
     * @param length
     * @return
     */
    private static String getRandomString(char[] characters, int length) {
        int start = RANDOM.nextInt(characters.length - length);
        return new String(characters, start, length);
    }

    /**
     * 读取resources文本
     *
     * @return
     */
    private static char[] getTxtChars() {
        //读取文件字符
        final int bufferSize = 1024;
        final char[] buffer = new char[bufferSize];
        List<Character> charList = new ArrayList<Character>();
        try {
            InputStream inputStream = IndexLearn.class.getClassLoader().getResourceAsStream("contentdemo.txt");
            InputStreamReader reader = new InputStreamReader(inputStream, "UTF-8");
            while (true) {
                int readLength = reader.read(buffer, 0, buffer.length);
                if (readLength < 0) {
                    break;
                }
                for (int i = 0; i < readLength; i++) {
                    if (Character.isSpaceChar(buffer[i]) || Character.isISOControl(buffer[i]) || Character.isWhitespace(buffer[i])) {
                        continue;
                    }
                    charList.add(buffer[i]);
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        char[] allStringChars = new char[charList.size()];
        for (int i = 0; i < charList.size(); i++) {
            allStringChars[i] = charList.get(i).charValue();
        }
        return allStringChars;
    }
}
