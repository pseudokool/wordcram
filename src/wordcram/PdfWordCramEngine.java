package wordcram;

/*
Copyright 2010 Daniel Bernier

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

import java.awt.Color;
import java.awt.geom.*;
import java.util.ArrayList;

import processing.core.*;

/* TODO subclassing WCE is temporary: if this gets moving, rename WCE -> DefaultWCE (or something), & make WCE an interface.
 * Actually, you might want to extract a WordCramRenderer, and leave much of WordCramEngine alone -- the part that calls
 * the components could stay unchanged, and pass in the results.  In that case, I guess you'd...pass a WordCramRenderer from WordCram
 * to WordCramEngine.
 */
public class PdfWordCramEngine extends WordCramEngine {

	private PGraphics destination;
	private WordColorer colorer;
	private WordFonter fonter;
	private WordSizer sizer;
	private WordAngler angler;

	public PdfWordCramEngine(PGraphics destination, Word[] words, WordFonter fonter, WordSizer sizer, WordColorer colorer, WordAngler angler, WordPlacer placer, WordNudger nudger, WordShaper shaper, BBTreeBuilder bbTreeBuilder, RenderOptions renderOptions) {
		super(destination, words, fonter, sizer, colorer, angler, placer, nudger, shaper, bbTreeBuilder, renderOptions);
		this.destination = destination;
		this.colorer = colorer;
		this.fonter = fonter;
		this.sizer = sizer;
		this.angler = angler;
	}

	public void drawAll() {
		drawNext();
	}
	
	public void drawNext() {
		destination.textMode(PConstants.MODEL);
		
		for (int i = 0; i < eWords.length; i++) {
			
			EngineWord eWord = eWords[i];
				
			boolean wasPlaced = placeWord(eWord);
			
			if (wasPlaced) {
				drawWordImage(eWord);
			}
		}
	}
	
	
	protected void drawWordImage(EngineWord word) {
		PVector location = word.getCurrentLocation();
		int color = word.word.getColor(colorer);

		boolean useJavaGeom = false;
		
		if (useJavaGeom) {

			boolean useGeomerative = false;
			
			if (useGeomerative) {

				  PathIterator pi = word.getShape().getPathIterator(AffineTransform.getTranslateInstance(location.x, location.y));
				  
				  destination.noStroke();
				  destination.beginShape();
				  destination.fill(color);
				  float[] coords = new float[6];
				  
				  pi.currentSegment(coords);
				  destination.vertex(coords[0], coords[1]);
				  
				  while(!pi.isDone()) {
				    int segtype = pi.currentSegment(coords);
				    switch(segtype) {
				      case PathIterator.SEG_LINETO:
				        destination.vertex(coords[0], coords[1]);
				        break;
				      case PathIterator.SEG_QUADTO:
				        destination.bezierVertex(coords[0], coords[1], coords[2], coords[3], coords[2], coords[3]);
				        break;
				      case PathIterator.SEG_CUBICTO:
				        destination.bezierVertex(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
				        break;
				      /*
				      case PathIterator.SEG_MOVETO:
				        breakShape();
				        break;
				      case PathIterator.SEG_CLOSE:
				        breakShape();
				        break;
				      */
				    }
				    pi.next();
				  }
				  destination.endShape(PConstants.CLOSE);
			}
			else {
				  float flatness = 0.1f;
				  PathIterator pi = word.getShape().getPathIterator(AffineTransform.getTranslateInstance(location.x, location.y), flatness);
				  ArrayList<PVector> points = new ArrayList<PVector>();
				  
				  destination.beginShape();
				  destination.fill(color);
				  destination.noStroke();
				  
				  float[] coords = new float[6];
				  while(!pi.isDone()) {
				    int segtype = pi.currentSegment(coords);
				    points.add(new PVector(coords[0], coords[1]));
				    
				    // only SEG_MOVETO, SEG_LINETO, and SEG_CLOSE
				    switch(segtype) {
				      case PathIterator.SEG_MOVETO:
				        destination.endShape(PConstants.CLOSE);
				        destination.beginShape();
				        destination.vertex(coords[0], coords[1]);
				        break;
				      case PathIterator.SEG_LINETO:
				        destination.vertex(coords[0], coords[1]);
				        break;
				      case PathIterator.SEG_CLOSE:
				        destination.endShape(PConstants.CLOSE);
				        break;
				    }  
				    
				    pi.next();
				  }
				  
				  destination.endShape(PConstants.CLOSE);
				  
				  /*
				  colorMode(HSB);
				  noFill();
				  strokeWeight(7);
				  PVector last = (PVector)points.get(0);
				  for (int i = 1; i < points.size(); i++) {
				    stroke(map(i, 0, points.size(), 100, 160), 255, 230);
				    PVector point = (PVector)points.get(i);
				    line(last.x, last.y, point.x, point.y);
				    last = point;
				  }
				  strokeWeight(1);
				  colorMode(RGB);
				  */
			}
		}
		else {
			
			
			/*
			parent.pushStyle();
			parent.stroke(30, 255, 255, 50);
			parent.noFill();
			Rectangle2D rect = word.getShape().getBounds2D();
			parent.rect(location.x, location.y, (float)rect.getWidth(), (float)rect.getHeight());
			parent.popStyle();
			*/
	
			destination.pushStyle();
			destination.fill(color);
			destination.textFont(word.word.getFont(fonter), word.word.getSize(sizer, 1, eWords.length));
			destination.textAlign(PConstants.LEFT, PConstants.TOP);
			
			destination.pushMatrix();
			destination.translate(location.x, location.y);
			destination.rotate(word.word.getAngle(angler));
				
			destination.text(word.word.word, 0, 0);
			
			destination.popMatrix();
			destination.popStyle();
			
			
			word.getBbTree().draw(destination);
		}
	}
}
