package fr.arinonia.bootstrap;

import fr.arinonia.abootstrap.Bootstrap;
import fr.arinonia.abootstrap.aupdater.DownloadJob;
import fr.arinonia.abootstrap.aupdater.DownloadListener;
import fr.arinonia.abootstrap.aupdater.DownloadManager;
import fr.arinonia.abootstrap.aupdater.Updater;
import fr.arinonia.abootstrap.aupdater.utils.ProgressBarHelper;
import fr.arinonia.abootstrap.runner.JarRunner;
import fr.arinonia.abootstrap.runner.JarRunnerException;
import fr.arinonia.abootstrap.runner.Runner;
import fr.arinonia.abootstrap.utils.OperatingSystem;
import fr.arinonia.abootstrap.utils.UiUtil;
import fr.arinonia.abootstrap.utils.Util;
import fr.arinonia.bootstrap.files.FileManager;
import fr.arinonia.bootstrap.utils.Constants;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

/**
 * @author Arinonia
 * Created at 24/01/2022 - 04:34
 **/
public class DragoniaBootstrap implements DownloadListener {

    private final Bootstrap bootstrap = new Bootstrap.BootstrapBuilder().setTitle(Constants.APP_NAME).setIcon(UiUtil.getIconImage("/images/icon.png"))
            .setPoweredLabel("Powered by " + Constants.APP_NAME).setDownloadLabelText("Bienvenue !").build();
    private final FileManager fileManager = new FileManager();
    private final Updater updater = new Updater();
    private final JarRunner jarRunner = new JarRunner();
    private Thread ui_update_thread;

    public void init() {
        this.bootstrap.setVisible(true);
        this.bootstrap.setAlwaysOnTop(false);
        this.bootstrap.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                super.windowClosing(e);
                if (!updater.getNeedToDownload().isEmpty()) {
                    int rep = JOptionPane.showOptionDialog(bootstrap,
                            "Une mise à jour est en cours, êtes vous sûr de vouloir quitter le bootstrap ?",
                            Constants.APP_NAME , JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
                    if (rep == JOptionPane.YES_OPTION) {
                        System.exit(0);
                    } else {
                        bootstrap.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                    }
                }
            }
        });
        this.bootstrap.getDownloadLabel().setFont(this.bootstrap.getDownloadLabel().getFont().deriveFont(16.0F));
        this.fileManager.setupFiles();

        if (!Util.checkJavaVersion("1.8")) {
            final DownloadJob javaJob = new DownloadJob("de Java", this);
            final DownloadManager javaManager = new DownloadManager(Constants.JAVA_DOWNLOAD_URL + OperatingSystem.getCurrentPlatform().getName() + "/instance.json",
                    javaJob, this.fileManager.getJavaDir());
            this.updater.addJobToDownload(javaManager);
            String javaPath = new File(this.fileManager.getJavaDir(), "bin" + File.separator + "java").getAbsolutePath();

            if (OperatingSystem.getCurrentPlatform() == OperatingSystem.WINDOWS) {
                javaPath += "w";
            }
            this.jarRunner.setJavaCommand(javaPath);
        }

        final DownloadJob launcherJob = new DownloadJob("du Launcher", this);
        final DownloadManager launcherManager = new DownloadManager(Constants.LAUNCHER_DOWNLOAD_URL, launcherJob, this.fileManager.getLauncherDir());
        this.updater.addJobToDownload(launcherManager);

        final Thread updateThread = new Thread(updater::start);
        updateThread.setName("Updater Thread");
        updateThread.start();
    }

    private void launch() {
        final File[] files = this.fileManager.getLibsDir().listFiles();

        try {
            final Process p = this.jarRunner.launch(new Runner("fr.dragonia.launcher.Main", this.makeClassPath(files)));
            try {
                this.bootstrap.setVisible(false);
                p.waitFor();
            } catch (InterruptedException ignored){}
            System.exit(0);
        } catch (JarRunnerException e) {
            e.printStackTrace();
        }
    }

    private String makeClassPath(final File[] files) {
        final StringBuilder builder = new StringBuilder();

        if (files != null)
            for (final File file : files)
                builder.append(file.getAbsolutePath()).append(File.pathSeparator);
        builder.append(this.fileManager.getLauncherJar());
        return builder.toString();
    }

    @Override
    public void onDownloadJobFinished(final DownloadJob downloadJob) {
        if (this.ui_update_thread != null && !ui_update_thread.isInterrupted()) {
            this.ui_update_thread.interrupt();
        }
        if (this.updater.getNeedToDownload().isEmpty()) {
            this.launch();
        }
    }

    @Override
    public void onDownloadJobProgressChanged(final DownloadJob downloadJob) {
        System.out.println(downloadJob.getAllFiles().size() - downloadJob.getRemainingFiles().size() + " / " + downloadJob.getAllFiles().size());
    }

    @Override
    public void onDownloadJobStarted(final DownloadJob downloadJob) {
        this.bootstrap.getProgressBar().setMinimum(0);
        this.bootstrap.getProgressBar().setMaximum((int)(ProgressBarHelper.getTotalBytesNeedToDownload() / 1000L));

        this.ui_update_thread = new Thread() {
            private long lastDownloaded = 0;

            @Override
            public void run() {
                while(!this.isInterrupted()) {
                    if (ProgressBarHelper.getDownloadedBytes() != 0) {
                        long downloaded = ProgressBarHelper.getDownloadedBytes();
                        int val = ((int)(ProgressBarHelper.getDownloadedBytes() / 1000L));

                        long inSeconde = downloaded - this.lastDownloaded;
                        this.lastDownloaded = downloaded;

                        try {
                            sleep(500L);
                        } catch (InterruptedException ignored){}

                        bootstrap.getDownloadLabel().setText("Téléchargement " + (val / 1000) + " / " + (ProgressBarHelper.getTotalBytesNeedToDownload() / 1000000) + " MB @ " + Util.convertBytesCount(inSeconde * 2) + "/s");
                        bootstrap.getDownloadLabel().setBounds(375 / 2 - bootstrap.getPanel().getLabelWidth(bootstrap.getDownloadLabel()) / 2, 300, 350, 40);
                        bootstrap.getProgressBar().setValue(val);
                    }
                }
            }
        };
        this.ui_update_thread.setName("Ui " + downloadJob.getName() + " Thread");
        this.ui_update_thread.start();
    }

}
