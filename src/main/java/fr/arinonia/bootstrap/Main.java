package fr.arinonia.bootstrap;

import fr.arinonia.abootstrap.utils.OperatingSystem;
import fr.arinonia.bootstrap.utils.Constants;

import javax.swing.*;

/**
 * @author Arinonia
 * Created at 24/01/2022 - 04:34
 **/
public class Main {

    public static void main(String[] args) {

        if (OperatingSystem.getCurrentPlatform() == OperatingSystem.UNKNOWN || OperatingSystem.getCurrentPlatform() == OperatingSystem.MACOS) {
            JOptionPane.showMessageDialog(null, "Votre system d'exploitation n'est pas reconu par le serveur ! " + OperatingSystem.getCurrentPlatform(), Constants.APP_NAME + "-Erreur", JOptionPane.ERROR_MESSAGE);
        } else {
            new DragoniaBootstrap().init();
        }
    }

}
