package de.embl.cba.platynereis.platybrowser;

import bdv.util.*;
import de.embl.cba.bdv.utils.BdvUtils;
import de.embl.cba.bdv.utils.sources.ARGBConvertedRealSource;
import de.embl.cba.platynereis.PlatynereisImageSourcesModel;
import de.embl.cba.tables.modelview.coloring.LazyLabelsARGBConverter;
import de.embl.cba.tables.modelview.images.DefaultImageSourcesModel;
import de.embl.cba.tables.modelview.images.SourceAndMetadata;
import de.embl.cba.tables.modelview.images.SourceMetadata;
import de.embl.cba.tables.modelview.segments.TableRowImageSegment;
import de.embl.cba.tables.modelview.views.DefaultTableAndBdvViews;
import ij.IJ;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;

import static de.embl.cba.bdv.utils.BdvUserInterfaceUtils.*;
import static de.embl.cba.platynereis.platybrowser.ExplorePlatynereisAtlasCommand.createAnnotatedImageSegmentsFromTableFile;

public class PlatyBrowserSourcesPanel extends JPanel
{
//    public List< Color > colors;
    protected Map< String, JPanel > sourceNameToPanel;
    private BdvHandle bdv;
    private final PlatynereisImageSourcesModel platySourcesModel;

    public PlatyBrowserSourcesPanel( File dataFolder )
    {
        platySourcesModel = new PlatynereisImageSourcesModel( dataFolder );
        sourceNameToPanel = new LinkedHashMap<>(  );
        configPanel();
//        initColors();
    }

    public void addSourceToPanelAndViewer( String sourceName )
    {
        addSourceToPanelAndViewer( platySourcesModel.sources().get( sourceName ) );
    }

    public ArrayList< String > getSourceNames()
    {
        return new ArrayList<>( platySourcesModel.sources().keySet() );
    }

    private void configPanel()
    {
        this.setLayout( new BoxLayout(this, BoxLayout.Y_AXIS ) );
        this.setAlignmentX( Component.LEFT_ALIGNMENT );
    }

//    private void initColors()
//    {
//        colors = new ArrayList<>(  );
//        colors.add( Color.YELLOW );
//        colors.add( Color.MAGENTA );
//        colors.add( Color.CYAN );
//        colors.add( Color.BLUE );
//        colors.add( Color.ORANGE );
//        colors.add( Color.GREEN );
//        colors.add( Color.PINK );
//    }

    private Color getColor( SourceMetadata metadata )
    {
        return metadata.displayColor;
//        else if ( sourceNameToPanel.size() <= colors.size()  & sourceNameToPanel.size() > 0 )
//        {
//            return colors.get( sourceNameToPanel.size() - 1 );
//        }
//        else
//        {
//            return colors.get( 0 );
//        }
    }



    private void addSourceToPanelAndViewer( SourceAndMetadata< ? > sourceAndMetadata )
    {
        if ( sourceNameToPanel.containsKey(  sourceAndMetadata.metadata().displayName ) )
            return;

        addSourceToViewer( sourceAndMetadata );
        addSourceToPanel( sourceAndMetadata );
    }

    private void addSourceToViewer( SourceAndMetadata< ? > sourceAndMetadata )
    {
        final SourceMetadata metadata = sourceAndMetadata.metadata();

        if ( metadata.flavour == SourceMetadata.Flavour.LabelSource )
        {
            if ( metadata.segmentsTable != null )
                metadata.bdvStackSource = showAnnotatedLabelsSource( sourceAndMetadata );
            else
                metadata.bdvStackSource = showLabelsSource( sourceAndMetadata );
        }
        else
        {
            metadata.bdvStackSource = showIntensitySource( sourceAndMetadata, metadata );;
        }
    }

    private BdvStackSource showIntensitySource( SourceAndMetadata< ? > sourceAndMetadata, SourceMetadata metadata )
    {
        final BdvStackSource bdvStackSource = BdvFunctions.show(
                sourceAndMetadata.source(),
                1,
                BdvOptions.options().sourceTransform(
                        metadata.sourceTransform ).addTo( bdv ) );

        bdvStackSource.setActive( true );

        bdvStackSource.setDisplayRange( metadata.displayRangeMin, metadata.displayRangeMax );

        bdv = bdvStackSource.getBdvHandle();
        return bdvStackSource;
    }

