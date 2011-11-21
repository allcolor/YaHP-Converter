# YaHP Converter

YaHP is a Java library that allows you to convert an HTML document into a PDF document. YaHP use a pluggable renderer system. However, since the 1.2.19 version only the renderer based on [Flying Saucer](http://code.google.com/p/flying-saucer/) is available.

## Contact

Please contact the creator of YAHP at <quentin.anciaux@advalvas.be> if you have any question.

## Changelog

### 1.2.20c (10/05/2011)
* Revert to previous itext and flying saucer librairies due to licensing problem. This version is LGPL

### 1.2.20b (17/01/2011)
* Do not validate html and do not### xhtml DTD from w3 site (thanks to Johnathan Crawford).
* Bug correction in entities normalization.
* Inline remote css into a style element inside the document.
* Updated itext and flying saucer librairies to latest version (thanks to Johnathan Crawford).

### 1.2.20a (18/12/2009)
* Fix a problem with multiple text node copy by jtidy.
* Updated jars.
* Remove numerous "INFO" default logging to the console.

### 1.2.20 (17/12/2009)
* Better handling of ms word generated html.
* Html document is normalized (entities are translated in characters) before rendering.
* Handle page break tag even if namespace declaration is missing

### 1.2.19d (16/06/2009)
* Allow multiple threads to concurrently use the same CYaHPConverter object instead of serializing access (or using one CYaHPConverter object per thread).

### 1.2.19c (07/05/2009)
* Can change page size and orientation after a page break.
* If the title tag is set in the html, it is used for the pdf title.
* Added LEGAL and LETTER constant for page size

### 1.2.19b (01/05/2009)
* Fixed Out Of Memory Error when a textarea was present in the html source.

### 1.2.19a (18/03/2009)
* Fixed incorrect page size when a page break is inserted.

### 1.2.19 (13/03/2009)
* Header and Footer can contain html.
* Updated samples.
* Removed old renderers

### 1.2.18c (07/07/2008)
* Rendering of form components (textfield, textarea, combo, radiobutton, checkbox, button , listbox) in the flying saucer renderer
* Corrected double encoding of &, < and >
* Normalize html entity before rendering
* known bug: Header/footer rendering does not works in headless mode for the flying saucer renderer
* Updated samples.
* Code cleanup

### 1.2.18b (05/07/2008)
* Tested with j2se 1.4.2.

### 1.2.18a (04/07/2008)
* Better rendering of tags soup.

### 1.2.18 (04/07/2008)
* Clean up, ensure compat with jdk 1.5.
* Updated samples.

### 1.2.18-pre1 (01/07/2008)
* Added flying saucer xhtml renderer.
* Updated samples.

### 1.2.17 (02/07/2007)
* Fixed a NPE if the FOP_TTF_FONT_PATH properties is not set.

### 1.2.16 (19/06/2007)
* Font embedding does not need anymore the fonts to be in the OS system fonts folder in jdk < 1.6 on windows OSs.(Thanks to Takis Bouyouris)

### 1.2.15 (17/06/2007)
* Font embedding does not need anymore the fonts to be in the OS system fonts folder in jdk < 1.6.

### 1.2.14 (15/06/2007)
* Sometimes the font embedding was still not working while running inside tomcat, this bug has been fixed.
* Works again in headless mode.
* Font embedding does not need anymore the fonts to be in the OS system fonts folder.

### 1.2.13 (13/06/2007)
* Fix bug: The font embedding was not working while running inside tomcat. (Thanks to Takis Bouyouris)
* Fix bug: Sometimes when running inside tomcat, the following error occured: 'UIDefaults.getUI() failed: no ComponentUI class for: javax.swing.JTextPane'. (Thanks to Takis Bouyouris)

### 1.2.12 (11/06/2007)
* Removed the method "getResources" from the classloader, because this method is marked as final in 1.4 JVM and so did break compatibility of yahp with 1.4 vm.

### 1.2.11 (11/05/2007)
* Updated classloader.
* Use current DPI screen settings to calculate page size.

### 1.2.10 (26/04/2007)
* Updated parser to ShaniXmlParser-v1.4.16.
* Updated xalan.

### 1.2.9 (24/04/2007)
* Updated parser to ShaniXmlParser-v1.4.15.
* The swing renderer can now render fieldset and legend tags.

### 1.2.8 (19/04/2007)
* Updated parser to ShaniXmlParser-v1.4.14.

### 1.2.7 (13/04/2007)
* v1.2.6 was broken in application server environment.

### 1.2.6 (13/04/2007) **NUKED**
* Use FOP 0.93.
* Can now embed automatically TrueType font by giving a path where TTF files are located with the yahp parameter IHtmlToPdfTransformer.FOP_TTF_FONT_PATH.
* The page-break <yahp:pb> works again.
* Updated samples PDF for the swing renderer.
* Updated javadoc.

### 1.2.5 (12/04/2007)
* Corrected PageSize class where bottom margin was set incorrectly in CM.
* Corrected support for accentuated letters.
* Updated classloading mechanism.
* Corrected incorrect page count on some html.

### 1.2.4 (11/04/2007)
* Remove infinite loop in the css parser.
* Tidyfy html before sending to rendering.
* Corrected a class cast exception in the swing border helper.
* If base url not set, take base tag as base url if found.

### 1.2.3 (09/04/2007)
* Updated xml/html parser.
* Default charset to utf-8.

### 1.2.2 (16/03/2007)
* Correct errors with commons-logging under tomcat on windows.

### 1.2.1 (05/01/2007)
* Ignore attribute's case on image tag.

### 1.2 (07/12/2006)
* Corrected rendering of elements with size set in percent.

### 1.1beta2 (10/08/2006)
* Rendering of CSS border in the swing renderer.
* Better memory usage.
* Use Shani xml parser v1.4.6.

### 1.0 (21/07/2006)
* Corrected non rendering of table row on edge of pages in the swing renderer.
* Better memory usage.
* Use Shani xml parser v1.4.2.

### 0.99 (05/07/2006)
* Corrected dissapearance of header/footer in the swing renderer.
* Better memory usage when css style is put on the document.

### 0.98 (05/07/2006)
* Huge memory usage improvement.
* Use Shani xml parser v1.3.8.

### 0.97 (23/06/2006)
* Add intelligent and automatic table rows break in the swing renderer.

### 0.96 (19/06/2006)
* Fix incorrect alignment with embedded fonts in the swing renderer.
* Javadoc updated.

### 0.95 (17/06/2006)
* Corrected incorrect right alignment of text in pdf generated by the swing renderer.
* Javadoc updated.

### 0.94 (08/06/2006)
* The swing renderer now has a pagebreak tag which permits to cut one document in several pages.
* Possibility to embed font with the yahp-fop-config.xml file.
* Javadoc updated.
* Sample application updated.

### 0.93 (15/04/2006)
* Better rendering of forms components (button, field, ...) in the swing renderer. (see widget.pdf)
* List box are now rendered with the swing renderer.
* Javadoc updated.
* Swing renderer samples files updated.

### 0.92 (14/04/2006)
* Rendering of forms components (button, field, ...) is now custom made in the swing renderer.
* The swing renderer is two times faster.
* Correct rendering of scaled page with the swing renderer.
* Rendering of the content of input field and textarea with the swing renderer.
* Sample application updated.
* Javadoc updated.
* Swing renderer samples files updated.

### 0.91 (11/04/2006)
* Can sign a document with a certificate.
* Code cleanup.
* Sample application updated.
* Javadoc updated.

### 0.90 (08/04/2006)
* Corrected "drawing error" occuring in acrobat reader of samples pdf generated with the firefox renderer by using latest ghostscript and not ghostscript eps.
* Ensure all buttons/combo/textfield are painted with the swing renderer.
* Sample application updated.
* Javadoc updated.
* All samples files updated.

### 0.20 (07/04/2006)
* Support header/footer in utf-8 with the firefox renderer.
* Support concurrent rendering with the firefox renderer.
* Better rendering of comboboxes and buttons with the swing renderer, they are painted as vector instead of bitmap.
* Sample application updated.
* Javadoc updated.
* All samples files updated.

### 0.19 (05/04/2006)
* Can set header/footer and page size with the firefox renderer.
* Recompiled iText to work on 1.4 JVM and corrected a LinkageError on 1.4 JVM.
* Sample application updated to use all the new properties.
* Javadoc updated.
* Firefox samples files updated.

### 0.18 (02/04/2006)
* Added a new renderer which use firefox as html renderer.
* Sample application updated to use all the new properties.
* Javadoc updated.
* Samples files updated.

### 0.17 (30/03/2006)
* Renderers are now pluggable.
* Added a new renderer which use OpenOffice.org writer as pdf generator.
* Sample application updated to use all the new properties.
* Refactoring and cleanup of code.
* Does not copy yahpxxx.jar in the temp directory anymore.
* Javadoc updated.
* Samples files updated.

### 0.16 (24/03/2006)
* Added handling of pdf encryption.
* Added several properties in IHtmlToPdfTransformer interface, see javadoc.
* Cleanup of code.
* Updated the javadoc.
* Updated to the new ShaniXmlParser 1.3.6.
* All samples files updated.

### 0.15 (23/03/2006)
* Correct rendering of page containing chinese characters.
* Better rendering of button/checkbox components.
* Updated to the new ShaniXmlParser 1.3.6-pre.
* Samples files updated.

### 0.14 (20/03/2006)
* Intelligent cutting of pages.
* Better rendering of page footer.

### 0.13 (17/03/2006)
* Detect if rendering in the event thread and avoid calling SwingUtilities invokeAndWait in this case.
* Ensure synchronized rendering of image inside the document.
* Updated to the new ShaniXmlParser 1.3.5.

### 0.12 (16/11/2005)
* Use SwingUtilities.InvokeAndWait to synchronize with the swing paint thread.
* Better rendering of page header. (page footer still rendered as image)
* Samples files updated.

### 0.11 (07/11/2005)
* Add property "FAST_TRANSFORM" default to true, which permits to have faster transformation, but will produce black background on transparent gif under kpdf (only so far).
* Circumvent a NullPointerException in JDK ParagraphView class under jdk 1.4.2

### 0.10 (02/11/2005)
* Set the org.apache.commons.logging.Log System property to force the use log4j instead of setting wrongly with a LogFactoryImplementation.

### 0.9 (28/10/2005)
* Correct rendering of comboboxes.
* Correct rendering of images with transparent zone (no more black background)
* Set the org.apache.commons.logging.Log System property to force the use of the default logger inside the Yahp context.
* Samples files updated.
* Javadoc updated

### 0.8 (26/10/2005)
* Corrected a memory leak in the classloader due to commons logging.
* Destroy the classloader on finalization.
* Added the META-INF/services/org.apache.commons.logging.LogFactory file to force the use of the default logger inside the Yahp context.

### 0.7 (23/10/2005)
* Remove not selected option tag from DOM.
* Updated xml parser.

### 0.6 (24/09/2005)
* Better rendering quality.
* Render directly in the pdfgraphics2d and dot not use an offscreen buffer which had bad rendering quality.
* Fonts are now vectorized and not as bitmap.
* Samples updated.
* Javadoc updated.

### 0.5 (23/09/2005)
* Force HTMLEditorKit on the JTextPane used for rendering. (prevent source display)
* Remove the doctype node if any before giving the source to the JTextPane
* Now render correclty the Text field, buttons, combobox, ... (before was blank)

### 0.4 (22/09/2005)
* Remove the use of TimeoutException in the CMutex clas because this exception only exists in JDK 1.5
* Set the thread context classloader to prevent Duplicate Class.

### 0.3 (21/09/2005)
* Document/javadoc
* Set antialiasing on the graphics2d object.
* Handle '../' in css and image links.

### 0.2
* Use a specialised classloader to load inner jar.
* Compile FOP for jdk1.4 instead of 1.5.

### 0.1
* Initial release

## License

* [LGPL 2.1](http://www.opensource.org/licenses/lgpl-2.1.php)