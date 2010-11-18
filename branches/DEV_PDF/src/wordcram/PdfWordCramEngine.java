package wordcram;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;

import processing.core.*;

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

/* TODO subclassing WCE is temporary: if this gets moving, rename WCE -> DefaultWCE (or something), & make WCE an interface.
 * Actually, you might want to extract a WordCramRenderer, and leave much of WordCramEngine alone -- the part that calls
 * the components could stay unchanged, and pass in the results.  In that case, I guess you'd...pass a WordCramRenderer from WordCram
 * to WordCramEngine.
 */
public class PdfWordCramEngine extends WordCramEngine {


	private PApplet parent;
	
	private WordFonter fonter;
	private WordSizer sizer;
	private WordColorer colorer;
	private WordAngler angler;
	private WordPlacer placer;
	private WordNudger nudger;

	private BBTreeBuilder bbTreeBuilder;
	private WordShaper wordShaper;
	
	private Word[] words;
	private Shape[] shapes;
	
	public PdfWordCramEngine(PApplet parent, Word[] words, WordFonter fonter,
			WordSizer sizer, WordColorer colorer, WordAngler angler,
			WordPlacer placer, WordNudger nudger) {
		super(parent, words, fonter, sizer, colorer, angler, placer, nudger);
		
		this.parent = parent;
		
		this.words = words;
		this.fonter = fonter;
		this.sizer = sizer;
		this.colorer = colorer;
		this.angler = angler;
		this.placer = placer;
		this.nudger = nudger;
		
		this.bbTreeBuilder = new BBTreeBuilder();
		this.wordShaper = new WordShaper(this.sizer, this.fonter, this.angler);
		
		renderWordsToShapes();
		makeBBTreesFromShapes();
	}

	
	private void renderWordsToShapes() {
		this.shapes = wordShaper.shapeWords(this.words); // ONLY returns shapes for words that are big enough to see
		this.words = Arrays.copyOf(words, shapes.length);  // Trim down the list of words
	}
	
	private void makeBBTreesFromShapes() {
		for (int i = 0; i < this.shapes.length; i++) {
			Word word = this.words[i];
			Shape shape = this.shapes[i];
			word.setBBTree(bbTreeBuilder.makeTree(shape, 7));  // TODO extract config setting for minBoundingBox, and add swelling option
		}
	}

	
	public void drawAll() {
		drawNext();
	}
	
	public void drawNext() {
		for (int i = 0; i < words.length; i++) {
			
			Word word = words[i];
			Shape wordShape = shapes[i];
	
			PVector wordLocation = placeWord(word, wordShape);
				
			if (wordLocation != null) {
				drawWordImage(word, wordLocation);
			}
			else {
				//System.out.println("couldn't place: " + word.word + ", " + word.weight);
			}
		}
	}
	
	
	private void drawWordImage(Word word, PVector location) {
		parent.rect(location.x, location.y, 20, 20);
	}

	
	
	// might this be useful? Look -- you need Shapes for collision detection, BUT,
	// you can't render them to PDF. So use them for collision, but then RE-RENDER
	// everything via parent. Where do you store the rendering params? In here:
	private class WordRenderMemo {
		public float size;
		public PFont font;
		public float angle;
		public int color;
		public PVector location;
	}
}
