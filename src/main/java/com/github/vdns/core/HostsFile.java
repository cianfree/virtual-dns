package com.github.vdns.core;

/**
 * @author Arvin
 * @version 1.0
 * @since 2018/12/26 15:56
 */
public class HostsFile {

    private final String filePath;

    private final boolean possibleChange;

    public HostsFile(String filePath, boolean possibleChange) {
        this.filePath = filePath;
        this.possibleChange = possibleChange;



    }

    public String getFilePath() {
        return filePath;
    }

    public boolean isPossibleChange() {
        return possibleChange;
    }
}
