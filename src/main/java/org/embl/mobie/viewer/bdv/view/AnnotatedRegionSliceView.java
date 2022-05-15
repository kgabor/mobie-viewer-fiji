package org.embl.mobie.viewer.bdv.view;

import bdv.util.BdvHandle;
import bdv.viewer.SourceAndConverter;
import bdv.viewer.TimePointListener;
import de.embl.cba.tables.color.ColoringListener;
import de.embl.cba.tables.tablerow.TableRow;
import net.imglib2.type.numeric.integer.IntType;
import org.embl.mobie.viewer.MoBIE;
import org.embl.mobie.viewer.bdv.render.BlendingMode;
import org.embl.mobie.viewer.color.OpacityAdjuster;
import org.embl.mobie.viewer.display.AnnotatedRegionDisplay;
import org.embl.mobie.viewer.select.SelectionListener;
import org.embl.mobie.viewer.source.LabelSource;
import sc.fiji.bdvpg.scijava.services.SourceAndConverterBdvDisplayService;
import sc.fiji.bdvpg.services.SourceAndConverterServices;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.HashMap;


public abstract class AnnotatedRegionSliceView< T extends TableRow > implements ColoringListener, SelectionListener< T >
{
	protected final SourceAndConverterBdvDisplayService displayService;
	protected final MoBIE moBIE;
	protected final AnnotatedRegionDisplay< T > display;
	protected BdvHandle bdvHandle;

	public AnnotatedRegionSliceView( MoBIE moBIE, AnnotatedRegionDisplay< T > display, BdvHandle bdvHandle  )
	{
		this.moBIE = moBIE;
		this.display = display;
		this.bdvHandle = bdvHandle;
		this.displayService = SourceAndConverterServices.getBdvDisplayService();
		display.sourceNameToSourceAndConverter = new HashMap<>();
		display.selectionModel.listeners().add( this );
		display.coloringModel.listeners().add( this );
	}

	protected void show( SourceAndConverter< ? > sourceAndConverter )
	{
		SourceAndConverterServices.getSourceAndConverterService().register( sourceAndConverter );

		SourceAndConverterServices.getSourceAndConverterService().setMetadata( sourceAndConverter, BlendingMode.BLENDING_MODE, display.getBlendingMode() );

		adjustLabelRendering( sourceAndConverter );

		OpacityAdjuster.adjustOpacity( sourceAndConverter, display.getOpacity() );

		// show in Bdv
		displayService.show( bdvHandle, display.isVisible(), sourceAndConverter );

		bdvHandle.getViewerPanel().addTimePointListener( ( TimePointListener ) sourceAndConverter.getConverter() );

		// register
		display.sourceNameToSourceAndConverter.put( sourceAndConverter.getSpimSource().getName(), sourceAndConverter );
	}

	private void adjustLabelRendering( SourceAndConverter< ? > sourceAndConverter )
	{
		final boolean showAsBoundaries = display.isShowAsBoundaries();
		final float boundaryThickness = display.getBoundaryThickness();
		( (LabelSource) sourceAndConverter.getSpimSource() ).showAsBoundary( showAsBoundaries, boundaryThickness );
		if ( sourceAndConverter.asVolatile() != null )
			( (LabelSource) sourceAndConverter.asVolatile().getSpimSource() ).showAsBoundary( showAsBoundaries, boundaryThickness );
	}

	public void close( boolean closeImgLoader )
	{
		for ( SourceAndConverter< ? > sourceAndConverter : display.sourceNameToSourceAndConverter.values() )
		{
			moBIE.closeSourceAndConverter( sourceAndConverter, closeImgLoader );
		}
		display.sourceNameToSourceAndConverter.clear();
	};

	public boolean isDisplayVisible() {
		Collection<SourceAndConverter<?>> sourceAndConverters = display.sourceNameToSourceAndConverter.values();
		// check if first source is visible
		return SourceAndConverterServices.getBdvDisplayService().isVisible( sourceAndConverters.iterator().next(), bdvHandle );
	}

	@Override
	public synchronized void coloringChanged()
	{
		bdvHandle.getViewerPanel().requestRepaint();
	}

	@Override
	public synchronized void selectionChanged()
	{
		bdvHandle.getViewerPanel().requestRepaint();
	}

	@Override
	public synchronized void focusEvent( T selection, Object origin  )
	{
		// must be defined in child classes
	}

	public BdvHandle getBdvHandle()
	{
		return bdvHandle;
	}

	public Window getWindow()
	{
		return SwingUtilities.getWindowAncestor( bdvHandle.getViewerPanel() );
	}
}
