package org.embl.mobie.viewer.command;

import mpicbg.spim.data.SpimData;
import org.embl.mobie.OMEZarrViewer;
import org.embl.mobie.io.ome.zarr.openers.OMEZarrOpener;

public class RunOMEZARRCommand extends OpenOMEZARRCommand {
    public static void main(final String[] args) {
//		final String fn = "/Users/pietzsch/workspace/data/111010_weber_resave.xml";
        final String filePath = "/home/gabor.kovacs/data/one_img_test.ome.zarr/020000";
        try {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
//			System.setProperty( "apple.awt.application.appearance", "system" );
//            UIUtils.installFlatLafInfos();
            SpimData spimData = OMEZarrOpener.openFile(filePath);
            final OMEZarrViewer viewer = new OMEZarrViewer(spimData);
            viewer.show();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
