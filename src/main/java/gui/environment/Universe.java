/*
 *  JFLAP - Formal Languages and Automata Package
 *
 *
 *  Susan H. Rodger
 *  Computer Science Department
 *  Duke University
 *  August 27, 2009

 *  Copyright (c) 2002-2009
 *  All rights reserved.

 *  JFLAP is open source software. Please see the LICENSE for terms.
 *
 */


package gui.environment;

import file.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.swing.JFileChooser;

/**
 * The <CODE>Universe</CODE> class serves as a large global "registry" for the
 * active windows and their associated environments.
 *
 * @author Thomas Finley
 */

public class Universe {
    /**
     * The registry of codecs universally used for saving.
     */
    public static final CodecRegistry CODEC_REGISTRY = new CodecRegistry();
    /**
     * The mapping of environments to frames.
     */
    private static final Map<Environment, EnvironmentFrame> environmentToFrame = new HashMap<>();
    /**
     * The mapping of files to frames.
     */
    private static final Map<String, EnvironmentFrame> fileToFrame = new HashMap<>();
    /**
     * This is the file listener that should be added to the environments when
     * their frames are created to ensure that no file is opened twice.
     */
    private static final FileChangeListener FILE_LISTENER = new FileChangeListener() {
        public void fileChanged(FileChangeEvent e) {
            // We must update the index.
            File oldFile = e.getOldFile();
            EnvironmentFrame frame = frameForEnvironment((Environment) e
                    .getSource());
            if (oldFile != null)
                fileToFrame.remove(getPath(oldFile));
            Environment env = (Environment) e.getSource();
            File newFile = env.getFile();
            if (newFile == null)
                return;
            fileToFrame.put(getPath(newFile), frame);
        }
    };
    /**
     * The universal JFileChooser.
     */
    public static JFileChooser CHOOSER = null;
    public static Profile curProfile = new Profile();
    /**
     * The number of frames that have been registered... this is used to
     * describe the untitled frames with something unique.
     */
    private static int numberRegistered = 0;

    static {
        try {
            CHOOSER = new JFileChooser(System.getProperties().getProperty("user.dir"));
        } catch (java.security.AccessControlException e) {
            // Nothing to do.
        }
        // Create the codec registry.
        XMLCodec xc = new XMLCodec();
        CODEC_REGISTRY.add(xc);
        // CODEC_REGISTRY.add(new SerializedCodec(xc));
//		CODEC_REGISTRY.add(new JFLAP3Codec());
        // CODEC_REGISTRY.add(new LenoreSystemsCodec());
    }

    /**
     * This class needn't have multiple instances, so we disable the main
     * constructor.
     */
    private Universe() {
    }

    /**
     * Returns the path for a file. This attempts to retrieve the canonical
     * path, but if that fails (it shouldn't) returns the absolute path
     *
     * @param file the file to get the path for
     * @return the canonical path, or alternatively the absolute path
     */
    private static String getPath(File file) {
        try {
            return file.getCanonicalPath();
        } catch (IOException e) {
            return file.getAbsolutePath();
        }
    }

    /**
     * Registers an environment frame.
     *
     * @param frame the environment frame to register
     * @return an integer for the number of frames that have been registered
     * sofar, including this one
     */
    public static int registerFrame(EnvironmentFrame frame) {
        Environment env = frame.getEnvironment();
        environmentToFrame.put(env, frame);
        File file = env.getFile();
        if (file != null)
            fileToFrame.put(getPath(file), frame);
        // Adds the listener that changes this object in the event
        // that the file of an environment changes.
        env.addFileChangeListener(FILE_LISTENER);
        // Hide the new dialog box.
        gui.action.NewAction.hideNew();

        return ++numberRegistered;
    }

    /**
     * Unregisters an environment frame.
     *
     * @param frame the environment frame to unregister
     */
    public static void unregisterFrame(EnvironmentFrame frame) {
        try {
            fileToFrame.remove(getPath(frame.getEnvironment().getFile()));
        } catch (NullPointerException e) {
            // The environment doesn't have a file.
        }
        environmentToFrame.remove(frame.getEnvironment());

        // If there are no other frames open, prompt for newness.
        if (numberOfFrames() == 0)
            gui.action.NewAction.showNew();
    }

    /**
     * Given a file, this returns the frame associated with that file.
     *
     * @param file a file that may be an active file for some environment
     * @return the environment frame associated with this file, or <CODE>null</CODE>
     * if there is no frame associated with this file
     */
    public static EnvironmentFrame frameForFile(File file) {
        if (file == null)
            return null;
        return fileToFrame.get(getPath(file));
    }

    /**
     * Given an environment, this returns the frame associated with that
     * environment.
     *
     * @param environment an environment that may have some frame
     * @return the environment frame associated with this environment, or <CODE>null</CODE>
     * if there is no frame associated with this environment
     */
    public static EnvironmentFrame frameForEnvironment(Environment environment) {
        return environmentToFrame.get(environment);
    }

    /**
     * Returns a list of the registered environment frames.
     *
     * @return an array containing all registered environment frames
     */
    public static EnvironmentFrame[] frames() {
        return environmentToFrame.values().toArray(
                new EnvironmentFrame[0]);
    }

    /**
     * Returns the number of currently open frames that hold a representation of
     * a structure (i.e. automaton, grammar, or regular expression).
     *
     * @return the number of currently open frames
     */
    public static int numberOfFrames() {
        return environmentToFrame.size();
    }
}