    private BdvStackSource showLabelsSource( SourceAndMetadata< ? > sourceAndMetadata )
    {
        final ARGBConvertedRealSource source =
                new ARGBConvertedRealSource( sourceAndMetadata.source(),
                new LazyLabelsARGBConverter() );

        return BdvFunctions.show( source,
                BdvOptions.options()
                        .addTo( bdv )
                        .sourceTransform( sourceAndMetadata.metadata().sourceTransform ) );
    }

    private BdvStackSource showAnnotatedLabelsSource( SourceAndMetadata< ? > sourceAndMetadata )
    {
        final SourceMetadata metadata = sourceAndMetadata.metadata();

        final LinkedHashMap< String, List< ? > > columns = new LinkedHashMap<>();

        final List< TableRowImageSegment > tableRowImageSegments
                = createAnnotatedImageSegmentsFromTableFile(
                        metadata.segmentsTable, columns );

        final DefaultImageSourcesModel imageSourcesModel
                = new DefaultImageSourcesModel( false );

        imageSourcesModel.addSourceAndMetadata(
                metadata.imageId, sourceAndMetadata );

        final DefaultTableAndBdvViews view = new DefaultTableAndBdvViews(
                tableRowImageSegments,
                imageSourcesModel,
                bdv );

        bdv = view.getImageSegmentsBdvView().getBdv();

        final BdvStackSource bdvStackSource = view
                        .getImageSegmentsBdvView()
                        .getCurrentSources().get( 0 )
                        .metadata().bdvStackSource;

        return bdvStackSource;
    }

    private void addSourceToPanel( SourceAndMetadata< ? > sourceAndMetadata )
    {
        final SourceMetadata metadata = sourceAndMetadata.metadata();
        final String sourceName = metadata.displayName;
        final BdvStackSource bdvStackSource = metadata.bdvStackSource;

        JPanel panel = new JPanel();
        sourceNameToPanel.put( sourceName, panel );

        panel.setLayout( new BoxLayout(panel, BoxLayout.LINE_AXIS) );
        panel.setBorder( BorderFactory.createEmptyBorder(0, 10, 0, 10 ) );
        panel.add( Box.createHorizontalGlue() );
        panel.setOpaque( true );
        panel.setBackground( getColor( metadata ) );

        JLabel jLabel = new JLabel( sourceName );
        jLabel.setHorizontalAlignment( SwingConstants.CENTER );

        int[] buttonDimensions = new int[]{ 50, 30 };

        final JButton colorButton =
                createColorButton( panel, buttonDimensions, bdvStackSource );
        final JButton brightnessButton =
                createBrightnessButton( buttonDimensions, sourceName, bdvStackSource );
        final JButton removeButton =
                createRemoveButton( sourceAndMetadata, bdvStackSource, buttonDimensions );
        final JCheckBox visibilityCheckbox =
                createVisibilityCheckbox( buttonDimensions, bdvStackSource, true );

        panel.add( jLabel );
        panel.add( colorButton );
        panel.add( brightnessButton );
        panel.add( removeButton );
        panel.add( visibilityCheckbox );

        add( panel );
        refreshGui();
    }

    private JButton createRemoveButton(
            SourceAndMetadata sourceAndMetadata,
            BdvStackSource bdvStackSource,
            int[] buttonDimensions )
    {
        JButton removeButton = new JButton( "X" );
        removeButton.setPreferredSize(
                new Dimension( buttonDimensions[ 0 ], buttonDimensions[ 1 ] ) );

        removeButton.addActionListener(
                e -> removeSourceFromPanelAndViewer(
                        sourceAndMetadata.metadata().displayName, bdvStackSource ) );

        return removeButton;
    }

    private void removeSourceFromPanelAndViewer(
            String sourceName,
            BdvStackSource bdvStackSource )
    {
        remove( sourceNameToPanel.get( sourceName ) );
        sourceNameToPanel.remove( sourceName );
        BdvUtils.removeSource( bdv, bdvStackSource );
        refreshGui();
    }

    private void refreshGui()
    {
        this.revalidate();
        this.repaint();
    }

    public BdvHandle getBdv()
    {
        return bdv;
    }
}