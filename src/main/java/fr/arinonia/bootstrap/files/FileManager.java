package fr.arinonia.bootstrap.files;

import fr.arinonia.abootstrap.utils.OperatingSystem;
import fr.arinonia.abootstrap.utils.Util;
import fr.arinonia.bootstrap.utils.Constants;

import java.io.File;

/**
 * @author Arinonia
 * Created at 24/01/2022 - 04:35
 **/
public class FileManager {

    private File launcherDir;
    private File libsDir;
    private File javaDir;
    private File launcherJar;

    public void setupFiles() {
        this.launcherDir = new File(this.createGameDir(), "launcher");
        this.libsDir = new File(this.getLauncherDir(), "libs");
        this.javaDir = new File(this.getLauncherDir(), "java");
        this.launcherJar = new File(this.getLauncherDir(), "launcher.jar");
    }

    private File createGameDir() {
        switch (OperatingSystem.getCurrentPlatform()) {
            case WINDOWS:
                return new File(System.getProperty("user.home") + "\\AppData\\Roaming\\." + Constants.APP_NAME);
            case MACOS:
                return new File(System.getProperty("user.home") + "Library/Application Support/" + Constants.APP_NAME);
            case LINUX:
            case UNKNOWN:
            default:
                return new File(System.getProperty("user.home") + "/." + Constants.APP_NAME);
        }
    }

    public File getLauncherDir() {
        if (!Util.checkFolder(this.launcherDir)) {
            System.err.println(this.launcherDir.getName() + " is not a folder or can't be create");
        }
        return this.launcherDir;
    }
    public File getLibsDir() {
        if (!Util.checkFolder(this.libsDir)) {
            System.err.println(this.libsDir.getName() + " is not a folder or can't be create");
        }
        return this.libsDir;
    }
    public File getJavaDir() {
        if (!Util.checkFolder(this.javaDir)) {
            System.err.println(this.javaDir.getName() + " is not a folder or can't be create");
        }
        return this.javaDir;
    }

    public File getLauncherJar() {
        return this.launcherJar;
    }
}
