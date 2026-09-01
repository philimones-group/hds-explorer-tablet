package mz.betainteractive.io.writers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by paul on 8/30/16. - version 1.0
 */
public class ZipMaker {

    private File zipFile;
    private List<File> filesToZip;

    public ZipMaker(String zipFile){
        this(new File(zipFile));
    }

    public ZipMaker(File zipFile){
        this.zipFile = zipFile;
        this.filesToZip = new ArrayList<File>();
    }

    public ZipMaker(File zipFile, File fileToZip){
        this(zipFile);
        addFile(fileToZip);
    }

    public ZipMaker(String zipFile, String fileToZip){
        this(zipFile);
        addFile(fileToZip);
    }

    public ZipMaker(File zipFile, Collection<? extends File> filesToZip){
        this(zipFile);
        addFiles(filesToZip);
    }

    public ZipMaker(String zipFile, List<String> filesToZip){
        this(zipFile);
        addFiles(filesToZip);
    }

    public void addFile(File file){
        this.filesToZip.add(file);
    }

    public void addFile(String file){
        this.filesToZip.add(new File(file));
    }

    public void addFiles(Collection<? extends File> files){
        this.filesToZip.addAll(files);
    }

    public void addFiles(List<String> files){
        for (String file : files){
            addFile(file);
        }
    }

    public boolean makeZip(){
        return xmakeZip(this.zipFile, this.filesToZip);
    }

    private boolean xmakeZip(File zipFile, List<File> sourceFiles) {

        Map<String, String> files = GetAllFiles(sourceFiles);

        try {

            //create byte buffer
            byte[] buffer = new byte[200 * 1024];

            FileOutputStream fout = new FileOutputStream(zipFile);
            ZipOutputStream zout = new ZipOutputStream(fout);


            for (String sourceFile : files.keySet()) {

                String zipEntryFile = files.get(sourceFile);

                FileInputStream fin = new FileInputStream(sourceFile);
                zout.putNextEntry(new ZipEntry(zipEntryFile));

                int length;

                while ((length = fin.read(buffer)) > 0) {
                    zout.write(buffer, 0, length);
                }

                fin.close();
            }

            zout.closeEntry();
            zout.close();

            return true;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;

    }

    private Map<String, String> GetAllFiles(List<File> sourceFiles) {
        Map<String, String> map = new HashMap<String, String>();

        for (File file : sourceFiles) {
            if (file.isFile()){
                map.put(file.toString(), file.getName());
                continue;
            }

            if (file.isDirectory()){
                String path = file.getParent().toString();
                Map<String, String> map2 = getFiles(path, file.listFiles());

                if (map2.size()>0){
                    map.putAll(map2);
                }

            }

        }

        return map;
    }

    private Map<String, String> getFiles(String path, File[] listFiles) {
        Map<String, String> map = new HashMap<String, String>();

        for (File file : listFiles) {
            if (file.isFile()){
                map.put(file.toString(), file.toString().replaceFirst(path+File.pathSeparator, ""));
                continue;
            }

            if (file.isDirectory()){
                Map<String, String> map2 = getFiles(path, file.listFiles());

                if (map2.size()>0){
                    map.putAll(map2);
                }
            }

        }

        return map;
    }

    /**
     * Extracts the contents of the target zipFile into the same directory where the zip file is located.
     * Includes security validation against Zip Slip path traversal vulnerabilities.
     *
     * @return true if extraction completes successfully, false otherwise.
     */
    public boolean extract() {
        // Get the parent folder of the zip file
        File targetDir = this.zipFile.getParentFile();
        if (targetDir == null) {
            return false;
        }

        return extract(targetDir);
    }

    public boolean extract(String targetDir) {
        return extract(new File(targetDir));
    }

    /**
     * Extracts the contents of the target zipFile into the directory specified by targetDir.
     * Includes security validation against Zip Slip path traversal vulnerabilities.
     *
     * @return true if extraction completes successfully, false otherwise.
     */
    public boolean extract(File targetDir) {
        if (this.zipFile == null || !this.zipFile.exists()) {
            return false;
        }

        if (targetDir == null) {
            return false;
        }

        try {
            // Get standard canonical path of target directory for exact security checking
            String targetCanonicalPath = targetDir.getCanonicalPath();

            // Match the existing buffer size used in xmakeZip
            byte[] buffer = new byte[200 * 1024];

            FileInputStream fin = new FileInputStream(this.zipFile);
            java.util.zip.ZipInputStream zin = new java.util.zip.ZipInputStream(fin);
            java.util.zip.ZipEntry entry;

            while ((entry = zin.getNextEntry()) != null) {
                File resolvedFile = new File(targetDir, entry.getName());
                String entryCanonicalPath = resolvedFile.getCanonicalPath();

                // ⚠️ SECURITY GUARD: Prevent Zip Slip Vulnerability
                if (!entryCanonicalPath.startsWith(targetCanonicalPath + File.separator) &&
                        !entryCanonicalPath.equals(targetCanonicalPath)) {
                    zin.close();
                    throw new SecurityException("Malicious ZIP entry blocked: " + entry.getName());
                }

                if (entry.isDirectory()) {
                    resolvedFile.mkdirs();
                } else {
                    // Create parent folders if nested structures exist inside the ZIP
                    File parentDir = resolvedFile.getParentFile();
                    if (parentDir != null) {
                        parentDir.mkdirs();
                    }

                    // Write out the file contents
                    FileOutputStream fout = new FileOutputStream(resolvedFile);
                    int length;
                    while ((length = zin.read(buffer)) > 0) {
                        fout.write(buffer, 0, length);
                    }
                    fout.close();
                }
                zin.closeEntry();
            }

            zin.close();
            return true;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }


}
