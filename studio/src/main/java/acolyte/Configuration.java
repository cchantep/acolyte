package acolyte;

import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.File;

import java.util.Properties;

/**
 * Configuration utility.
 *
 * @author Cedric Chantepie
 */
final class Configuration {

    /**
     * Loads configuration from |file|.
     *
     * @param config Configuration container
     * @param f File to be loaded
     */
    protected static void loadConfig(final Properties config,
                                       final File f) {
        InputStreamReader r = null;

        try {
            final FileInputStream in = new FileInputStream(f);
            r = new InputStreamReader(in, "UTF-8");

            config.load(r);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (r != null) {
                try {
                    r.close();
                } catch (Exception e) {
                    e.printStackTrace();
                } // end of catch
            } // end of if
        } // end of finally
    } // end of loadConfig
} // end of class Configuration
