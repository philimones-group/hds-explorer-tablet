package mz.betainteractive.io.readers;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import mz.betainteractive.odk.storage.access.OdkStorageType;
import mz.betainteractive.odk.storage.access.anthonymandra.framework.XDocumentFile;

public class FileIO {
    private String filename;

    public FileIO(String filename) {
        this.filename = filename;
    }

    public String readFile() {
        try {
            return read(Files.newInputStream(Paths.get(this.filename)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private String read(InputStream inputStream) {
        try {
            final BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

            int read = 0;
            char[] buffer = new char[4096];
            String line;
            StringBuilder builder = new StringBuilder();
            while ((read = in.read(buffer)) != -1) {
                builder.append(buffer, 0, read);
            }
            in.close();

            return builder.toString();

        } catch (final IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String readFile(Context mContext, OdkStorageType odkStorageType, String uriFilename) {

        InputStream inputStream = null;

        try {

            if (odkStorageType == OdkStorageType.ODK_SCOPED_FOLDER_PROJECTS || odkStorageType == OdkStorageType.ODK_SCOPED_FOLDER_NO_PROJECTS) {
                //Log.d("urifilename", uriFilename);
                XDocumentFile odkFilesDocFile = XDocumentFile.fromUri(mContext, Uri.parse(uriFilename));
                ContentResolver mResolver = mContext.getContentResolver();
                inputStream = mResolver.openInputStream(odkFilesDocFile.getUri());
            } else {
                inputStream = Files.newInputStream(Paths.get(uriFilename));
            }

            return new FileIO("").read(inputStream);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
