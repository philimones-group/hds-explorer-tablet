package mz.betainteractive.odk.storage.access.anthonymandra.framework;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Representation of a document backed by either a
 * {@link android.provider.DocumentsProvider} or a raw file on disk.
 * <p>
 * There are several differences between documents and traditional files:
 * <ul>
 * <li>Documents express their display name and MIME type as separate fields,
 * instead of relying on file extensions. Some documents providers may still
 * choose to append extensions to their display names, but that's an
 * implementation detail.
 * <li>A single document may appear as the child of multiple directories, so it
 * doesn't inherently know who its parent is. That is, documents don't have a
 * strong notion of path. You can easily traverse a tree of documents from
 * parent to child, but not from child to parent.
 * <li>Each document has a unique identifier within that provider. This
 * identifier is an <em>opaque</em> implementation detail of the provider, and
 * as such it must not be parsed.
 * </ul>
 * <p>
 * As you navigate the tree of DocumentFile instances, you can always use
 * {@link #getUri()} to obtain the Uri representing the underlying document for
 * that object, for use with {@link ContentResolver#openInputStream(Uri)}, etc.
 * <p>
 * To simplify your code on devices running
 * {@link android.os.Build.VERSION_CODES#KITKAT} or earlier, you can use
 * file scheme uris.  Note: These ONLY work prior to 4.4.  Passing a file uri
 * on 4.4+ will not have write access!
 * <p>
 * Why was this necessary?  There are many flaws in the existing:
 * <ul>
 * <li>{link androidx.core.provider.SingleDocumentFile}
 * <li>{link androidx.core.provider.TreeDocumentFile}
 * </ul>
 * making them useless in any dynamic environment:
 * <ul>
 * <li>https://code.google.com/p/android/issues/detail?id=200941
 * <li>https://code.google.com/p/android/issues/detail?id=199562
 * <ul>
 * @see android.provider.DocumentsProvider
 * @see android.provider.DocumentsContract
 */
public class XDocumentFile
{
    private static final String TAG = XDocumentFile.class.getSimpleName();

    private final XDocumentFile mParent;
    private Context mContext;
    private Uri mUri;
    private FileData mFileData;

    XDocumentFile(XDocumentFile parent, Context context, Uri uri) {
        mParent = parent;
        mContext = context;
        mUri = uri;
    }

    public static XDocumentFile fromUri(@NonNull Context c, @NonNull Uri uri)
    {
/*        if (FileUtil.isFileScheme(uri) && Util.hasKitkat())
        {
                *//*  Although not documented file-based DocumentFiles are entirely unsupported
                    in 4.4+.  They are there solely for backwards compatibility.  To avoid
                    any confusion on the matter we throw an exception here. *//*
                 throw new IllegalArgumentException("File-based DocumentFile is unsupported in 4.4+.");
        }
        else */if (DocumentUtil.isTreeUri(uri)) // A tree uri is not useful by itself
    {
        uri = DocumentsContractApi21.prepareTreeUri(uri);   // Generate the document portion of uri
    }
        return new XDocumentFile(null, c, uri);
    }

    /**
     * Return the parent file of this document. Only defined inside of the
     * user-selected tree; you can never escape above the top of the tree.
     * <p>
     * This method is a significant enhancement over the official DocumentFile
     * variants in that it will attempt to determine the parent given the
     * hierarchical tree within the uri itself.
     * <p>
     * Note: This may not be the <i>only</i> parent as Documents may have multiple parents.
     * This will simply attempt to acquire the parent used to generate the tree.
     * For filesystem use this is sufficient.
     */
    @Nullable
    public XDocumentFile getParentFile()
    {
        if (mParent != null)
        {
            return mParent;
        }
        return getParentDocument();
    }

    public String getDocumentId()
    {

        try
        {
            if (DocumentsContract.isDocumentUri(mContext, mUri))
            {
                return DocumentsContract.getDocumentId(mUri);
            } else
            {
                return DocumentsContract.getTreeDocumentId(mUri);
            }
        }
        catch (IllegalArgumentException e)
        {
            // This is not a document uri, for now I'll try to handle this gracefully.
            // While it may be convenient for a user to be able to use this object for all uri,
            // it may be difficult to manage all aspects gracefully.
            return null;
        }
    }

    @Nullable
    protected XDocumentFile getParentDocument()
    {
        if (FileUtil.isFileScheme(mUri))
        {
            File f = new File(mUri.getPath());
            File parent = f.getParentFile();
            if (parent == null)
                return null;
            return new XDocumentFile(null, mContext, Uri.fromFile(parent));
        }

        String documentId = getDocumentId();
        String[] parts = DocumentUtil.getPathSegments(documentId);
        if (parts == null)
        {
            // Might be a root, try to get the tree uri
            try
            {
                String treeId = DocumentsContract.getTreeDocumentId(mUri);
                return XDocumentFile.fromUri(mContext, DocumentsContract.buildTreeDocumentUri(mUri.getAuthority(), treeId));
            }
            catch (IllegalArgumentException e)
            {
                return null;
            }
        }

        String[] parentParts = Arrays.copyOfRange(parts, 0, parts.length - 1);
        String path = TextUtils.join("/", parentParts);
        String root = DocumentUtil.getRoot(documentId);
        String parentId = DocumentUtil.createNewDocumentId(root, path);

        Uri parentUri;
        /** Removed isDocumentUri check because it involves a resolver check (slow)
         * and the benefits of blindly creating a treeUri are doubtful, the only way I could
         * think of this happening is trying to get parent of a SAF permission which would
         * step above the permission and would only be useful in the case of redundant stacked permissions,
         * ie: luckily grabbing a parent tree that does have permission.
         */
//        if (DocumentsContract.isDocumentUri(mContext, mUri)) // has tree or document segment
//        {
        /**
         *  It's very important we retain tree id because tree is what defines the permission.
         *  Even if a file is under a permission root if the tree id is corrupted it will
         *  fail to gain write permission! */
        // TODO: Doubtful, but can you game document uris to write where you're not allowed
        // using a known valid tree?
        if (DocumentUtil.hasTreeDocumentId(mUri)) // has tree segment
        {
            parentUri = DocumentsContract.buildDocumentUriUsingTree(mUri, parentId);
        }
        else // has only document segment, if there are write restrictions this is useless!
        {
            parentUri = DocumentsContract.buildDocumentUri(mUri.getAuthority(), parentId);
        }
//        }
//        else // attempt to build a tree...this is of dubious usefulness as a permission would have to line up
//        {
//            parentUri = DocumentsContract.buildTreeDocumentUri(mUri.getAuthority(), parentId);
//        }
        return XDocumentFile.fromUri(mContext, parentUri);
    }

    /**
     * Search through {@link #listFiles()} for the first document matching the
     * given display name. Returns {@code null} when no matching document is
     * found.
     */
    public XDocumentFile findFile(String displayName) {
        for (XDocumentFile doc : listFiles()) {
            if (displayName.equals(doc.getName())) {
                return doc;
            }
        }
        return null;
    }

    /**
     * Return a Uri for the underlying document represented by this file. This
     * can be used with other platform APIs to manipulate or share the
     * underlying content.
     *
     * @see Intent#setData(Uri)
     * @see Intent#setClipData(android.content.ClipData)
     * @see ContentResolver#openInputStream(Uri)
     * @see ContentResolver#openOutputStream(Uri)
     * @see ContentResolver#openFileDescriptor(Uri, String)
     */
    public Uri getUri() {
        return mUri;
    }

    /**
     * Create a new document as a direct child of this directory.
     *
     * @param mimeType MIME type of new document, such as {@code image/png} or
     *            {@code audio/flac}
     * @param displayName name of new document, without any file extension
     *            appended; the underlying provider may choose to append the
     *            extension
     * @return file representing newly created document, or null if failed
     * @see android.provider.DocumentsContract#createDocument(ContentResolver,
     *      Uri, String, String)
     */
    public XDocumentFile createFile(String mimeType, String displayName) {
        if (FileUtil.isFileScheme(mUri))
            return createFileFile(mimeType, displayName);
        return createFileUri(mimeType, displayName);
    }
    private XDocumentFile createFileFile(String mimeType, String displayName) {
        File mFile = new File(mUri.getPath());

        // Tack on extension when valid MIME type provided
        final String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
        if (extension != null) {
            displayName += "." + extension;
        }
        final File target = new File(mFile, displayName);
        try {
            if(target.createNewFile())
                return new XDocumentFile(this, mContext, Uri.fromFile(target));
            return null;
        } catch (IOException e) {
            Log.w(TAG, "Failed to createFile: " + e);
            return null;
        }
    }
    private XDocumentFile createFileUri(String mimeType, String displayName) {
        if (!Util.hasLollipop())
            throw new UnsupportedOperationException();

        final Uri result;
        try {
            result = DocumentsContractApi21.createFile(mContext, mUri, mimeType, displayName);
        } catch (FileNotFoundException e) {
            return null;
        }
        return (result != null) ? new XDocumentFile(this, mContext, result) : null;
    }

    /**
     * Create a new directory as a direct child of this directory.
     *
     * @param displayName name of new directory
     * @return file representing newly created directory, or null if failed
     * @see android.provider.DocumentsContract#createDocument(ContentResolver,
     *      Uri, String, String)
     */
    public XDocumentFile createDirectory(String displayName) {
        if (FileUtil.isFileScheme(mUri))
            return createDirectoryFile(displayName);
        return createDirectoryUri(displayName);
    }
    private XDocumentFile createDirectoryFile(String displayName) {
        File mFile = new File(mUri.getPath());

        final File target = new File(mFile, displayName);
        if (target.isDirectory() || target.mkdir()) {
            return new XDocumentFile(this, mContext, Uri.fromFile(target));
        } else {
            return null;
        }
    }
    private XDocumentFile createDirectoryUri(String displayName) {
        if (!Util.hasLollipop())
            throw new UnsupportedOperationException();

        final Uri result;
        try {
            result = DocumentsContractApi21.createDirectory(mContext, mUri, displayName);
        } catch (FileNotFoundException e) {
            return null;
        }
        return (result != null) ? new XDocumentFile(this, mContext, result) : null;
    }

    /**
     * Return the display name of this document.  Attempts to parse the name from the uri
     * when {@link DocumentsContractApi19#getName(Context, Uri)} fails.  It appears to fail
     * on hidden folders, possibly others.
     *
     * @see android.provider.DocumentsContract.Document#COLUMN_DISPLAY_NAME
     */
    public String getName() {
        if (FileUtil.isFileScheme(mUri))
            return getNameFile();
        return getNameUri();
    }
    private String getNameFile() {
        File mFile = new File(mUri.getPath());
        return mFile.getName();
    }
    private String getNameUri() {
        String name = DocumentsContractApi19.getName(mContext, mUri);
        if (name == null)
        {
            return getName(mUri);
        }
        return name;
    }
    private static String getName(Uri uri)
    {
        String[] pathParts = DocumentUtil.getPathSegments(uri);
        if (pathParts != null)
            return pathParts[pathParts.length-1];
        return null;
    }

    /**
     * Return the MIME type of this document.
     *
     * @see android.provider.DocumentsContract.Document#COLUMN_MIME_TYPE
     */
    public String getType() {
        if (FileUtil.isFileScheme(mUri))
            return getTypeFile();
        return getTypeUri();
    }
    private String getTypeFile() {
        File mFile = new File(mUri.getPath());
        return getType(mFile);
    }

    private String getTypeUri() {
        return DocumentsContractApi19.getType(mContext, mUri);
    }

    private static String getType(File mFile) {

        if (mFile.isDirectory()) {
            return null;
        } else {
            return getTypeForName(mFile.getName());
        }
    }
    private static String getTypeForName(String name) {
        final int lastDot = name.lastIndexOf('.');
        if (lastDot >= 0) {
            final String extension = name.substring(lastDot + 1).toLowerCase();
            final String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            if (mime != null) {
                return mime;
            }
        }

        return "application/octet-stream";
    }

    /**
     * Indicates if this file represents a <em>directory</em>.
     *
     * @return {@code true} if this file is a directory, {@code false}
     *         otherwise.
     * @see android.provider.DocumentsContract.Document#MIME_TYPE_DIR
     */
    public boolean isDirectory() {
        if (FileUtil.isFileScheme(mUri))
            return isDirectoryFile();
        return isDirectoryUri();
    }
    private boolean isDirectoryFile() {
        File mFile = new File(mUri.getPath());
        return mFile.isDirectory();
    }
    private boolean isDirectoryUri() {
        return DocumentsContractApi19.isDirectory(mContext, mUri);
    }

    /**
     * Indicates if this file represents a <em>file</em>.
     *
     * @return {@code true} if this file is a file, {@code false} otherwise.
     * @see android.provider.DocumentsContract.Document#COLUMN_MIME_TYPE
     */
    public boolean isFile() {
        if (FileUtil.isFileScheme(mUri))
            return isFileFile();
        return isFileUri();
    }
    private boolean isFileFile() {
        File mFile = new File(mUri.getPath());
        return mFile.isFile();
    }
    private boolean isFileUri() {
        return DocumentsContractApi19.isFile(mContext, mUri);
    }

    /**
     * Returns the time when this file was last modified, measured in
     * milliseconds since January 1st, 1970, midnight. Returns 0 if the file
     * does not exist, or if the modified time is unknown.
     *
     * @return the time when this file was last modified.
     * @see android.provider.DocumentsContract.Document#COLUMN_LAST_MODIFIED
     */
    public long lastModified() {
        if (FileUtil.isFileScheme(mUri))
            return lastModifiedFile();
        return lastModifiedUri();
    }
    private long lastModifiedFile() {
        File mFile = new File(mUri.getPath());
        return mFile.lastModified();
    }
    private long lastModifiedUri() {
        return DocumentsContractApi19.lastModified(mContext, mUri);
    }

    /**
     * Returns the length of this file in bytes. Returns 0 if the file does not
     * exist, or if the length is unknown. The result for a directory is not
     * defined.
     *
     * @return the number of bytes in this file.
     * @see android.provider.DocumentsContract.Document#COLUMN_SIZE
     */
    public long length() {
        if (FileUtil.isFileScheme(mUri))
            return lengthFile();
        return lengthUri();
    }
    private long lengthFile() {
        File mFile = new File(mUri.getPath());
        return mFile.length();
    }
    private long lengthUri() {
        return DocumentsContractApi19.length(mContext, mUri);
    }

    /**
     * Indicates whether the current context is allowed to read from this file.
     *
     * @return {@code true} if this file can be read, {@code false} otherwise.
     */
    public boolean canRead() {
        if (FileUtil.isFileScheme(mUri))
            return canReadFile();
        return canReadUri();
    }
    private boolean canReadFile() {
        File mFile = new File(mUri.getPath());
        return mFile.canRead();
    }
    private boolean canReadUri() {
        return canRead(mContext, mUri);
    }
    private static boolean canRead(Context c, Uri uri) {
        return DocumentsContractApi19.canRead(c, uri);
    }

    /**
     * Indicates whether the current context is allowed to write to this file.
     *
     * @return {@code true} if this file can be written, {@code false}
     *         otherwise.
     * @see android.provider.DocumentsContract.Document#COLUMN_FLAGS
     * @see android.provider.DocumentsContract.Document#FLAG_SUPPORTS_DELETE
     * @see android.provider.DocumentsContract.Document#FLAG_SUPPORTS_WRITE
     * @see android.provider.DocumentsContract.Document#FLAG_DIR_SUPPORTS_CREATE
     */
    public boolean canWrite() {
        if (FileUtil.isFileScheme(mUri))
            return canWriteFile();
        return canWriteUri();
    }
    private boolean canWriteFile() {
        File mFile = new File(mUri.getPath());
        return mFile.canWrite();
    }
    private boolean canWriteUri() {
        return canWrite(mContext, mUri);
    }
    private static boolean canWrite(Context c, Uri uri) {
        return DocumentsContractApi19.canWrite(c, uri);
    }

    /**
     * Deletes this file.
     * <p>
     * Note that this method does <i>not</i> throw {@code IOException} on
     * failure. Callers must check the return value.
     *
     * @return {@code true} if this file was deleted, {@code false} otherwise.
     * @see android.provider.DocumentsContract#deleteDocument(ContentResolver,
     *      Uri)
     */
    public boolean delete() {
        if (FileUtil.isFileScheme(mUri))
            return deleteFile();
        return deleteUri();
    }
    private boolean deleteFile() {
        File mFile = new File(mUri.getPath());
        deleteContents(mFile);
        return mFile.delete();
    }
    private static boolean deleteContents(File dir) {
        File[] files = dir.listFiles();
        boolean success = true;
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    success &= deleteContents(file);
                }
                if (!file.delete()) {
                    Log.w(TAG, "Failed to delete " + file);
                    success = false;
                }
            }
        }
        return success;
    }
    private boolean deleteUri() {
        return DocumentsContractApi19.delete(mContext, mUri);
    }

    /**
     * Returns a boolean indicating whether this file can be found.
     *
     * @return {@code true} if this file exists, {@code false} otherwise.
     */
    public boolean exists() {
        if (FileUtil.isFileScheme(mUri))
            return existsFile();
        return existsUri();
    }
    private boolean existsFile() {
        File mFile = new File(mUri.getPath());
        return mFile.exists();
    }
    private boolean existsUri() {
        return DocumentsContractApi19.exists(mContext, mUri);
    }

    /**
     * Returns an array of files contained in the directory represented by this
     * file.
     *
     * @return an array of files or {@code null}.
     * @see android.provider.DocumentsContract#buildChildDocumentsUriUsingTree(Uri,
     *      String)
     */
    public XDocumentFile[] listFiles() {
        if (FileUtil.isFileScheme(mUri))
            return listFilesFile();
        return listFilesUri();
    }
    private XDocumentFile[] listFilesFile() {
        File mFile = new File(mUri.getPath());
        final ArrayList<XDocumentFile> results = new ArrayList<>();
        final File[] files = mFile.listFiles();
        if (files != null) {
            for (File file : files) {
                results.add(new XDocumentFile(this, mContext, Uri.fromFile(file)));
            }
        }
        return results.toArray(new XDocumentFile[results.size()]);
    }
    private XDocumentFile[] listFilesUri() {
        if (!Util.hasLollipop())
            throw new UnsupportedOperationException();

        final Uri[] result = DocumentsContractApi21.listFiles(mContext, mUri);
        final XDocumentFile[] resultFiles = new XDocumentFile[result.length];
        for (int i = 0; i < result.length; i++) {
            resultFiles[i] = new XDocumentFile(this, mContext, result[i]);
        }
        return resultFiles;
    }

    /**
     * Renames this file to {@code displayName}.
     * <p>
     * Note that this method does <i>not</i> throw {@code IOException} on
     * failure. Callers must check the return value.
     * <p>
     * Some providers may need to create a new document to reflect the rename,
     * potentially with a different MIME type, so {@link #getUri()} and
     * {@link #getType()} may change to reflect the rename.
     * <p>
     * When renaming a directory, children previously enumerated through
     * {@link #listFiles()} may no longer be valid.
     *
     * @param displayName the new display name.
     * @return true on success.
     * @see android.provider.DocumentsContract#renameDocument(ContentResolver,
     *      Uri, String)
     */
    public boolean renameTo(String displayName) {
        if (FileUtil.isFileScheme(mUri))
            return renameToFile(displayName);
        return renameToUri(displayName);
    }
    private boolean renameToFile(String displayName) {
        File mFile = new File(mUri.getPath());
        final File target = new File(mFile.getParentFile(), displayName);
        if (mFile.renameTo(target)) {
            mUri = Uri.fromFile(target);
            return true;
        } else {
            return false;
        }
    }
    private boolean renameToUri(String displayName) {
        if (!Util.hasLollipop())
            throw new UnsupportedOperationException();

        final Uri result;
        try {
            result = DocumentsContractApi21.renameTo(mContext, mUri, displayName);
        } catch (FileNotFoundException e) {
            return false;
        }
        if (result != null) {
            mUri = result;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns cached file data.  If cache does not exist it's populated {@link #getData()}.
     * The cached data could be incorrect if the file system has changed since the data was populated.
     * Therefore this is best used as a convenience method to link file data and document
     * functionality for code that requires multiple queries for a short time.
     * @return Cached file data, null on exception (likely !exists)
     */
    public @Nullable FileData getCachedData()
    {
        if (mFileData == null)
            mFileData = getData();
        return mFileData;
    }

    /**
     * This will retrieve all file-related data for a uri in a single query.
     * Every get requires a query, so if you are interested in more than one field than
     * this method will offer a SIGNIFICANT performance improvement.
     * @return All file data for the given document, null on exception (likely !exists)
     */
    public @Nullable FileData getData()
    {
        if (FileUtil.isFileScheme(mUri))
            return FileData.fromFile(new File(mUri.getPath()));
        XDocumentFile parent = getParentFile();
        Uri p = null;
        if (parent != null)
            p = parent.getUri();

        return FileData.fromUri(mContext, mUri, p);
    }

    /**
     * POJO for storing all file data in one go.  If a user is interested in more than one
     * field at a time this will reduce many queries to a single query
     */
    public static class FileData
    {
        public boolean canRead;
        public boolean canWrite;
        public boolean exists;
        public String type;
        public Uri uri;
        public boolean isDirectory;
        public boolean isFile;
        public long lastModified;
        public long length;
        public String name;
        public Uri parent;

        private static FileData fromFile(File f)
        {
            FileData fd = new FileData();
            fd.canRead = f.canRead();
            fd.canWrite = f.canWrite();
            fd.exists = f.exists();
            fd.type = getType(f);
            fd.uri = Uri.fromFile(f);
            fd.isDirectory = f.isDirectory();
            fd.isFile = f.isFile();
            fd.lastModified = f.lastModified();
            fd.length = f.length();
            fd.name = f.getName();
            fd.parent = Uri.fromFile(f.getParentFile());
            return fd;
        }

        /**
         * Gather all file data in a single resolver call.  This is much faster if a code segment
         * requires 2 or more calls to file-related data which inidividually involve resolver calls
         * @param c host context
         * @param uri uri of the object
         * @param parent parent
         * @return POJO representing file data, null on exception (likely !exists)
         */
        private static @Nullable FileData fromUri(Context c, Uri uri, Uri parent)
        {
            FileData fd = new FileData();
            fd.uri = uri;
            fd.parent = parent;

            String[] columns = new String[] {
                    DocumentsContract.Document.COLUMN_MIME_TYPE,
                    DocumentsContract.Document.COLUMN_LAST_MODIFIED,
                    DocumentsContract.Document.COLUMN_SIZE,
                    DocumentsContract.Document.COLUMN_FLAGS,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME
            };

            try (Cursor cursor = c.getContentResolver().query(uri, columns, null, null, null))
            {
                fd.exists = cursor != null && cursor.getCount() > 0;
                if (!fd.exists)
                    return fd;

                cursor.moveToFirst();

                // Ignore if grant doesn't allow read
                final boolean readPerm = c.checkCallingOrSelfUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        == PackageManager.PERMISSION_GRANTED;
                final boolean writePerm = c.checkCallingOrSelfUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                        == PackageManager.PERMISSION_GRANTED;
                final String rawType = cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE));
                final int flags = cursor.getInt(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_FLAGS));
                final boolean hasMime = !TextUtils.isEmpty(rawType);
                final boolean supportsDelete = (flags & DocumentsContract.Document.FLAG_SUPPORTS_DELETE) != 0;
                final boolean supportsCreate = (flags & DocumentsContract.Document.FLAG_DIR_SUPPORTS_CREATE) != 0;
                final boolean supportsWrite = (flags & DocumentsContract.Document.FLAG_SUPPORTS_WRITE) != 0;
                final String name = cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME));

                fd.isDirectory = DocumentsContract.Document.MIME_TYPE_DIR.equals(rawType);
                if (fd.isDirectory)
                {
                    fd.type = null;
                    fd.isFile = false;
                }
                else
                {
                    fd.type = rawType;
                    fd.isFile = hasMime;
                }

                fd.name = name != null ? name : getName(uri);
                fd.canRead = readPerm && hasMime;
                fd.canWrite = writePerm && (supportsDelete || (fd.isDirectory && supportsCreate) || (hasMime && supportsWrite));
                fd.lastModified = cursor.getLong(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED));
                fd.length = cursor.getLong(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE));
                DocumentsContractApi19.closeQuietly(cursor);

                return fd;
            }
            catch(Exception e)  // This is what DocumentContract.exists does, likely means !exists
            {
                return null;
            }
        }
    }
}
