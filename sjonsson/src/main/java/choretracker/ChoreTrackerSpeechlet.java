/**
    Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.

    Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at

        http://aws.amazon.com/apache2.0/

    or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package choretracker;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Speechlet that answers questions about which kid does which chore for a given
 * room
 */
public class ChoreTrackerSpeechlet implements Speechlet {
    private static final Logger log = LoggerFactory.getLogger(ChoreTrackerSpeechlet.class);

    private static final String ROOM_KEY = "ROOM";
    private static final String ROOM_SLOT = "Room";
    private static final String DATE_KEY = "DATE";
    private static final String DATE_SLOT = "Date";
    
    // Reference day used to cite a known chore list
    public static Calendar REFERENCE_DAY;    

    public ChoreTrackerSpeechlet() {
        REFERENCE_DAY = Calendar.getInstance();
        REFERENCE_DAY.clear();
        REFERENCE_DAY.set(2017, Calendar.JULY, 15);          
    }
    
    @Override
    public void onSessionStarted(final SessionStartedRequest request, final Session session)
            throws SpeechletException {
        log.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
        // any initialization logic goes here
    }

    @Override
    public SpeechletResponse onLaunch(final LaunchRequest request, final Session session)
            throws SpeechletException {
        log.info("onLaunch requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
        return getWelcomeResponse();
    }

    @Override
    public SpeechletResponse onIntent(final IntentRequest request, final Session session)
            throws SpeechletException {
        log.info("onIntent requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());

        // Get intent from the request object.
        Intent intent = request.getIntent();
        String intentName = (intent != null) ? intent.getName() : null;

        // Note: If the session is started with an intent, no welcome message will be rendered;
        // rather, the intent specific response will be returned.
        if ("TheRoomIsIntent".equals(intentName)) {
            return setRoomInSession(intent, session);
        } else if ("TheDateIsIntent".equals(intentName)) {
            return setDateInSession(intent, session);
        } else if ("WhoDoesWhichChoreIntent".equals(intentName)) {
            return getChoreDetails(intent, session);
        } else {
            throw new SpeechletException("Invalid Intent");
        }
    }

    @Override
    public void onSessionEnded(final SessionEndedRequest request, final Session session)
            throws SpeechletException {
        log.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
        // any cleanup logic goes here
    }

    /**
     * Creates and returns a {@code SpeechletResponse} with a welcome message.
     *
     * @return SpeechletResponse spoken and visual welcome message
     */
    private SpeechletResponse getWelcomeResponse() {
        // Create the welcome message.
        String speechText =
                "Welcome to the Yonson Chore Tracker. Please tell me a room name. For example bathroom";
        String repromptText =
                "Please tell me a room name. For example bathroom";

        return getSpeechletResponse(speechText, repromptText, true);
    }

    /**
     * Creates a {@code SpeechletResponse} for the intent and stores the extracted room in the
     * Session.
     *
     * @param intent
     *            intent for the request
     * @return SpeechletResponse spoken and visual response the given intent
     */
    private SpeechletResponse setRoomInSession(final Intent intent, final Session session) {
        // Get the slots from the intent.
        Map<String, Slot> slots = intent.getSlots();

        // Get the room slot from the list of slots.
        Slot roomSlot = slots.get(ROOM_SLOT);
        String speechText, repromptText;

        // Check for favorite color and create output to user.
        if (roomSlot != null) {
            // Store the user's room selection in the Session and create response.
            String roomSelection = roomSlot.getValue();
            session.setAttribute(ROOM_KEY, roomSelection);
            speechText =
                    String.format("You selected %s. Now tell me a date.  For example by saying 7/30/2017 or today."
                            , roomSelection);
            repromptText =
                    "Now tell me a date.  For example by saying 7/30/2017 or today.";

        } else {
            // Render an error since we don't know what the users room selection is.
            speechText = "I am not familiar with that room, please try again. "
                    + "Which room would you like to get chore details for?";
            repromptText =
                    "I am not familiar with that room, please try again. "
                    + "Which room would you like to get chore details for?";
        }

        return getSpeechletResponse(speechText, repromptText, true);
    }

    /**
     * Creates a {@code SpeechletResponse} for the intent and stores the extracted date in the
     * Session.
     *
     * @param intent
     *            intent for the request
     * @return SpeechletResponse spoken and visual response the given intent
     */
    private SpeechletResponse setDateInSession(final Intent intent, final Session session) {
        // Get the slots from the intent.
        Map<String, Slot> slots = intent.getSlots();

        // Get the color slot from the list of slots.
        Slot dateSlot = slots.get(DATE_SLOT);
        String speechText, repromptText;

        // Check for favorite color and create output to user.
        if (dateSlot != null) {
            // Store the user's selected date in the Session and create response.
            Calendar selectedDate = getCalendar(intent);
            
            //String selectedDate = dateSlot.getValue();
            session.setAttribute(DATE_KEY, selectedDate);
            speechText =
                    String.format("You selected %s. Here are the chore details. ", selectedDate.getTime())
                        + getChoreDetails(null, session);
            repromptText =
                    "You can ask for chore details by saying, get chore details";

        } else {
            // Render an error since we don't know what the users favorite color is.
            speechText = "I'm not familiar with that date, please try again.  For example say 7/30/2017, "
                    + "next Saturday, or today";
            repromptText =
                    "I'm not familiar with that date, please try again.  For example say 7/30/2017, "
                    + "next Saturday, or today";
        }

        return getSpeechletResponse(speechText, repromptText, true);
    }

    /**
     * Creates a {@code SpeechletResponse} for the intent and get the user's favorite color from the
     * Session.
     *
     * @param intent
     *            intent for the request
     * @return SpeechletResponse spoken and visual response for the intent
     */
    private SpeechletResponse getChoreDetails(final Intent intent, final Session session) {
        boolean isAskResponse = false;
        String speechText = null;
        
        // Get the user's room selection from the session.
        String room = (String) session.getAttribute(ROOM_KEY);

        // Check to make sure room is set in the session.
        if (StringUtils.isNotEmpty(room)) {
            speechText = String.format("The chore details for %s. on ", room);
        } else {
            // Since the user's favorite color is not set render an error message.
            speechText =
                    "I'm missing room selection.  Please select one, for example by saying, the room is bathroom";
            isAskResponse = true;
        }
        
        // Get the user's date selection from the session.
        Calendar date = (Calendar) session.getAttribute(DATE_KEY);        

        // Check to make sure date is set in the session.
        if (date != null) {
            speechText += String.format("date %s are ", date.getTime());
        } else {
            // Since the user's favorite color is not set render an error message 
            speechText =
                    "I'm missing date selection.  Please select a date, for example by saying, the date is";
            isAskResponse = true;
        }        
        
        if (!isAskResponse) {
            speechText = getChoreDetails(date, room);
        }

        return getSpeechletResponse(speechText, speechText, isAskResponse);
    }

    /**
     * Returns a Speechlet response for a speech and reprompt text.
     */
    private SpeechletResponse getSpeechletResponse(String speechText, String repromptText,
            boolean isAskResponse) {
        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("Session");
        card.setContent(speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        if (isAskResponse) {
            // Create reprompt
            PlainTextOutputSpeech repromptSpeech = new PlainTextOutputSpeech();
            repromptSpeech.setText(repromptText);
            Reprompt reprompt = new Reprompt();
            reprompt.setOutputSpeech(repromptSpeech);

            return SpeechletResponse.newAskResponse(speech, reprompt, card);

        } else {
            return SpeechletResponse.newTellResponse(speech, card);
        }
    }
    
    /**
     * Function to accept an intent containing a Day slot (date object) and return the Calendar
     * representation of that slot value. If the user provides a date, then use that, otherwise use
     * today. The date is in server time, not in the user's time zone. So "today" for the user may
     * actually be tomorrow.
     * 
     * @param intent
     *            the intent object containing the day slot
     * @return the Calendar representation of that date
     */
    private Calendar getCalendar(Intent intent) {
        Slot daySlot = intent.getSlot(DATE_SLOT);
        Date date;
        Calendar calendar = Calendar.getInstance();
        if (daySlot != null && daySlot.getValue() != null) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-d");
            try {
                date = dateFormat.parse(daySlot.getValue());
            } catch (ParseException e) {
                date = new Date();
            }
        } else {
            date = new Date();
        }
        calendar.setTime(date);
        return calendar;
    }

    private String getChoreDetails(Calendar date, String room) {
        String choreDetails = null;
        switch (room) {
            case "Bedroom": 
                choreDetails = getBathroomChoreDetails(date);
                break;
            default:
                choreDetails = "unknown";
                break;
        }
        return choreDetails;
    }
    

    protected String getBathroomChoreDetails(Calendar date) {
        
        int daysApart = (int) ChronoUnit.DAYS.between(REFERENCE_DAY.toInstant(), date.toInstant());
        //int daysApart = (int) getDateDifference(REFERENCE_DAY.getTime(), date.getTime());
                
        if (daysApart % 7 != 0) { // Bathroom chore only done on Saturdays
            return "No bathroom chore on this day";
        }
        else {
            int weeksApart = daysApart / 7;
            switch (weeksApart % 3) {
                case 0:
                    return "Kristine does bathtub, Katherine does toilet, Daniel does sink, mirror and floor";
                case 1:
                    return "Daniel does bathtub, Kristine does toilet, Katherine does sink, mirror and floor";
                default:
                    return "Katherine does bathtub, Daniel does toilet, Kristine does sink, mirror and floor";
            }
            
        }
    }  
    
//    private long getDayDifference(Calendar c1, Calendar c2) {
//        long diff = c2.get(Calendar.d).getTime() - c1.getTime();
//        return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
//    }
    
    private long getDateDifference(Date d1, Date d2) {
        long diff = d2.getTime() - d1.getTime();
        return diff / (24 * 60 * 60 * 1000);
//        return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
    }
}
