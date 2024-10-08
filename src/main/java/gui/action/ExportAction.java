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

package gui.action;

import gui.environment.Environment;
import gui.environment.Universe;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.swing.svg.SVGFileFilter;
import org.w3c.dom.Document;

/**
 * This action handles saving structures as a vector or raster image, from which we can't retrieve
 * the original structure. It will attempt to export the currently active component in the
 * environment.
 *
 * @author Andrew Ross, Henry Qin
 */
public class ExportAction extends RestrictedAction {
  /** */
  private static final long serialVersionUID = 1L;

  /** The environment. */
  private final Environment environment;

  /**
   * Instantiates a new <CODE>ExportAction</CODE>.
   *
   * @param environment
   */
  public ExportAction(Environment environment) {
    super("Export to SVG", null);
    this.environment = environment;
    putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E, MAIN_MENU_MASK));
  }

  /**
   * This will trigger the export.
   *
   * @param e the action event
   */
  public void actionPerformed(ActionEvent e) {
    JComponent c = (JComponent) environment.getActive();
    Component comp = environment.tabbed.getSelectedComponent();

    SVGDOMImplementation domImpl =
        (SVGDOMImplementation) SVGDOMImplementation.getDOMImplementation();

    String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
    Document document = domImpl.createDocument(svgNS, "svg", null);

    SVGGeneratorContext cntx = SVGGeneratorContext.createDefault(document);
    cntx.setComment("Generated by JFLAP using Batik SVG Generator");
    cntx.setEmbeddedFontsOn(true);

    SVGGraphics2D svgGenerator = new SVGGraphics2D(cntx, false);

    Image canvasimage = comp.createImage(comp.getWidth(), comp.getHeight());
    Graphics imgG = canvasimage.getGraphics(); // voodoo
    comp.paint(imgG);

    comp.print(svgGenerator);

    //    svgGenerator.drawImage(canvasimage, null, null);

    Universe.CHOOSER.resetChoosableFileFilters();
    Universe.CHOOSER.setAcceptAllFileFilterUsed(false);
    Universe.CHOOSER.addChoosableFileFilter(new SVGFileFilter());
    int result = Universe.CHOOSER.showSaveDialog(c);
    while (result == JFileChooser.APPROVE_OPTION) {
      File file = Universe.CHOOSER.getSelectedFile();

      if (!new SVGFileFilter().accept(file)) file = new File(file.getAbsolutePath() + ".svg");

      if (file.exists()) {
        int confirm =
            JOptionPane.showConfirmDialog(
                Universe.CHOOSER,
                "File exists. Shall I overwrite?",
                "FILE OVERWRITE ATTEMPTED",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.NO_OPTION) {
          result = Universe.CHOOSER.showSaveDialog(c);
          continue;
        }
      }

      try {
        Writer out = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
        svgGenerator.stream(out, true);
        return;
      } catch (IOException ioe) {
        JOptionPane.showMessageDialog(
            c,
            "Export failed with error:\n" + ioe.getMessage(),
            "Export failed",
            JOptionPane.ERROR_MESSAGE);
        return;
      }
    }
  }
}
