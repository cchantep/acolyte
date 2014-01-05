package acolyte;

import java.lang.reflect.Method;

import java.awt.Image;

/**
 * Native utility.
 *
 * @author Cedric Chantepie
 */
final class Native {

    /**
     * Sets |icon| for Mac OS X dock.
     * @return true if set
     */
    public static boolean setDockIcon(final Image icon) {
        try {
            final Class<?> clazz = Class.forName("com.apple.eawt.Application");
            final Method am = clazz.getMethod("getApplication");
            final Object app = am.invoke(null, new Object[0]);
            final Method si = clazz.getMethod("setDockIconImage", 
                                              new Class[] { Image.class });
            
            si.invoke(app, new Object[] { icon });

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } // end of catch

        return false;
    } // end of setDockIcon
} // end of class Native
