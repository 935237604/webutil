package com.joe.web.starter.core;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.CodeSource;
import java.util.Arrays;

/**
 * DocumentRootHelper，帮助查找doc-root
 *
 * @author joe
 * @version 2018.05.24 16:40
 */
@Slf4j
public class DocumentRootHelper {
    private static final String DEFAULT_DOC_ROOT = Thread.currentThread().getContextClassLoader().getResource("")
            .getFile();
    private static final String[] COMMON_DOC_ROOTS = {"src/main/webapp", "public",
            "static"};


    /**
     * 获取当前系统的doc-root（spring自带的在IDEA中运行时有问题，不能正确找到doc-root）
     *
     * @return 当前系统的doc-root
     */
    public static final File getValidDocumentRoot() {
        // If document root not explicitly set see if we are running from a war archive
        File file = getWarOrJarFileDocumentRoot();
        // If not a war archive maybe it is an exploded war
        file = file != null ? file : getExplodedWarFileDocumentRoot();
        file = file != null ? file : getIDEDocumentRoot();
        if (file == null) {
            log.debug("None of the document roots " + Arrays.asList(COMMON_DOC_ROOTS)
                    + " point to a directory and will be ignored.");
        } else {
            log.debug("Document root: " + file);
        }
        return file;
    }

    /**
     * 获取war或者jar文件
     *
     * @return war文件
     */
    private static File getWarOrJarFileDocumentRoot() {
        File file = getArchiveFileDocumentRoot(".war");
        return file == null ? getArchiveFileDocumentRoot(".jar") : file;
    }

    /**
     * @return
     */
    private static File getExplodedWarFileDocumentRoot() {
        return getExplodedWarFileDocumentRoot(getCodeSourceArchive());
    }

    /**
     * 当用户在IDE中运行系统时该方法会生效
     *
     * @return doc-root
     */
    private static File getIDEDocumentRoot() {
        File docRoot = new File(DEFAULT_DOC_ROOT);
        if (docRoot.exists()) {
            log.debug("当前在IDE中运行");
            return docRoot;
        } else {
            log.debug("当前没有在IDE中运行");
            return null;
        }
    }

    private static File getArchiveFileDocumentRoot(String extension) {
        File file = getCodeSourceArchive();
        log.debug("Code archive: " + file);
        if (file != null && file.exists() && !file.isDirectory()
                && file.getName().toLowerCase().endsWith(extension)) {
            return file.getAbsoluteFile();
        }
        return null;
    }

    /**
     * 获取解压后的代码对应的doc-root目录
     *
     * @param codeSourceFile 当前代码所处的文件
     * @return 解压后的代码对应的doc-root目录
     */
    private static File getExplodedWarFileDocumentRoot(File codeSourceFile) {
        log.debug("Code archive: " + codeSourceFile);
        if (codeSourceFile != null && codeSourceFile.exists()) {
            String path = codeSourceFile.getAbsolutePath();
            int webInfPathIndex = path.indexOf(File.separatorChar + "WEB-INF" + File.separatorChar);
            if (webInfPathIndex >= 0) {
                path = path.substring(0, webInfPathIndex);
                return new File(path);
            }
        }
        return null;
    }

    /**
     * 获取当前代码的文件
     *
     * @return 当前代码所处的文件
     */
    private static File getCodeSourceArchive() {
        try {
            CodeSource codeSource = DocumentRootHelper.class.getProtectionDomain().getCodeSource();
            URL location = (codeSource == null ? null : codeSource.getLocation());
            if (location == null) {
                return null;
            }
            String path = location.getPath();
            URLConnection connection = location.openConnection();
            if (connection instanceof JarURLConnection) {
                path = ((JarURLConnection) connection).getJarFile().getName();
            }
            if (path.indexOf("!/") != -1) {
                path = path.substring(0, path.indexOf("!/"));
            }
            return new File(path);
        } catch (IOException ex) {
            return null;
        }
    }
}
