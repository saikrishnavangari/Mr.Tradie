package au.com.appscore.mrtradie.camerautility;
import java.io.File;

public  abstract class AlbumStorageDirFactory {
    public abstract File getAlbumStorageDir(String albumName);
}