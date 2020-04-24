package com.reedelk.google.drive.v3.internal.command;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.FileList;
import com.reedelk.google.drive.v3.internal.commons.Mappers;
import com.reedelk.google.drive.v3.internal.exception.FileListException;
import com.reedelk.runtime.api.exception.PlatformException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class FileListCommand implements Command<List<Map>> {

    private final String nextPageToken;
    private final String driveId;
    private final String orderBy;
    private final String query;
    private final int pageSize;

    public FileListCommand(String driveId,
                           String orderBy,
                           String query,
                           String nextPageToken,
                           int pageSize) {
        this.nextPageToken = nextPageToken;
        this.pageSize = pageSize;
        this.driveId = driveId;
        this.orderBy = orderBy;
        this.query = query;
    }

    @Override
    public List<Map> execute(Drive drive) throws IOException {
        Drive.Files.List list = drive.files().list();
        list.setPageToken(nextPageToken);
        list.setPageSize(pageSize);
        list.setDriveId(driveId);
        list.setOrderBy(orderBy);
        list.setQ(query);

        FileList files = list.setFields("*").execute();

        return files.getFiles()
                .stream()
                .map(Mappers.FILE)
                .collect(toList());
    }

    @Override
    public PlatformException onException(Exception exception) {
        String error = exception.getMessage(); // TODO: Fixme
        return new FileListException(error, exception);
    }
}