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

import java.awt.geom.*;
import java.util.ArrayList;

import processing.core.*;

/* TODO subclassing WCE is temporary: if this gets moving, rename WCE -> DefaultWCE (or something), & make WCE an interface.
 * Actually, you might want to extract a WordCramRenderer, and leave much of WordCramEngine alone -- the part that calls
 * the components could stay unchanged, and pass in the results.  In that case, I guess you'd...pass a WordCramRenderer from WordCram
 * to WordCramEngine.
 */
public class PdfWordCramEngine extends WordCramEngine {

	private PApplet parent;

	public PdfWordCramEngine(PApplet parent, Word[] words, WordFonter fonter,
			WordSizer sizer, WordColorer colorer, WordAngler angler,
			WordPlacer placer, WordNudger nudger, boolean printSkippedWords) {
		super(parent, words, fonter, sizer, colorer, angler, placer, nudger, printSkippedWords);
		
		this.parent = parent;
	}

	public void drawAll() {
		drawNext();
	}
	
	public void drawNext() {
		parent.textMode(PConstants.MODEL);
		
		for (int i = 0; i < words.length; i++) {
			
			EngineWord eWord = words[i];
				
			boolean wasPlaced = placeWord(eWord);
				
			if (wasPlaced) {
				drawWordImage(eWord);
			}
		}
		System.out.println(Timer.getInstance().report());
	}
	
	
	protected void drawWordImage(EngineWord word) {
		PVector location = word.getCurrentLocation();

		boolean useJavaGeom = false;
		
		if (useJavaGeom) {

			boolean useGeomerative = true;
			
			if (useGeomerative) {

				  PathIterator pi = word.getShape().getPathIterator(AffineTransform.getTranslateInstance(location.x, location.y));
				  
				  parent.noStroke();
				  parent.beginShape();
				  parent.fill(word.color);
				  float[] coords = new float[6];
				  
				  pi.currentSegment(coords);
				  parent.vertex(coords[0], coords[1]);
				  
				  while(!pi.isDone()) {
				    int segtype = pi.currentSegment(coords);
				    switch(segtype) {
				      case PathIterator.SEG_LINETO:
				        parent.vertex(coords[0], coords[1]);
				        break;
				      case PathIterator.SEG_QUADTO:
				        parent.bezierVertex(coords[0], coords[1], coords[2], coords[3], coords[2], coords[3]);
				        break;
				      case PathIterator.SEG_CUBICTO:
				        parent.bezierVertex(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
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
				  parent.endShape(PConstants.CLOSE);
			}
			else {
				  float flatness = 0.1f;
				  PathIterator pi = word.getShape().getPathIterator(AffineTransform.getTranslateInstance(location.x, location.y), flatness);
				  ArrayList<PVector> points = new ArrayList<PVector>();
				  
				  parent.beginShape();
				  parent.fill(word.color);
				  parent.noStroke();
				  
				  float[] coords = new float[6];
				  while(!pi.isDone()) {
				    int segtype = pi.currentSegment(coords);
				    points.add(new PVector(coords[0], coords[1]));
				    
				    // only SEG_MOVETO, SEG_LINETO, and SEG_CLOSE
				    switch(segtype) {
				      case PathIterator.SEG_MOVETO:
				        parent.endShape(PConstants.CLOSE);
				        parent.beginShape();
				        parent.vertex(coords[0], coords[1]);
				        break;
				      case PathIterator.SEG_LINETO:
				        parent.vertex(coords[0], coords[1]);
				        break;
				      case PathIterator.SEG_CLOSE:
				        parent.endShape(PConstants.CLOSE);
				        break;
				    }  
				    
				    pi.next();
				  }
				  
				  parent.endShape(PConstants.CLOSE);
				  
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
	
			parent.pushStyle();
			parent.fill(word.color);
			parent.textFont(word.font, word.size);
			parent.textAlign(PConstants.LEFT, PConstants.TOP);
			
			parent.pushMatrix();
			parent.translate(location.x, location.y);
			parent.rotate(word.angle);
				
			parent.text(word.word.word, 0, 0);
				
			parent.popMatrix();
			parent.popStyle();
			
			
			word.drawBBTree(parent.g);
		}
	}
}
