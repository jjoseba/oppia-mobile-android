/* 
 * This file is part of OppiaMobile - https://digital-campus.org/
 * 
 * OppiaMobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * OppiaMobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with OppiaMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package org.digitalcampus.oppia.utils.xmlreaders;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.exception.InvalidXMLException;
import org.digitalcampus.oppia.model.TrackerLog;
import org.joda.time.DateTime;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.util.Log;

public class CourseTrackerXMLReader {
	public static final String TAG = CourseTrackerXMLReader.class.getSimpleName();
	private Document document;
	
	public CourseTrackerXMLReader(String filename) throws InvalidXMLException {
		File courseXML = new File(filename);
		if (courseXML.exists()) {

			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder;
			try {
				builder = factory.newDocumentBuilder();
				document = builder.parse(courseXML);

			} catch (ParserConfigurationException e) {
				throw new InvalidXMLException(e);
			} catch (SAXException e) {
				throw new InvalidXMLException(e);
			} catch (IOException e) {
				throw new InvalidXMLException(e);
			}
		}
	}
	
	
	public ArrayList<TrackerLog> getTrackers(){
		ArrayList<TrackerLog> trackers = new ArrayList<TrackerLog>();
		if (this.document == null){
			return trackers;
		}
		NodeList actTrackers = document.getFirstChild().getChildNodes();
		for (int i=0; i<actTrackers.getLength(); i++) {
			NamedNodeMap attrs = actTrackers.item(i).getAttributes();
			String digest = attrs.getNamedItem("digest").getTextContent();
			String submittedDateString = attrs.getNamedItem("submitteddate").getTextContent();
			DateTime sdt = MobileLearning.DATETIME_FORMAT.parseDateTime(submittedDateString);
			
			boolean completed;
			try {
				completed = Boolean.parseBoolean(attrs.getNamedItem("completed").getTextContent());
			} catch (NullPointerException npe) {
				completed = true;
			}
			
			String type;
			try {
				type = attrs.getNamedItem("type").getTextContent();
			} catch (NullPointerException npe) {
				type = null;
			}
			
			TrackerLog t = new TrackerLog();
			t.setDigest(digest);
			t.setSubmitted(true);
			t.setDatetime(sdt);
			t.setCompleted(completed);
			t.setType(type);
			trackers.add(t);
		}
		return trackers;
	}
	
	public ArrayList<TrackerLog> getQuizzes(){
		ArrayList<TrackerLog> trackers = new ArrayList<TrackerLog>();
		if (this.document == null){
			return trackers;
		}
		NodeList actTrackers = document.getFirstChild().getChildNodes();
		for (int i=0; i<actTrackers.getLength(); i++) {
			NamedNodeMap attrs = actTrackers.item(i).getAttributes();
			
			String type;
			try {
				type = attrs.getNamedItem("type").getTextContent();
			} catch (NullPointerException npe) {
				type = null;
			}
			
			// if quiz activity then get the results etc
			if (type != null && type.equalsIgnoreCase("quiz")){
				NodeList quizNodes = actTrackers.item(i).getChildNodes();
				for (int j=0; j<quizNodes.getLength(); j++) {
					NamedNodeMap quizAttrs = quizNodes.item(j).getAttributes();
					String maxscore = quizAttrs.getNamedItem("maxscore").getTextContent();
					String score = quizAttrs.getNamedItem("score").getTextContent();
					Log.d(TAG,maxscore);
					Log.d(TAG,score);
				}
			}
		}
		return trackers;
	}
}
