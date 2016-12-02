package com.blackorwhite.osobackend;

import com.googlecode.objectify.Key;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import static com.blackorwhite.osobackend.OfyService.ofy;

public class CronController extends HttpServlet {
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        MessagingEndpoint _Msg = new MessagingEndpoint();

        ConfigRecord NbTry = ofy().load().type(ConfigRecord.class).filter("Key", "NbTry").first().now();
        if (NbTry == null) {
            NbTry = new ConfigRecord("NbTry");
            NbTry.SetValue(0);
        }

        ConfigRecord LastState = ofy().load().type(ConfigRecord.class).filter("Key", "LastState").first().now();
        if (LastState == null) {
            LastState = new ConfigRecord("LastState");
            LastState.SetValue(0);
        }

        try {
            OSOClient Cli;
            Cli = new OSOClient();

            String State = Cli.downloadUrl("GetState");

            if (State.equals("2")){
                _Msg.sendMessage(OSOClient.alarm_salon_fired);
            }
            if (State.equals("3")){
                _Msg.sendMessage(OSOClient.alarm_garage_fired);
            }
            if (State.equals("5")){
                _Msg.sendMessage(OSOClient.alarmsilenced_salon_fired);
            }
            if (State.equals("6")){
                _Msg.sendMessage(OSOClient.alarmsilenced_garage_fired);
            }

            LastState.SetValue(Integer.parseInt(State));
            NbTry.SetValue(0);
        }
        catch (IOException ex) {
            NbTry.SetValue(NbTry.GetValue() + 1);

            if (NbTry.GetValue() > 3 && LastState.GetValue() > 0)
                _Msg.sendMessage(OSOClient.alarm_unreachable);
        }
        finally {
            ofy().save().entity(LastState).now();
            ofy().save().entity(NbTry).now();
        }
    };
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doGet(req, resp);
    }
}