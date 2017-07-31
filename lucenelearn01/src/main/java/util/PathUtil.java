package util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;

/**
 * Created by zwguo on 2017/7/31.
 */
public class PathUtil {
    private static PathUtil pathUtil = new PathUtil();

    private PathUtil() {
    }

    public static PathUtil getInstance() {
        return pathUtil;
    }

    /**
     * 获取当前source路径
     *
     * @return F:\kitesource\gitsource\lucenelearnkite
     */
    public String getCurrentSourceCodePath() {
        return System.getProperty("user.dir");
    }

    /**
     * 获取当前source路径
     *
     * @return /F:/kitesource/gitsource/lucenelearnkite/out/production/lucenelearn01/
     */
    public String getCurrentSourceCodePath2() {
        URL location = PathUtil.class.getProtectionDomain().getCodeSource().getLocation();
        return location.getFile();
    }

    /**
     * 获取当前source路径
     *
     * @return F:\kitesource\gitsource\lucenelearnkite\.
     */
    public String getCurrentSourceCodePath3() throws IOException {
        File currentDir = new File(new File(".").getAbsolutePath());
        //System.out.println(currentDir.getCanonicalPath()); //F:\kitesource\gitsource\lucenelearnkite
        //System.out.println(currentDir.getAbsolutePath()); //F:\kitesource\gitsource\lucenelearnkite\.
        return currentDir.getPath(); //F:\kitesource\gitsource\lucenelearnkite\.
    }

    public static void main(String[] args) throws IOException {
        System.out.println(PathUtil.getInstance().getCurrentSourceCodePath());
        System.out.println(PathUtil.getInstance().getCurrentSourceCodePath2());
        System.out.println(PathUtil.getInstance().getCurrentSourceCodePath3());
    }
}
