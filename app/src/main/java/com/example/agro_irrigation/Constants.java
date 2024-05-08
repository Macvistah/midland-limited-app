package com.example.agro_irrigation;

import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Constants {

    public static final int CONNECT_TIMEOUT = 60 * 1000;

    public static final int READ_TIMEOUT = 60 * 1000;

    public static final int WRITE_TIMEOUT = 60 * 1000;

    public static final String MPESA_BASE_URL = "https://sandbox.safaricom.co.ke/";
    public static final String BUSINESS_SHORT_CODE = "174379";
    public static final String PASSKEY = "bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919";
    public static final String TRANSACTION_TYPE = "CustomerPayBillOnline";
    public static final String PARTYB = "174379"; //same as business shortcode above

    public static String URL = "http://157.245.143.110:5000/";
   // public static String URL = "http://192.168.100.2/midland/";
    public static final String CALLBACKURL = URL+"app/payment/index.php";

    public static final String BASE_URL = URL+"app/" ;

    public static void setWelcomeMessage(TextView txt_welcome,String userName) {
        int hour = Calendar.getInstance(TimeZone.getTimeZone("Africa/Nairobi")).get(Calendar.HOUR_OF_DAY);
        if(hour >= 1 && hour <= 12)
            txt_welcome.setText(new StringBuilder("Good Morning,\n").append(userName).append("."));
        else if(hour >= 13 && hour <= 17)
            txt_welcome.setText(new StringBuilder("Good Afternoon,\n").append(userName).append("."));
        else
            txt_welcome.setText(new StringBuilder("Good Evening,\n").append(userName).append("."));
    }
    public static String formatDateTimeToAmPm(String inputDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.getDefault());
        LocalDateTime dateTime = LocalDateTime.parse(inputDate, formatter);

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
        return dateTime.format(timeFormatter);
    }
    public static String formatDateShot(String inputDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.getDefault());
        LocalDateTime dateTime = LocalDateTime.parse(inputDate, formatter);

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("dd MMM");
        return dateTime.format(timeFormatter);
    }

    public static String getRelativeTimeInfo(String inputDate) {
        try {
            // Parse the input date string to a LocalDateTime object
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.getDefault());
            LocalDateTime dateTime = LocalDateTime.parse(inputDate, formatter);

            // Get the current date and time
            LocalDateTime now = LocalDateTime.now();

            // Calculate the difference in minutes
            long minutesDifference = java.time.Duration.between(dateTime, now).toMinutes();

            // Return relative time information
            if (minutesDifference <= 1) {
                return "Just Now";
            } else if (minutesDifference < 60) {
                return minutesDifference + " minutes ago";
            } else if (minutesDifference < 1440) {
                return (minutesDifference / 60) + " hours ago";
            } else if (isYesterday(dateTime)) {
                return "Yesterday";
            } else if (minutesDifference < 10080) { // 1440 minutes * 7 days
                return dateTime.getDayOfWeek() + "";
            } else {
                return ""+formatter.format(dateTime);
            }
        } catch (DateTimeParseException e) {
            e.printStackTrace();
            return "Invalid Date Format";
        }
    }
    private static boolean isYesterday(LocalDateTime dateTime) {
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        return dateTime.toLocalDate().isEqual(yesterday.toLocalDate());
    }
}
