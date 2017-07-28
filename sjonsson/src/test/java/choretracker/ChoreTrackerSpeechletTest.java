package choretracker;

import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.SpeechletResponse;
import java.util.Calendar;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author a673549
 */
public class ChoreTrackerSpeechletTest {
    
    public ChoreTrackerSpeechletTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @org.junit.Test
    public void testGetBathroomChoreDetails_DateIsReferenceDay_ReturnsKristineDoesBathtub() {
        System.out.println("getBathroomChoreDetails");
        ChoreTrackerSpeechlet instance = new ChoreTrackerSpeechlet();
        String choreDetails = instance.getBathroomChoreDetails(ChoreTrackerSpeechlet.REFERENCE_DAY);
        System.out.println(">>> ChoreTrackerSpeechlet.REFERENCE_DAY: " + ChoreTrackerSpeechlet.REFERENCE_DAY);
        assertTrue(choreDetails.startsWith("Kristine does bathtub"));
    }

    @org.junit.Test
    public void testGetBathroomChoreDetails_DateIsWeekAfterReferenceDay_ReturnsDanielDoesBathtub() {
        System.out.println(">>> getBathroomChoreDetails");
//        System.out.println(">>> REFERENCE_DAY: " + ChoreTrackerSpeechlet.REFERENCE_DAY);
//        Calendar date = (Calendar) ChoreTrackerSpeechlet.REFERENCE_DAY.clone();
//        System.out.println(">>> date bf: " + date);
//        date.add(Calendar.DAY_OF_MONTH, 7);
//        System.out.println(">>> date af: " + date);
//        Calendar.getInstance();
        Calendar date = Calendar.getInstance();
        date.set(2017, Calendar.JULY, 22);
        System.out.println(">>> date af: " + date);
        ChoreTrackerSpeechlet instance = new ChoreTrackerSpeechlet();
        String choreDetails = instance.getBathroomChoreDetails(date);
        System.out.println(">>> choreDetails: " + choreDetails);
        assertTrue(choreDetails.startsWith("Daniel does bathtub"));
    }

    @org.junit.Test
    public void testGetBathroomChoreDetails_DateIsWeekAfterReferenceDay_ReturnsKatherineDoesBathtub() {
        Calendar date = Calendar.getInstance();
        date.set(2017, Calendar.JULY, 29);
        ChoreTrackerSpeechlet instance = new ChoreTrackerSpeechlet();
        String choreDetails = instance.getBathroomChoreDetails(date);
        assertTrue(choreDetails.startsWith("Katherine does bathtub"));
    }

    @org.junit.Test
    public void testGetBathroomChoreDetails_DateIsWeekBeforeReferenceDay_ReturnsKatherineDoesBathtub() {
        Calendar date = Calendar.getInstance();
        date.set(2017, Calendar.JULY, 8);
        ChoreTrackerSpeechlet instance = new ChoreTrackerSpeechlet();
        String choreDetails = instance.getBathroomChoreDetails(date);
        assertTrue(choreDetails.startsWith("Katherine does bathtub"));
    }

    @org.junit.Test
    public void testGetBathroomChoreDetails_DateIsNotSaturday_ReturnsNoChores() {
        Calendar date = Calendar.getInstance();
        date.set(2017, Calendar.JULY, 19);
        ChoreTrackerSpeechlet instance = new ChoreTrackerSpeechlet();
        String choreDetails = instance.getBathroomChoreDetails(date);
        assertTrue(choreDetails.startsWith("No bathroom chore on this day"));
    }
    
}
